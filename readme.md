### Readme

LeafLoadingView 用于练习自定义 View 学习的基础部分，其中包括：
-  [自定义 View - 基础]()
-  [自定义 View - Canvas - 图形绘制]()
-  [自定义 View 实践（一）- 自定义圆点视图]()
-  [自定义 View - Canvas - 画布操作和快照]()
-  [自定义 View 实践（二）- 简易时钟]()
-  [自定义 View - Canvas - 绘制图片]()

---

效果图：
![GIF.gif](GIF.gif)


参考了 [FROM  GA_studio](http://blog.csdn.net/tianjian4592/article/details/44538605) 的效果，主要做了以下优化：
- 改进进度条的绘制，不再计算弧形
- 增加了树叶旋转周期等控制
- 增加了树叶添加数量随进度增加变化，不再重复绘制
- 增加了完成时的动画

以上。

工程还未完成，未完成的部分包括但不限于：
- 接口暴露
- 自定义属性控制
- 内部尺寸比例控制
- view 测量
- 其他
