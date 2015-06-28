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
package com.codemacro.jcm.sub;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.SubscribeDef.ResponseUnit;

public class SubscriberBase {
  public interface Listener {
    void onInit(ClusterManager clusterManager);
    void onClusterUpdate(Cluster cluster);
    void onClusterRemove(String name);
  }
  
  private static Logger logger = LoggerFactory.getLogger(SubscriberBase.class);
  private ClusterManager clusterManager = new ClusterManager();
  private List<Listener> listeners = new LinkedList<Listener>();

  public void addListener(Listener listener) {
    this.listeners.add(listener);
  }

  protected void init() {
    for (Listener listener : this.listeners) {
      listener.onInit(clusterManager);
    }
  }

  protected void procResp(ResponseUnit unit) {
    switch (unit.kind) {
    case RESP_REMOVE:
      removeCluster(unit.name);
      break;
    case RESP_UPDATE:
      updateCluster(unit.cluster);
      break;
    case RESP_STATUS:
      updateStatus(unit.name, unit.invalids);
      break;
    }
  }
  
  protected void removeCluster(String name) {
    logger.debug("receive remove cluster: {}", name);
    this.clusterManager.remove(name);
    for (Listener listener : this.listeners) {
      listener.onClusterRemove(name);
    }
  }

  protected void updateCluster(Cluster cluster) {
    logger.debug("receive update cluster: {}", cluster.getName());
    this.clusterManager.update(cluster);
    for (Listener listener : this.listeners) {
      listener.onClusterUpdate(cluster);
    }
  }

  protected void updateStatus(String name, List<String> invalids) {
    logger.debug("receive update status: {}", name);
    Cluster cluster = clusterManager.find(name);
    if (cluster == null) {
      logger.warn("not found cluster: {}", name);
      return ;
    } 
    for (String spec : invalids) {
      cluster.setNodeStatus(spec, NodeStatus.INVALID);
    }
  } 
}
