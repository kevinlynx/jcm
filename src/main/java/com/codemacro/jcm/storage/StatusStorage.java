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
package com.codemacro.jcm.storage;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;

public class StatusStorage extends ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(StatusStorage.class);
  private ClusterManager clusterManager;
  private Set<String> clusterNames;

  public StatusStorage(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
  }

  public void writeStatusList(String clusterName, Map<String, NodeStatus> statusList) {
    String path = fullPath + "/" + clusterName;
    String data = JsonUtil.toString(statusList);
    logger.debug("flush node status to cluster [{}]", clusterName);
    // update local status from zookeeper
    writeData(path, data.getBytes());
  }
  
  @Override
  String getPath() {
    return "status";
  }

  @Override
  void onConnected() { 
    touch(fullPath);
    loadAll();
  }

  @Override
  void onListChanged() {
    Set<String> names = new HashSet<String>(getChildren());
    logger.info("node status list changed {}", names.size());
    Set<String> added = Sets.difference(names, this.clusterNames);
    for (String name : added) {
      loadNodesStatus(name);
    }
    this.clusterNames = names;
  }

  @Override
  void onChildData(String childName) {
    loadNodesStatus(childName);
  }
  
  // use Map to represent node stauts, a simple but safe implementation
  private void loadNodesStatus(String name) {
    try {
      String path = fullPath + "/" + name;
      String data = new String(getData(path));
      Map<String, NodeStatus> statusList = JsonUtil.fromString(data, 
          new TypeReference<Map<String, NodeStatus>>() {});
      Cluster cluster = clusterManager.find(name);
      if (cluster != null) {
        logger.debug("update cluster [{}] nodes status", name);
        cluster.setNodesStatus(statusList);
      } else {
        logger.warn("not found cluster [{}] when update node status", name);
      }
    } catch (IOException e) {
      logger.warn("load cluster [{}] node status failed", name);
    }
  }
  
  private void loadAll() {
    List<String> names = getChildren();
    this.clusterNames = new HashSet<String>(names);
    for (String name : names) {
      loadNodesStatus(name);
    }
  }
}
