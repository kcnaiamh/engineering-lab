"""
Core Banking Balance Search Simulator
A FastAPI application that simulates balance search operations with configurable fault injection,
metrics collection, and JSON logging for a high-reliability banking system.
"""

import asyncio
import json
import logging
import os
import random
import time
from datetime import datetime
from typing import Dict, Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request, Response, status, HTTPException
from fastapi.responses import JSONResponse, PlainTextResponse
from pydantic import BaseModel, Field
from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST
import uvicorn

# Configure JSON logging
logging.basicConfig(
    level=logging.INFO,
    format='%(message)s',
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

# Environment variables for fault injection
FAULT_RATE = float(os.getenv('FAULT_RATE', '0.02'))  # 2% default failure rate
EXTRA_LATENCY_MS = int(os.getenv('EXTRA_LATENCY_MS', '0'))  # No extra latency by default
MAX_WORKERS = int(os.getenv('MAX_WORKERS', '100'))  # Concurrency limit

# Prometheus metrics
search_counter = Counter(
    'balance_search_requests_total',
    'Total number of balance search requests',
    ['tenant', 'route', 'status']
)

search_latency = Histogram(
    'balance_search_duration_seconds',
    'Balance search request duration in seconds',
    ['tenant', 'route'],
    buckets=(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1.0, 2.5, 5.0, 7.5, 10.0)
)

# Request/Response models
class BalanceSearchRequest(BaseModel):
    account_id: str = Field(..., description="Account ID to search balance for")
    customer_id: Optional[str] = Field(None, description="Optional customer ID for validation")

class BalanceSearchResponse(BaseModel):
    account_id: str
    balance: float
    currency: str = "USD"
    timestamp: str
    transaction_id: str

class ErrorResponse(BaseModel):
    error: str
    error_code: str
    message: str
    timestamp: str
    transaction_id: str

# Simulated account balances cache (for consistency)
account_balances_cache: Dict[str, float] = {}

# Semaphore for concurrency control
semaphore = asyncio.Semaphore(MAX_WORKERS)

def generate_transaction_id() -> str:
    """Generate a unique transaction ID for tracking"""
    return f"TXN-{int(time.time() * 1000000)}-{random.randint(1000, 9999)}"

def extract_tenant_from_host(host: Optional[str]) -> str:
    """Extract tenant identifier from Host header"""
    if not host:
        return "default"

    # Handle localhost and IP addresses
    if host.startswith("localhost") or host.startswith("127.0.0.1"):
        return "localhost"

    # Extract subdomain as tenant (e.g., "bank1.example.com" -> "bank1")
    parts = host.split('.')
    if len(parts) > 2:
        return parts[0]

    return host.split(':')[0]  # Remove port if present

async def simulate_latency():
    """Simulate network/processing latency"""
    base_latency = random.uniform(0.01, 0.05)  # 10-50ms base latency
    extra_latency = EXTRA_LATENCY_MS / 1000.0 if EXTRA_LATENCY_MS > 0 else 0
    total_latency = base_latency + extra_latency
    await asyncio.sleep(total_latency)

def should_fail() -> bool:
    """Determine if the request should fail based on FAULT_RATE"""
    return random.random() < FAULT_RATE

def get_failure_scenario() -> tuple[int, ErrorResponse]:
    """Generate a random failure scenario"""
    transaction_id = generate_transaction_id()
    timestamp = datetime.utcnow().isoformat() + "Z"

    scenarios = [
        (
            status.HTTP_404_NOT_FOUND,
            ErrorResponse(
                error="ACCOUNT_NOT_FOUND",
                error_code="ERR-404",
                message="The specified account could not be found in the system",
                timestamp=timestamp,
                transaction_id=transaction_id
            )
        ),
        (
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            ErrorResponse(
                error="INTERNAL_SERVER_ERROR",
                error_code="ERR-500",
                message="An internal error occurred while processing the request",
                timestamp=timestamp,
                transaction_id=transaction_id
            )
        ),
        (
            status.HTTP_503_SERVICE_UNAVAILABLE,
            ErrorResponse(
                error="SERVICE_UNAVAILABLE",
                error_code="ERR-503",
                message="The balance service is temporarily unavailable",
                timestamp=timestamp,
                transaction_id=transaction_id
            )
        ),
        (
            status.HTTP_408_REQUEST_TIMEOUT,
            ErrorResponse(
                error="REQUEST_TIMEOUT",
                error_code="ERR-408",
                message="The request timed out while fetching balance information",
                timestamp=timestamp,
                transaction_id=transaction_id
            )
        ),
        (
            status.HTTP_429_TOO_MANY_REQUESTS,
            ErrorResponse(
                error="RATE_LIMIT_EXCEEDED",
                error_code="ERR-429",
                message="Too many requests. Please try again later",
                timestamp=timestamp,
                transaction_id=transaction_id
            )
        )
    ]

    return random.choice(scenarios)

def get_or_generate_balance(account_id: str) -> float:
    """Get existing balance or generate new one for consistency"""
    if account_id not in account_balances_cache:
        # Generate a realistic balance
        account_balances_cache[account_id] = round(random.uniform(100.0, 100000.0), 2)
    return account_balances_cache[account_id]

def log_request(tenant: str, route: str, status_code: int, duration_ms: float, transaction_id: str):
    """Log request details in JSON format"""
    log_entry = {
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "tenant": tenant,
        "route": route,
        "status": status_code,
        "duration_ms": round(duration_ms, 2),
        "transaction_id": transaction_id,
        "level": "ERROR" if status_code >= 500 else "INFO"
    }
    logger.info(json.dumps(log_entry))

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Manage application lifecycle"""
    logger.info(json.dumps({
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "message": "Starting Core Banking Balance Search Simulator",
        "config": {
            "fault_rate": FAULT_RATE,
            "extra_latency_ms": EXTRA_LATENCY_MS,
            "max_workers": MAX_WORKERS
        },
        "level": "INFO"
    }))
    yield
    logger.info(json.dumps({
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "message": "Shutting down Core Banking Balance Search Simulator",
        "level": "INFO"
    }))

# Create FastAPI app
app = FastAPI(
    title="Core Banking Balance Search Simulator",
    description="High-performance balance search simulation with fault injection and metrics",
    version="1.0.0",
    lifespan=lifespan
)

@app.post("/search", response_model=BalanceSearchResponse)
async def balance_search(
    request: Request,
    search_request: BalanceSearchRequest
) -> JSONResponse:
    """
    Simulate a balance search operation with configurable fault injection.

    This endpoint simulates various scenarios including success, failures,
    and SLO breaches while maintaining consistency for banking operations.
    """
    start_time = time.time()
    transaction_id = generate_transaction_id()

    # Extract tenant from Host header
    host_header = request.headers.get("host")
    tenant = extract_tenant_from_host(host_header)
    route = "/search"

    async with semaphore:  # Concurrency control
        try:
            # Simulate processing latency
            await simulate_latency()

            # Determine success or failure
            if should_fail():
                # Generate failure scenario
                status_code, error_response = get_failure_scenario()
                error_response.transaction_id = transaction_id

                # Record metrics
                duration = time.time() - start_time
                search_counter.labels(tenant=tenant, route=route, status=str(status_code)).inc()
                search_latency.labels(tenant=tenant, route=route).observe(duration)

                # Log the request
                log_request(tenant, route, status_code, duration * 1000, transaction_id)

                return JSONResponse(
                    status_code=status_code,
                    content=error_response.dict()
                )

            # Success scenario
            balance = get_or_generate_balance(search_request.account_id)
            response = BalanceSearchResponse(
                account_id=search_request.account_id,
                balance=balance,
                currency="USD",
                timestamp=datetime.utcnow().isoformat() + "Z",
                transaction_id=transaction_id
            )

            # Record metrics
            duration = time.time() - start_time
            search_counter.labels(tenant=tenant, route=route, status="200").inc()
            search_latency.labels(tenant=tenant, route=route).observe(duration)

            # Log the request
            log_request(tenant, route, 200, duration * 1000, transaction_id)

            return JSONResponse(
                status_code=status.HTTP_200_OK,
                content=response.dict()
            )

        except Exception as e:
            # Handle unexpected errors
            duration = time.time() - start_time
            search_counter.labels(tenant=tenant, route=route, status="500").inc()
            search_latency.labels(tenant=tenant, route=route).observe(duration)

            log_request(tenant, route, 500, duration * 1000, transaction_id)

            error_response = ErrorResponse(
                error="UNEXPECTED_ERROR",
                error_code="ERR-500",
                message=f"An unexpected error occurred: {str(e)}",
                timestamp=datetime.utcnow().isoformat() + "Z",
                transaction_id=transaction_id
            )

            return JSONResponse(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                content=error_response.dict()
            )

@app.get("/metrics")
async def metrics():
    """
    Expose Prometheus metrics for monitoring.

    Returns metrics in Prometheus text format including:
    - Request counters by tenant, route, and status
    - Request latency histograms
    """
    return Response(
        content=generate_latest(),
        media_type=CONTENT_TYPE_LATEST
    )

@app.get("/health")
async def health_check():
    """Health check endpoint for container orchestration"""
    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content={
            "status": "healthy",
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "config": {
                "fault_rate": FAULT_RATE,
                "extra_latency_ms": EXTRA_LATENCY_MS
            }
        }
    )

@app.get("/")
async def root():
    """Root endpoint with API information"""
    return {
        "service": "Core Banking Balance Search Simulator",
        "version": "1.0.0",
        "endpoints": {
            "/search": "POST - Balance search operation",
            "/metrics": "GET - Prometheus metrics",
            "/health": "GET - Health check"
        },
        "documentation": "/docs"
    }

if __name__ == "__main__":
    # Run with uvicorn for production-ready async performance
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        workers=1,  # Use single worker with async for consistency
        loop="uvloop",  # High-performance event loop
        log_config=None  # Use our custom JSON logging
    )