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
package com.codemacro.jcm.sub;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.SubscribeDef.Request;
import com.codemacro.jcm.model.SubscribeDef.RequestUnit;
import com.codemacro.jcm.model.SubscribeDef.Response;
import com.codemacro.jcm.model.SubscribeDef.ResponseUnit;

public class Subscriber extends Thread {
  public interface Listener {
    void onInit(ClusterManager clusterManager);
    void onClusterUpdate(Cluster cluster);
    void onClusterRemove(String name);
  }
  
  private static Logger logger = LoggerFactory.getLogger(Subscriber.class);
  private static final String TAG = "java client v0.1.0";
  private ClusterManager clusterManager = new ClusterManager();
  private List<Listener> listeners = new LinkedList<Listener>();
  private Map<String, RequestUnit> requests = new HashMap<String, RequestUnit>();
  private JCMServerClient client;
  private volatile boolean stop = false;

  public Subscriber(List<String> hosts, List<String> names) {
    if (names.size() == 0) {
      throw new IllegalArgumentException("subscribe no cluster");
    }
    if (hosts.size() == 0) {
      throw new IllegalArgumentException("subscribe no hosts");
    }
    for (String name : names) {
      RequestUnit unit = new RequestUnit(name, 0);
      requests.put(name, unit);
    }
    client = new JCMServerClient(hosts);
  }

  public void addListener(Listener listener) {
    this.listeners.add(listener);
  }

  public boolean startup() {
    stop = false;
    logger.info("subscriber starts");
    for (Listener listener : this.listeners) {
      listener.onInit(clusterManager);
    }
    boolean ret = doSubscribe();
    super.start();
    return ret;
  }
  
  public void shutdown() {
    stop = true;
    try {
      this.join();
    } catch (InterruptedException e) {
    }
    this.client.close();
    logger.info("subscriber shutdown");
  }

  @Override
  public void run() {
    logger.debug("subscriber thread starts");
    while (!stop) {
      doSubscribe();
      if (stop) break;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
    logger.debug("subscriber thread exits");
  }
  
  private boolean doSubscribe() {
    Request req = new Request(TAG, requests.values());
    Response resp = client.send(req);
    if (resp == null) {
      return false;
    }
    logger.debug("process subscribe response [{}]", resp.units.size());
    for (ResponseUnit unit : resp.units) {
      procResp(unit);
    }
    return true;
  }
  
  private void procResp(ResponseUnit unit) {
    switch (unit.kind) {
    case RESP_REMOVE:
      removeCluster(unit.name);
      break;
    case RESP_UPDATE:
      updateCluster(unit.cluster);
      break;
    case RESP_STATUS:
      updateStatus(unit.name, unit.invalids);
      break;
    }
  }
  
  private void removeCluster(String name) {
    logger.debug("receive remove cluster: {}", name);
    this.requests.remove(name);
    this.clusterManager.remove(name);
    for (Listener listener : this.listeners) {
      listener.onClusterRemove(name);
    }
  }

  private void updateCluster(Cluster cluster) {
    logger.debug("receive update cluster: {}", cluster.getName());
    this.clusterManager.update(cluster);
    RequestUnit unit = this.requests.get(cluster.getName());
    unit.version = cluster.getVersion();
    for (Listener listener : this.listeners) {
      listener.onClusterUpdate(cluster);
    }
  }

  private void updateStatus(String name, List<String> invalids) {
    logger.debug("receive update status: {}", name);
    Cluster cluster = clusterManager.find(name);
    if (cluster == null) {
      logger.warn("not found cluster: {}", name);
      return ;
    } 
    for (String spec : invalids) {
      cluster.setNodeStatus(spec, NodeStatus.INVALID);
    }
  } 
}
