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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.model.Node;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class HttpChecker extends BaseChecker {
  private static Logger logger = LoggerFactory.getLogger(HttpChecker.class);
  private AsyncHttpClient http;
  
  public HttpChecker() {
  }

  public HttpChecker(CheckProvider provider) {
    super(provider, CheckType.HTTP);
  }

  @Override
  public void startup() {
    createHttp();
    super.startup();
  }
  
  @Override
  protected void runOnce(Map<String, Cluster> clusters) {
    for (Cluster cluster : clusters.values()) {
      checkCluster(cluster);
    }
  }
  
  private void createHttp() {
    int timeout = this.interval / 10 * 8;
    if (timeout == 0) {
      throw new IllegalArgumentException("interval is too small");
    }
    // TODO: because one whole http request may cost much more time than `interval',
    // there're more than one connections on a node
    // right now, we just set these nodes timeout
    AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
        .setConnectTimeout(timeout / 2)
        .setMaxRequestRetry(1)
        .setRequestTimeout(timeout).build();
    this.http = new AsyncHttpClient(config);
    logger.info("initialize http client with timeout {}", timeout);
  }

  private void checkCluster(Cluster cluster) {
    for (Node n : cluster.getNodes()) {
      if (n.getOnline() == OnlineStatus.ONLINE) {
        checkNode(cluster, n);
      }
    }
  }
  
  private void checkNode(final Cluster cluster, final Node node) {
    String file = cluster.getCheckFile();
    String url = getNodeCheckURL(file == null ? "" : file, node);
    http.prepareGet(url).execute(new AsyncCompletionHandler<Integer>() {
      @Override
      public Integer onCompleted(Response response) throws Exception {
        int code = response.getStatusCode();
        if (logger.isTraceEnabled()) {
          logger.trace("http completed {} {}", node, code);
        }
        setCheckResult(cluster.getName(), node.getSpec(), code == 200 ? 
            NodeStatus.NORMAL : NodeStatus.ABNORMAL);
        return code;
      }
      
      @Override
      public void onThrowable(Throwable t) {
        if (logger.isTraceEnabled()) {
          logger.trace("http error {} {}", node, t.getMessage());
        }
        setCheckResult(cluster.getName(), node.getSpec(), NodeStatus.TIMEOUT);
      }
    });
  }

  private String getNodeCheckURL(String file, Node node) {
    Node.ServiceProto proto = node.getProto(ProtoType.HTTP);
    if (proto == null) {
      logger.warn("not found http proto for node {}", node);
      return null;
    }
    return String.format("http://%s:%d/%s", node.getIp(), proto.port, file);
  }
}
