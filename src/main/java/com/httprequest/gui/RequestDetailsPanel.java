package com.httprequest.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RequestDetailsPanel extends JPanel {
    private JTextArea requestBodyArea;
    private JTextArea responseArea;
    private JPanel requestBodyPanel;
    private JComboBox<String> methodComboBox;

    public RequestDetailsPanel(JComboBox<String> methodComboBox) {
        this.methodComboBox = methodComboBox;
        initComponents();
        updateRequestBodyVisibility();
    }

    private void initComponents() {
        setLayout(new GridLayout(2, 1));

        // 请求体面板
        requestBodyPanel = new JPanel();
        requestBodyPanel.setLayout(new BorderLayout());
        requestBodyPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        requestBodyPanel.add(new JLabel("请求体:"), BorderLayout.NORTH);
        requestBodyArea = new JTextArea(5, 50);
        requestBodyArea.setLineWrap(true);
        requestBodyPanel.add(new JScrollPane(requestBodyArea), BorderLayout.CENTER);

        // 响应区域
        JPanel responsePanel = new JPanel();
        responsePanel.setLayout(new BorderLayout());
        responsePanel.add(new JLabel("响应结果:"), BorderLayout.NORTH);
        responseArea = new JTextArea();
        responseArea.setLineWrap(true);
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);

        add(requestBodyPanel);
        add(responsePanel);

        // 设置响应区域的最小大小
        responsePanel.setMinimumSize(new Dimension(0, 300));
    }

    public void updateRequestBodyVisibility() {
        String method = (String) methodComboBox.getSelectedItem();
        // 始终显示请求体面板
        requestBodyPanel.setVisible(true);
        // 只有POST和PUT方法时可编辑
        boolean editable = "POST".equals(method) || "PUT".equals(method);
        requestBodyArea.setEditable(editable);
        
        // 可编辑时白色背景，不可编辑时置灰
        //requestBodyArea.setBackground(editable ? Color.WHITE : Color.LIGHT_GRAY);
        
        revalidate();
        repaint();
    }

    public JTextArea getRequestBodyArea() {
        return requestBodyArea;
    }

    public JTextArea getResponseArea() {
        return responseArea;
    }
}