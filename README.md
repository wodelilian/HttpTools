# HTTP请求工具

## 项目简介
这是一个基于Java Swing开发的HTTP请求工具，提供了图形化界面，方便用户发送各种HTTP请求并查看响应结果。同时集成了POC（概念验证）测试功能，可用于安全测试和漏洞验证。

## 功能特性

### HTTP请求功能
- 支持多种HTTP请求方法：GET、POST、PUT、DELETE、HEAD、OPTIONS
- 可自定义请求URL和请求头
- 支持请求体编辑（针对POST、PUT等方法）
- 显示完整的响应结果，包括状态码、响应头和响应体
- 支持HTTPS连接，自动信任所有SSL证书

### 代理设置功能
- 可配置HTTP代理服务器
- 支持代理认证

### POC测试功能
- 支持国威HB1910数字程控电话交换机RCE漏洞测试
- 支持命令执行和结果查看
- 可配置线程数进行并发测试
- 支持结果导出功能

### 文件上传POC功能
- 支持天锐绿盘云文档安全管理系统文件上传测试
- 支持Crocus系统RepairRecord.do文件上传测试
- 可配置Shell内容、文件名和线程数
- 支持多线程文件上传测试

## 项目结构
```
Java/
├── META-INF/                          # 元数据信息
│   └── MANIFEST.MF
├── src/                               # 源代码目录
│   └── main/java/com/httprequest/gui/ # 主要源码
│       ├── Main.java                  # 程序入口点
│       ├── HttpRequestGUI.java        # 主界面类
│       ├── POCTestPanel.java          # POC测试面板
│       ├── FileUploadPOCPanel.java    # 文件上传POC面板
│       ├── Utils.java                 # 工具类
│       ├── POC/                       # POC实现类目录
│       │   ├── GuoweiHB1910RCE.java               # 国威HB1910 RCE漏洞POC
│       │   ├── TianruiLvpanyunFileUpload.java     # 天锐绿盘云文件上传POC
│       │   └── CrocusRepairRecordUpload.java      # Crocus系统文件上传POC
│       └── blank/                     # 空白面板目录
├── .gitignore                         # Git忽略配置
├── README.md                          # 项目说明文档
├── compile.sh                         # 编译脚本
├── pom.xml                            # Maven项目配置文件
├── run.sh                             # 运行脚本
└── run_app.sh                         # 应用运行脚本
```

## 核心功能模块说明

### Main.java
程序入口点，主要功能：
- 设置系统属性以确保使用UTF-8编码
- 针对Windows和macOS操作系统进行特定优化
- 在Windows下启用文本抗锯齿和高分辨率显示支持
- 在macOS下解决IMKCFRunLoopWakeUpReliable错误
- 启动GUI界面

### HttpRequestGUI.java
主界面类，实现HTTP请求的核心功能：
- 提供请求方法选择、URL输入、请求头配置和请求体编辑功能
- 集成了POCTestPanel和FileUploadPOCPanel面板的切换功能
- 实现请求发送逻辑和响应结果展示
- 初始化SSL上下文以支持HTTPS连接

### POCTestPanel.java
POC测试面板，主要实现命令执行漏洞测试：
- 支持选择国威HB1910数字程控电话交换机RCE漏洞测试
- 提供命令输入框和线程数配置
- 实现多线程并发测试
- 支持测试结果导出功能
- 包含请求构建、发送和响应处理逻辑

### FileUploadPOCPanel.java
文件上传POC测试面板：
- 支持选择天锐绿盘云文档安全管理系统和Crocus系统文件上传测试
- 提供Shell内容输入、文件名设置和线程数配置
- 实现文件上传请求构建和响应处理
- 支持多线程上传测试

### Utils.java
工具类，提供项目中通用的方法：
- 创建信任所有证书的SSL上下文
- 根据协议和端口是否为默认值来决定是否在Host中包含端口号

### POC实现类

#### GuoweiHB1910RCE.java
实现国威HB1910数字程控电话交换机RCE漏洞利用：
- 构建命令执行的GET请求
- 对命令进行URL编码
- 处理HTTPS连接的SSL证书信任
- 返回请求响应结果

#### TianruiLvpanyunFileUpload.java
实现天锐绿盘云文档安全管理系统uploadFolder文件上传功能：
- 构建文件上传的POST请求
- 处理多部分表单数据
- 处理HTTPS连接的SSL证书信任
- 返回请求响应结果

#### CrocusRepairRecordUpload.java
实现Crocus系统RepairRecord.do文件上传功能：
- 构建JSON格式的文件上传请求
- 处理base64编码的图片内容
- 处理HTTPS连接的SSL证书信任
- 返回请求响应结果

## 系统要求
- JDK 8或更高版本
- Maven 3.x（用于构建项目）
- Windows、macOS或Linux操作系统

## 使用方法

### 使用脚本运行
1. 授予脚本执行权限：`chmod +x compile.sh run.sh run_app.sh`
2. 编译项目：`./compile.sh`
3. 运行程序：`./run.sh` 或 `./run_app.sh`

### 使用Maven构建
1. 确保已安装Maven 3.x
2. 执行构建命令：`mvn clean package`
3. 在target目录中找到生成的JAR文件并运行

### 直接运行JAR文件
如果已存在可执行JAR文件，可直接双击运行或使用命令行：`java -jar HttpRequestTool.jar`

## 操作指南

### 发送HTTP请求
1. 在主界面选择所需的HTTP请求方法（GET、POST等）
2. 在URL输入框中输入目标地址
3. 如需添加请求头，点击相应按钮添加
4. 对于需要请求体的方法（如POST、PUT），在请求体文本框中输入内容
5. 点击"发送"按钮执行请求
6. 在下方响应区域查看状态码、响应头和响应体

### 执行POC测试
1. 切换到"POC测试"标签页
2. 选择POC类型（如"国威HB1910数字程控电话交换机RCE"）
3. 输入要执行的命令
4. 配置线程数（默认为1）
5. 点击"测试"按钮开始测试
6. 查看测试结果，可点击"导出结果"保存

### 执行文件上传POC测试
1. 切换到"文件上传POC"标签页
2. 选择文件上传POC类型
3. 输入Shell内容和文件名
4. 配置线程数
5. 点击"测试"按钮开始执行文件上传测试
6. 查看测试结果

## 注意事项
1. 该工具仅供学习和授权测试使用，请勿用于未授权的系统测试
2. 使用POC功能时，请确保您有测试目标系统的授权
3. 对于HTTPS连接，程序会自动信任所有证书，可能存在安全风险
4. 大批量测试时请注意配置适当的线程数，避免对目标系统造成过大压力
5. 在Windows系统上，程序会自动优化字体显示以提供更好的用户体验

## 开发环境
- JDK 8或更高版本
- Maven 3.x
- 依赖库：Java Swing（内置）

## 许可证
[请在使用本项目前阅读法律法规，确保合法合规使用](https://github.com/Threekiii/Awesome-Laws)

## 免责声明
1. 本工具仅用于学习、研究和合法的安全测试，不得用于任何非法用途或未经授权的系统测试
2. 使用本工具进行任何测试时，用户必须确保已获得充分的授权和许可
3. 工具的作者和贡献者不对用户使用本工具的行为承担任何法律责任
4. 用户使用本工具产生的任何直接或间接后果由用户自行承担
5. 如发现本工具被用于非法目的，作者保留采取法律行动的权利
6. 本工具的设计目标是帮助安全研究人员和开发人员发现并修复系统中的安全漏洞，而非用于破坏或入侵系统