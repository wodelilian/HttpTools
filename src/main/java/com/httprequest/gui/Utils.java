package com.httprequest.gui;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * 工具类，包含项目中通用的方法
 */
public class Utils {
    
    /**
     * 创建一个信任所有证书的SSL上下文
     */
    public static SSLContext createAllTrustingSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
        
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }
    
    /**
     * 根据协议和端口是否为默认值来决定是否在Host中包含端口号
     */
    public static String getHostWithPort(URL url) {
        int port = url.getPort();
        String protocol = url.getProtocol().toLowerCase();
        
        // 如果URL没有显式指定端口（返回-1），则使用默认端口
        if (port == -1) {
            port = "https".equals(protocol) ? 443 : 80;
        }
        
        // 只有当端口不是默认端口时才添加端口号
        if (("http".equals(protocol) && port != 80) || 
            ("https".equals(protocol) && port != 443)) {
            return url.getHost() + ":" + port;
        }
        
        // 默认端口不需要显示
        return url.getHost();
    }
    
    /**
     * 创建接受所有主机名的主机名校验器
     */
    public static HostnameVerifier createAllTrustingHostnameVerifier() {
        return new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }
}