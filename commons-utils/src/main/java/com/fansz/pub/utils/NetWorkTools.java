package com.fansz.pub.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

public final class NetWorkTools {
    private final static Logger logger = LoggerFactory.getLogger(NetWorkTools.class);

    private final static String DEFAULT_IP_ADDR = "127.0.0.1";

    /**
     * 获取本机IP地址,如果获取失败,返回null;
     *
     * @return
     */
    public static String getLocalIpAddress() {
        String ip = null;
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                System.out.println(netInterface.getName());
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ipAddr = (InetAddress) addresses.nextElement();
                    if (ipAddr != null && ipAddr instanceof Inet4Address) {
                        if (!DEFAULT_IP_ADDR.equals(ipAddr.getHostAddress())) {
                            ip = ipAddr.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("获取本机IP地址错误", e);
        }
        return ip;
    }
}
