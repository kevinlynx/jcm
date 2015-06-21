package com.codemacro.jcm.util;

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtil {

  public static String toString(Object obj) {
    ObjectMapper mapper = new ObjectMapper();
    String json = "";
    try {
      json = mapper.writeValueAsString(obj);
    } catch (IOException e) {
    }
    return json;
  }
  
  public static <T> T fromString(String str, Class<T> clazz) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(str, clazz);
  }
}
