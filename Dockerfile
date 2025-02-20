FROM openjdk:21-jdk-bullseye

RUN groupadd --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Install prerequisites
RUN apt-get update && apt-get install -y unzip

# working directory for gatling
WORKDIR /app

RUN mkdir -p /app

COPY . /app
RUN chown -R appuser:appgroup /app

ENV aws_cli=2.1.27

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64-2.1.27.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

USER 2000
ENTRYPOINT ["/app/run.sh"]