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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wink.client.MockHttpServer;

import com.codemacro.jcm.health.BaseChecker;
import com.codemacro.jcm.health.CheckProvider;
import com.codemacro.jcm.health.HttpChecker;
import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Node;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

public class TestHttpChecker extends TestCase implements CheckProvider {
  private static final int PORT = 3333;
  private Map<String, Cluster> clusters = new ConcurrentHashMap<String, Cluster>();
  private Node n1 = new Node("127.0.0.1", "http:" + (PORT + 1));
  private Node n2 = new Node("10.28.29.30", "http:" + (PORT + 1));
  private Node n3 = new Node("127.0.0.1", "http:9999|tcp:8888");
  private MockHttpServer mockServer = new MockHttpServer(PORT);
  
  @Override
  public void setUp() {
    n1.setOnline(OnlineStatus.ONLINE);
    n2.setOnline(OnlineStatus.ONLINE);
    n3.setOnline(OnlineStatus.ONLINE);
    Cluster c1 = new Cluster("http", ImmutableSet.of(n1, n3));
    Cluster c2 = new Cluster("http2", ImmutableSet.of(n2));
    clusters.put(c1.getName(), c1);
    clusters.put(c2.getName(), c2);
  }
  
  public void testCheck() throws InterruptedException {
    mockServer.startServer();
    setStatus(mockServer, 200, 2);
    HttpChecker checker = new HttpChecker(this);
    checker.setInterval(200);
    invoke(checker, HttpChecker.class, "createHttp");
    invoke(checker, BaseChecker.class, "runOnce");
    assertEquals(n1.getStatus(), NodeStatus.NORMAL);
    assertEquals(n2.getStatus(), NodeStatus.TIMEOUT);
    assertEquals(n3.getStatus(), NodeStatus.TIMEOUT);
    setStatus(mockServer, 404, 2);
    invoke(checker, BaseChecker.class, "runOnce");
    assertEquals(n1.getStatus(), NodeStatus.ABNORMAL);

    mockServer.stopServer();
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
  
  private void setStatus(MockHttpServer server, int status, int cnt) {
    MockHttpServer.MockHttpServerResponse[] resp = new MockHttpServer.MockHttpServerResponse[cnt];
    for (int i = 0; i < cnt; ++i) {
      MockHttpServer.MockHttpServerResponse response = new MockHttpServer.MockHttpServerResponse();
      response.setMockResponseContent("ok");
      response.setMockResponseCode(status);
      resp[i] = response;
    }
    server.setMockHttpServerResponses(resp);
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
