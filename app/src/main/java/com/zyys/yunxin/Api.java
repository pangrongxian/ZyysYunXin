package com.zyys.yunxin;

/**
 * Created by Administrator on 2016/3/15.
 */
public class Api {


    public static String hostName = "zy120.ddns.net:8202";

    /**
     * 测试服(云信)
     */

    public static final String avatarUrl = "http://"+hostName+"/api/im/avatar?accid=";

    public static final String ordersUrl = "http://"+hostName+"/api/drug/orders";

    public static final String loginUrl = "http://zy120.ddns.net:8202/api/drug/login";

    public static final  String phoneUrl = "http://"+hostName+"/api/im/phone?voipAccount=";


    public static final  String orderUrl = "http://"+hostName+"/api/drug/order";

    public static final  String OrderDetailsUrl = "http://"+hostName+"/api/drug/order?id=";


}
