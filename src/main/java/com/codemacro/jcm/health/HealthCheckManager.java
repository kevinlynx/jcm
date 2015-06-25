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
package com.codemacro.jcm.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.storage.StatusStorage;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class HealthCheckManager implements CheckProvider {
  private static Logger logger = LoggerFactory.getLogger(HealthCheckManager.class);
  private Map<String, Cluster> checkClusters;
  private List<BaseChecker> checkers = new ArrayList<BaseChecker>();
  private ServerLocator locator = new ServerLocator();
  private StatusStorage statusStorage;
  private ClusterManager clusterManager;
  
  public HealthCheckManager() {
  }

  public HealthCheckManager(ClusterManager clusterManager, StatusStorage statusStorage) {
    this.clusterManager = clusterManager;
    this.statusStorage = statusStorage;
    checkClusters = new ConcurrentHashMap<String, Cluster>();
    checkers.add(new HttpChecker(this));
  }
  
  public void startup(int interval) {
    for (BaseChecker checker : checkers) {
      checker.setInterval(interval);
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
  
  public Set<String> getCheckClusterList() {
    return checkClusters.keySet();
  }
  
  public void onServerListChanged(int count, String selfSpec) {
    locator.serversChanged(selfSpec, count);
    updateCheckedClusters();
  }
  
  public void onClusterListChanged() {
    updateCheckedClusters();
  }

  public void flushCheckResults(String clusterName, Map<String, NodeStatus> statusList) {
    statusStorage.writeStatusList(clusterName, statusList);
  }
  
  private void updateCheckedClusters() {
    this.checkClusters = new ConcurrentHashMap<String, Cluster>();
    for (Cluster cluster : clusterManager.getAll().values()) {
      if (locator.isResponsible(cluster.getName())) {
        checkClusters.put(cluster.getName(), cluster);
      }
    }
    logger.info("update checked cluster list {}", checkClusters.size());
  }
}
