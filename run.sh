#!/usr/bin/env sh

TIMESTAMP=$(date +"%Y-%m-%dT%H-%M-%S")
OUTPUT_PATH=cpmg-gatling-performance-tests

mvn gatling:test -DUsers=1 -DDuration=1 -DEnv=preprod -DThinkTime=3 -DDate=2021-03-19 -q

aws s3 cp /app/target/gatling/ s3://${S3_BUCKET_NAME}/${OUTPUT_PATH}/${TIMESTAMP}/reports --recursive
