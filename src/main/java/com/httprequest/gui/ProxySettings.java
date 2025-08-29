package com.httprequest.gui;

import javax.swing.*;
import java.awt.*;

public class ProxySettings {
    private JCheckBox useProxyCheckBox;
    private JComboBox<String> proxyTypeComboBox;
    private JTextField proxyHostField;
    private JTextField proxyPortField;

    public ProxySettings() {
        initializeComponents();
    }

    private void initializeComponents() {
        useProxyCheckBox = new JCheckBox("使用代理");
        proxyTypeComboBox = new JComboBox<>(new String[] {"HTTP", "HTTPS", "SOCKS"});
        proxyHostField = new JTextField();
        proxyHostField.setText("localhost");
        proxyPortField = new JTextField();
        proxyPortField.setText("8080");
    }

    public void addToPanel(JPanel panel, GridBagConstraints gbc) {
        // 代理设置标签
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("代理设置:"), gbc);

        // 使用代理复选框
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(useProxyCheckBox, gbc);

        // 代理类型标签
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("类型:"), gbc);

        // 代理类型选择框
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(proxyTypeComboBox, gbc);

        // 代理主机标签
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("主机:"), gbc);

        // 代理主机输入框
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(proxyHostField, gbc);

        // 代理端口标签
        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("端口:"), gbc);

        // 代理端口输入框
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(proxyPortField, gbc);
    }

    public boolean isUseProxy() {
        return useProxyCheckBox.isSelected();
    }

    public String getProxyType() {
        return (String) proxyTypeComboBox.getSelectedItem();
    }

    public String getProxyHost() {
        return proxyHostField.getText();
    }

    public int getProxyPort() {
        try {
            return Integer.parseInt(proxyPortField.getText());
        } catch (NumberFormatException e) {
            return 8080; // 默认端口
        }
    }

    public void applyProxySettings() {
        if (isUseProxy()) {
            String proxyHost = getProxyHost();
            int proxyPort = getProxyPort();
            String proxyType = getProxyType();

            // 设置系统代理属性
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));

            if ("SOCKS".equals(proxyType)) {
                System.setProperty("socksProxyHost", proxyHost);
                System.setProperty("socksProxyPort", String.valueOf(proxyPort));
            }
        } else {
            // 清除代理设置
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }
}