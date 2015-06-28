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
package com.codemacro.jcm.sub.allocator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.model.Node;
import com.codemacro.jcm.sub.NodeAllocator;

public class RRAllocator implements NodeAllocator {
  protected ClusterManager clusterManager;
  private Map<String, Integer> clusterPosList;

  public void onInit(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
    this.clusterPosList = new ConcurrentHashMap<String, Integer>();
  }

  public synchronized void onClusterUpdate(Cluster cluster) {
    clusterPosList.put(cluster.getName(), 0);
  }

  public synchronized void onClusterRemove(String name) {
    clusterPosList.remove(name);
  }

  public synchronized Host alloc(String name, ProtoType proto) {
    Cluster cluster = clusterManager.find(name);
    if (cluster == null) {
      return null;
    }
    int pos = this.clusterPosList.get(name);
    int ncnt = cluster.getNodes().size();
    if (ncnt == 0) {
      return null;
    }
    Node[] nodes = new Node[ncnt];
    cluster.getNodes().toArray(nodes);
    Node foundNode = null;
    int foundPort = 0;
    for (int i = 0; i < nodes.length; ++i) {
      Node n = nodes[pos];
      pos = (pos + 1) % nodes.length;
      if (n.getStatus() == NodeStatus.NORMAL) {
        Node.ServiceProto sp = proto == ProtoType.ANY ?
            n.getProtos().iterator().next() : 
            n.getProto(proto);
        if (sp != null) {
          foundNode = n;
          foundPort = sp.port;
          break;
        }
      }
    }
    this.clusterPosList.put(name, pos);
    if (foundNode == null) {
      return null;
    }
    return new Host(foundNode.getIp(), foundPort);
  }

}
