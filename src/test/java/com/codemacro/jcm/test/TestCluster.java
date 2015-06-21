package com.codemacro.jcm.test;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.codemacro.jcm.model.Node;

import junit.framework.TestCase;

public class TestCluster extends TestCase {
  public void testNode() throws JsonGenerationException, JsonMappingException, IOException {
    Node node = new Node("127.0.0.1", "tcp:9000|http:8000", "");
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(node);
    Node node2 = mapper.readValue(json, Node.class);
    assertEquals(node, node2);
  }
}
