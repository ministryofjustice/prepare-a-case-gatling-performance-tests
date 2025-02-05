FROM openjdk:21-jdk-slim-buster

RUN groupadd --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# working directory for gatling
WORKDIR /app

RUN mkdir -p /app

COPY . /app
RUN chown -R appuser:appgroup /app

USER 2000
CMD ["/app/run.sh"]
