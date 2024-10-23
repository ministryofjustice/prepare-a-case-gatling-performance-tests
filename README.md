prepare-a-case-gatling-performance-tests
=========================================

To test it out, simply execute the following command:

    $./gradlew gatlingRun --simulation pac.CaseListSimulation -Dusername=[username] -Dpassword=[password] -Denv=dev -Dusers=1 -Dramp=10 -Dpause=2

    --simulation => Specify which simulation to run. Not supplying this flag will present a menu to choose the simulation to run
    --all => will run all simultions
    -Dusername => Username for auth
    -Dpassword => Password for auth
    -Dusers => The number of simulated users, default is 1
    -Dramp => This is the duration over which the users will be lineraly started, in seconds. Default is 10
    -Denv => This will allow the tests to be ran against an enviroment. Default is dev
    -Dpause => This will allow for the test to be ran with pauses inbewteen requests. Default is 2 seconds
     
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
