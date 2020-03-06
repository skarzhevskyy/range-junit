
See how the tests will run in multiple forks of  Maven Surefire Plugin. 

This example have 10 classes by 10 tests each and each test runs 1 second.
Total tests execution time 100 seconds.  (1 minute 40 seconds)

Assume that if we run the tests in 10 forks the maven build duration will be 10 seconds 
 @see https://stackoverflow.com/questions/51308145/any-way-to-run-junit5-tests-in-parallel
 
  run test:

     junit-jupiter:          5.5.2 
     maven-surefire-plugin:  2.22.2

                         reuseForks: true   reuseForks: false
    mvn -DforkCount=1

        Total time:      01:42 min


    mvn -DforkCount=2

        Total time:       01:31 min       |   53s
                            50 sec
                          01:01 min 

    mvn -DforkCount=4     01:01 min 


    mvn -DforkCount=5

        Total time:  01:01 min   
                 or  01:11 min 
                 or  01:21 min  
                 or 51 sec


    mvn -DforkCount=8   (Expected 21 s)

         2.22.2   Total time: 01:21 min         |   21s
         2.22.1   Total time: 01:01 min
                           or 51 sec
         3.0.0-M4 Total time: 01:41 min

    mvn -DforkCount=10

        Total time:       01:42 min         |    11s

