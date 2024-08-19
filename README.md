prepare-a-case-gatling-performance-tests
=========================

Simple showcase of a maven project using the gatling-maven-plugin.

To test it out, simply execute the following command:

    $mvn gatling:test -Dgatling.simulationsClass=simulations.ProbationRecordScreen

or simply:

 mvn clean gatling:test -DUsers=1 -DDuration=1 -DEnv=preprod -DThinkTime=5 -DDate=2021-03-30 -Dusername=<username> -Dpassword=<password>

    
    -DUSER => This will allow the test to run with 1 user via the terminal command prompt. 
    -DDuration => This will allow the test to run for 1 minute via the terminal command prompt. - we normally specify that it should be a 10 minute test.
    -DEnv=preprod => This will allow the tests to be ran against the preprod enviroment. - In previous tests we tested against 'preprod'.
    -DDate=2021-03-30 => This will allow for the front-end to show cases against the given date. It will auto input 2021-03-30 if nothing is specified on cmd line. 
    This date is chosen as auto as it has suffecient data across all courts for tests to be ran.
    -DThinkTime=5 => This will allow for the test to be ran with pauses of 5 seconds inbewteen requests.
    This is what is done to mimic real user actions via the front end. NOTE: For debugging we rn with either 0 or 1. 
     
We can see response times for the requests within the /results directory, when opening this, find the date and time you ran the test
You will then see a file named 'index.html' if you open this, it will show reports from the run.

# NOTE 
If data refresh or enviroment changes made, user credentials may be affected. If this occurs either update the user details in prepare-a-case-gatling-performance-tests\src\test\resources\data\mined-data\users OR insert new details over the top of old (requires a username and password that works on the front-end).

# NOTE
If the date chosen on cmd line or enviroment changes effects data on 2021-03-30 front-end. Data will need to be put on to the front-end for a date on every court for the tests not to fail due to data. 
  
# Build and run Docker image locally

If making changes to the docker image you can build and run these locally using the following commands:

`docker build ./ -t prepare-a-case-gatling-performance-tests`

You will need to retrieve the AWS S3 access details from the kubernetes cluster secret to run the tests with the following command:

`docker run -e AWS_ACCESS_KEY_ID={ACCESS_KEY_ID} -e AWS_SECRET_ACCESS_KEY={SECRET_ACCESS_KEY} -e S3_BUCKET_NAME={S3_BUCKET_NAME} prepare-a-case-performance-test:latest`
