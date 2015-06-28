## JCM

JCM is a distributed name service implementation based on ZooKeeper.

### FEATURE

* map cluster name to node(ip) list
* health checking on every node
* persistent storage cluster list into zookeeper
* totally distributed, read and write every JCM server
* HTTP api based on JSON

### Run

start zookeeper first

    java -jar jcm.server-0.1.0.jar

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

