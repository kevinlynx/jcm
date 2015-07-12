package com.codemacro.jcm.test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wink.client.MockHttpServer;

import com.codemacro.jcm.health.BaseChecker;
import com.codemacro.jcm.health.CheckProvider;
import com.codemacro.jcm.health.TcpChecker;
import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Node;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

public class TestTcpChecker extends TestCase implements CheckProvider {
  private static final int PORT = 3333;
  private Map<String, Cluster> clusters = new ConcurrentHashMap<String, Cluster>();
  private Node n1 = new Node("127.0.0.1", "tcp:" + (PORT + 1));
  private Node n2 = new Node("10.28.29.30", "tcp:" + (PORT + 1));
  private Node n3 = new Node("127.0.0.1", "http:9999|tcp:8889");
  private MockHttpServer mockServer = new MockHttpServer(PORT);
  
  @Override
  public void setUp() {
    n1.setOnline(OnlineStatus.ONLINE);
    n2.setOnline(OnlineStatus.ONLINE);
    n3.setOnline(OnlineStatus.ONLINE);
    Cluster c1 = new Cluster("tcp", ImmutableSet.of(n1, n3));
    Cluster c2 = new Cluster("tcp2", ImmutableSet.of(n2));
    clusters.put(c1.getName(), c1);
    clusters.put(c2.getName(), c2);
  }

  public void testCheck() throws InterruptedException {
    mockServer.startServer();
    TcpChecker checker = new TcpChecker(this);
    checker.setInterval(1000);
    invoke(checker, BaseChecker.class, "runOnce");
    assertEquals(n1.getStatus(), NodeStatus.NORMAL);
    assertEquals(n2.getStatus(), NodeStatus.TIMEOUT);
    assertEquals(n3.getStatus(), NodeStatus.TIMEOUT);
    mockServer.stopServer();
    invoke(checker, BaseChecker.class, "runOnce");
    assertEquals(n1.getStatus(), NodeStatus.TIMEOUT);
    assertEquals(n2.getStatus(), NodeStatus.TIMEOUT);
    assertEquals(n3.getStatus(), NodeStatus.TIMEOUT);
  }

  private void invoke(BaseChecker checker, Class<?> clazz, String name) {
    try {
      Method m = clazz.getDeclaredMethod(name, (Class<?>[])null);
      m.setAccessible(true);
      m.invoke(checker, (Object[]) null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Map<String, Cluster> getCheckClusters(CheckType checkType) {
    return clusters;
  }

  public void flushCheckResults(String clusterName, Map<String, NodeStatus> statusList) {
    System.out.println("update cluster status " + clusterName);
    Cluster c = clusters.get(clusterName);
    c.setNodesStatus(statusList);
  }

}
