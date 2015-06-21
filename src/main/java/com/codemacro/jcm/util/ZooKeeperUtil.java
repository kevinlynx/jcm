package com.codemacro.jcm.util;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperUtil {

  public static void delete(ZooKeeper zk, String path) {
    try {
      List<String> children = zk.getChildren(path, false);
      for (String child : children) {
        delete(zk, path + "/" + child);
      }
      zk.delete(path, -1);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean touch(ZooKeeper zk, String path) {
    final String SEP = "/";
    String curPath = "";
    for (String sub : path.substring(1).split(SEP)) {
      curPath += SEP + sub;
      try {
        if (null == zk.exists(curPath, false)) {
          zk.create(curPath, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }
}
