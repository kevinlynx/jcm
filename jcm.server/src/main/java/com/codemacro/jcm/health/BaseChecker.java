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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Node;

public abstract class BaseChecker extends Thread {
  private static Logger logger = LoggerFactory.getLogger(BaseChecker.class);
  private static final int STAT_INTERVAL = Integer.parseInt(
      System.getProperty("jcm.stat.checker", "300"));
  private volatile boolean done = false;
  protected int interval = 0; // milliseconds
  protected CheckProvider provider;
  private Map<String, Map<String, NodeStatus>> clusterStatusList;
  private CheckType checkType;
  private long statChkTotalCost = 0;
  private long statFlushTotalCost = 0;
  private int statCount = 0;
  
  public BaseChecker() {
  }

  public BaseChecker(CheckProvider provider, CheckType checkType) {
    this.provider = provider;
    this.clusterStatusList = new HashMap<String, Map<String, NodeStatus>>();
    this.checkType = checkType;
  }
  
  public void setInterval(int interval) {
    this.interval = interval;
  }
  
  public void startup() {
    done = false;
    super.start();
  }

  public void shutdown() {
    done = true;
    try {
      this.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void run() {
    while (!done) {
      runOnce();
    }
  }
  
  private void runOnce() { 
    long startAt = System.currentTimeMillis();
    Map<String, Cluster> clusters = provider.getCheckClusters(checkType);
    if (clusters.size() > 0) {
      startCheck(clusters);
      runOnce(clusters);
    }
    long cost = System.currentTimeMillis() - startAt;
    statChkTotalCost += cost;
    long left = interval - cost;
    if (left > 0) {
      try {
        Thread.sleep(left);
      } catch (InterruptedException e) {
      }
    } 
    startAt = System.currentTimeMillis();
    flushAllCheckResults();
    long flushCost = System.currentTimeMillis() - startAt;
    statFlushTotalCost += flushCost;
    if (flushCost + cost > interval) {
      logger.warn("checker {} is slow {}(ms) > {}(ms)", toString(), flushCost + cost, interval);
    }
    if (++statCount >= STAT_INTERVAL) {
      logger.info("checker {} stat count {} avg check cost(ms) {}, avg flush cost(ms) {}", 
          toString(), statCount, statChkTotalCost * 1.0 / statCount, statFlushTotalCost * 1.0 / statCount);
      statFlushTotalCost = 0;
      statChkTotalCost = 0;
      statCount = 0;
    }
  }
  
  protected void startCheck(Map<String, Cluster> clusters) {
    clusterStatusList.clear();
    for (Cluster c : clusters.values()) {
      Map<String, NodeStatus> statusList = new HashMap<String, NodeStatus>();
      for (Node n : c.getNodes()) {
        if (n.getOnline() == OnlineStatus.ONLINE) {
          statusList.put(n.getSpec(), NodeStatus.TIMEOUT);
        }
      }
      clusterStatusList.put(c.getName(), statusList);
    }
  }
  
  protected void flushAllCheckResults() {
    for (Map.Entry<String, Map<String, NodeStatus>> entry : clusterStatusList.entrySet()) {
      provider.flushCheckResults(entry.getKey(), entry.getValue());
    }
    clusterStatusList.clear();
  }
  
  protected void setCheckResult(String clusterName, String nodeSpec, NodeStatus status) {
    Map<String, NodeStatus> statusList = clusterStatusList.get(clusterName);
    statusList.put(nodeSpec, status);
  }
  
  abstract protected void runOnce(Map<String, Cluster> clusters);
}
