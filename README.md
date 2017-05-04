个人练习
    由于svg动画的原因，目前只支持4.1-4.4。模拟器测试。
[基础配置]

file gradle/wrapper/gradle-wrapper.properties

    distributionUrl=https\://services.gradle.org/distributions/gradle-3.3-all.zip

file app/build.gradle

    android {
        compileSdkVersion 25
        buildToolsVersion "25.0.2"
    }
    dependencies {
        compile 'com.android.support:appcompat-v7:25.2.0'
    }

高德地图
* 展示地图
* 自定义定位按钮
* 动画平移到定位座标
    
UI控件
* svg动画
* 自定义头部工具栏
* 侧滑
* 打开关闭窗口

功能测试
* 打开关闭定时时钟以及播放 #本来是播放音乐。但由于非本机测试，路径不同，所以不会有音乐。
* 文件读取展示
* 离开
* 系统的浮在底部的提示
* 系统的底部提示以及交互
* 读取json网站的数据显示
