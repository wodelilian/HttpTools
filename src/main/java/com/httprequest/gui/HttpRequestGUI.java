package com.httprequest.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class HttpRequestGUI extends JFrame {
    private JComboBox<String> methodComboBox;
    private JTextField urlField;
    private JTextField userAgentField;
    private RequestDetailsPanel requestDetailsPanel;
    private JTextArea requestBodyArea;
    private JTextArea responseArea;
    private ProxySettings proxySettings;
    private ExecutorService currentExecutor;
    private JButton stopButton;
    private BlankPanel blankPanel;
    private POCTestPanel pocTestPanel;
    private FileUploadPOCPanel fileUploadPOCPanel;


    public HttpRequestGUI() {
    setTitle("HTTP请求工具");
    setSize(850, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    proxySettings = new ProxySettings();
    initTrustAllSSLContext(); // 初始化SSL上下文
    initComponents();
}

    private void initComponents() {
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建标签页
        JTabbedPane tabbedPane = new JTabbedPane();

        // 顶部面板 - 包含请求方法、URL等
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 请求方法
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("请求方法:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0; // 不设置权重，让选择框宽度仅容纳内容
        methodComboBox = new JComboBox<>(new String[] {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"});
        methodComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRequestBodyVisibility();
            }
        });
        // 不设置fill为HORIZONTAL，保持原有宽度
        topPanel.add(methodComboBox, gbc);

        // URL
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        topPanel.add(new JLabel("请求地址:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 4; // 跨越更多列
        gbc.weightx = 1.0; // 占据剩余宽度
        gbc.fill = GridBagConstraints.HORIZONTAL;
        urlField = new JTextField();
        urlField.setText(""); // 移除预输入信息
        urlField.setEditable(true);  // 确保可编辑
        urlField.setFocusable(true); // 确保可获取焦点
        urlField.setFont(new Font("Monospaced", Font.PLAIN, 12)); // 设置字体以确保特殊字符显示
        topPanel.add(urlField, gbc);

        // 发送按钮
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0; // 不设置权重
        JButton sendButton = new JButton("发送请求");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });
        // 不设置fill为HORIZONTAL，保持原有宽度
        topPanel.add(sendButton, gbc);

        // 停止按钮
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1; // 不设置权重
        stopButton = new JButton("停止请求");
        stopButton.setEnabled(false); // 初始禁用
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAllRequests();
            }
        });
        topPanel.add(stopButton, gbc);

        // User-Agent
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        topPanel.add(new JLabel("User-Agent:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 9; // 占据剩余所有列
        gbc.weightx = 0; // 填满整行
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userAgentField = new JTextField();
        userAgentField.setText("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        // 设置字体以确保在Windows系统上正确显示中文
        userAgentField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        topPanel.add(userAgentField, gbc);

        // 添加代理设置
        proxySettings.addToPanel(topPanel, gbc);

        // 添加顶部面板到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 创建请求详情面板
        requestDetailsPanel = new RequestDetailsPanel(methodComboBox);
        requestBodyArea = requestDetailsPanel.getRequestBodyArea();
        responseArea = requestDetailsPanel.getResponseArea();
        
        // 创建空白面板
        blankPanel = new BlankPanel();
        
        // 创建POC测试面板
        pocTestPanel = new POCTestPanel();
        
        // 添加面板到标签页
        tabbedPane.addTab("请求详情", requestDetailsPanel);
        tabbedPane.addTab("请求头编辑", blankPanel);
        tabbedPane.addTab("命令执行POC测试", pocTestPanel);
        
        // 创建文件上传POC测试面板
        fileUploadPOCPanel = new FileUploadPOCPanel();
        tabbedPane.addTab("文件上传POC测试", fileUploadPOCPanel);

        
        // 将标签页添加到主面板
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 添加底部提示信息和作者信息
        Box bottomBox = Box.createHorizontalBox();
        JLabel warningLabel = new JLabel("请勿用于非法测试！");
        warningLabel.setBorder(new EmptyBorder(5, 10, 0, 0));
        bottomBox.add(warningLabel);
        
        bottomBox.add(Box.createHorizontalGlue());
        
        JLabel authorLabel = new JLabel("作者: wodelilian");
        authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        authorLabel.setBorder(new EmptyBorder(5, 0, 0, 10));
        bottomBox.add(authorLabel);
        
        mainPanel.add(bottomBox, BorderLayout.SOUTH);

        add(mainPanel);

        // 初始隐藏请求体
    updateRequestBodyVisibility();
}

// 提供公共方法获取URL
public String getUrl() {
    return urlField.getText().trim();
}

// 提供公共方法设置停止按钮状态
    public void setStopButtonEnabled(boolean enabled) {
        stopButton.setEnabled(enabled);
    }

    // 提供公共方法获取ProxySettings
    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    private void updateRequestBodyVisibility() {
        // 始终显示请求体
        requestBodyArea.setVisible(true);
        
        // 只有POST和PUT方法时可编辑
        String method = (String) methodComboBox.getSelectedItem();
        boolean editable = "POST".equals(method) || "PUT".equals(method);
        requestBodyArea.setEditable(editable);
    }

    private void sendRequest() {
        // 触发POC测试
        if (pocTestPanel != null && !"不使用".equals(pocTestPanel.getSelectedPocType())) {
            // 如果POC类型不为"不使用"，则只发送POC请求，不发送主请求
            pocTestPanel.sendRCEPoC();
            return;
        }
        
        // 如果命令执行POC类型为"不使用"，且文件上传POC类型为"Crocus系统RepairRecord.do文件上传"，则发送文件上传POC请求
        if (pocTestPanel != null && "不使用".equals(pocTestPanel.getSelectedPocType()) && 
            fileUploadPOCPanel != null && "Crocus系统RepairRecord.do文件上传".equals(fileUploadPOCPanel.getSelectedPocType())) {
            fileUploadPOCPanel.sendFileUploadPoC();
            return;
        }
        
        // 如果命令执行POC类型为"不使用"，且文件上传POC类型为"天锐绿盘云文档安全管理uploadFolder存在文件上传"，则发送对应的POC请求
        if (pocTestPanel != null && "不使用".equals(pocTestPanel.getSelectedPocType()) && 
            fileUploadPOCPanel != null && "天锐绿盘云文档安全管理uploadFolder存在文件上传".equals(fileUploadPOCPanel.getSelectedPocType())) {
            fileUploadPOCPanel.sendFileUploadPoC();
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String method = (String) methodComboBox.getSelectedItem();
                    String input = urlField.getText().trim();
                    String userAgent = userAgentField.getText();
                    String requestBody = requestBodyArea.getText();

                    if (input.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            responseArea.setText("错误: 输入不能为空");
                        });
                        return;
                    }

                    final List<String> urls = new ArrayList<>();

                    // 判断输入是URL还是文件路径
                    if (input.startsWith("http://") || input.startsWith("https://")) {
                        // 单个URL
                        urls.add(input);
                    } else {
                        // 尝试作为文件路径处理
                        try {
                            List<String> fileUrls = readUrlsFromFile(input);
                            if (fileUrls.isEmpty()) {
                                SwingUtilities.invokeLater(() -> {
                                    responseArea.setText("警告: 文件中未找到有效的URL");
                                });
                                return;
                            }
                            urls.addAll(fileUrls);
                        } catch (IOException e) {
                            // 文件读取失败，尝试将输入视为单个URL
                            urls.add(input);
                        }
                    }

                    // 应用代理设置
                    proxySettings.applyProxySettings();

                    // 处理多个URL
                    SwingUtilities.invokeLater(() -> {
                        responseArea.setText("请求结果:\n");
                    });

                    // 取消之前可能正在运行的请求
                    if (currentExecutor != null && !currentExecutor.isShutdown()) {
                        currentExecutor.shutdownNow();
                    }

                    // 创建固定大小为10的线程池
                    currentExecutor = Executors.newFixedThreadPool(10);
                    ExecutorService executor = currentExecutor;
                    CountDownLatch latch = new CountDownLatch(urls.size());

                    // 启用停止按钮
                    SwingUtilities.invokeLater(() -> {
                        stopButton.setEnabled(true);
                    });

                    for (String urlStr : urls) {
                        executor.submit(() -> {
                            try {
                                URL url = new URL(urlStr);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                // 如果是HTTPS连接，应用SSL设置
                                if (connection instanceof HttpsURLConnection) {
                                    HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                                    httpsConnection.setSSLSocketFactory(sslSocketFactory);
                                    httpsConnection.setHostnameVerifier(hostnameVerifier);
                                }

                                // 设置请求方法
                                connection.setRequestMethod(method);

                                // 设置请求头
                                connection.setRequestProperty("User-Agent", userAgent);
                                connection.setRequestProperty("Accept", "*/*");
                                connection.setConnectTimeout(20000); // 20秒超时
                                connection.setReadTimeout(20000);

                                // 应用自定义请求头
                                if (blankPanel != null) {
                                    java.util.Map<String, String> customHeaders = blankPanel.getHeaders();
                                    for (java.util.Map.Entry<String, String> entry : customHeaders.entrySet()) {
                                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                                    }
                                }

                                // 对于POST和PUT方法，设置请求体
                                if ("POST".equals(method) || "PUT".equals(method)) {
                                    connection.setDoOutput(true);
                                    connection.setRequestProperty("Content-Type", "application/json; utf-8");

                                    try (OutputStream os = connection.getOutputStream()) {
                                        byte[] inputBytes = requestBody.getBytes("utf-8");
                                        os.write(inputBytes, 0, inputBytes.length);
                                    }
                                }

                                // 设置请求超时时间为20秒
                                connection.setConnectTimeout(20000);
                                connection.setReadTimeout(20000);

                                // 获取响应码
                                int responseCode = connection.getResponseCode();
                                final StringBuilder resultBuilder = new StringBuilder();
                                resultBuilder.append(urlStr).append(" - 响应码: " ).append(responseCode).append("\n");

                                // 判断是否是单个URL请求（非文件批量请求）
                                boolean isSingleUrl = urls.size() == 1 && (urlStr.startsWith("http://") || urlStr.startsWith("https://"));

                                if (isSingleUrl) {
                                    // 单个URL请求，显示完整响应体
                                    resultBuilder.append("\n响应头:\n");
                                    // 获取所有响应头
                                    java.util.Map<String, java.util.List<String>> headers = connection.getHeaderFields();
                                    for (java.util.Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
                                        String key = entry.getKey();
                                        for (String value : entry.getValue()) {
                                            resultBuilder.append(key != null ? key : "Status").append(": " ).append(value).append("\n");
                                        }
                                    }

                                    // 获取响应体
                                    resultBuilder.append("\n响应体:\n");
                                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                                        String inputLine;
                                        StringBuilder response = new StringBuilder();
                                        while ((inputLine = in.readLine()) != null) {
                                            response.append(inputLine).append("\n");
                                        }
                                        resultBuilder.append(response.toString());
                                    } catch (IOException e) {
                                        // 尝试从错误流中读取
                                        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                                            String inputLine;
                                            StringBuilder response = new StringBuilder();
                                            while ((inputLine = in.readLine()) != null) {
                                                response.append(inputLine).append("\n");
                                            }
                                            resultBuilder.append(response.toString());
                                        } catch (IOException ex) {
                                            resultBuilder.append("无法读取响应体: " ).append(ex.getMessage()).append("\n");
                                        }
                                    }
                                }

                                final String urlResult = resultBuilder.toString();
                                SwingUtilities.invokeLater(() -> {
                                    responseArea.append(urlResult);
                                });

                                connection.disconnect();
                            } catch (Exception e) {
                                final String errorMsg;
                                if (e instanceof java.net.SocketTimeoutException) {
                                    errorMsg = urlStr + " - 请求超时\n";
                                } else {
                                    errorMsg = urlStr + " - 错误: " + e.getMessage() + "\n";
                                }
                                SwingUtilities.invokeLater(() -> {
                                    responseArea.append(errorMsg);
                                });
                            } finally {
                                latch.countDown();
                            }
                        });
                    }

                    // 等待所有请求完成
                    executor.submit(() -> {
                        try {
                            latch.await();
                            SwingUtilities.invokeLater(() -> {
                                responseArea.append("\n所有请求处理完成\n");
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            executor.shutdown();
                        }
                    });

                    // 移除重复的完成消息，由latch.await()后的代码处理
                    // 所有请求处理完成
                    // SwingUtilities.invokeLater(() -> {
                    //     responseArea.append("\n所有请求处理完成\n");
                    // });

                } catch (Exception e) {
                    final String errorMsg = "处理请求时出错: " + e.getMessage();
                    SwingUtilities.invokeLater(() -> {
                        responseArea.setText(errorMsg);
                    });
                }
            }
        }).start();
    }

    // 信任所有SSL证书的相关变量
