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

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cluster {
  private String name;
  private OnlineStatus online = OnlineStatus.OFFLINE;
  private Set<Node> nodes;
  private String checkFile;
  private CheckType checkType = CheckType.NONE;

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

  public String getCheckFile() {
    return checkFile;
  }

  public void setCheckFile(String checkFile) {
    this.checkFile = checkFile;
  }

  public CheckType getCheckType() {
    return checkType;
  }

  public void setCheckType(CheckType checkType) {
    this.checkType = checkType;
  }

  public void setNodeStatus(String spec, NodeStatus status) {
    synchronized (nodes) {
      Node n = getNode(spec);
      if (n != null) {
        n.setStatus(status);
      }
    }
  }

  public void setNodeOnline(String spec, OnlineStatus status) {
    synchronized (nodes) {
      Node n = getNode(spec);
      if (n != null) {
        n.setOnline(status);
        n.updateLastCheck();
      }
    }
  }

  public void setNodesStatus(Map<String, NodeStatus> statusSet) {
    Map<String, Node> mNodes = null;
    synchronized (nodes) {
      mNodes = Maps.uniqueIndex(nodes, new Function<Node, String>() {
        public String apply(Node node) {
          return node.getSpec();
        }
      });
    }
    for (Map.Entry<String, NodeStatus> entry : statusSet.entrySet()) {
      Node n = mNodes.get(entry.getKey());
      if (n != null) {
        n.setStatus((NodeStatus) entry.getValue());
        n.updateLastCheck();
      }
    }
  }
  
  public void addNode(Node node) {
    synchronized (nodes) {
      nodes.add(node);
    }
  }
  
  public void removeNode(final String spec) {
    synchronized (nodes) {
      Node n = getNode(spec);
      if (n != null) {
        nodes.remove(n);
      }
    }
  }
  
  public Node findNode(final String spec) {
    synchronized (nodes) {
      return getNode(spec);
    }
  }
  
  private Node getNode(final String spec) {
    try {
      return Iterables.find(nodes, new Predicate<Node>() {
        public boolean apply(Node node) {
          return node.isMatch(spec);
        }
      });
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @JsonIgnore
  public boolean isValid() {
    return name.length() > 0;
  }

  @Override
  public String toString() {
    String ns = nodes == null ? "" : Joiner.on(",").join(nodes);
    return "{" + name + ", [" + ns + "]}";
  }
}
