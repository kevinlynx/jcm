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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Hash {
  // from Guava
  public static int consistentHash(long input, int buckets) {
    long h = input;
    int candidate = 0;
    int next;

    // Jump from bucket to bucket until we go out of range
    while (true) {
      // See http://en.wikipedia.org/wiki/Linear_congruential_generator
      // These values for a and m come from the C++ version of this function.
      h = 2862933555777941757L * h + 1;
      double inv = 0x1.0p31 / ((int) (h >>> 33) + 1);
      next = (int) ((candidate + 1) * inv);

      if (next >= 0 && next < buckets) {
        candidate = next;
      } else {
        return candidate;
      }
    }
  }
  
  public static Long murhash(String key) {
    ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
    int seed = 0x1234ABCD;

    ByteOrder byteOrder = buf.order();
    buf.order(ByteOrder.LITTLE_ENDIAN);

    long m = 0xc6a4a7935bd1e995L;
    int r = 47;

    long h = seed ^ (buf.remaining() * m);

    long k;
    while (buf.remaining() >= 8) {
      k = buf.getLong();

      k *= m;
      k ^= k >>> r;
      k *= m;

      h ^= k;
      h *= m;
    }

    if (buf.remaining() > 0) {
      ByteBuffer finish = ByteBuffer.allocate(8).order(
          ByteOrder.LITTLE_ENDIAN);
      // for big-endian version, do this first:
      // finish.position(8-buf.remaining());
      finish.put(buf).rewind();
      h ^= finish.getLong();
      h *= m;
    }

    h ^= h >>> r;
    h *= m;
    h ^= h >>> r;

    buf.order(byteOrder);
    return h;
  }
}
