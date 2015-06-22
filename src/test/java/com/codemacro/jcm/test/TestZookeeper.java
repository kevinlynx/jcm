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
    ServerStorage ze1 = new ServerStorage(null, spec1);
    zk1.addWatcher(ze1);
    zk1.open(ZKHOST, 5000);
    assertEquals(spec1, ze1.electLeader());

    String spec2 = "127.0.0.1|8889|9999";
    ZookeeperStorageEngine zk2 = new ZookeeperStorageEngine(ROOT);
    ServerStorage ze2 = new ServerStorage(null, spec2);
    zk2.addWatcher(ze2);
    zk2.open(ZKHOST, 5000);
    assertEquals(spec1, ze1.electLeader());
    assertEquals(spec1, ze2.electLeader());

    zk1.close();
    assertEquals(spec2, ze2.electLeader());

    zk2.close();
  }
}
