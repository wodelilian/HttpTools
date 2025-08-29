package com.httprequest.gui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 设置系统属性以确保使用UTF-8编码
        System.setProperty("file.encoding", "UTF-8");
        
        // 优化Windows下的字体显示
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            // 启用文本抗锯齿
            System.setProperty("swing.aatext", "true");
            // 设置高分辨率显示支持
            System.setProperty("sun.java2d.uiScale", "1");
            // 设置默认字体
            System.setProperty("awt.useSystemAAFontSettings", "on");
        }
        
        SwingUtilities.invokeLater(() -> {
            HttpRequestGUI gui = new HttpRequestGUI();
            gui.setVisible(true);
        });
    }
}