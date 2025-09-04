package com.httprequest.gui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // 设置系统属性以确保使用UTF-8编码
        System.setProperty("file.encoding", "UTF-8");
        
        // 获取操作系统名称
        String osName = System.getProperty("os.name").toLowerCase();
        
        // 优化Windows下的字体显示
        if (osName.contains("win")) {
            // 启用文本抗锯齿
            System.setProperty("swing.aatext", "true");
            // 设置高分辨率显示支持
            System.setProperty("sun.java2d.uiScale", "1");
            // 设置默认字体
            System.setProperty("awt.useSystemAAFontSettings", "on");
        }
        
        // 优化macOS下的Swing行为，解决IMKCFRunLoopWakeUpReliable错误
        if (osName.contains("mac")) {
            // 基础系统属性设置
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("sun.java2d.opengl", "true");
            System.setProperty("java.awt.focus.autoraise", "false");
            System.setProperty("com.apple.accessibility.AXEnhancedUserInterface", "false");
            System.setProperty("apple.awt.brushMetalLook", "false");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "HTTP请求工具");
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("apple.awt.application.name", "HTTP请求工具");
            
            // 关键：解决IMKCFRunLoopWakeUpReliable错误的系统属性
            // 禁用输入法框架的可靠唤醒功能 - 这是最关键的设置
            System.setProperty("apple.awt.imk.ril", "false");
            
            // 禁用所有与输入法相关的功能
            System.setProperty("apple.awt.imk.status", "hidden");
            System.setProperty("java.awt.im.style", "off");
            System.setProperty("apple.awt.enableInputMethods", "false");
            System.setProperty("apple.awt.textinputui", "off");
            System.setProperty("apple.awt.java6textinput", "false");
            
            // 新增：禁用所有与IMKCFRunLoop相关的功能
            System.setProperty("apple.awt.imk.autoactivate", "false");
            System.setProperty("apple.awt.imk.allowinputmethods", "false");
            System.setProperty("apple.awt.imk.forceoff", "true");
            
            // 新增：禁用与Cocoa事件循环相关的功能
            System.setProperty("com.apple.macos.useScreenMenuBar", "false");
            System.setProperty("com.apple.macos.smallTabs", "true");
            
            // 优化图形和渲染性能
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("sun.java2d.translaccel", "true");
        }
        
        // 使用invokeLater确保在Event Dispatch Thread中创建和显示GUI
        SwingUtilities.invokeLater(() -> {
            HttpRequestGUI gui = new HttpRequestGUI();
            gui.setVisible(true);
        });
    }
}