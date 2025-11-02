.PHONY: build clean run
SHELL := /bin/bash

PROXY := https://myuser:test@localhost:3128
SEARCH_URL := http://alpha.kclab.site/search
TRANSFER_URL := http://beta.kclab.site/transfer

build:
	./br_cleanup.sh
	./br_setup.sh&
	docker compose -f compose.dmz.yml up -d --wait && \
	docker compose -f compose.monitor.yml up -d

clean:
	docker compose -f compose.dmz.yml -f compose.monitor.yml down
	./br_cleanup.sh

# Run the request (follows redirects; cleaner error if non-2xx)
run:
	@curl --proxy-insecure -x $(PROXY) http://alpha.kclab.site/search\
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"account_id\": \"$$(uuidgen)\"}"
	@echo
	@curl --proxy-insecure -x $(PROXY) http://alpha.kclab.site/transfer \
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"client_transfer_id\": \"test-001\",\"source_account\": \"$$(uuidgen)\",\"destination_account\": \"$$(uuidgen)\",\"amount\": $$(( ( RANDOM % 151 ) - 10 )),\"currency\": \"BDT\",\"reference\": \"Test payment\"}"
	@echo

	@curl --proxy-insecure -x $(PROXY) http://beta.kclab.site/search \
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"account_id\": \"$$(uuidgen)\"}"
	@echo
	@curl --proxy-insecure -x $(PROXY) http://beta.kclab.site/transfer \
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"client_transfer_id\": \"test-001\",\"source_account\": \"$$(uuidgen)\",\"destination_account\": \"$$(uuidgen)\",\"amount\": $$(( ( RANDOM % 151 ) - 10 )),\"currency\": \"BDT\",\"reference\": \"Test payment\"}"
	@echo

	@curl --proxy-insecure -x $(PROXY) http://gamma.kclab.site/search \
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"account_id\": \"$$(uuidgen)\"}"
	@echo
	@curl --proxy-insecure -x $(PROXY) http://gamma.kclab.site/transfer \
		-X 'POST' \
		-H 'Content-Type: application/json' \
		-d "{\"client_transfer_id\": \"test-001\",\"source_account\": \"$$(uuidgen)\",\"destination_account\": \"$$(uuidgen)\",\"amount\": $$(( ( RANDOM % 151 ) - 10 )),\"currency\": \"BDT\",\"reference\": \"Test payment\"}"
	@echo

