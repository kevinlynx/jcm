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

import com.codemacro.jcm.util.Hash;

public class ServerLocator {
  private String serverSpec;
  private long serverHash;
  private int serverId = 0;
  private int bucketCnt = 0;
  
  public void serversChanged(String spec, int total) {
    serverSpec = spec;
    bucketCnt = total;
    serverHash = Hash.murhash(serverSpec);
    serverId = Hash.consistentHash(serverHash, total);
  }
  
  public boolean isResponsible(String clusterName) {
    long hash = Hash.murhash(clusterName);
    return serverId == Hash.consistentHash(hash, bucketCnt);
  }
}
