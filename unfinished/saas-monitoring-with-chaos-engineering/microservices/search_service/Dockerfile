# Multi-stage build for production
FROM python:3.11-slim AS builder

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Create virtual environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy and install requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Production stage
FROM python:3.11-slim

# Copy virtual environment from builder
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Create non-root user
RUN useradd -m -u 1000 appuser

# Set working directory
WORKDIR /app


# Copy application code
COPY --chown=appuser:appuser app/ ./app/


RUN mkdir -p /tmp/prometheus
RUN chown appuser:appuser /tmp/prometheus

# Switch to non-root user
USER appuser



# Expose port
EXPOSE 8001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8001/metrics/')"

# Run the application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001", "--loop", "uvloop"]


# ###########################################################################
# # ---- Build stage ----
# FROM python:3.11.4-slim AS build
# WORKDIR /app

# ENV PIP_NO_CACHE_DIR=1 PIP_DISABLE_PIP_VERSION_CHECK=1
# COPY ./app/requirements.txt /app/requirements.txt
# RUN pip install --upgrade pip \
#  && pip install --no-cache-dir -r requirements.txt

# # ---- Runtime stage ----
# FROM gcr.io/distroless/python3
# WORKDIR /app

# COPY --from=build /usr/local/lib/python3.11/site-packages /usr/local/lib/python3.11/site-packages

# COPY ./app /app

# # (Optional) make sure Python can see site-packages
# ENV PYTHONPATH=/usr/local/lib/python3.11/site-packages

# EXPOSE 8001

# CMD ["-m", "uvicorn", "search:app", "--host", "0.0.0.0", "--port", "8001"]
