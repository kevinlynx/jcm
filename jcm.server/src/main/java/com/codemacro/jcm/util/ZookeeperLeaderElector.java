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
