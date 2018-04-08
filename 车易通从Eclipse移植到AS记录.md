## 车易通从Eclipse移植到AS记录

说明：由于前面已经有了移植经验，所以想将2023实验室中的车易通项目（比以前移植的项目要复杂很多）也从Eclipse移植到AS，提高开发效率。以前移植经验总结如下：

#### 0. IntelligentParkingAssistant主要是更新了BaiduMap的LBS类型的API（LBS选择中添加了导航和定位的API）

#### 1. HJSystem(从Eclipse的mountainRestore移植到AS的HJSystem);主要涉及BaiduMap的Libs
C:\Users\pengpeng\Documents\eclipse\mountainRestore
C:\Users\pengpeng\AndroidStudioProjects\HJSystem

#### 2. TireDetectionBTClient(从Eclipse的BluetoothClient移植到AS的TireDetectionBTClient);主要涉及科大讯飞的Libs（并更新了代码中的用法）
C:\Users\pengpeng\Documents\eclipse\BluetoothClient
C:\Users\pengpeng\AndroidStudioProjects\TireDetectionBTClient

#### 3. TireDetectionBTServer(从Eclipse的BluetoothServer移植到AS的TireDetectionBTServer)
C:\Users\pengpeng\Documents\eclipse\BluetoothServer
C:\Users\pengpeng\AndroidStudioProjects\TireDetectionBTServer

##移植记录：
1. 首先参考AS中IntelligentParkingAssistant的文件结构，将C:\Users\pengpeng\Documents\eclipse\CarEasyGo-Latest中的CarEasyGo-Latest工程文件夹打开，并在AS中新建一个项目CarEasyGo_v3（包名、APP名尽量和原来相同）

2. 将CarEasyGo-Latest中的libs中的*.jar拷贝到CarEasyGo_v3中的libs文件夹下，并在AS中的File->Project Structure(Ctrl+Shift+alt+S)中添加相应jar包的依赖

*CarEasyGo_v3中的libs文件夹:C:\Users\pengpeng\AndroidStudioProjects\CarEasyGo_v3\app\libs*

3. 将CarEasyGo-Latest中的libs中的*.so动态库（包括文件夹）拷贝到CarEasyGo_v3中的jniLibs文件夹下

*CarEasyGo_v3中的jniLibs文件夹:C:\Users\pengpeng\AndroidStudioProjects\CarEasyGo_v3\app\src\main\jniLibs*

4. 由于车易通项目中BaiduMap中的API比较老，就没有assets文件夹，所以不需要拷贝assets文件夹的内容，只需要在C:\Users\pengpeng\AndroidStudioProjects\CarEasyGo_v3\app\src\main中新建一个assets文件夹即可。

5. 将对应的java文件拷贝到新的工程CarEasyGo_v3中（参考AS中IntelligentParkingAssistant的文件结构），原来的文件可替换掉

6. 将res文件夹中的内容拷贝到新的工程CarEasyGo_v3中（参考AS中IntelligentParkingAssistant的文件结构），原来的文件可替换掉

7. 替换AndroidManifest文件，并将AndroidManifest文件在AS打开，会发现有些错误，错误原因是AndroidManifest文件中有很多声明的Activity以及server，在工程文件中已经删除，可以将错误的声明先注释掉

8. 根据Eclipse中车易通项目的Android版本修改新工程中Module的build.gradle文件中的版本号            	
    compileSdkVersion 23

    buildToolsVersion '25.0.0'

    minSdkVersion 14

    targetSdkVersion 26

然后修改

    compile 'com.android.support:appcompat-v7:23.+'

9. 在编译的时候会报APPT：adapterXXX的错误，先

    在app的  build.gradle里添加以下两句：
    
    
    defaultConfig中添加
    aaptOptions.cruncherEnabled = false
    aaptOptions.useNewCruncher = false
    
    
    这个可以关闭Android Studio的PNG合法性检查

资料：https://blog.csdn.net/gaobaoshen1/article/details/51395433

10. 然后会提示一些APPT：.9.Patch文件错误，原来是车易通工程中的9Patch图片制作不符合规范，在AS中编译不通过，首先将drawable文件夹中的图片文件全部检查一遍（文件夹内查看），看看有没有不能正常显示的图片。然后在AS中一一将9Patch文件打开，查看制作是否规范。

11. 经过上述步骤，调试通过，程序可以烧写到魅族手机中。

## 下一步：更新程序中的API包，并重写代码中的过时方法。