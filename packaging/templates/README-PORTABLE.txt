帐里说帐 - Windows 便携版（解压即用）

1. 将整个「BillInBill」文件夹解压到有写权限的路径（桌面、文档等），不要放在仅管理员可写的目录。

2. 进入 BillInBill，双击 run.bat 或 start.bat（两者等价，start.bat 会调用 run.bat）。

3. 启动后在浏览器访问：http://127.0.0.1:8080

4. 压缩包已包含 SQLite 数据库文件 data\wechat-bill.db。备份请先关闭窗口，再复制 data 文件夹。

5. 压缩包内已包含 jdk-21\ 目录（构建时从本机 JAVA_HOME / JDK_PACK_PATH 或 -JdkPath 拷贝）。若打包时使用了 -SkipJdkBundle，则需使用方本机已安装 Java 21。

6. 端口、数据库路径等基础配置见 config\application.properties，可按需修改。
