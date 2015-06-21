package com.codemacro.jcm.model;

public final class Common {
  public static final int HTTP = 0;
  public static final int TCP = 1;
  public static final String[] PROTO_STR = {"http", "tcp"};
  
  public enum OnlineStatus { INITING, ONLINE, OFFLINE };
  public enum NodeStatus { INVALID, NORMAL, ABNORMAL };
}
