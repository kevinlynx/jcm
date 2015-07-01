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
package com.codemacro.jcm.storage;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterWatchStat {
  private static Logger logger = LoggerFactory.getLogger(ClusterWatchStat.class);
  private static class Stat {
    long last = 0;
    long rtSum = 0;
    long count = 0;
    int err = 0;
  }
  private Map<String, Stat> clusterStats;
  private String statName;
  private int interval;
  
  public ClusterWatchStat(String statName) {
    clusterStats = new HashMap<String, Stat>();
    this.statName = statName;
    this.interval = Integer.parseInt(System.getProperty("jcm.stat.watch", "300"));
    logger.info("watch stat interval {}", interval);
  }
  
  public synchronized void begin(String name) {
    Stat stat = clusterStats.get(name);
    if (stat == null) {
      stat = new Stat();
      clusterStats.put(name, stat);
    }
    if (stat.last > 0) {
      stat.err ++;
    }
    stat.last = System.currentTimeMillis();
  }
  
  public synchronized void end(String name) {
    Stat stat = clusterStats.get(name);
    if (stat == null) {
      return ;
    }
    // if this time match wrong, RT is very small, that's ok
    stat.rtSum += (System.currentTimeMillis() - stat.last);
    stat.last = 0;
    if (++stat.count >= interval) {
      int total = 0, err = 0;
      long rtSum = 0;
      for (Stat s : clusterStats.values()) {
        total += s.count;
        err += s.err;
        rtSum += s.rtSum;
      }
      logger.info(String.format("watch stat [%s] total %d, err %d, rt_avg(ms) %.2f",
          statName, total, err, rtSum * 1.0 / total));
      clusterStats.clear();
    }
  }
}
