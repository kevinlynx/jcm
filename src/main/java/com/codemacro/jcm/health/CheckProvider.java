package com.codemacro.jcm.health;

import java.util.Map;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;

// to make writing unit tests easier
public interface CheckProvider {
  Map<String, Cluster> getCheckClusters(final CheckType checkType);
  void flushCheckResults(String clusterName, Map<String, NodeStatus> statusList);
}
