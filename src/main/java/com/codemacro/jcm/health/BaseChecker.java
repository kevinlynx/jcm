package com.codemacro.jcm.health;

import java.util.HashMap;
import java.util.Map;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Node;

public abstract class BaseChecker extends Thread {
  private volatile boolean done = false;
  protected int interval = 0; // milliseconds
  protected CheckProvider provider;
  private Map<String, Map<String, NodeStatus>> clusterStatusList;
  private CheckType checkType;
  
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
    long left = interval - (System.currentTimeMillis() - startAt);
    if (left > 0) {
      try {
        Thread.sleep(left);
      } catch (InterruptedException e) {
      }
    }
    flushAllCheckResults();
  }
  
  protected void startCheck(Map<String, Cluster> clusters) {
    clusterStatusList.clear();
    for (Cluster c : clusters.values()) {
      Map<String, NodeStatus> statusList = new HashMap<String, NodeStatus>();
      for (Node n : c.getNodes()) {
        statusList.put(n.getSpec(), NodeStatus.TIMEOUT);
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
