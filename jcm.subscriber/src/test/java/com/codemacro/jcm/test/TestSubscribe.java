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

import java.util.Set;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Node;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.model.SubscribeDef.ResponseUnit;
import com.codemacro.jcm.sub.NodeAllocator;
import com.codemacro.jcm.sub.SubscriberBase;
import com.codemacro.jcm.sub.allocator.RRAllocator;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

public class TestSubscribe extends TestCase {
  class MySubscriber extends SubscriberBase implements SubscriberBase.Listener {
    public ClusterManager clusterManager;
    
    public MySubscriber() {
    }

    public void init() {
      super.init();
    }

    public void proc(ResponseUnit...respList) {
      for (ResponseUnit unit : respList) {
        super.procResp(unit);
      }
    }
    
    public void onInit(ClusterManager clusterManager) {
      this.clusterManager = clusterManager;
    }

    public void onClusterUpdate(Cluster cluster) {
    }

    public void onClusterRemove(String name) {
    }
  }

  private MySubscriber subscriber = new MySubscriber();
  private RRAllocator rrAlloc = new RRAllocator();
  
  @Override
  public void setUp() {
    subscriber.addListener(rrAlloc);
    subscriber.addListener(subscriber);
    subscriber.init();
  }
  
  public void testProcResp() {
    // cluster update/remove
    Cluster c1 = new Cluster("hello", createNodes());
    subscriber.proc(ResponseUnit.create(c1));
    assertEquals(1, subscriber.clusterManager.getAll().size());
    Cluster c2 = new Cluster("hello2", createNodes());
    subscriber.proc(ResponseUnit.create(c2));
    assertEquals(2, subscriber.clusterManager.getAll().size());
    subscriber.proc(ResponseUnit.create("hello2"));
    assertEquals(1, subscriber.clusterManager.getAll().size());
    // status update
    Cluster cluster = subscriber.clusterManager.find("hello");
    assertTrue(cluster != null);
    String spec = "10.2.3.3|http:8000|tcp:9000";
    subscriber.proc(ResponseUnit.create("hello", spec));
    assertTrue(NodeStatus.NORMAL != cluster.findNode(spec).getStatus());

    // not exist
    spec = "10.2.3.6|http:8000|tcp:9000";
    subscriber.proc(ResponseUnit.create("hello", spec));
  }
  
  public void testRRAlloc() {
    Cluster c1 = new Cluster("hello", createNodes());
    subscriber.proc(ResponseUnit.create(c1));
    Cluster c2 = new Cluster("hello2", createNodes());
    subscriber.proc(ResponseUnit.create(c2));
    
    String spec = "10.2.3.4|http:8000|tcp:9000";
    NodeAllocator.Host host = rrAlloc.alloc("hello", ProtoType.HTTP);
    assertEquals(host.ip, "10.2.3.3");
    assertEquals(host.port, 8000);

    host = rrAlloc.alloc("hello", ProtoType.HTTP);
    assertEquals(host.ip, "10.2.3.4");

    host = rrAlloc.alloc("hello", ProtoType.HTTP);
    assertEquals(host.ip, "10.2.3.5");

    host = rrAlloc.alloc("hello", ProtoType.TCP);
    assertEquals(host.ip, "10.2.3.3");
    assertEquals(host.port, 9000);
    
    subscriber.proc(ResponseUnit.create("hello", spec));

    // skip 10.2.3.4
    host = rrAlloc.alloc("hello", ProtoType.HTTP);
    assertEquals(host.ip, "10.2.3.5");
  }
  
  private Set<Node> createNodes() {
    Set<Node> nodes = ImmutableSet.of(
        new Node("10.2.3.3", "tcp:9000|http:8000", ""),
        new Node("10.2.3.4", "tcp:9000|http:8000", ""),
        new Node("10.2.3.5", "tcp:9000|http:8000", ""));
    for (Node n : nodes) {
      n.setStatus(NodeStatus.NORMAL);
    }
    return nodes;
  }
}
