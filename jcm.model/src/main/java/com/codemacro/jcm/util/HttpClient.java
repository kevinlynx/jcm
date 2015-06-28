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
package com.codemacro.jcm.util;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class HttpClient {
  private static Logger logger = LoggerFactory.getLogger(HttpClient.class);

  public static String simpleGet(String url) {
    AsyncHttpClient asyncHttpClient = createClient();
    Future<Response> f = asyncHttpClient.prepareGet(url).execute();
    String resp = getRespString(f);
    asyncHttpClient.close();
    return resp;
  }
  
  public static String postJson(String url, String json) {
    AsyncHttpClient asyncHttpClient = createClient();
    Future<Response> f = asyncHttpClient.preparePost(url).setBody(json)
        .setHeader("Content-Type", "application/json").execute();
    String resp = getRespString(f);
    asyncHttpClient.close();
    return resp;  
  }
  
  private static String getRespString(Future<Response> f) {
    String resp = null;
    try {
      Response r = f.get();
      resp = r.getResponseBody();
    } catch (InterruptedException e) {
      logger.warn("http get response failed: {}", e);
    } catch (ExecutionException e) {
      logger.warn("http get response failed: {}", e);
    } catch (IOException e) {
      logger.warn("http get response failed: {}", e);
    }   
    return resp;
  }

  private static AsyncHttpClient createClient() {
    AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
        .setConnectTimeout(1000)
        .setMaxRequestRetry(1)
        .setRequestTimeout(1000).build();
    return new AsyncHttpClient(config);
  }
}
