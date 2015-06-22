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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.storage.ClusterStorage;

@Controller
@RequestMapping("/c")
public class ClusterController {
  private static Logger logger = LoggerFactory.getLogger(ClusterController.class);
  @Autowired
  private ClusterStorage clusterStorage;
  @Autowired
  private ClusterManager clusterManager;
  
  @RequestMapping(value = "/new")
  public @ResponseBody Result create(@RequestBody Cluster cluster) {
    logger.debug("create cluster {}", cluster);
    if (!cluster.isValid()) {
      return new Result(-1, "invalid cluster");
    }
    boolean ret = clusterStorage.updateCluster(cluster);
    return ret ? Result.OK : Result.FAILED;
  }
  
  @RequestMapping(value = "/del/{name}")
  public @ResponseBody Result delete(@PathVariable String name) {
    logger.debug("delete cluster {}", name);
    boolean ret = clusterStorage.removeCluster(name);
    return ret ? Result.OK : Result.FAILED;
  }
  
  @RequestMapping(value = "/get/{name}")
  public @ResponseBody Result get(@PathVariable String name) {
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return new Result(-1, "not found");
    }
    return new Result(c);
  }

  @RequestMapping(value = "/list")
  public @ResponseBody Result list() {
    Map<String, Cluster> all = clusterManager.getAll();
    return new Result(all.values());
  }
}
