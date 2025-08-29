package com.httprequest.gui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

public class POCTestPanel extends JPanel {
    private JComboBox<String> pocTypeComboBox;
    private JTextArea responseArea;
    private JTextField commandField;
    private JLabel commandLabel;
    private JTextField threadCountField; // 线程数输入框
    private JLabel threadCountLabel; // 线程数标签
    private Map<String, String> pocTypeMap;
    private volatile boolean stopRequested = false;

    public POCTestPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 初始化POC类型映射
        pocTypeMap = new HashMap<>();
        pocTypeMap.put("不使用", "");
        pocTypeMap.put("POC1", "");
        pocTypeMap.put("POC2", "");
        pocTypeMap.put("国威HB1910数字程控电话交换机RCE", "命令执行");

        // 创建顶部面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // POC类型选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("POC类型:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        pocTypeComboBox = new JComboBox<>(new String[]{"不使用", "POC1", "POC2", "国威HB1910数字程控电话交换机RCE"});
        topPanel.add(pocTypeComboBox, gbc);
        
        // 添加事件监听器以根据POC类型显示/隐藏命令输入框
        pocTypeComboBox.addActionListener(e -> updateCommandFieldVisibility());
        
        // 命令输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        commandLabel = new JLabel("命令:");
        topPanel.add(commandLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        commandField = new JTextField(20);
        topPanel.add(commandField, gbc);
        
        // 添加导出按钮
        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> exportResults());
        topPanel.add(exportButton, gbc);
        
        // 添加线程数设置
        gbc.gridx = 3;
        gbc.gridy = 1;
        threadCountLabel = new JLabel("线程数:");
        topPanel.add(threadCountLabel, gbc);
        
        gbc.gridx = 4;
        gbc.gridy = 1;
        threadCountField = new JTextField("10", 5); // 默认10线程
        topPanel.add(threadCountField, gbc);
        
        // 初始时隐藏命令输入框
        updateCommandFieldVisibility();

        // 响应显示区域
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        JScrollPane responseScrollPane = new JScrollPane(responseArea);

