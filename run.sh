#!/usr/bin/env sh
echo "Running gatling tests " ${PERF_TEST_USERS}

TIMESTAMP=$(date +"%Y-%m-%dT%H-%M-%S")
OUTPUT_PATH=prepare-a-case-gatling-performance-tests

./gradlew gatlingRun --all -Dusername=${USERNAME} -Dpassword=${PASSWORD} -Denv=preprod -Dusers=${PERF_TEST_USERS} -Dramp=${PERF_TEST_DURATION} -Dpause=2

echo "USERNAME: " ${USERNAME}

# Start from the top-level directory
start_directory="app/build/reports/gatling"

# Function to recursively echo contents of a directory
echo_contents () {
  local directory=$1
  for entry in "$directory"/*; do
    if [ -d "$entry" ]; then
      echo "Directory: $entry"
      echo_contents "$entry"
    else
      echo "File: $entry"
    fi
  done
}

# Start from the specified directory
echo_contents "$start_directory"

aws s3 cp /app/build/reports/gatling/ s3://${S3_BUCKET_NAME}/${OUTPUT_PATH}/${TIMESTAMP}/reports --recursive
