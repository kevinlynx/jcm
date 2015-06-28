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
package com.codemacro.jcm.test;

import java.util.Arrays;

import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.sub.Subscriber;
import com.codemacro.jcm.sub.allocator.RRAllocator;

public class SubscriberSample {

  public static void main(String[] args) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
    Subscriber subscriber = new Subscriber(
        Arrays.asList("127.0.0.1:8080"),
        Arrays.asList("hello9", "hello"));
    RRAllocator rr = new RRAllocator();
    subscriber.addListener(rr);
    subscriber.startup();
    for (int i = 0; i < 2; ++i) {
      System.out.println(rr.alloc("hello9", ProtoType.HTTP));
    }
    subscriber.shutdown();
  }
}
