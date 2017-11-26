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

3.  创建`/app/src/main/libs/`目录，把SDK的`com.baidu.tts_2.3.0.jar`文件放入其中。

4.  创建`/app/src/main/assets/`目录，把SDK的以下3个数据文件放入其中：  
`bd_etts_text.dat`  
`bd_etts_speech_male.dat`  
`bd_etts_speech_female.dat`

5.  创建`\app\jni`目录，把SDK的二进制程序目录放入其中：  
`armeabi-v7a`  
`arm64-v8a`  
通常放这两个就可以了
