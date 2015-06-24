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
package com.codemacro.jcm.storage;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperStorageEngine implements Watcher {
  private static Logger logger = LoggerFactory.getLogger(ZookeeperStorageEngine.class);
  private volatile ZooKeeper zk;
  private String root;
  private CountDownLatch countDownLatch;
  private int sessionTimeout;
  private String zkHost;
  private Map<String, ZookeeperPathWatcher> watchers;
  
  public ZookeeperStorageEngine() {
    // StatusStorage depends on ClusterStorage
    this.watchers = Collections.synchronizedMap(
        new LinkedHashMap<String, ZookeeperPathWatcher>());
  }

  public void init(String host, String root, int msTimeout) {
    this.zkHost = host;
    this.root = root;
    this.sessionTimeout = msTimeout;
  }

  public boolean open() throws IOException, InterruptedException {
    this.countDownLatch = new CountDownLatch(1);
    this.zk = new ZooKeeper(zkHost, sessionTimeout, this);
    countDownLatch.await();
    logger.info("connect zookeeper {} success", zkHost);
    return true;
  }

  public synchronized void close() throws InterruptedException {
    this.zk.close();
    logger.info("disconnect to zookeeper");
  }

  public ZooKeeper getZooKeeper() {
    return zk;
  }

  public void addWatcher(ZookeeperPathWatcher watcher) {
    String fullPath = root + "/" + watcher.getPath();
    logger.debug("add path watcher on {}", fullPath);
    watcher.onRegister(this, fullPath);
    watchers.put(fullPath, watcher);
  }

  // in the event thread
  public void process(WatchedEvent event) {
    if (event.getType() == Event.EventType.None) {
      switch (event.getState()) {
      case SyncConnected: // no matter session expired or not, we simply reload all
        for (ZookeeperPathWatcher w : watchers.values()) {
          w.onConnected();
        }
        countDownLatch.countDown();
        break;
      case Disconnected: // zookeeper client will reconnect
        logger.warn("disconnected from zookeeper");
        break;
      // to test session expired, just close the laptop cover, making OS sleeping
      case Expired: // session expired by zookeeper server (after reconnected)
        logger.warn("session expired from zookeeper");
        for (ZookeeperPathWatcher w : watchers.values()) {
          w.onSessionExpired();
        }
        reconnect();
        break;
      default:
        break;
      }
    }
    String path = event.getPath();
    ZookeeperPathWatcher watcher = findWatcher(path);
    if (watcher == null) {
      return ;
    }
    switch (event.getType()) {
    case NodeChildrenChanged:
      watcher.onListChanged();
      break;
    case NodeDataChanged:
      watcher.onChildData(path.substring(1 + path.lastIndexOf('/')));
    default:
      break;
    }
  }
  
  private synchronized void reconnect() {
    try {
      logger.info("reconnect to zookeeper...");
      this.zk.close();
      this.zk = new ZooKeeper(this.zkHost, this.sessionTimeout, this);
    } catch (Exception e) {
      logger.error("reconnect to zookeeper failed {}", e);
    }
  }

  private ZookeeperPathWatcher findWatcher(String fullPath) {
    if (fullPath == null) {
      return null; // not path event
    }
    if (!fullPath.startsWith(this.root)) {
      return null; // root does not match
    }
    if (fullPath.charAt(root.length()) != '/') {
      return null; // root does not match
    }
    int pos = fullPath.indexOf('/', root.length() + 1);
    if (pos < 0) { // only contains sub path
      return watchers.get(fullPath);
    }
    String sub = fullPath.substring(0, pos);
    return watchers.get(sub);
  }
}
