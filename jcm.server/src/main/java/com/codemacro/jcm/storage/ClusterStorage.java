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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.health.HealthCheckManager;
import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.util.JsonUtil;
import com.google.common.collect.Sets;

public class ClusterStorage extends ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(ClusterStorage.class);
  private ClusterManager clusterManager;
  private HealthCheckManager healthCheckManager;

  public ClusterStorage(ClusterManager clusterManager, HealthCheckManager healthCheckManager) {
    this.clusterManager = clusterManager;
    this.healthCheckManager = healthCheckManager;
  }

  @Override
  String getPath() {
    return "clusters";
  }

  @Override
  void onListChanged() {
    Set<String> names = new HashSet<String>(getChildren());
    logger.info("cluster list changed {}", names.size());
    Set<String> existed = clusterManager.getNames();
    Set<String> added = Sets.difference(names, existed);
    for (String name : added) {
      loadCluster(name);
    }
    Set<String> removed = Sets.difference(existed, names);
    for (String name : removed) {
      clusterManager.remove(name);
    }
    if (healthCheckManager != null) {
      healthCheckManager.onClusterListChanged();
    }
  }

  @Override
  void onChildData(String childName) {
    logger.info("cluster {} changed", childName);
    loadCluster(childName);
    if (healthCheckManager != null) {
      healthCheckManager.onClusterListChanged();
    }
  }

  @Override
  void onConnected() { // first connected or reconnected
    touch(fullPath);
    loadClusters();
    if (healthCheckManager != null) {
      healthCheckManager.onClusterListChanged();
    }
  }
  
  public boolean updateCluster(Cluster cluster) {
    cluster.setVersion(System.currentTimeMillis());
    String data = JsonUtil.toString(cluster);
    if (data.isEmpty()) {
      return false;
    }
    String path = fullPath + "/" + cluster.getName();
    return writeData(path, data.getBytes());
  }
  
  public boolean removeCluster(String name) {
    try {
      zkStorage.getZooKeeper().delete(fullPath + "/" + name, -1);
      clusterManager.remove(name);
      return true;
    } catch (Exception e) {
      logger.warn("remove cluster failed [{}]", name);
      e.printStackTrace();
      return false;
    }
  }
  
  private void loadClusters() {
    List<String> names = getChildren();
    for (String name : names) {
      loadCluster(name);
    }
    logger.info("load all clusters done");  
  }
  
  private void loadCluster(String name) {
    String path = this.fullPath + "/" + name;
    byte[] data = getData(path);
    if (data.length > 0) {
      String json = new String(data);
      try {
        Cluster cluster = JsonUtil.fromString(json, Cluster.class);
        clusterManager.update(cluster);
      } catch (IOException e) {
        logger.warn("decode cluster failed [{}] {}", name, e);
      }
    }
  }
}
