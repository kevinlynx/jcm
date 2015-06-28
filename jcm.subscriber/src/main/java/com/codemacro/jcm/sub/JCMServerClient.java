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
package com.codemacro.jcm.sub;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.SubscribeDef.Request;
import com.codemacro.jcm.model.SubscribeDef.Response;
import com.codemacro.jcm.util.JsonUtil;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class JCMServerClient {
  private static Logger logger = LoggerFactory.getLogger(JCMServerClient.class);
  private List<String> hosts;
  private int pos;
  private AsyncHttpClient http;
  
  public JCMServerClient(List<String> hosts) {
    this.hosts = hosts;
    this.pos = 0;
    AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
        .setConnectTimeout(1000)
        .setMaxRequestRetry(1)
        .setRequestTimeout(1000).build();
    this.http = new AsyncHttpClient(config);
  }
  
  public void close() {
    this.http.close();
  }

  public Response send(Request req) {
    String url = getURL();
    Future<com.ning.http.client.Response> f = http.preparePost(url).setBody(JsonUtil.toString(req))
        .setHeader("Content-Type", "application/json").execute();
    try {
      String json = f.get().getResponseBody();
      return JsonUtil.fromString(json, Response.class);
    } catch (IOException e) {
      logger.warn("get JCM resp failed {}", e);
    } catch (InterruptedException e) {
      logger.warn("get JCM resp failed {}", e);
    } catch (ExecutionException e) {
      logger.warn("get JCM resp failed {}", e);
    }
    return null;
  }

  private String getURL() {
    this.pos = (this.pos + 1) % hosts.size();
    return "http://" + hosts.get(pos) + "/sub";
  }
}
