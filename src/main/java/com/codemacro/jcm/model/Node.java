package com.codemacro.jcm.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import static com.codemacro.jcm.model.Common.*;

public class Node {
  public static class ServiceProto {
    public short port;
    public int proto;
    
    @Override
    public String toString() {
      return PROTO_STR[proto] + ":" + port;
    }
    
    static ServiceProto fromString(String str) {
      String[] kvs = str.split(":");
      if (kvs.length != 2) {
        throw new IllegalArgumentException(str);
      }
      ServiceProto proto = new ServiceProto();
      for (int i = 0; i < PROTO_STR.length; ++i) {
        if (PROTO_STR[i].equals(kvs[0])) {
          proto.proto = i;
        }
      }
      proto.port = (short) Integer.parseInt(kvs[1]);
      return proto;
    }
  }

  private String ip;
  private ServiceProto[] proto;
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
    this.proto = new ServiceProto[2];
    for (String s : ps) {
      ServiceProto sp = ServiceProto.fromString(s);
      if (this.proto[sp.proto] != null) {
        throw new IllegalArgumentException(protoStr);
      }
      this.proto[sp.proto] = sp;
    }
  }

  @JsonIgnore
  public String getSpec() {
    String spec = ip + "|" + proto[0];
    if (proto[1] != null) {
      spec += "|" + proto[1];
    }
    System.out.println(spec);
    return spec;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public ServiceProto[] getProto() {
    return proto;
  }

  public ServiceProto getProto(int ptype) {
    return this.proto[ptype];
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
}
