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

import static com.codemacro.jcm.model.Common.*;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {
  public static class ServiceProto {
    public short port;
    public ProtoType proto;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + port;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ServiceProto)) {
        return false;
      }
      ServiceProto other = (ServiceProto) obj;
      if (port != other.port) {
        return false;
      }
      if (proto != other.proto) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return PROTO_STR.get(proto) + ":" + port;
    }

    static ServiceProto fromString(String str) {
      String[] kvs = str.split(":");
      if (kvs.length != 2) {
        throw new IllegalArgumentException(str);
      }
      ServiceProto proto = new ServiceProto();
      proto.proto = Common.getProtoType(kvs[0]);
      proto.port = (short) Integer.parseInt(kvs[1]);
      return proto;
    }
  }

  private String ip;
  private Set<ServiceProto> protos;
  private String udata;
  private NodeStatus status = NodeStatus.INVALID;
  private OnlineStatus online = OnlineStatus.ONLINE;

  public Node() {
  }

  public Node(String ip, String protoStr) {
    this(ip, protoStr, "");
  }

  public Node(String ip, String protoStr, String udata) {
    this.ip = ip;
    this.udata = udata;
    String[] ps = protoStr.split("\\|");
    this.protos = new HashSet<ServiceProto>();
    for (String s : ps) {
      ServiceProto sp = ServiceProto.fromString(s);
      if (getProto(sp.proto) != null) {
        throw new IllegalArgumentException(protoStr);
      }
      this.protos.add(sp);
    }
  }

  @JsonIgnore
  public String getSpec() {
    if (protos.size() == 1) {
      return ip + "|" + protos.iterator().next();
    } else {
      String spec = ip + "|" + getProto(ProtoType.HTTP) + "|" + getProto(ProtoType.TCP);
      return spec;
    }
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public Set<ServiceProto> getProtos() {
    return protos;
  }

  @JsonIgnore
  public ServiceProto getProto(ProtoType ptype) {
    for (ServiceProto p : this.protos) {
      if (p.proto == ptype) {
        return p;
      }
    }
    return null;
  }

  public String getUdata() {
    return udata;
  }

  public void setUdata(String udata) {
    this.udata = udata;
  }

  public NodeStatus getStatus() {
    return status;
  }

  public void setStatus(NodeStatus status) {
    this.status = status;
  }

  public OnlineStatus getOnline() {
    return online;
  }

  public void setOnline(OnlineStatus online) {
    this.online = online;
  }

  @Override
  public int hashCode() {
    return getSpec().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof Node)) {
      return false;
    }
    Node other = (Node) obj;
    return other.getSpec().equals(getSpec());
  }
  
  @Override
  public String toString() {
    return getSpec();
  }
}
