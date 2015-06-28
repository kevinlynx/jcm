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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codemacro.jcm.storage.ServerStorage;
import com.codemacro.jcm.util.HttpClient;
import com.codemacro.jcm.util.JsonUtil;

@Component
public class RequestRedirector {
  private static Logger logger = LoggerFactory.getLogger(RequestRedirector.class);

  @Autowired
  private ServerStorage serverStorage;
  
  public Result redirect(String uri, Object arg) {
    String url = formatURL(uri);
    logger.debug("redirect request {}", uri);
    String resp = HttpClient.postJson(url, JsonUtil.toString(arg));
    if (resp == null) {
      return new Result(-2, "redirect failed");
    }
    try {
      return JsonUtil.fromString(resp, Result.class);
    } catch (IOException e) {
      e.printStackTrace();
      return new Result(-1, "format redirect resp failed");
    }
  }
  
  public boolean shouldRedirect() {
    return !serverStorage.isLeader();
  }
  
  private String formatURL(String uri) {
    String leaderSpec = serverStorage.getLeaderSpec();
    String[] secs = leaderSpec.split("\\|");
    return String.format("http://%s:%s", secs[0], secs[1]) + uri;
  }
}
