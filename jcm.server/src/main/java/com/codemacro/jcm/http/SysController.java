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
import org.springframework.web.bind.annotation.ResponseBody;

import com.codemacro.jcm.health.HealthCheckManager;
import com.codemacro.jcm.storage.ServerStorage;
import com.codemacro.jcm.storage.StatusStorage;

@Controller
@RequestMapping("/sys")
public class SysController {
  @Autowired
  private HealthCheckManager healthCheckManager;
  @Autowired
  private ServerStorage serverStorage;
  @Autowired
  private StatusStorage statusStorage;

  @RequestMapping(value = "/status")
  public @ResponseBody Result getStatus() {
    SysStatus status = new SysStatus();
    status.checkClusters = healthCheckManager.getCheckClusterList();
    status.leader = serverStorage.getLeaderSpec();
    status.statusCache = statusStorage.isEnableCache();
    return new Result(status);
  }

  @RequestMapping(value = "/nscache")
  public @ResponseBody Result enableStatusCache(@RequestBody boolean enable) {
    statusStorage.setEnableCache(enable);
    return Result.OK;
  }
}
