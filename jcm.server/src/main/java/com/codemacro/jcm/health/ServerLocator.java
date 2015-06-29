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
package com.codemacro.jcm.health;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.codemacro.jcm.storage.ServerStorage;
import com.codemacro.jcm.util.Hash;

public class ServerLocator {
  private List<String> serverList;
  private String selfId;
  
  public void serversChanged(String selfId, List<String> ids) {
    this.selfId = selfId;
    this.serverList = ids;
    Collections.sort(this.serverList, new Comparator<String>() {
      public int compare(String arg0, String arg1) {
        return parseId(arg0).compareTo(parseId(arg1));
      }
    });
  }
  
  public boolean isResponsible(String clusterName) {
    if (serverList == null) {
      return false;
    }
    long hash = Hash.murhash(clusterName);
    int id = Hash.consistentHash(hash, serverList.size());
    String sid = serverList.get(id);
    return sid.equals(selfId);
  }
  
  private Integer parseId(String str) {
    return Integer.parseInt(str.substring(ServerStorage.JCM_PREFIX.length()));
  }
}
