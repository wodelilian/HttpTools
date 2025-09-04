package com.httprequest.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BlankPanel extends JPanel {
    private JTextField[] headerFields;
    private String[] headers;
    private String[] lastSavedHeaders;
    private JTextField customHeaderField;
    private JTextField customHeaderValue;

    public BlankPanel() {
        initComponents();
    }
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        // 添加标题
        JLabel titleLabel = new JLabel("HTTP请求头编辑");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // 常见请求头字段
        headers = new String[]{
            "Authorization", "Cookie", "Accept", "Accept-Language",
            "Accept-Encoding", "Connection", "Cache-Control",
            "Content-Type", "Content-Length"
        };

        // 初始化文本框数组
        headerFields = new JTextField[headers.length];

        // 为每个请求头字段创建标签和文本框
        for (int i = 0; i < headers.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.weightx = 0.1;
            add(new JLabel(headers[i] + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.9;
            headerFields[i] = new JTextField(30);
            // 设置字体以确保在Windows系统上正确显示中文
            headerFields[i].setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            add(headerFields[i], gbc);
        }
        
        // 添加自定义请求头字段
        gbc.gridx = 0;
        gbc.gridy = headers.length + 1;
        gbc.weightx = 0.1;
        add(new JLabel("自定义字段:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        customHeaderField = new JTextField(30);
        customHeaderField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        add(customHeaderField, gbc);
        
        // 添加自定义请求头值
        gbc.gridx = 0;
        gbc.gridy = headers.length + 2;
        gbc.weightx = 0.1;
        add(new JLabel("自定义值:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        customHeaderValue = new JTextField(30);
        customHeaderValue.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        add(customHeaderValue, gbc);

        // 添加按钮面板
        gbc.gridx = 0;
        gbc.gridy = headers.length + 3;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        // 清空按钮
        JButton clearButton = new JButton("一键清空");
        clearButton.addActionListener(e -> {
            // 保存当前内容
            lastSavedHeaders = new String[headerFields.length];
            for (int i = 0; i < headerFields.length; i++) {
                lastSavedHeaders[i] = headerFields[i].getText();
            }
            // 清空输入框
            for (JTextField field : headerFields) {
                field.setText("");
            }
        });
        buttonPanel.add(clearButton);
        
        // 还原按钮
        JButton restoreButton = new JButton("还原请求");
        restoreButton.addActionListener(e -> {
            if (lastSavedHeaders != null) {
                for (int i = 0; i < headerFields.length && i < lastSavedHeaders.length; i++) {
                    headerFields[i].setText(lastSavedHeaders[i]);
                }
            }
        });
        buttonPanel.add(restoreButton);
        
        add(buttonPanel, gbc);

        // 添加上下空间
        gbc.gridx = 0;
        gbc.gridy = headers.length + 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    // 获取非空的请求头字段
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String value = headerFields[i].getText().trim();
            if (!value.isEmpty()) {
                headerMap.put(headers[i], value);
            }
        }
        
        // 添加自定义请求头
        String customHeader = customHeaderField.getText().trim();
        String customValue = customHeaderValue.getText().trim();
        if (!customHeader.isEmpty() && !customValue.isEmpty()) {
            headerMap.put(customHeader, customValue);
        }
        
        return headerMap;
    }
}