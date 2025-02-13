#!/usr/bin/env sh

./gradlew gatlingRun --all -Dusername=${USERNAME} -Dpassword=${PASSWORD} -Denv=preprod -Dusers=${PERF_TEST_USERS} -Dramp=${PERF_TEST_DURATION} -Dpause=2
