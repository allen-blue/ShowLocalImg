
# Library-lis(仿微信本地图片选择)

标签（空格分隔）：api

---

1.简介
 library-lis是仿照微信本地图片选择的效果实现的一个库。本库只是实现了图片显示部分，topBar（开发者自己实现），底部的菜单涉及到具体的样式效果，交给用户自己实现，或者使用包中默认效果。
 ---
2.使用说明 

* library-lis使用很简单，你可以使用默认的`DefaultListImgFragment`快速集成到自己的activity中，并实现`BaseListImgFragment.OnResultListener`接口（获取选择中的图片集合）。
*  如果你想定义自己风格的`BottomView`，library-lis也是支持的，你只需要参照`DefaultListImgFragment`定义自己的`fragment继承BaseListImgFragment`并实现如下方法即可。

```java
//获取底部布局的id
public abstract int getBottomLayoutId();
//初始化底部布局
public abstract void initChildView(View view, Bundle   savedInstanceState);
/**
返回选择的文件夹的名字，（用于切换文件夹）
*/
public abstract void onBucketSelect(String bucketName);
```

* `BaseListImgFragment` 也提供了一些方法供开发者使用。

```java
/**
    判断切换文件夹的对话框是否显示
    @return true:显示，false:隐藏
*/
 public boolean isPopShow();
 /**
    设置最多选择的图片数量，默认为9(当maxSize<=1时，不会显示复选框)
   @params maxSize 应大于等于1
 */
  public void setMaxSize(int maxSize)
  /**
    设置是否支持图片预览
    @params flag true:支持
    备注：图片预览功能还未添加（后期会加入）
  */
   public void setIsPreView(boolean flag)
   //显示文件夹切换对话框
   public void showPop()
    //隐藏文件夹切换对话框
    public void hidePop()
```

[DemoActivity]


 





