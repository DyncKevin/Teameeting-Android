# Teameeting
Submitted for the first time.Implement initialization, create a conference room, the overall framework design.

# 提交记录
##### 2015年12月16日 
1. 增加极光推送 （自己验证）
2. 修改`listView`移动动画
3. `splash`引导页动画

##### 2015年12月17日20:44:42
1. 增加极光推送 （服务器验证成功）
2. `NetWork`代码抽取封装
3. 修改验证 `post` 共计`20`条请求封装。(服务器验证成功)


##### 2015年12月20日17:39:38

######增加
1. `SweetAlertDialogLibrary`更友好的提示动画
2. 自定义`RoomControls`控件

######改进
1. 创建房间`ListViwe`滚动动画冲突
2. 聊天界面动画以及配色
3. 通话页面**横屏切换**隐藏按钮无法显示问题。（`GlSufaceView`重绘图层覆盖）
4. `Anmis`动画完善。

##### 2015年12月21日20:40:29

###### 增加
1. 视频界面事实消息 **弹幕**功能；(**原创**)
 

###### 优化
1. 实时聊天界面`bug`
2. 优化页面跳转动画


#####2015年12月22日20:52:09
###### 添加
1. `短信` `微信`分享加入会议连接。
2. 网络异常处理
3. 会议提醒控制控件

##### 修改
1. `复制` `网络异常`提示对话框动画（第三方）

##### 2015年12月24日17:10:29
1. 平板侧滑聊天菜单以及动画
2. 不同界面平板适配 

##### 2015年12月26日11:15:45
1. 修改进程被强制杀手，获取列表为空的问题。
2. 是否要保存离线列表
3. 修改一些平板的适配的问题。
4. 出现一个非常奇怪的问题。AppContion、
5. 重构了第一次代码，出现许多问题。比如第一次进入缓慢的。异常杀死进程，在次进入为空。
希望明天能够优化

##### 2015年12月28日20:17:43 
###### 修复
1. 用户强杀进程，获取列表为空（原因`Application`)会重新创建
2. 用户获取列表不用`EvenBus`保存在`Application`中传递容易丢失

##### 添加
1. 修改网络访问框架为 `async-http`,json解析为`GSON`。
2. `MVC`架构包的设计初步形成。

##### 2015年12月29日16:18:42
 
###### 修复
1.设置推送状态 主界面没更新
2.访问网络的框架修改完成。


``` python
@requires_authorization
def somefunc(param1='', param2=0):
    '''A docstring'''
    if param1 > param2: # interesting
        print 'Greater'
    return (param2 - param1 + 1) or None
class SomeClass:
    pass
>>> message = '''interpreter
... prompt'''
```
