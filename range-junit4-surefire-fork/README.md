
See how the tests will run in multiple forks of  Maven Surefire Plugin. 

This example have 10 classes by 10 tests each and each test runs 1 second.
Total tests execution time 100 seconds.  (1 minute 40 seconds)

Assume that if we run the tests in 10 forks the maven build duration will be 10 seconds 

  run test:
     reuseForks: true
     junit-vintage-engine: 5.5.2
     maven-surefire-plugin: 2.22.1
                            2.22.2* not working

    mvn -DforkCount=1

        Total time:   min


    mvn -DforkCount=2

        Total time: 1:11 min


    mvn -DforkCount=5

        Total time: 1:21 min


    mvn -DforkCount=8

        Total time: 1:31 min



    mvn -DforkCount=10

        Total time: 1:01 min  or 51 s

