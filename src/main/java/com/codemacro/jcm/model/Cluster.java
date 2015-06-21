package com.codemacro.jcm.model;

import java.util.Set;

import com.codemacro.jcm.model.Common.OnlineStatus;

public class Cluster {
  private String name;
  private OnlineStatus online = OnlineStatus.INITING;
  private Set<Node> nodes;
  
  public Cluster() { // default constructor for json
  }

  public Cluster(String name) {
    this(name, null);
  }
  
  public Cluster(String name, Set<Node> nodes) {
    this.name = name;
    this.nodes = nodes;
  }
  
  public Set<Node> getNodes() {
    return nodes;
  }

  public OnlineStatus getOnline() {
    return online;
  }

  public void setOnline(OnlineStatus online) {
    this.online = online;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
