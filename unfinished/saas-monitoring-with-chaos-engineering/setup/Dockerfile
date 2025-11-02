ARG STACK_VERSION

# https://www.docker.elastic.co/
FROM docker.elastic.co/elasticsearch/elasticsearch:${STACK_VERSION:-9.1.1}

ENTRYPOINT ["/entrypoint.sh"]
