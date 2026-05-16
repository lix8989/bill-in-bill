帐里说帐 — Windows 绿色便携版
====================================

一、使用前准备
-----------
1. 将整个文件夹解压到你本机上「可读写」的路径（示例：桌面、D:\Apps\BillInBill），不要解压到 Program Files 等可能没有写权限的目录。
2. Java 任选其一：
   （A）若压缩包内含 jdk-21\ 便携运行时，start.bat 会优先选用；
   （B）本机已安装 JDK 21 且 java 已在 PATH：可不附带 jdk-21。


二、启动
-----
双击 start.bat。控制台出现 Started 后即表示后端已就绪。
浏览器打开：http://127.0.0.1:8080

若端口 8080 被占用，可在本文件夹下增设 config，并写入 application.properties 修改端口，例如：
  （1）将 config\application.properties.example 复制为 config\application.properties
  （2）编辑 server.port=8081


三、可选：外置配置文件
-------------
与本目录并排创建文件夹 config，放入 application.properties 即可覆盖端口、数据源（仍为 SQLite）等。
可参考本包内附带示例：config\application.properties.example

也可通过环境变量增加配置位置：
  set JAVA_OPTS=--spring.config.additional-location=file:%cd%/config/


四、数据与备份（SQLite）
-----------------
默认数据库在工作目录 .\data\wechat-bill.db（工作目录=start.bat 所在文件夹）。
请先关闭运行窗口再复制备份 wechat-bill.db 或整个 data 文件夹。


五、升级
-----
新版本通常只需替换 lib\bill-in-bill-backend.jar；升级前请先备份 data\。


六、JDK 便携版版权声明
---------------
若在发布的 zip 中捆绑 JDK，请遵循该 JDK 发行方（Temurin、Zulu、Microsoft Build of OpenJDK 等）的二进制再分发条款。
