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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.ClusterManager;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Node;
import com.codemacro.jcm.storage.ClusterStorage;

@Controller
@RequestMapping("/c")
public class ClusterController {
  private static Logger logger = LoggerFactory.getLogger(ClusterController.class);
  @Autowired
  private HttpServletRequest request;
  @Autowired
  private ClusterStorage clusterStorage;
  @Autowired
  private ClusterManager clusterManager;
  @Autowired
  private RequestRedirector redirector;
  
  @RequestMapping(value = "", method = RequestMethod.POST)
  public @ResponseBody Result create(@RequestBody Cluster cluster) {
    logger.debug("create cluster {}", cluster);
    if (!cluster.isValid()) {
      return Result.INVALID_ARG;
    }
    if (redirector.shouldRedirect()) {
      return redirector.redirect(request.getRequestURI(), cluster);
    }
    boolean ret = clusterStorage.updateCluster(cluster);
    return ret ? Result.OK : Result.FAILED;
  }
  
  @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
  public @ResponseBody Result delete(@PathVariable String name) {
    logger.debug("delete cluster {}", name);
    if (!clusterManager.exists(name)) {
      return Result.NOT_FOUND;
    }
    boolean ret = clusterStorage.removeCluster(name);
    return ret ? Result.OK : Result.FAILED;
  }
  
  @RequestMapping(value = "/{name}", method = RequestMethod.GET)
  public @ResponseBody Result get(@PathVariable String name) {
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return Result.NOT_FOUND;
    }
    return new Result(c);
  }

  @RequestMapping(value = "", method = RequestMethod.GET)
  public @ResponseBody Result getAll() {
    Map<String, Cluster> all = clusterManager.getAll();
    return new Result(all.values());
  }

  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public @ResponseBody Result list() {
    Map<String, Cluster> all = clusterManager.getAll();
    return new Result(all.keySet());
  }
  
  @RequestMapping(value = "/online/{name}", method = RequestMethod.POST)
  public @ResponseBody Result online(@PathVariable String name, @RequestBody boolean online) {
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return Result.NOT_FOUND;
    }
    if (redirector.shouldRedirect()) {
      return redirector.redirect(request.getRequestURI(), online);
    }
    c.setOnline(online ? OnlineStatus.ONLINE : OnlineStatus.OFFLINE);
    return clusterStorage.updateCluster(c) ? Result.OK : Result.FAILED;
  }
  
  @RequestMapping(value = "/node/{name}", method = RequestMethod.POST)
  public @ResponseBody Result addNodes(@PathVariable String name, 
      @RequestBody List<Node> nodes) {
    if (nodes.size() == 0) {
      return Result.INVALID_ARG;
    }
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return Result.NOT_FOUND;
    }
    if (redirector.shouldRedirect()) {
      return redirector.redirect(request.getRequestURI(), nodes);
    }
    for (Node n : nodes) {
      c.addNode(n);
    }
    return clusterStorage.updateCluster(c) ? Result.OK : Result.FAILED;
  }

  @RequestMapping(value = "/node/{name}", method = RequestMethod.DELETE)
  public @ResponseBody Result delNodes(@PathVariable String name, 
      @RequestBody List<String> nodes) {
    if (nodes.size() == 0) {
      return Result.INVALID_ARG;
    }
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return Result.NOT_FOUND;
    }
    if (redirector.shouldRedirect()) {
      return redirector.redirect(request.getRequestURI(), nodes);
    }
    for (String n : nodes) {
      c.removeNode(n);
    }
    return clusterStorage.updateCluster(c) ? Result.OK : Result.FAILED;
  }

  public static class SetNodeOnlineArg {
    public String spec;
    public boolean online;
  }

  @RequestMapping(value = "/node/online/{name}", method = RequestMethod.POST)
  public @ResponseBody Result onlineNode(@PathVariable String name, 
      @RequestBody SetNodeOnlineArg arg) {
    Cluster c = clusterManager.find(name);
    if (c == null) {
      return Result.NOT_FOUND;
    }
    if (redirector.shouldRedirect()) {
      return redirector.redirect(request.getRequestURI(), arg);
    }
    Node n = c.findNode(arg.spec);
    if (n == null) {
      return Result.NOT_FOUND;
    }
    n.setOnline(arg.online ? OnlineStatus.ONLINE : OnlineStatus.OFFLINE);
    if (!arg.online) {
      n.setStatus(NodeStatus.INVALID);
    }
    return clusterStorage.updateCluster(c) ? Result.OK : Result.FAILED;
  }
}
