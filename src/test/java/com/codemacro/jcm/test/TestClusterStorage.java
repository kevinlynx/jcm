package com.codemacro.jcm.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.storage.ClusterStorage;
import com.codemacro.jcm.storage.ZookeeperStorageEngine;
import com.codemacro.jcm.util.ZooKeeperUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:jcm-core.xml")
public class TestClusterStorage {
  final static String ZKHOST = "127.0.0.1:2181";
  final static String ROOT = "/jcm";
  @Autowired
  ClusterStorage clusterStorage;
  @Autowired
  ZookeeperStorageEngine zkStorage;
  @Autowired
  ClusterManager clusterManager;
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() throws InterruptedException {
    ZooKeeperUtil.delete(zkStorage.getZooKeeper(), ROOT);
    zkStorage.close();
  }

  @Test
  public void testCreate() throws IOException, InterruptedException {
    zkStorage.addWatcher(clusterStorage);
    zkStorage.open(ZKHOST, 5000);
    assertEquals(0, clusterManager.getAll().size());
    clusterStorage.updateCluster(createCluster());
    assertEquals(1, clusterManager.getAll().size());
    zkStorage.close();
    clusterManager.getAll().clear();
    zkStorage.open(ZKHOST, 5000);
    assertEquals(1, clusterManager.getAll().size());
  }
  
  @Test
  public void testSync() throws IOException, InterruptedException {
    zkStorage.addWatcher(clusterStorage);
    zkStorage.open(ZKHOST, 5000);
    clusterStorage.updateCluster(createCluster());
    assertEquals(1, clusterManager.getAll().size());
    
    ClusterManager cMgr2 = new ClusterManager();
    ClusterStorage cStorage2 = new ClusterStorage(cMgr2);
    ZookeeperStorageEngine ze2 = new ZookeeperStorageEngine(ROOT);
    ze2.addWatcher(cStorage2);
    ze2.open(ZKHOST, 5000);
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
