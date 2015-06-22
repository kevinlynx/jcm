package com.codemacro.jcm.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.storage.StatusStorage;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class HealthCheckManager implements CheckProvider {
  private static Logger logger = LoggerFactory.getLogger(HealthCheckManager.class);
  private Map<String, Cluster> checkClusters;
  private List<BaseChecker> checkers = new ArrayList<BaseChecker>();
  private StatusStorage statusStorage;
  
  public HealthCheckManager() {
  }

  public HealthCheckManager(StatusStorage statusStorage) {
    this.statusStorage = statusStorage;
    checkClusters = new ConcurrentHashMap<String, Cluster>();
    checkers.add(new HttpChecker(this));
  }
  
  public void startup() {
    for (BaseChecker checker : checkers) {
      checker.setInterval(1000);
      checker.startup();
    }
  }
  
  public void shutdown() {
    for (BaseChecker checker : checkers) {
      checker.shutdown();
    }
  }

  // require thread-safe
  public Map<String, Cluster> getCheckClusters(final CheckType checkType) {
    return Maps.filterValues(checkClusters, new Predicate<Cluster>() {
      public boolean apply(Cluster cluster) {
        return cluster.getCheckType().equals(checkType);
      }
    });
  }
  
  public void onServerListChanged(List<String> serverList, String selfSpec) {
    
  }
  
  public void onClusterListChanged(Map<String, Cluster> all) {
    // TODO: filter clusters
    this.checkClusters = all;
    logger.debug("update health check clusters {}", all.size());
  }

  public void flushCheckResults(String clusterName, Map<String, NodeStatus> statusList) {
    statusStorage.writeStatusList(clusterName, statusList);
  }
}
