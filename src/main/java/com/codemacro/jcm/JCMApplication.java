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
package com.codemacro.jcm;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.health.HealthCheckManager;
import com.codemacro.jcm.storage.ClusterStorage;
import com.codemacro.jcm.storage.ServerStorage;
import com.codemacro.jcm.storage.StatusStorage;
import com.codemacro.jcm.storage.ZookeeperStorageEngine;

public class JCMApplication {
  private static Logger logger = LoggerFactory.getLogger(JCMApplication.class);
  private ZookeeperStorageEngine zkStorageEngine;
  private ServerStorage serverStorage;
  private ClusterStorage clusterStorage;
  private StatusStorage statusStorage;
  private HealthCheckManager healthCheckManager;

  public JCMApplication(ZookeeperStorageEngine zookeeperStorageEngine, 
      ServerStorage serverStorage, ClusterStorage clusterStorage,
      StatusStorage statusStorage, 
      HealthCheckManager healthCheckManager) {
    this.zkStorageEngine = zookeeperStorageEngine;
    this.serverStorage = serverStorage;
    this.clusterStorage = clusterStorage;
    this.statusStorage = statusStorage;
    this.healthCheckManager = healthCheckManager;
  }
  
  public boolean startup() throws IOException, InterruptedException {
    logger.info("JCM starting...");
    zkStorageEngine.addWatcher(serverStorage);
    zkStorageEngine.addWatcher(clusterStorage);
    zkStorageEngine.addWatcher(statusStorage);
    zkStorageEngine.open("127.0.0.1:2181", 5000);
    healthCheckManager.startup();
    return true;
  }
  
  public void shutdown() throws InterruptedException {
    logger.info("JCMP shutting down...");
    zkStorageEngine.close();
    healthCheckManager.shutdown();
  }
}

