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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Node;
import com.codemacro.jcm.util.JsonUtil;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

public class TestCluster extends TestCase {
  public void testNode() throws JsonGenerationException, JsonMappingException, IOException {
    Node node = new Node("127.0.0.1", "tcp:9000|http:8000", "");
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(node);
    Node node2 = mapper.readValue(json, Node.class);
    assertEquals(node, node2);
  }
  
  public void testCluster() {
    Set<Node> nodes = ImmutableSet.of(new Node("127.0.0.1", "tcp:9000|http:8000", ""));
    Cluster c1 = new Cluster("hello", nodes);
    String json = JsonUtil.toString(c1);
    System.out.println(json);
  }
  
  public void testStatus() throws IOException {
    Map<String, NodeStatus> statusList = Maps.newHashMap();
    statusList.put("127.0.0.1|http:8000", NodeStatus.TIMEOUT);
    statusList.put("127.0.0.1|http:9000", NodeStatus.NORMAL);
    String json = JsonUtil.toString(statusList);
    Map<String, NodeStatus> map = 
      JsonUtil.fromString(json, new TypeReference<Map<String, NodeStatus>>() {});
    System.out.println(map);
  }
}
