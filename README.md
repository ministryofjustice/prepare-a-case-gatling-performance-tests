gatling-maven-plugin-demo
=========================

Simple showcase of a maven project using the gatling-maven-plugin.

To test it out, simply execute the following command:

    $mvn gatling:test -Dgatling.simulationsClass=simulations.ProbationRecordScreen

or simply:

 mvn clean gatling:test -DUsers=1 -DDuration=1 -DEnv=preprod -DThinkTime=5

    
    -DUSER => This will allow the test to run with 1 user via the terminal command prompt. 
    -DDuration => This will allow the test to run for 1 minute via the terminal command prompt. - we normally specify that it should be a 10 minute test.
    -DEnv=preprod => This will allow the tests to be ran against the preprod enviroment. - In previous tests we tested against 'preprod' 
    
    -DData=normalData => This will allow for the test to be ran with mock data just representing as live data. Is used if the user does not have access to live data.
    OR USE
    -DThinkTime=5 => This will allow for the test to be ran with pauses of 5 seconds inbewteen requests.
    This is what is done to mimic real user actions via the front end. NOTE: For debugging we rn with either 0 or 1. 
     
We can see response times for the requests within the /results directory, when opening this, find the date and time you ran the test
You will then see a file named 'index.html' if you open this, it will show reports from the run.
  
# Build and run Docker image locally

If making changes to the docker image you can build and run these locally using the following commands:

`docker build ./ -t prepare-a-case-gatling-performance-tests`

You will need to retrieve the AWS S3 access details from the kubernetes cluster secret to run the tests with the following command:

`docker run -e AWS_ACCESS_KEY_ID={ACCESS_KEY_ID} -e AWS_SECRET_ACCESS_KEY={SECRET_ACCESS_KEY} -e S3_BUCKET_NAME={S3_BUCKET_NAME} prepare-a-case-performance-test:latest`