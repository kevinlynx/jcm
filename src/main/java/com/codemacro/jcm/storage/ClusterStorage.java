package com.codemacro.jcm.storage;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.util.JsonUtil;
import com.google.common.collect.Sets;

public class ClusterStorage extends ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(ClusterStorage.class);
  private ClusterManager clusterManager;
  
  public ClusterStorage(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
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
  }

  @Override
  void onConnected() { // first connected or reconnected
    touch(fullPath);
    loadClusters();
  }
  
  public boolean updateCluster(Cluster cluster) {
    String data = JsonUtil.toString(cluster);
    if (data.isEmpty()) {
      return false;
    }
    String path = fullPath + "/" + cluster.getName();
    if (writeData(path, data.getBytes())) {
      // it does not matter to receive watch event later
      clusterManager.update(cluster);
      return true;
    }
    return false;
  }
  
  public boolean removeCluster(String name) {
    try {
      zkStorage.getZooKeeper().delete(fullPath + "/" + name, -1);
      clusterManager.remove(name);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      logger.warn("remove cluster failed [{}]", name);
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
        logger.debug("add cluster [{}]", cluster.getName());
      } catch (IOException e) {
        logger.warn("decode cluster failed [{}] {}", name, e);
      }
    }
  }
}
