#!/bin/bash

# 直接从编译类文件运行应用程序，显式设置所有关键系统属性
java -Dapple.awt.imk.ril=false -Dapple.awt.imk.status=hidden -Djava.awt.im.style=off -Dapple.awt.enableInputMethods=false -Dapple.awt.textinputui=off -Dapple.awt.java6textinput=false -Dapple.awt.imk.autoactivate=false -Dapple.awt.imk.allowinputmethods=false -Dapple.awt.imk.forceoff=true -Dcom.apple.macos.useScreenMenuBar=false -Dcom.apple.macos.smallTabs=true -cp target/classes com.httprequest.gui.Main