        add(topPanel, BorderLayout.NORTH);
        add(responseScrollPane, BorderLayout.CENTER);
    }
    
    // 根据选择的POC类型更新命令输入框的可见性
    private void updateCommandFieldVisibility() {
        String selectedPoc = (String) pocTypeComboBox.getSelectedItem();
        String pocType = pocTypeMap.getOrDefault(selectedPoc, "");
        
        if ("命令执行".equals(pocType)) {
            commandLabel.setVisible(true);
            commandField.setVisible(true);
        } else {
            commandLabel.setVisible(false);
            commandField.setVisible(false);
        }
        
        // 重新验证和重绘面板以确保UI更新
        revalidate();
        repaint();
    }



    public String getSelectedPocType() {
        return (String) pocTypeComboBox.getSelectedItem();
    }
    
    // 导出结果到txt文件
    private void exportResults() {
        try {
            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存文件");
            fileChooser.setSelectedFile(new File("result.txt"));
            
            // 显示保存对话框
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                // 确保文件扩展名为.txt
                String fileName = fileToSave.getAbsolutePath();
                if (!fileName.endsWith(".txt")) {
                    fileName += ".txt";
                    fileToSave = new File(fileName);
                }
                
                // 写入文件
                try (FileWriter writer = new FileWriter(fileToSave)) {
                    writer.write(responseArea.getText());
                    JOptionPane.showMessageDialog(this, "结果已成功导出到: " + fileName);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendRCEPoC() {
        // 重置停止请求标志
        stopRequested = false;
        
        String selectedPoc = getSelectedPocType();
        
        // 只有选择了特定POC类型时才发送请求
        if ("不使用".equals(selectedPoc)) {
            return;
        }
        
        // 获取HttpRequestGUI中的URL
        HttpRequestGUI gui = (HttpRequestGUI) SwingUtilities.getWindowAncestor(this);
        String baseUrl = gui.getUrl();
        
        if (baseUrl == null || baseUrl.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                responseArea.setText("错误: 请求地址不能为空");
            });
            return;
        }
        
        // 启用停止按钮
        SwingUtilities.invokeLater(() -> {
            gui.setStopButtonEnabled(true);
        });
        
        // 在后台线程中执行请求，避免阻塞UI线程
        new Thread(() -> {
            try {
                // 检查baseUrl是否为文件路径
                File urlFile = new File(baseUrl);
                if (urlFile.exists() && urlFile.isFile()) {
                    // 从文件中逐行读取URL并发送请求
                    sendRequestsFromFile(urlFile, selectedPoc, gui);
                } else {
                    // 发送单个请求
                    sendSingleRequest(baseUrl, selectedPoc, gui);
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    responseArea.append("\n请求过程中发生异常: " + e.getMessage() + "\n");
                });
            } finally {
                // 请求完成后禁用停止按钮
                SwingUtilities.invokeLater(() -> {
                    gui.setStopButtonEnabled(false);
                });
            }
        }).start();
    }
    
    // 从文件中逐行读取URL并发送请求
    private void sendRequestsFromFile(File urlFile, String selectedPoc, HttpRequestGUI gui) {
        try (BufferedReader reader = new BufferedReader(new FileReader(urlFile))) {
            String line;
            List<String> urls = new ArrayList<>();
            
            // 先读取所有URL
            while ((line = reader.readLine()) != null && !stopRequested) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // 检查URL是否包含协议，如果没有则添加http://
                    if (!line.contains("://")) {
                        line = "http://" + line;
                    }
                    urls.add(line);
                }
            }
            
            if (stopRequested) {
                SwingUtilities.invokeLater(() -> {
                    responseArea.append("\n请求已被用户停止。\n");
                });
                return;
            }
            
            // 获取用户设置的线程数，默认为10
            int threadCount = 10;
            try {
                threadCount = Integer.parseInt(threadCountField.getText());
            } catch (NumberFormatException e) {
                // 如果输入的不是有效数字，使用默认值10
            }
            
            // 使用用户设置的线程数创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<String>> futures = new ArrayList<>();
            
            // 提交所有请求任务到线程池
            for (String url : urls) {
                Future<String> future = executor.submit(() -> {
                    if (stopRequested) {
                        return "URL: " + url + "\n请求被取消\n\n----------------------------------------\n\n";
                    }
                    try {
                        String result = sendSingleRequestAndGetResponse(url, selectedPoc, gui);
                        return "URL: " + url + "\n" + result + "\n----------------------------------------\n\n";
                    } catch (Exception e) {
                        return "URL: " + url + "\n请求失败: " + e.getMessage() + "\n\n----------------------------------------\n\n";
                    }
                });
                futures.add(future);
            }
            
            // 关闭线程池，不再接受新任务
            executor.shutdown();
            
            // 收集所有结果
            for (int i = 0; i < futures.size(); i++) {
                final int index = i; // 创建final变量以在lambda表达式中使用
                if (stopRequested) {
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("\n请求已被用户停止。\n");
                    });
                    // 取消未完成的任务
                    for (int j = index; j < futures.size(); j++) {
                        futures.get(j).cancel(true);
                    }
                    break;
                }
                
                try {
                    // 获取结果，设置超时以避免无限等待
                    final String result = futures.get(index).get(25, TimeUnit.SECONDS);
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append(result);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("URL: " + urls.get(index) + "\n请求被中断\n\n----------------------------------------\n\n");
                    });
                    // 取消剩余任务
                    for (int j = index + 1; j < futures.size(); j++) {
                        futures.get(j).cancel(true);
                    }
                    break;
                } catch (ExecutionException e) {
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("URL: " + urls.get(index) + "\n请求执行异常: " + e.getCause().getMessage() + "\n\n----------------------------------------\n\n");
                    });
                } catch (TimeoutException e) {
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("URL: " + urls.get(index) + "\n请求超时\n\n----------------------------------------\n\n");
                    });
                    // 取消当前任务
                    futures.get(index).cancel(true);
                }
            }
            
            // 请求完成后禁用停止按钮
            SwingUtilities.invokeLater(() -> {
                gui.setStopButtonEnabled(false);
                responseArea.append("\n所有请求已结束\n");
            });
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                responseArea.setText("读取文件失败: " + e.getMessage());
            });
        }
    }
    
    // 发送单个请求
    private void sendSingleRequest(String baseUrl, String selectedPoc, HttpRequestGUI gui) {
        try {
            String response = sendSingleRequestAndGetResponse(baseUrl, selectedPoc, gui);
            SwingUtilities.invokeLater(() -> {
                responseArea.append(response);
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                responseArea.append("请求失败: " + e.getMessage());
            });
        } finally {
            // 请求完成后禁用停止按钮
            SwingUtilities.invokeLater(() -> {
                gui.setStopButtonEnabled(false);
                responseArea.append("\n所有请求已结束\n");
            });
        }
    }
    
    // 设置停止请求标志
    public void stopRequests() {
        stopRequested = true;
    }
    
    // 创建一个信任所有证书的SSL上下文
    private SSLContext createAllTrustingSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
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
    
    // 发送单个请求并返回响应
    private String sendSingleRequestAndGetResponse(String baseUrl, String selectedPoc, HttpRequestGUI gui) throws Exception {
        String requestUrl = baseUrl;
        String requestMethod = "GET";
        
        // 根据不同的POC类型设置不同的请求URL和参数
        switch (selectedPoc) {
            case "POC1":
                requestUrl = baseUrl + "/poc1/path";
                break;
            case "POC2":
                requestUrl = baseUrl + "/poc2/path";
                break;
            case "国威HB1910数字程控电话交换机RCE":
                String command = commandField.getText();
                if (command == null || command.isEmpty()) {
                    command = "id"; // 默认命令
                }
                try {
                    String result = com.httprequest.gui.POC.GuoweiHB1910RCE.sendRCEAndGetResponse(baseUrl, command);
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append(result);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("请求失败: " + e.getMessage());
                    });
                }
                // 确保只发送一条请求，不执行其他逻辑
                break;
            default:
                // 对于未知的POC类型，使用默认URL
                requestUrl = baseUrl;
                break;
        }
        
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
        
        // 应用代理设置
        if (gui != null) {
            gui.getProxySettings().applyProxySettings();
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
            while ((line = reader.readLine()) != null && !stopRequested) {
                final String lineToAppend = line;
                SwingUtilities.invokeLater(() -> {
                    responseArea.append(lineToAppend + "\n");
                });
                // 同时添加到response中，以便在方法返回时包含完整的响应
                response.append(line).append("\n");
            }
        }

        // 返回请求详情和响应详情
        return "请求详情:\n" + requestDetails.toString() + "\n响应详情:\n" + response.toString();
    }
}