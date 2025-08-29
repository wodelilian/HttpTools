#!/bin/bash

# 创建输出目录
classes_dir="target/classes"
mkdir -p "$classes_dir"

# 编译Java源文件
javac -d "$classes_dir" -cp "$classes_dir" src/main/java/com/httprequest/gui/*.java

echo "编译完成！"