package com.codemacro.jcm.util;

import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperLeaderElector {
  public static byte[] elect(ZooKeeper zk, String path, final String prefix) 
      throws KeeperException, InterruptedException {
    List<String> children = zk.getChildren(path, false);
    if (children.size() == 0) {
      return null;
    }
    int min = -1;
    String minPath = null;
    for (String child : children) {
      String idStr = child.substring(prefix.length());
      int id = Integer.parseInt(idStr);
      if (min < 0 || id < min) {
        min = id;
        minPath = child;
      }
    }
    String fullPath = path + "/" + minPath;
    return zk.getData(fullPath, false, null);
  }
}
