package com.codemacro.jcm.storage;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codemacro.jcm.util.ZookeeperLeaderElector;

public class ServerStorage extends ZookeeperPathWatcher {
  private static Logger logger = LoggerFactory.getLogger(ServerStorage.class);
  private static final String JCM_PREFIX = "jcm_server";
  private String serverSpec;
  private String leaderSpec;

  public ServerStorage(String serverSpec) {
    this.serverSpec = serverSpec;
  }

  public String electLeader() {
    byte[] spec = null;
    try {
      spec = ZookeeperLeaderElector.elect(zkStorage.getZooKeeper(), fullPath, JCM_PREFIX);
    } catch (Exception e) {
      logger.error("elect leader faild {}", e);
      return null;
    }
    return new String(spec);
  }
  
  public boolean isLeader() {
    return leaderSpec.equals(serverSpec);
  }

  @Override
  void onConnected() {
    touch(fullPath);
    String path = fullPath + "/" + JCM_PREFIX;
    try {
      path = zkStorage.getZooKeeper().create(path, serverSpec.getBytes(), Ids.OPEN_ACL_UNSAFE, 
        CreateMode.EPHEMERAL_SEQUENTIAL);
      getChildren(); // watch 
      logger.info("register server {} on zookeeper {} success", serverSpec, path);
    } catch (Exception e) {
      logger.error("register server on zookeeper failed {}", e);
    }
  }

  @Override
  void onDisconnected() { // TODO: handle reconnect stuff
    logger.info("disconnect from zookeeper");
  }

  @Override
  String getPath() {
    return "election";
  }

  @Override
  void onListChanged() {
    logger.info("server list changed");
    // TODO: notify to health checking module
  }
}
