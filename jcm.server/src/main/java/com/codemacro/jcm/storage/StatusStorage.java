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
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.util.CompressionUtil;
import com.codemacro.jcm.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;

public class StatusStorage extends ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(StatusStorage.class);
  private ClusterManager clusterManager;
  private Set<String> clusterNames;
  private Map<String, String> statusCache;
  private volatile boolean enableCache = true;

  public StatusStorage(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
    this.statusCache = new ConcurrentHashMap<String, String>();
  }

  public void writeStatusList(String clusterName, Map<String, NodeStatus> statusList) {
    String path = fullPath + "/" + clusterName;
    if (logger.isTraceEnabled()) {
      logger.trace("flush node status to cluster [{}] {}", clusterName, statusList.size());
    }
    String data = JsonUtil.toString(statusList);
    if (!enableCache || refreshCache(clusterName, data)) {
      // only write to zookeeper, local status will be updated from zookeeper
      byte[] compress;
      try {
        compress = CompressionUtil.compress(data.getBytes());
      } catch (IOException e) {
        logger.warn("compress [{}] status failed {}", clusterName, e);
        return ;
      }
      writeData(path, compress, false);
    }
  }
  
  public void removeInvalidCache(Set<String> validNames) {
    int old = statusCache.size();
    for (String name : statusCache.keySet()) {
      if (!validNames.contains(name)) {
        statusCache.remove(name);
      }
    }
    logger.debug("remove invalid cache cnt {} -> {}", old, statusCache.size());
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
      String json = loadStatusData(name);
      if (json == null) {
        return ;
      }
      Map<String, NodeStatus> statusList = JsonUtil.fromString(json, 
          new TypeReference<Map<String, NodeStatus>>() {});
      Cluster cluster = clusterManager.find(name);
      if (cluster != null) {
        if (logger.isTraceEnabled()) {
          logger.trace("update cluster [{}] nodes status", name);
        }
        cluster.setNodesStatus(statusList);
      } else {
        logger.warn("not found cluster [{}] when update node status", name);
      }
    } catch (IOException e) {
      logger.warn("load cluster [{}] node status failed", name);
    }
  }
  
  private String loadStatusData(String name) {
    String path = fullPath + "/" + name;
    byte[] raw = getData(path);
    if (raw.length == 0) {
      return null;
    }
    try {
      byte[] decompress = CompressionUtil.decompress(raw);
      return new String(decompress);
    } catch (IOException e) {
      logger.warn("decompress [{}] status data failed {}", name, e);
    } catch (DataFormatException e) {
      logger.warn("decompress [{}] status data failed {}", name, e);
    }
    return null;
  }
  
  private void loadAll() {
    List<String> names = getChildren();
    this.clusterNames = new HashSet<String>(names);
    for (String name : names) {
      loadNodesStatus(name);
    }
  }
  
  private boolean refreshCache(String cluster, String data) {
    String old = statusCache.get(cluster);
    if (old == null || !old.equals(data)) {
      statusCache.put(cluster, data);
      return true;
    }
    return false;
  }

  public boolean isEnableCache() {
    return enableCache;
  }

  public void setEnableCache(boolean enableCache) {
    this.enableCache = enableCache;
  }
}
