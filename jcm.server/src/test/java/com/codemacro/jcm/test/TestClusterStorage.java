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
package com.codemacro.jcm.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.storage.ClusterStorage;
import com.codemacro.jcm.storage.ZookeeperStorageEngine;
import com.codemacro.jcm.util.ZooKeeperUtil;

public class TestClusterStorage {
  final static String ZKHOST = "127.0.0.1:2181";
  final static String ROOT = "/jcm_ut";
  ZookeeperStorageEngine zkStorage;
  ClusterManager clusterManager;
  ClusterStorage clusterStorage;
  
  @Before
  public void setUp() {
    zkStorage = new ZookeeperStorageEngine();
    clusterManager = new ClusterManager();
    clusterStorage = new ClusterStorage(clusterManager, null);
  
  }
  
  @After
  public void tearDown() throws InterruptedException {
    ZooKeeperUtil.delete(zkStorage.getZooKeeper(), ROOT);
    zkStorage.close();
  } 

  @Test
  public void testCreate() throws IOException, InterruptedException {
    zkStorage.init(ZKHOST, ROOT, 5000);
    zkStorage.addWatcher(clusterStorage);
    zkStorage.open();
    assertEquals(0, clusterManager.getAll().size());
    assertTrue(clusterStorage.updateCluster(createCluster()));
    Thread.sleep(100);
    assertEquals(1, clusterManager.getAll().size());
    zkStorage.close();
    clusterManager.getAll().clear();
    zkStorage.init(ZKHOST, ROOT, 5000);
    zkStorage.addWatcher(clusterStorage);
    zkStorage.open();
    assertEquals(1, clusterManager.getAll().size());
  }
  
  @Test
  public void testSync() throws IOException, InterruptedException {
    zkStorage.init(ZKHOST, ROOT, 5000);
    zkStorage.addWatcher(clusterStorage);
    zkStorage.open();
    clusterStorage.updateCluster(createCluster());
    Thread.sleep(100);
    assertEquals(1, clusterManager.getAll().size());
    
    ClusterManager cMgr2 = new ClusterManager();
    ClusterStorage cStorage2 = new ClusterStorage(cMgr2, null);
    ZookeeperStorageEngine ze2 = new ZookeeperStorageEngine();
    ze2.init(ZKHOST, ROOT, 5000);
    ze2.addWatcher(cStorage2);
    ze2.open();
    assertEquals(1, cMgr2.getAll().size());
   
    // create
    clusterStorage.updateCluster(new Cluster("zk2"));
    Thread.sleep(100); // wait to sync done
    assertTrue(cMgr2.find("zk2") != null);
    
    // remove
    clusterStorage.removeCluster("zk2");
    Thread.sleep(100); // wait to sync done
    assertTrue(!cMgr2.exists("zk2"));

    ze2.close();
  }
  
  private Cluster createCluster() {
    Cluster c = new Cluster("zk");
    return c;
  }
}
