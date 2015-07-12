## JCM

JCM is a distributed name service implementation based on ZooKeeper.

### FEATURE

* manage more than hundred thousand nodes (ip)
* map cluster name to node(ip) list
* health checking on every node
    * http status check
    * tcp connection check
* persistent storage cluster list into zookeeper
* totally distributed, read and write every JCM server
* HTTP api based on JSON

### Run

1. start zookeeper first (standalone or distributed)
2. write config file, see [config/application.properties](https://github.com/kevinlynx/jcm/blob/master/jcm.server/config/application.properties)
3. start JCM server

        java -jar jcm.server-0.1.0.jar

4. add some clusters with nodes to JCM with HTTP api

        curl -i -X POST http://10.181.97.106:8080/c -H "Content-Type:application/json" --data-binary @./doc/cluster_sample.json

5. use jcm.subscriber to subscribe clusters, to get nodes from JCM server

### Subscriber Usage

Subscriber is a library used in client side.

    Subscriber subscriber = new Subscriber( Arrays.asList("127.0.0.1:8080"),
        Arrays.asList("hello9", "hello"));
    RRAllocator rr = new RRAllocator();
    subscriber.addListener(rr);
    subscriber.startup();
    for (int i = 0; i < 2; ++i) {
      System.out.println(rr.alloc("hello9", ProtoType.HTTP));
    }
    subscriber.shutdown();

see more examples [jcm.subscriber/src/test](https://github.com/kevinlynx/jcm/tree/master/jcm.subscriber/src/test/java/com/codemacro/jcm/test)

### HTTP API Reference

See [doc/api.md](https://github.com/kevinlynx/jcm/blob/master/doc/api.md)

### Compile

    mvn package -Dmaven.test.skip=true

If run with tests, startup zookeeper first

### Implementation Brief

Architecture overview:

![simple-arch.jpg](https://raw.githubusercontent.com/kevinlynx/jcm/master/doc/asset/simple-arch.jpg)

Module overview:

![impl-module.jpg](https://raw.githubusercontent.com/kevinlynx/jcm/master/doc/asset/impl-module.jpg)

### LICENSE

    /*******************************************************************************
     *  Copyright Kevin Lynx (kevinlynx@gmail.com) 2015
     *
     *    Licensed under the Apache License, Version 2.0 (the "License");
     *    you may not use this file except in compliance with the License.
     *    You may obtain a copy of the License at
     *
     *        http://www.apache.org/licenses/LICENSE-2.0
     *
     *    Unless required by applicable law or agreed to in writing, software
     *    distributed under the License is distributed on an "AS IS" BASIS,
     *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     *    See the License for the specific language governing permissions and
     *    limitations under the License.
     *******************************************************************************/

