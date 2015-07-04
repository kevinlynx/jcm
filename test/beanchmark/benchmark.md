
## Prepare

Test on :

    memory : 48G
    CPU: 24 cores

    $cat /proc/cpuinfo | grep name | cut -f2 -d: | uniq -c
         24  Intel(R) Xeon(R) CPU E5-2630 0 @ 2.30GHz

zookeeper standalone mode :

    $echo stat | nc 127.0.0.1 2181
    Zookeeper version: 3.4.3-1240972, built on 02/06/2012 10:48 GMT

install java 1.7

    sudo yum install ali-jdk -b current

    $java -version
    java version "1.7.0_75"
    Java(TM) SE Runtime Environment (build 1.7.0_75-b13)
    OpenJDK (Alibaba) 64-Bit Server VM (build 24.75-b04-internal, mixed mode)

config

    server.port=8080

    [jcm]
    zookeeper.host=10.181.97.17:2181
    zookeeper.root=/jcm
    health.interval=1000

deploy:

    10.181.97.17   zookeeper standalone
    10.181.97.120  health check dest
    10.181.97.4    jcm instance 1
    10.181.97.106  jcm instance 2

generate clusters:

    ./gencluster.sh

set clusters to jcm (40000 nodes, 80 clusters)

    ./setcluster.sh

## Result

2 instance enable write cache

Network traffic

    $tsar -i 2 -l --traffic
    Time              ---------------------traffic--------------------
    Time               bytin  bytout   pktin  pktout  pkterr  pktdrp
    01/07/15-21:30:48   3.2M    4.1M   33.5K   34.4K    0.00    0.00
    01/07/15-21:30:50   3.3M    4.2M   33.7K   35.9K    0.00    0.00
    01/07/15-21:30:52   2.8M    4.1M   32.6K   41.6K    0.00    0.00
    01/07/15-21:30:54   1.7M    3.5M   25.2K   43.5K    0.00    0.00
    01/07/15-21:30:56   2.2M    3.9M   27.2K   42.3K    0.00    0.00
    01/07/15-21:30:58   4.0M    4.6M   40.2K   39.6K    0.00    0.00
    01/07/15-21:31:00   3.7M    4.5M   37.8K   39.0K    0.00    0.00

CPU usage

      PID USER      PR  NI  VIRT  RES  SHR S %CPU %MEM    TIME+  COMMAND
    13301 admin     20   0 13.1g 1.1g  12m R 76.6  2.3   2:40.74 java         httpchecker
    13300 admin     20   0 13.1g 1.1g  12m S 72.9  2.3   0:48.31 java
    13275 admin     20   0 13.1g 1.1g  12m S 20.1  2.3   0:18.49 java

Health checker stats

    2015-07-01 21:32:40.918  INFO 16162 --- [       Thread-3] com.codemacro.jcm.health.BaseChecker     : checker HttpChecker stat count 20 avg check cost(ms) 542.05, avg flush cost(ms) 41.35
    2015-07-01 21:33:01.453  INFO 16162 --- [       Thread-3] com.codemacro.jcm.health.BaseChecker     : checker HttpChecker stat count 20 avg check cost(ms) 467.2, avg flush cost(ms) 26.55
    2015-07-01 21:33:22.368  INFO 16162 --- [       Thread-3] com.codemacro.jcm.health.BaseChecker     : checker HttpChecker stat count 20 avg check cost(ms) 606.95, avg flush cost(ms) 45.6
    2015-07-01 21:33:43.156  INFO 16162 --- [       Thread-3] com.codemacro.jcm.health.BaseChecker     : checker HttpChecker stat count 20 avg check cost(ms) 496.9, avg flush cost(ms) 39.25

Disable cache, watch stats

    2015-07-01 22:55:33.338  INFO 13239 --- [ain-EventThread] c.c.jcm.storage.ClusterWatchStat         : watch stat [NodeStatus] total 38380, err 39, rt_avg(ms) 7.35

