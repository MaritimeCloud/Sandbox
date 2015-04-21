mc-mms-cli
==========================

Maritime Cloud MMS command line utilities.

Initially, there is a benchmark tool available.

## Prerequisites

* Java 8
* Maven (for building)
* A running and compatible MMS server to test against

## Building ##

    mvn clean install

## Launch
The benchmark tool is invoked using the following format:

**java -jar target/mc-mms-cli-0.3-SNAPSHOT.jar benchmark 
  [-ids *mmsi-list*]  [-n *iterations*] [-c *concurrency*] [-ttl *time-to-live*] 
  [-broadcasts *broadcast-no*] [-invocations *invocation-no*] [-t *benchmark-timeout*] 
  [-bin] [-v] [ws[s]://hostname[:port]]**
    
| Option      | Description |
| ----------- | ------------|
| ids         | List of MMSI client ID's, in the form m1,m2,m3-m4 |
| n           | The number of iterations for each client |
| c           | The number of concurrent clients. By default this will be identical to the number of MMSI ID's |
| ttl         | Minimum time-to-live of the client connection in milliseconds |
| broadcasts  | Number of broadcasts to send per client connection |
| invocations | Number of endpoint invocations to perform per client connection |
| t           | Maximum duration of the benchmark test in seconds |
| bin         | Turns on the use of the binary Protobuf protocol instead of a text-based JSON protocol |
| v           | Turn on verbosity |


Example:

    java -jar target/mc-mms-cli-0.3-SNAPSHOT.jar benchmark \
        -ids 266081000,222000000-222000010 -c 6 -n 2 -ttl 2000 \
        -broadcasts 1 -invocations 1 -bin wss://mms-test.e-navigation.net
    
## Docker

The mc-mms-cli utility can also be run via Docker.

To pull the docker image, use:

    docker pull dmadk/mc-mms-cli

To run the docker image, use the following format:

**docker run -e "ARGUMENTS=[arguments]"  dmadk/mc-mms-cli**

- where the *arguments* takes the format described above. Example:

    docker run -e \
        "ARGUMENTS=benchmark ids 266081000,269837000 -n 100 -broadcasts 1 wss://mms-test.e-navigation.net" \
        dmadk/mc-mms-cli
