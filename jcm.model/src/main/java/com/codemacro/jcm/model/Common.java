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
package com.codemacro.jcm.model;

import java.util.HashMap;
import java.util.Map;

public final class Common {
  public static final Map<ProtoType, String> PROTO_STR = new 
      HashMap<ProtoType, String>();

  static {
    PROTO_STR.put(ProtoType.HTTP, "HTTP");
    PROTO_STR.put(ProtoType.TCP, "TCP");
  }

  public static ProtoType getProtoType(String str) {
    str = str.toUpperCase();
    for (Map.Entry<ProtoType, String> entry : PROTO_STR.entrySet()) {
      if (entry.getValue().equals(str)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public enum ProtoType {
    HTTP, TCP
  };
  
  public enum OnlineStatus {
    INITING, ONLINE, OFFLINE
  };

  public enum NodeStatus {
    INVALID, NORMAL, ABNORMAL, TIMEOUT
  };
  
  public enum CheckType {
    NONE, HTTP, TCP
  }
}
