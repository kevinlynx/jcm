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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codemacro.jcm.model.Cluster;
import com.codemacro.jcm.model.Common.CheckType;
import com.codemacro.jcm.model.Common.NodeStatus;
import com.codemacro.jcm.model.Common.OnlineStatus;
import com.codemacro.jcm.model.Common.ProtoType;
import com.codemacro.jcm.model.Node;

public class TcpChecker extends BaseChecker {
  private static Logger logger = LoggerFactory.getLogger(TcpChecker.class);
  private Selector selector;
  private static class ClusterNode {
    public Cluster cluster;
    public Node node;
    public ClusterNode(Cluster cluster, Node node) {
      this.cluster = cluster;
      this.node = node;
    }
  }
  
  public TcpChecker(CheckProvider provider) {
    super(provider, CheckType.TCP);
  }

  @Override
  protected void runOnce(Map<String, Cluster> clusters) {
    try {
      selector = Selector.open();
      for (Cluster cluster : clusters.values()) {
        checkCluster(cluster);
      } 
      select();
      selector.close();
    } catch (IOException e) {
      logger.warn("tcp health check failed {}", e);
    }
  }

  private void select() throws IOException {
    long timeout = this.interval / 10 * 8;
    waitSelect(timeout);
    for (SelectionKey key : selector.keys()) {
      SocketChannel channel = (SocketChannel)key.channel();
      channel.close();
      key.cancel();
    }
  }
  
  private void waitSelect(long maxTime) throws IOException {
    long timeout = maxTime / 5;
    int count = selector.keys().size();
    long cost = maxTime;
    while (count > 0 && cost > 0) {
      long start = System.currentTimeMillis();
      int readyChannels = selector.select(timeout);
      if (readyChannels == 0) {
        cost -= (System.currentTimeMillis() - start);
        continue;
      }
      logger.debug("select {}", readyChannels);
      Set<SelectionKey> selectedKeys = selector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        if (key.isConnectable()) {
          ClusterNode cn = (ClusterNode) key.attachment();
          SocketChannel channel = (SocketChannel) key.channel();
          try {
            channel.finishConnect();
          } catch (IOException e) {
            setCheckResult(cn.cluster.getName(), cn.node.getSpec(), NodeStatus.TIMEOUT);
          }
          if (channel.isConnected()) {
            logger.debug("tcp connectable {}", channel.getRemoteAddress());
            setCheckResult(cn.cluster.getName(), cn.node.getSpec(), NodeStatus.NORMAL);
          }
          key.cancel();
        }
      }
      cost -= (System.currentTimeMillis() - start);
      count -= readyChannels;
    }
  }
  
  private void checkCluster(Cluster cluster) throws IOException {
    for (Node n : cluster.getNodes()) {
      if (n.getOnline() == OnlineStatus.ONLINE) {
        checkNode(cluster, n);
      }
    }
  }
  
  private void checkNode(Cluster cluster, Node node) throws IOException {
    Node.ServiceProto proto = node.getProto(ProtoType.TCP);
    if (proto == null) {
      return ;
    }
    ClusterNode cn = new ClusterNode(cluster, node);
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);
    channel.connect(new InetSocketAddress(node.getIp(), proto.port));
    channel.register(selector, SelectionKey.OP_CONNECT, cn);
  }
  
}
