package com.httprequest.gui.POC;

import com.httprequest.gui.Utils;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CrocusRepairRecordUpload {
    
    // 发送Crocus系统RepairRecord.do文件上传请求并返回响应
    public static String sendFileUploadRequest(String baseUrl, String base64EncodedImage) throws Exception {
        // 更新请求URL
        String requestUrl = baseUrl + "/RepairRecord.do?Action=imageupload";
        String requestMethod = "POST";
        
        URL url = new URL(requestUrl);
        HttpURLConnection connection;
        
        // 如果是HTTPS连接，设置信任所有证书的SSL上下文
        if ("https".equals(url.getProtocol().toLowerCase())) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
            httpsConnection.setSSLSocketFactory(Utils.createAllTrustingSSLContext().getSocketFactory());
            httpsConnection.setHostnameVerifier(Utils.createAllTrustingHostnameVerifier());
            connection = httpsConnection;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        
        // 更新请求头
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Host", Utils.getHostWithPort(url));
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // 设置JSON请求体，使用传入的base64EncodedImage
        String jsonRequestBody = "{\n" +
                                "  \"username\": \"streamax20020818\",\n" +
                                "  \"license\": \"1\",\n" +
                                "  \"chnnel\": 1,\n" +
                                "  \"type\": 1,\n" +
                                "  \"imsage\": \"" + base64EncodedImage + "\",\n" +
                                "  \"picturename\": \"a.jsp\"\n" +
                                "}";
        
        try (java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream())) {
            dos.writeBytes(jsonRequestBody);
            dos.flush();
        }
        
        // 设置连接和读取超时为20秒
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);

        // 构建请求详情
        StringBuilder requestDetails = new StringBuilder();
        // 添加原始请求头
        requestDetails.append("原始请求头:\n");
        requestDetails.append("POST " + url.getFile() + " HTTP/1.1\n");
        requestDetails.append("Host: " + Utils.getHostWithPort(url) + "\n");
        requestDetails.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0\n");
        requestDetails.append("Content-Type: application/json\n");
        requestDetails.append("Accept-Encoding: gzip, deflate\n");
        requestDetails.append("Accept: */*\n");
        requestDetails.append("Connection: close\n\n");
        
        // 添加请求体信息
        requestDetails.append("请求体:\n" + jsonRequestBody + "\n");

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