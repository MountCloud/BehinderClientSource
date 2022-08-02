# 冰蝎客户端源码
首先致敬作者rebeyond：https://github.com/rebeyond/Behinder/releases

# 本git更新内容【非官方内容】
```
2021-08-02 V4.0.2 官方原版逆向，请支持原版作者rebeyond。
2022-07-31 V4.0.1 官方原版逆向，请支持原版作者rebeyond。
2021-05-20 V3.0 Beta11_t00ls 官方原版逆向，请支持原版作者rebeyond。
2021-04-25 V3.0 Beta10 官方原版逆向，本git注入内存马加了是否选择网站的非空判断。
2021-04-22 V3.0 Beta9 fixed 官方原版逆向未做修改
2021-04-19 V3.0 Beta9 打包去掉javafx依赖，所以使用jre或者jdk8运行，jre或者jdk11运行需要安装javafx
2021-04-11 V3.0 Beta7 并且修复原版虚拟终端无法输入命令问题
```

# 郑重声明
拿刀的不一定是屠夫，也有可能是伙夫，当然也有可能是大夫，本代码仅供学习，请保证必须一定勿用于非法用途！！！

# 说明
1.本程序是逆向作者源码重新构建开发工程得来的，所以代码可能阅读起来不是特别美丽，但是给了我们无尽的可能，再次致敬作者rebeyond。

2.程序重新构建为maven工程，因为个人习惯了maven。

3.server目录下是服务端小马程序。

# 二开说明
```
1：JDK，从4.0开始，本源码使用JDK11进行的逆向工程。
2：JavaFx，本次使用的是javafx-sdk-17.0.2，根据需求在官网下载：https://gluonhq.com/products/javafx/
3：因为是jdk11，所以Jvm启动参数需要加上JavaFx：
--module-path "你javafx的lib文件夹" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web
例如：
--module-path "C:\tool\javafx-sdk-17.0.2\lib" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web
如果爆数据库丢失错误，把data.db复制到项目根路径。
```

# 分支
本git的分支对标原版程序版本号。

# 经验交流
http://mountcloud.org

mountcloud@outlook.com

渗透&测试&情报QQGroup:337571436
