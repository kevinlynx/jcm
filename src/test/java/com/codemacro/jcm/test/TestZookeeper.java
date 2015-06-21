package com.codemacro.jcm.test;

import java.io.IOException;
import org.apache.zookeeper.KeeperException;
import com.codemacro.jcm.storage.ServerStorage;
import com.codemacro.jcm.storage.ZookeeperStorageEngine;

import junit.framework.TestCase;

// start zookeeper first
public class TestZookeeper extends TestCase {

  public void testElect() throws IOException, InterruptedException, KeeperException {
    final String ROOT = "/jcm";
    final String ZKHOST = "127.0.0.1:2181";
    ZookeeperStorageEngine zk1 = new ZookeeperStorageEngine(ROOT);
    String spec1 = "127.0.0.1|8888|9998";
    ServerStorage ze1 = new ServerStorage(spec1);
    zk1.addWatcher(ze1);
    zk1.open(ZKHOST, 5000);
    assertEquals(spec1, ze1.electLeader());

    String spec2 = "127.0.0.1|8889|9999";
    ZookeeperStorageEngine zk2 = new ZookeeperStorageEngine(ROOT);
    ServerStorage ze2 = new ServerStorage(spec2);
    zk2.addWatcher(ze2);
    zk2.open(ZKHOST, 5000);
    assertEquals(spec1, ze1.electLeader());
    assertEquals(spec1, ze2.electLeader());

    zk1.close();
    assertEquals(spec2, ze2.electLeader());

    zk2.close();
  }
}
