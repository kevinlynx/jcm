package com.codemacro.jcm.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterManager {
  private static Logger logger = LoggerFactory.getLogger(ClusterManager.class);
  private Map<String, Cluster> clusters = new ConcurrentHashMap<String, Cluster>();
  
  public ClusterManager() {
    
  }
  
  public Cluster find(String name) {
    return clusters.get(name);
  }
  
  public void update(Cluster cluster) {
    logger.debug("update a cluster {}", cluster.getName());
    clusters.put(cluster.getName(), cluster);
  }
  
  public void remove(String name) {
    logger.debug("remove a cluster {}", name);
    clusters.remove(name);
  }
  
  public boolean exists(String name) {
    return clusters.containsKey(name);
  }
  
  public Map<String, Cluster> getAll() {
    return clusters;
  }
  
  public Set<String> getNames() {
    return clusters.keySet();
  }
}