private SSLSocketFactory sslSocketFactory;
private HostnameVerifier hostnameVerifier;

// 停止所有请求
    private void stopAllRequests() {
        if (currentExecutor != null && !currentExecutor.isShutdown()) {
            currentExecutor.shutdownNow();
            SwingUtilities.invokeLater(() -> {
                responseArea.append("\n请求已停止\n");
                stopButton.setEnabled(false);
            });
        }
        
        // 停止POCTestPanel中的请求
        if (pocTestPanel != null) {
            pocTestPanel.stopRequests();
        }
        
        // 停止FileUploadPOCPanel中的请求
        if (fileUploadPOCPanel != null) {
            fileUploadPOCPanel.stopRequests();
        }
    }

    private void initTrustAllSSLContext() {
    try {
        // 使用Utils工具类创建SSL上下文
        SSLContext sslContext = Utils.createAllTrustingSSLContext();
        sslSocketFactory = sslContext.getSocketFactory();

        // 使用Utils工具类创建主机名校验器
        hostnameVerifier = Utils.createAllTrustingHostnameVerifier();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private List<String> readUrlsFromFile(String filePath) throws IOException {
        List<String> urls = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在");
        }
        if (!file.isFile()) {
            throw new IOException("路径不是文件");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释行
                if (!line.isEmpty() && !line.startsWith("#")) {
                    urls.add(line);
                }
            }
        }
        return urls;
    }
}