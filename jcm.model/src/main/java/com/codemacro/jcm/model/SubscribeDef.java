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
package com.codemacro.jcm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class SubscribeDef {
  public static class RequestUnit {
    public String name;
    public long version;
    public RequestUnit() {
    }
    public RequestUnit(String name, long version) {
      super();
      this.name = name;
      this.version = version;
    }
  }
  
  public static class Request {
    public String tag;
    public Collection<RequestUnit> units;
    
    public Request() {
    }

    public Request(String tag, Collection<RequestUnit> units) {
      this.tag = tag;
      this.units = units;
    }

    public void add(String name, long version) {
      units.add(new RequestUnit(name, version));
    }
  }
  
  public enum RespType {
    RESP_UPDATE,
    RESP_STATUS,
    RESP_REMOVE
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ResponseUnit {
    public String name;
    public RespType kind;
    public Cluster cluster;
    public List<String> invalids; // invalid node specs
    
    public static ResponseUnit create(Cluster cluster) {
      ResponseUnit resp = new ResponseUnit();
      resp.cluster = cluster;
      resp.kind = RespType.RESP_UPDATE;
      resp.name = cluster.getName();
      return resp;
    }

    public static ResponseUnit create(String name) {
      ResponseUnit resp = new ResponseUnit();
      resp.name = name;
      resp.kind = RespType.RESP_REMOVE;
      return resp;
    }

    public static ResponseUnit create(String name, String...specs) {
      ResponseUnit resp = new ResponseUnit();
      resp.name = name;
      resp.invalids = Arrays.asList(specs);
      resp.kind = RespType.RESP_STATUS;
      return resp;
    }
  }

  public static class Response {
    public List<ResponseUnit> units = new ArrayList<ResponseUnit>();
  }
}
