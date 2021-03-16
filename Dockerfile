FROM maven:3.6.3-jdk-11

# working directory for gatling
WORKDIR /app

RUN mkdir -p /app

COPY . /app

ENV aws_cli=2.1.27

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64-2.1.27.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

CMD ["/app/run.sh"]