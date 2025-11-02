"""Core Banking Payments Simulator with Prometheus Metrics."""

import asyncio
import json
import logging
import os
import random
import sys
import time
from collections import OrderedDict
from datetime import datetime, timezone
from decimal import Decimal
from enum import Enum
from typing import Any, Dict, Optional
from uuid import UUID, uuid4

from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.responses import PlainTextResponse, JSONResponse
from pydantic import BaseModel, Field, field_validator
from prometheus_client import (
    CONTENT_TYPE_LATEST,
    CollectorRegistry,
    Counter,
    Histogram,
    generate_latest,
    make_asgi_app,
    multiprocess,
)

# Configure structured JSON logging
logging.basicConfig(
    level=logging.INFO,
    format='%(message)s',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger(__name__)

# Environment configuration
FAULT_RATE = float(os.getenv("FAULT_RATE", "0.02"))
EXTRA_LATENCY_MS = int(os.getenv("EXTRA_LATENCY_MS", "0"))
LATENCY_JITTER_MS = int(os.getenv("LATENCY_JITTER_MS", "50"))
TIMEOUT_MS = int(os.getenv("TIMEOUT_MS", "800"))
RANDOM_SEED = os.getenv("RANDOM_SEED")
MAX_CACHE_SIZE = int(os.getenv("MAX_CACHE_SIZE", "100000"))
CACHE_TTL_SECONDS = int(os.getenv("CACHE_TTL_SECONDS", "600"))
PROMETHEUS_MULTIPROC_DIR = os.getenv("PROMETHEUS_MULTIPROC_DIR")

# Seed RNG if configured
if RANDOM_SEED:
    random.seed(int(RANDOM_SEED))

# Log effective configuration at startup
logger.info(json.dumps({
    "ts": datetime.now(timezone.utc).isoformat(),
    "level": "DEBUG",
    "message": "Effective configuration",
    "config": {
        "FAULT_RATE": FAULT_RATE,
        "EXTRA_LATENCY_MS": EXTRA_LATENCY_MS,
        "LATENCY_JITTER_MS": LATENCY_JITTER_MS,
        "TIMEOUT_MS": TIMEOUT_MS,
        "RANDOM_SEED": RANDOM_SEED,
        "MAX_CACHE_SIZE": MAX_CACHE_SIZE,
        "CACHE_TTL_SECONDS": CACHE_TTL_SECONDS,
        "PROMETHEUS_MULTIPROC_DIR": PROMETHEUS_MULTIPROC_DIR,
    }
}))


class FailureReason(str, Enum):
    """Failure reason codes."""
    INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS"
    DAILY_LIMIT_EXCEEDED = "DAILY_LIMIT_EXCEEDED"
    ACCOUNT_CLOSED = "ACCOUNT_CLOSED"
    COMPLIANCE_BLOCK = "COMPLIANCE_BLOCK"
    TIMEOUT_UPSTREAM = "TIMEOUT_UPSTREAM"
    DUPLICATE_TRANSFER = "DUPLICATE_TRANSFER"
    INVALID_TENANT = "INVALID_TENANT"


# Failure to HTTP status mapping
FAILURE_STATUS_MAP = {
    FailureReason.INSUFFICIENT_FUNDS: status.HTTP_402_PAYMENT_REQUIRED,
    FailureReason.DAILY_LIMIT_EXCEEDED: status.HTTP_429_TOO_MANY_REQUESTS,
    FailureReason.ACCOUNT_CLOSED: status.HTTP_409_CONFLICT,
    FailureReason.COMPLIANCE_BLOCK: status.HTTP_423_LOCKED,
    FailureReason.TIMEOUT_UPSTREAM: status.HTTP_504_GATEWAY_TIMEOUT,
    FailureReason.DUPLICATE_TRANSFER: status.HTTP_409_CONFLICT,
    FailureReason.INVALID_TENANT: status.HTTP_400_BAD_REQUEST,
}

# Human-readable messages
FAILURE_MESSAGES = {
    FailureReason.INSUFFICIENT_FUNDS: "Insufficient funds in source account",
    FailureReason.DAILY_LIMIT_EXCEEDED: "Daily transfer limit exceeded",
    FailureReason.ACCOUNT_CLOSED: "Account is closed",
    FailureReason.COMPLIANCE_BLOCK: "Transfer blocked for compliance reasons",
    FailureReason.TIMEOUT_UPSTREAM: "Upstream service timeout",
    FailureReason.DUPLICATE_TRANSFER: "Duplicate transfer detected",
    FailureReason.INVALID_TENANT: "Invalid or missing tenant identifier",
}


# Request/Response models
class TransferRequest(BaseModel):
    """Transfer request payload."""
    client_transfer_id: str = Field(..., min_length=1, max_length=100)
    source_account: str = Field(..., min_length=1)
    destination_account: str = Field(..., min_length=1)
    amount: Decimal = Field(..., gt=0, decimal_places=2)
    currency: str = Field(..., pattern="^[A-Z]{3}$")
    reference: Optional[str] = Field(None, max_length=120)

    @field_validator('amount')
    @classmethod
    def validate_amount(cls, v: Decimal) -> Decimal:
        """Ensure amount has exactly 2 decimal places."""
        if v.as_tuple().exponent < -2:
            raise ValueError("Amount must have at most 2 decimal places")
        return v.quantize(Decimal("0.01"))


class SuccessResponse(BaseModel):
    """Successful transfer response."""
    status: str = "SUCCESS"
    transaction_id: str
    client_transfer_id: str
    processing_time_ms: int
    message: str = "Transfer completed"


class FailureResponse(BaseModel):
    """Failed transfer response."""
    status: str = "FAILED"
    reason_code: str
    message: str
    client_transfer_id: str


# Prometheus metrics setup
if PROMETHEUS_MULTIPROC_DIR:
    registry = CollectorRegistry()
    multiprocess.MultiProcessCollector(registry)
else:
    registry = None

payments_requests_total = Counter(
    'payments_requests_total',
    'Total payment requests',
    ['host', 'route', 'method', 'status'],
    registry=registry
)

payments_failures_total = Counter(
    'payments_failures_total',
    'Total payment failures',
    ['host', 'route', 'reason'],
    registry=registry
)

payments_latency_seconds = Histogram(
    'payments_latency_seconds',
    'Payment request latency',
    ['host', 'route', 'method'],
    buckets=(0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2),
    registry=registry
)


# Idempotency cache
class IdempotencyCache:
    """Thread-safe idempotency cache with TTL and LRU eviction."""

    def __init__(self, max_size: int = 100000, ttl_seconds: int = 600):
        self.max_size = max_size
        self.ttl_seconds = ttl_seconds
        self.cache: OrderedDict[str, tuple[Any, float, int]] = OrderedDict()
        self.locks: Dict[str, asyncio.Lock] = {}
        self.global_lock = asyncio.Lock()

    async def get_lock(self, key: str) -> asyncio.Lock:
        """Get or create a per-key lock."""
        async with self.global_lock:
            if key not in self.locks:
                self.locks[key] = asyncio.Lock()
            return self.locks[key]

    async def get(self, key: str) -> Optional[tuple[Any, int]]:
        """Get cached response if exists and not expired."""
        async with self.global_lock:
            if key in self.cache:
                response, timestamp, status_code = self.cache[key]
                if time.time() - timestamp < self.ttl_seconds:
                    # Move to end (LRU)
                    self.cache.move_to_end(key)
                    return response, status_code
                else:
                    # Expired
                    del self.cache[key]
                    if key in self.locks:
                        del self.locks[key]
        return None

    async def set(self, key: str, response: Any, status_code: int) -> None:
        """Store response in cache with TTL."""
        async with self.global_lock:
            # LRU eviction if at capacity
            if len(self.cache) >= self.max_size and key not in self.cache:
                oldest = next(iter(self.cache))
                del self.cache[oldest]
                if oldest in self.locks:
                    del self.locks[oldest]

            self.cache[key] = (response, time.time(), status_code)
            self.cache.move_to_end(key)


# Initialize cache
idempotency_cache = IdempotencyCache(MAX_CACHE_SIZE, CACHE_TTL_SECONDS)


def redact_account(account: str) -> str:
    """Redact account number except last 4 characters."""
    if len(account) <= 4:
        return "****"
    return "*" * (len(account) - 4) + account[-4:]


async def simulate_processing_time() -> int:
    """Simulate processing time with configured latency."""
    base_ms = random.randint(5, 20)
    jitter_ms = random.randint(0, LATENCY_JITTER_MS) if LATENCY_JITTER_MS > 0 else 0
    total_ms = base_ms + EXTRA_LATENCY_MS + jitter_ms

    await asyncio.sleep(total_ms / 1000.0)
    return total_ms


def should_fail() -> bool:
    """Determine if request should fail based on fault rate."""
    return random.random() < FAULT_RATE


def select_failure_reason() -> FailureReason:
    """Randomly select a failure reason."""
    return random.choice(list(FailureReason))


# FastAPI app
app = FastAPI(title="Core Banking Payments Simulator", version="1.0.0")


@app.post("/transfer", response_model=None)
async def transfer(
    request: Request,
    transfer_req: TransferRequest,
    host: Optional[str] = Header(None),
    idempotency_key: Optional[str] = Header(None),
    x_request_id: Optional[str] = Header(None),
) -> JSONResponse:
    """Process a payment transfer."""
    start_time = time.time()

    # Determine tenant and request ID
    tenant = host or "unknown"
    req_id = x_request_id or str(uuid4())

    # Determine idempotency key (header takes precedence)
    idem_key = idempotency_key or transfer_req.client_transfer_id

    # Get per-key lock for idempotency
    key_lock = await idempotency_cache.get_lock(idem_key)

    async with key_lock:
        # Check cache for idempotent response
        cached = await idempotency_cache.get(idem_key)
        if cached:
            cached_response, cached_status = cached

            # Observe metrics for replay
            duration = time.time() - start_time
            payments_requests_total.labels(
                host=tenant,
                route="/transfer",
                method="POST",
                status=str(cached_status)
            ).inc()

            payments_latency_seconds.labels(
                host=tenant,
                route="/transfer",
                method="POST"
            ).observe(duration)

            # Log the replay
            logger.info(json.dumps({
                "ts": datetime.now(timezone.utc).isoformat(),
                "level": "INFO",
                "tenant": tenant,
                "route": "/transfer",
                "method": "POST",
                "status": cached_status,
                "duration_ms": int(duration * 1000),
                "req_id": req_id,
                "outcome": cached_response.get("status", "UNKNOWN"),
                "reason": cached_response.get("reason_code"),
                "amount": str(transfer_req.amount),
                "currency": transfer_req.currency,
                "client_transfer_id": transfer_req.client_transfer_id,
                "transaction_id": cached_response.get("transaction_id"),
                "idempotent": True
            }))

            return JSONResponse(
                status_code=cached_status,
                content=cached_response,
                headers={"X-Request-ID": req_id}
            )

        # Process new request
        processing_ms = await simulate_processing_time()

        # Determine outcome
        if should_fail():
            reason = select_failure_reason()
            status_code = FAILURE_STATUS_MAP[reason]

            # Special handling for timeout
            if reason == FailureReason.TIMEOUT_UPSTREAM:
                timeout_delay = min(TIMEOUT_MS, 1000) / 1000.0
                await asyncio.sleep(timeout_delay - (processing_ms / 1000.0))
                processing_ms = int(timeout_delay * 1000)

            response_body = {
                "status": "FAILED",
                "reason_code": reason.value,
                "message": FAILURE_MESSAGES[reason],
                "client_transfer_id": transfer_req.client_transfer_id
            }

            # Increment failure counter
            payments_failures_total.labels(
                host=tenant,
                route="/transfer",
                reason=reason.value
            ).inc()

            outcome = "FAILED"
            transaction_id = None
        else:
            # Success
            transaction_id = str(uuid4())
            status_code = status.HTTP_200_OK

            response_body = {
                "status": "SUCCESS",
                "transaction_id": transaction_id,
                "client_transfer_id": transfer_req.client_transfer_id,
                "processing_time_ms": processing_ms,
                "message": "Transfer completed"
            }

            outcome = "SUCCESS"

        # Store in cache
        await idempotency_cache.set(idem_key, response_body, status_code)

        # Observe metrics
        duration = time.time() - start_time
        payments_requests_total.labels(
            host=tenant,
            route="/transfer",
            method="POST",
            status=str(status_code)
        ).inc()

        payments_latency_seconds.labels(
            host=tenant,
            route="/transfer",
            method="POST"
        ).observe(duration)

        # Structured logging
        log_entry = {
            "ts": datetime.now(timezone.utc).isoformat(),
            "level": "INFO",
            "tenant": tenant,
            "route": "/transfer",
            "method": "POST",
            "status": status_code,
            "duration_ms": int(duration * 1000),
            "req_id": req_id,
            "outcome": outcome,
            "amount": str(transfer_req.amount),
            "currency": transfer_req.currency,
            "client_transfer_id": transfer_req.client_transfer_id,
            "source_account": redact_account(transfer_req.source_account),
            "destination_account": redact_account(transfer_req.destination_account),
        }

        if outcome == "FAILED":
            log_entry["reason"] = response_body["reason_code"]
        else:
            log_entry["transaction_id"] = transaction_id

        logger.info(json.dumps(log_entry))

        return JSONResponse(
            status_code=status_code,
            content=response_body,
            headers={"X-Request-ID": req_id}
        )


# Mount Prometheus metrics endpoint
metrics_app = make_asgi_app(registry=registry)
app.mount("/metrics", metrics_app)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)