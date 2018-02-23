### 安卓安装包（.apk文件）编译指南
1.  注册一个[百度语音](http://yuyin.baidu.com)帐户，并创建一个语音合成项目，得到一个分配的Key。

2.  创建`/app/src/main/res/values/secrets.xml`文件，把一上步分配的Key填入：  
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="APP_ID">10066666</string>
    <string name="API_KEY">CwfSlkdicAsSDfDScsewLpfx</string>
    <string name="SECRET_KEY">32cde97ab8232ffed93ac11ef</string>
</resources>
```

3.  创建`/app/src/main/libs/`目录，把SDK的jar文件放入其中。这里可以改一下文件名，比如改成`com.baidu.tts_2.3.1.jar`。

4.  创建`/app/src/main/assets/`目录，把SDK的以下5个数据文件放入其中，不要改文件名：  
`bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat`  
`bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat`  
`bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat`  
`bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat`  
`bd_etts_text.dat`

5.  创建`/app/jni`目录，把SDK的二进制程序目录放入其中：  
`armeabi-v7a`  
`arm64-v8a`  
（通常放这两个就可以了，如果需要在电脑上调试可以再放一个`x86`）

### 服务器端、PC端
下载[此项目](https://github.com/animalize/ting_py)

服务器端：把server目录放到24小时运行的服务器上运行。（需要Python 3.x和安装tornado模块）

PC端：给电脑安装Python 3.6并安装pyperclip、requests模块。把`/pc/vars.py`文件里的`host`变量改成服务器地址。
