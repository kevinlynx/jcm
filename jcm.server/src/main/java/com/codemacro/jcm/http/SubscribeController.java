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
package com.codemacro.jcm.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.SubscribeDef;
import com.codemacro.jcm.model.Common.OnlineStatus;

@Controller
@RequestMapping("/sub")
public class SubscribeController {
  @Autowired
  private ClusterManager clusterManager;
  
  @RequestMapping(value = "", method = RequestMethod.POST)
  public @ResponseBody SubscribeDef.Response subscriber(@RequestBody SubscribeDef.Request request) {
    SubscribeDef.Response resp = new SubscribeDef.Response();
    for (SubscribeDef.RequestUnit unit : request.units) {
      resp.units.add(procReqUnit(unit));
    }
    return resp;
  }
  
  private SubscribeDef.ResponseUnit procReqUnit(SubscribeDef.RequestUnit reqUnit) {
    SubscribeDef.ResponseUnit respUnit = new SubscribeDef.ResponseUnit();
    respUnit.name = reqUnit.name;
    Cluster cluster = clusterManager.find(reqUnit.name);
    if (cluster == null || cluster.getOnline() != OnlineStatus.ONLINE) {
      respUnit.kind = SubscribeDef.RespType.RESP_REMOVE;
    } else if (cluster.getVersion() > reqUnit.version) {
      respUnit.cluster = cluster;
      respUnit.kind = SubscribeDef.RespType.RESP_UPDATE;
    } else {
      respUnit.kind = SubscribeDef.RespType.RESP_STATUS;
      respUnit.invalids = cluster.getInvalidNodes();
    }
    return respUnit;
  }
}
