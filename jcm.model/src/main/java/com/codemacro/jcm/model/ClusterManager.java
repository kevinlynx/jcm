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
