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
package com.codemacro.jcm.test;

import java.util.Arrays;
import java.util.Set;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.model.Node;
import com.codemacro.jcm.sub.NodeAllocator;
import com.codemacro.jcm.sub.Subscriber;
import com.codemacro.jcm.sub.allocator.RRAllocator;
import com.codemacro.jcm.util.HttpClient;
import com.codemacro.jcm.util.JsonUtil;
import com.google.common.collect.ImmutableSet;

public class SubscriberPerf implements Runnable {
  private static final String CLUSTER = "_cluster";
  private int callCnt = 0;
  private long cost = 0;
  private Thread thread;
  private RRAllocator rrAlloc;
  
  public SubscriberPerf(int cnt, RRAllocator alloc) {
    this.rrAlloc = alloc;
    this.callCnt = cnt;
  }

  public void start() {
    thread = new Thread(this);
    thread.start();
  }

  public void waitFor() {
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    long start = System.currentTimeMillis();
    for (int i = 1; i <= callCnt; ++i) {
      NodeAllocator.Host host = rrAlloc.alloc(CLUSTER, ProtoType.HTTP);
      if (i % 10000 == 0) {
        System.err.println("callcnt " + i);
      }
      if (host == null) {
        System.err.println("alloc failed " + i);
        break;
      }
    }
    this.cost = System.currentTimeMillis() - start;
  }

  private static void setUp(String srvIP, short srvPort) {
    String http = "http:" + srvPort;
    Set<Node> nodes = ImmutableSet.of(
        new Node(srvIP, "tcp:9000|" + http, ""),
        new Node(srvIP, "tcp:9001|" + http, ""),
        new Node(srvIP, "tcp:9002|" + http, ""),
        new Node(srvIP, "tcp:9003|" + http, ""),
        new Node(srvIP, "tcp:9004|" + http, ""));
    for (Node n : nodes) {
      n.setOnline(OnlineStatus.ONLINE);
    }
    Cluster cluster = new Cluster(CLUSTER, nodes);
    cluster.setCheckType(CheckType.HTTP);
    cluster.setCheckFile("/c/" + CLUSTER); // health check 200
    String url = String.format("http://%s:%d/c", srvIP, srvPort);
    HttpClient.postJson(url, JsonUtil.toString(cluster));
    waitUp(100);
    url = String.format("http://%s:%d/c/online/%s", srvIP, srvPort, CLUSTER);
    HttpClient.postJson(url, "true");
    waitUp(3000);
  }
  
  private static void tearDown(String srvIP, short srvPort) {
    String url = String.format("http://%s:%d/c/%s", srvIP, srvPort, CLUSTER);
    HttpClient.simpleDelete(url);
  }
  
  public static void main(String[] args) {
    String IP = "127.0.0.1";
    short port = 8080;
    setUp(IP, port);
    Subscriber subscriber = new Subscriber(
        Arrays.asList(IP + ":" + port), Arrays.asList(CLUSTER));
    RRAllocator rr = new RRAllocator();
    subscriber.addListener(rr);
    subscriber.startup();
    System.out.println("start test");
    SubscriberPerf[] perflist = new SubscriberPerf[100];
    for (int i = 0; i < perflist.length; ++i) {
      perflist[i] = new SubscriberPerf(100000, rr);
      perflist[i].start();
    }
    int totalCall = 0;
    long totalCost = 0;
    for (int i = 0; i < perflist.length; ++i) {
      perflist[i].waitFor();
      totalCall += perflist[i].callCnt;
      totalCost += perflist[i].cost;
    }
    System.out.println(String.format("test end: callCnt %d, cost(ms) %d, avg(ms) %.2f",
        totalCall, totalCost, totalCost * 1.0 / totalCall));
    subscriber.shutdown();
    tearDown(IP, port);
  }

  private static void waitUp(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
