# CircleMenu

整理一下前段时间改的一个View

# 一、原版仓库
[github上的Android-Wheel-Menu](https://github.com/anupcowkur/Android-Wheel-Menu)

感谢大佬5555555

readme中使用方法和预览都很详尽，可以先看看~

---
# 二、预览效果和简单说明
预览效果如下：

![](https://img-blog.csdnimg.cn/c58a9d6603e743509d97acbc6f887327.gif#pic_center)

说明有以下特点：
1. 转盘转到指定位置（预览为最上方变色区域）的select监听；
2. 只能点击指定区域（预览为最上方变色区域）的click监听，点击其余区域不响应；
3. 可以随时更换转盘图片，一般在select监听事件中使用；
4. 无强烈的物理惯性；
5. 需要每个区域被select的切图，对应代码中区域位置是逆时针方向旋转，如下图。

![](https://img-blog.csdnimg.cn/74792c17570041aa8f11754e2fd2d71e.png#pic_center)


---
# 三、改后代码
**CircleMenu.class**

```java
package com.myx.circlemenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class CircleMenu extends AppCompatImageView {

    private Bitmap imageOriginal, imageScaled;     //variables for original and re-sized image
    private Matrix matrix;                         //Matrix used to perform rotations
    private int circleHeight, circleWidth;           //height and width of the view
    private int top;                               //the current top of the circle (calculated in
    // circle divs)
    private double totalRotation;                  //variable that counts the total rotation
    // during a given rotation of the circle by the user (from ACTION_DOWN to ACTION_UP)
    private int divCount;                          //no of divisions in the circle
    private int divAngle;                          //angle of each division
    private int selectedPosition;                  //the section currently selected by the user.
    private boolean snapToCenterFlag = true;       //variable that determines whether to snap the
    // circle to the center of a div or not
    private Context context;
    private CircleChangeListener circleChangeListener;

    public CircleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //initializations
    private void init(Context context) {
        this.context = context;
        this.setScaleType(ScaleType.MATRIX);
        selectedPosition = 0;

        // initialize the matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            matrix.reset();
        }

        //touch events listener
        this.setOnTouchListener(new CircleTouchListener());
    }

    /**
     * Add a new listener to observe user selection changes.
     *
     * @param circleChangeListener
     */
    public void setCircleChangeListener(CircleChangeListener circleChangeListener) {
        this.circleChangeListener = circleChangeListener;
    }

    /**
     * Returns the position currently selected by the user.
     *
     * @return the currently selected position between 1 and divCount.
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Set no of divisions in the circle menu.
     *
     * @param divCount no of divisions.
     */
    public void setDivCount(int divCount) {
        this.divCount = divCount;

        divAngle = 360 / divCount;
        totalRotation = -1 * (divAngle / 2);
    }

    /**
     * Set the snap to center flag. If true, circle will always snap to center of current section.
     *
     * @param snapToCenterFlag
     */
    public void setSnapToCenterFlag(boolean snapToCenterFlag) {
        this.snapToCenterFlag = snapToCenterFlag;
    }

    /**
     * Set a different top position. Default top position is 0.
     * Should be set after {#setDivCount(int) setDivCount} method and the value should be greater
     * than 0 and lesser
     * than divCount, otherwise the provided value will be ignored.
     *
     * @param newTopDiv
     */
    public void setAlternateTopDiv(int newTopDiv) {

        if (newTopDiv < 0 || newTopDiv >= divCount)
            return;
        else
            top = newTopDiv;

        selectedPosition = top;
    }

    /**
     * Set the circle image.
     *
     * @param drawableId the id of the drawable to be used as the circle image.
     */
    public void setCircleImage(int drawableId) {
        imageOriginal = BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    /**
     * Modify the circle image
     * @param drawableId
     */
    public void modifyCircleImage(int drawableId){
        imageOriginal = BitmapFactory.decodeResource(context.getResources(), drawableId);
        Matrix resize = new Matrix();
        resize.postScale((float) Math.min(circleWidth, circleHeight) / (float) imageOriginal
                .getWidth(), (float) Math.min(circleWidth,
                circleHeight) / (float) imageOriginal.getHeight());
        imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(),
                imageOriginal.getHeight(), resize, false);
        // translate the matrix to the image view's center
        float translateX = circleWidth / 2 - imageScaled.getWidth() / 2;
        float translateY = circleHeight / 2 - imageScaled.getHeight() / 2;
        matrix.postTranslate(translateX, translateY);
        CircleMenu.this.setImageBitmap(imageScaled);
        CircleMenu.this.setImageMatrix(matrix);
    }
    /*
     * We need this to get the dimensions of the view. Once we get those,
     * We can scale the image to make sure it's proper,
     * Initialize the matrix and align it with the views center.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // method called multiple times but initialized just once
        if (circleHeight == 0 || circleWidth == 0) {
            circleHeight = h;
            circleWidth = w;
            Log.i("menu", "hei="+h+", wid="+w+")");
            Log.i("menu", "gethei="+this.getHeight()+", getwid="+this.getWidth()+")");
            // resize the image
            Matrix resize = new Matrix();
            resize.postScale((float) Math.min(circleWidth, circleHeight) / (float) imageOriginal
                    .getWidth(), (float) Math.min(circleWidth,
                    circleHeight) / (float) imageOriginal.getHeight());
            imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(),
                    imageOriginal.getHeight(), resize, false);
            // translate the matrix to the image view's center
            float translateX = circleWidth / 2 - imageScaled.getWidth() / 2;
            float translateY = circleHeight / 2 - imageScaled.getHeight() / 2;
            matrix.postTranslate(translateX, translateY);
            CircleMenu.this.setImageBitmap(imageScaled);
            CircleMenu.this.setImageMatrix(matrix);
        }
    }

    /**
     * get the angle of a touch event.
     */
    private double getAngle(double x, double y) {
        x = x - (circleWidth / 2d);
        y = circleHeight - y - (circleHeight / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    /**
     * get the quadrant of the circle which contains the touch point (x,y)
     *
     * @return quadrant 1,2,3 or 4
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    /**
     * rotate the circle by the given angle
     *
     * @param degrees
     */
    private void rotateCircle(float degrees) {
        matrix.postRotate(degrees, circleWidth / 2, circleHeight / 2);
        CircleMenu.this.setImageMatrix(matrix);

        //add the rotation to the total rotation
        totalRotation = totalRotation + degrees;
    }

    /**
     * check one point in the top region of circle
     * @param x
     * @param y
     * @return
     */
    private boolean checkInRegion(double x, double y){
        // because the view = circle, radius = circleHeight/2 = circleWidth/2
        x = x - circleWidth / 2;
        y = circleHeight / 2 - y;
        Log.i("menu", "checkInRegion: ("+x+", "+y+")");
        double everyDivAngle = 360 / divCount;   //every div's angle
        double maxAbsX = Math.abs(y) * Math.tan(Math.toRadians(everyDivAngle / 2));
        Log.i("menu", "checkInRegion: maxAbsX="+maxAbsX);
        if(Math.abs(y) > circleHeight / 2 || Math.abs(x) > maxAbsX || (x < 0 && y < 0)) return false;
        return true;
    }

    /**
     * Interface to to observe user selection changes.
     */
    public interface CircleChangeListener {
        /**
         * Called when user selects a new position in the circle menu.
         *
         * @param selectedPosition the new position selected.
         */
        public void onSelectionChange(int selectedPosition);
        /**
         * Called when user clicks a new position in the circle menu.
         *
         * @param clickedPosition the new position selected.
         */
        public void onClickedChange(int clickedPosition);
    }

    //listener for touch events on the circle
    private class CircleTouchListener implements OnTouchListener {
        private double startAngle, lastX, lastY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //get the start angle for the current move event
                    startAngle = getAngle(event.getX(), event.getY());
                    lastX = event.getX();
                    lastY = event.getY();
                    Log.e("menu", "ACTION_DOWN"+event.getX()+", "+event.getY());
                    break;


                case MotionEvent.ACTION_MOVE:
                    //get the current angle for the current move event
                    double currentAngle = getAngle(event.getX(), event.getY());

                    //rotate the circle by the difference
                    rotateCircle((float) (startAngle - currentAngle));

                    //current angle becomes start angle for the next motion
                    startAngle = currentAngle;
                    break;


                case MotionEvent.ACTION_UP:
                    Log.e("menu", "ACTION_UP"+event.getX()+", "+event.getY());
                    //get the total angle rotated in 360 degrees
                    totalRotation = totalRotation % 360;

                    //represent total rotation in positive value
                    if (totalRotation < 0) {
                        totalRotation = 360 + totalRotation;
                    }

                    //calculate the no of divs the rotation has crossed
                    int no_of_divs_crossed = (int) ((totalRotation) / divAngle);

                    //calculate current top
                    top = (divCount + top - no_of_divs_crossed) % divCount;

                    //for next rotation, the initial total rotation will be the no of degrees
                    // inside the current top
                    totalRotation = totalRotation % divAngle;

                    //snapping to the top's center
                    if (snapToCenterFlag) {

                        //calculate the angle to be rotated to reach the top's center.
                        double leftover = divAngle / 2 - totalRotation;

                        rotateCircle((float) (leftover));

                        //re-initialize total rotation
                        totalRotation = divAngle / 2;
                    }

                    //set the currently selected option
                    if (top == 0) {
                        selectedPosition = divCount - 1;//loop around the array
                    } else {
                        selectedPosition = top - 1;
                    }

                    if (circleChangeListener != null) {
                        if(lastX == event.getX() && lastY == event.getY() && checkInRegion(event.getX(), event.getY())) {
                            circleChangeListener.onClickedChange(selectedPosition);
                        }else {
                            circleChangeListener.onSelectionChange(selectedPosition);
                        }
                    }

                    break;
            }

            return true;
        }
    }

}

```

---
# 四、使用方法和demo
0. github仓库
[CircleMenu](https://github.com/Mayer-myx/CircleMenu)

1. 放在项目目录下

![](https://img-blog.csdnimg.cn/75c5616313ab4244a28d931b775ffc66.png#pic_center)

3. 在对应布局文件xml中使用

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <ImageView
        android:layout_width="273dp"
        android:layout_height="273dp"
        android:src="@mipmap/background"
        android:layout_centerInParent="true"/>

    <com.myx.circlemenu.CircleMenu
        android:id="@+id/main_cm"
        android:layout_width="270dp"
        android:layout_height="270dp"
        android:layout_centerInParent="true"/>
</RelativeLayout>
```

4. java代码中初始化。必须先setDivCount、setCircleImage，否则会抛出NullPointerException。

```java
        main_cm = findViewById(R.id.main_cm);
        main_cm.setDivCount(4);
        main_cm.setCircleImage(R.mipmap.menu_0);
        main_cm.setCircleChangeListener(new CircleMenu.CircleChangeListener() {
            @Override
            public void onSelectionChange(int selectedPosition) {
                //main_tv_select.setText("select="+selectedPosition);
                switch (selectedPosition){
                    case 0:
                        main_cm.modifyCircleImage(R.mipmap.menu_0);
                        break;
                    case 1:
                        main_cm.modifyCircleImage(R.mipmap.menu_1);
                        break;
                    case 2:
                        main_cm.modifyCircleImage(R.mipmap.menu_2);
                        break;
                    case 3:
                        main_cm.modifyCircleImage(R.mipmap.menu_3);
                        break;
                }
            }

            @Override
            public void onClickedChange(int clickedPosition) {
                //main_tv_click.setText("click="+clickedPosition);
                switch (clickedPosition){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
            }
        });
```
