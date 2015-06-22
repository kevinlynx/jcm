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

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
  public static final Result OK = new Result();
  public static final Result FAILED = new Result(-1, "failed");
  private int code;
  private String msg;
  private Object data;
  
  public Result(Object data) {
    this();
    this.data = data;
  }

  public Result(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }
  
  public Result() {
    this.code = 0;
    this.msg = "";
  }

  public int getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
  
  public Object getData() {
    return data;
  }
}
