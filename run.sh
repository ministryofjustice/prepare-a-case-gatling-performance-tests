#!/usr/bin/env sh
echo "Running gatling tests " ${PERF_TEST_USERS}

TIMESTAMP=$(date +"%Y-%m-%dT%H-%M-%S")
OUTPUT_PATH=prepare-a-case-gatling-performance-tests

./gradlew --debug --stacktrace gatlingRun --all -Dusername=${USERNAME} -Dpassword=${PASSWORD} -Denv=preprod -Dusers=${PERF_TEST_USERS} -Dramp=${PERF_TEST_DURATION} -Dpause=2

aws s3 cp /app/build/reports/gatling/ s3://${S3_BUCKET_NAME}/${OUTPUT_PATH}/${TIMESTAMP}/reports --recursive
