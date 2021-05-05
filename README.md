## Ting

Android的文本朗读器。

1. 有分页、进度记忆功能，长短篇皆宜。
2. 使用百度语音合成服务，可在线、离线使用，提供较全面的控制选项。

[点击这里](https://github.com/animalize/Ting_Android/releases)下载编译好的安卓安装包，需要Android 4.0+。
（安装后首次运行会自动退出，重新运行即可。）

🅰 不要在开车、马路行走时听，会分散注意力，很危险。
🅱 语音合成时会把文本发往百度服务器，因此有泄漏隐私、机密的风险。

### 1.截图

<table>
<tr>
<td>列表</td><td>查看文本</td><td>分页跳转</td><td>设置</td>
</tr>
<tr>
<td><img src="https://github.com/animalize/pics/raw/master/Ting/a.png" /></td>
<td><img src="https://github.com/animalize/pics/raw/master/Ting/b.png" /></td>
<td><img src="https://github.com/animalize/pics/raw/master/Ting/c.png" /></td>
<td><img src="https://github.com/animalize/pics/raw/master/Ting/d.png" /></td>
</tr>
</table>

### 2.配置电脑端程序

在添加文章、安卓端进行“刷新列表”操作时，需要开启电脑端程序。

配置电脑端的方法：
https://github.com/animalize/ting_py/releases/

<img width="460" src="https://raw.githubusercontent.com/animalize/pics/master/Ting/pc.PNG" />

在使用天涯、贴吧、百度百科、FT中文网等网站时，复制帖子/文章网址，然后点击`调用tz2txt`会自动处理。

🔸🔸🔸🔸普通用户不必再看后面的内容🔸🔸🔸🔸

### 3.分别配置服务器端、PC端

如果有24小时运行的服务器，可以部署服务器端程序，方法如下：

下载[ting_py项目](https://github.com/animalize/ting_py)，包括了服务器端和PC端。

#### 服务器端
把server目录放到24小时运行的服务器上，运行`/server/web_server.py`，需要Python 3.x和安装`tornado`模块。
（如果没有服务器，也可以在普通电脑上运行，程序随用随开。）

架设好服务器后，在Ting的设置界面填入服务器地址。

#### PC端
pc目录为PC端程序，给电脑安装Python 3.x并安装`pyperclip`、`requests`模块。

把`/pc/vars.py`文件里的`host`变量改成服务器地址。
如果想使用[tz2txt](https://github.com/animalize/tz2txt)，把`/pc/vars.py`文件里的`tz2txt_path`变量改成`tz2txt.py`文件的路径。

双击`pc_side.py`启动PC端程序。

### 4.apk文件编译指南

1.  注册[百度语音](http://yuyin.baidu.com)帐户。
创建一个语音合成项目（包名填入`com.github.animalize.ting`），得到一个分配的Key。

2.  创建`/app/src/main/res/values/secrets.xml`文件(内容如下)，把一上步分配的Key填入。
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="APP_ID">10066666</string>
    <string name="API_KEY">CwfSlkdicAsSDfDScsewLpfx</string>
    <string name="SECRET_KEY">32cde97ab8232ffed93ac11ef</string>
</resources>
```

3.  创建`/app/src/main/libs/`目录，把SDK的jar文件放入其中。

4.  创建`/app/jni`目录，把SDK的二进制程序目录放入其中：
`armeabi-v7a`
`arm64-v8a`
（通常放这两个就可以了，如果需要在电脑上调试可以再放一个`x86`）
