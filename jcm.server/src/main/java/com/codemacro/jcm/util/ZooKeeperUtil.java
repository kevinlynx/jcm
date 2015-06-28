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
