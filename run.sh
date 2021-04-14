#!/usr/bin/env sh

TIMESTAMP=$(date +"%Y-%m-%dT%H-%M-%S")
OUTPUT_PATH=prepare-a-case-gatling-performance-tests

mvn gatling:test -DUsers=${PERF_TEST_USERS} -DDuration=${PERF_TEST_DURATION} -DUsername=${USERNAME} -DPassword=${PASSWORD}  -DEnv=preprod -DThinkTime=3 -DDate=$(date +"%Y-%m-%d") -q

aws s3 cp /app/target/gatling/ s3://${S3_BUCKET_NAME}/${OUTPUT_PATH}/${TIMESTAMP}/reports --recursive
