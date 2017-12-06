package com.arno.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Arno on 2017/11/30.
 */

public class LeafLoadingView extends View {
    private static final String TAG = "AAAA";

    //叶子图片信息
    private Bitmap mLeafBitmap;
    private int mLeafWidth;
    private int mLeafHeight;

    //风扇图片信息
    private Bitmap mFanBitmap;
    private int mFanWidth;
    private int mFanHeight;

    //浅黄色
    private String COLOR_BG = "#fce48b";
    //橙色
    private String COLOR_PROGRESS = "#ffa800";

    // 外 - 高度
    private int mBgHeight;
    // 外 - 宽度
    private int mBgWidth;
    // 外 - 半圆半径
    private int mBgCircleRadio;
    // 外 - 矩形宽度
    private int mBgRectWidth;
    // 外内之间的空隙
    private int mOutBoundWidth;

    // 内 - 高度
    private int mBgProgressHeight;
    // 内 - 宽度
    private int mBgProgressWidth;
    // 内 - 半圆半径
    private int mBgCircleProgressRadio;
    // 内 - 矩形宽度
    private int mBgRectProgressWidth;

    // 叶子信息
    private List<Leaf> mLeafInfo;
    // 叶子默认运动周期
    private static final int LEAF_CYCLE_TIME = 1500;
    // 叶子默认旋转周期
    public static final int ROTATE_TIME = 1500;
    // 叶子运动周期
    private static int mCycleTime = LEAF_CYCLE_TIME;
    // 叶子旋转周期
    private int mRotateTime = ROTATE_TIME;
    // 叶子运动振幅 - 中
    private int mAmplitudeMid = 50;
    // 叶子振幅差
    private int mApmlitudeDiff = 20;
    // 叶子数量
    private int mLeafMax = 7;

    //风扇旋转周期
    private static final int FAN_CYCLE_TIME = 2000;
    private int mFanRotate;
    private int mFanCycleTime = FAN_CYCLE_TIME;

    private int mFanOutBoundWidth;

    //路径
    private Path mPath;

    //画笔
    private Paint mBasePaint;
    private Paint mBgPaint;
    private Paint mProgressPaint;
    private Paint mBitmapPaint;
    private Paint mFanBgPaint;
    private Paint mTextPaint;

    //进度
    private float mProgress;
    //设置新进度时的旧进度
    private float mOldProgress;
    //设置新进度时的时间
    private long mProgressSetTime;
    //进度过渡动画的绘制完成时间
    private float mIntervalDrawTime = 500;
    //进度完成时显示的文字
    private String mTextComplete = "100%";

    //progress 绘制录像
    private Picture mProgressPicture;


    public LeafLoadingView(Context context) {
        super(context);
        init(context);
    }

    public LeafLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        //初始化画笔
        initPaint();
        //获取叶子
        initLeafInfo();
        //初始化图片
        initBitmap(context);
        //初始化尺寸
        initDimens();
        //初始化路径
        initPath();
        //初始化录像
        initPicture();
    }

    private void initPaint() {
        if (mBasePaint == null) {
            mBasePaint = new Paint();
            mBasePaint.setAntiAlias(true);
            mBasePaint.setStyle(Paint.Style.FILL);
            mBasePaint.setStrokeCap(Paint.Cap.ROUND);
            mBasePaint.setStrokeJoin(Paint.Join.ROUND);
        }
        if (mBgPaint == null) {
            mBgPaint = new Paint(mBasePaint);
            mBgPaint.setColor(Color.parseColor(COLOR_BG));
        }
        if (mProgressPaint == null) {
            mProgressPaint = new Paint(mBasePaint);
            mProgressPaint.setColor(Color.parseColor(COLOR_PROGRESS));
        }
        if (mBitmapPaint == null) {
            mBitmapPaint = new Paint();
            mBitmapPaint.setAntiAlias(true);
            mBitmapPaint.setDither(true);
            mBitmapPaint.setFilterBitmap(true);
        }
        if (mFanBgPaint == null) {
            mFanBgPaint = new Paint(mBasePaint);
        }
        if (mTextPaint == null) {
            mTextPaint = new Paint(mBasePaint);
            mTextPaint.setTextSize(40);
            mTextPaint.setStrokeWidth(20f);
            mTextPaint.setColor(Color.WHITE);
        }
    }

    private void initLeafInfo() {
        if (mLeafInfo == null) {
            mLeafInfo = new LinkedList<>();
            mLeafInfo = LeafFactory.generateLeaves(1);
        }
    }

    private void initBitmap(Context context) {

        mLeafBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leaf);
        mLeafWidth = mLeafBitmap.getWidth();
        mLeafHeight = mLeafBitmap.getHeight();

        mFanBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fengshan);
        mFanWidth = mFanBitmap.getWidth();
        mFanHeight = mFanBitmap.getHeight();

    }

    private void initDimens() {
        mBgHeight = 200;
        mBgWidth = (int) (mBgHeight * 4.5f);
        mOutBoundWidth = 20;
        mFanOutBoundWidth = 10;

        mBgCircleRadio = mBgHeight / 2;
        mBgCircleProgressRadio = mBgCircleRadio - mOutBoundWidth;

        mBgRectWidth = mBgWidth - mBgCircleRadio;
        mBgRectProgressWidth = mBgRectWidth - mOutBoundWidth;

        mBgProgressHeight = mBgHeight - 2 * mOutBoundWidth;
        mBgProgressWidth = mBgWidth - 2 * mOutBoundWidth;

        mProgressSetTime = System.currentTimeMillis();
    }

    private void initPath() {
        mPath = new Path();
        RectF rectF = new RectF(mOutBoundWidth, mOutBoundWidth, 2 * mBgCircleProgressRadio + mOutBoundWidth, mBgHeight - mOutBoundWidth);
        mPath.addArc(rectF, 90, 180);
        mPath.lineTo(mBgWidth, mOutBoundWidth);
        mPath.lineTo(mBgWidth, mBgProgressHeight + mOutBoundWidth);
        mPath.lineTo(mBgCircleRadio, mBgProgressHeight + mOutBoundWidth);
    }

    private void initPicture() {
        if (mProgressPicture == null) {
            mProgressPicture = new Picture();
            Canvas canvas = mProgressPicture.beginRecording(mBgWidth, mBgHeight);
            canvas.save();
            canvas.drawPath(mPath, mProgressPaint);
            canvas.restore();
            mProgressPicture.endRecording();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawLoadingBg(canvas);

        drawLeafFly(canvas);

        drawLoadingProgress(canvas);

        drawFan(canvas);

        postInvalidate();
    }

    //绘制浅橙色背景
    private void drawLoadingBg(Canvas canvas) {
        //绘制圆
        canvas.drawCircle(mBgCircleRadio, mBgCircleRadio, mBgCircleRadio, mBgPaint);
        //绘制矩形
        canvas.drawRect(mBgCircleRadio, 0, mBgWidth, mBgHeight, mBgPaint);
        //绘制不规则图形
//        canvas.drawPath(mPath,mBgPaint);
    }

    private void drawLeafFly(Canvas canvas) {
        long currentT = System.currentTimeMillis();

        canvas.save();
        canvas.clipPath(mPath);
        canvas.translate(mBgWidth, mBgCircleRadio);
        for (int i = 0; i < mLeafInfo.size(); ) {
            Leaf leaf = mLeafInfo.get(i);
            if (currentT > leaf.startT && leaf.startT != 0) {
                canvas.save();
                generateLeafLocation(leaf);
                generateLeafRotation(leaf);

                Matrix matrix = new Matrix();
                matrix.postTranslate(leaf.x, leaf.y);
                matrix.postRotate(leaf.rotateAngle, leaf.x + mLeafWidth / 2, leaf.y + mLeafHeight / 2);

                canvas.drawBitmap(mLeafBitmap, matrix, mBitmapPaint);
                canvas.restore();

            }

            //移除已经飞到末端的树叶
            if (isFlyAway(leaf)) {
                mLeafInfo.remove(i);
                if (mLeafInfo.size() == 0) {//保持至少有一片树叶
                    Log.d(TAG, "drawLeafFly: addLeaf");
                    mLeafInfo.add(LeafFactory.generateLeaf());
                }
            } else {
                i++;
            }
        }
        canvas.restore();
    }

    //绘制橙色滚动条
    private void drawLoadingProgress(Canvas canvas) {
        int width = generateProgressWidth();
        PictureDrawable proPd = new PictureDrawable(mProgressPicture);
        proPd.setBounds(0, 0, width, mBgHeight);
        proPd.draw(canvas);
    }

    //绘制风扇
    private void drawFan(Canvas canvas) {
        canvas.save();
        canvas.translate(mBgWidth, mBgCircleRadio);
        //白圆
        mFanBgPaint.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, mBgCircleRadio, mFanBgPaint);

        //橙圆
        mFanBgPaint.setColor(Color.parseColor(COLOR_PROGRESS));
        canvas.drawCircle(0, 0, mBgCircleRadio - mFanOutBoundWidth, mFanBgPaint);

        if (generateProgressWidth() >= mBgWidth) {//完成文字
            //获取文字尺寸
            Rect rect = new Rect();
            mTextPaint.getTextBounds(mTextComplete, -0, mTextComplete.length(), rect);
            canvas.drawText(mTextComplete, -(rect.right - rect.left) / 2, (rect.bottom - rect.top) / 2, mTextPaint);
        } else {//风扇
            //计算旋转角度
            mFanRotate = (int) (System.currentTimeMillis() % mFanCycleTime / (float) mFanCycleTime * 360);
            canvas.rotate(mFanRotate, 0, 0);
            //圆和风扇间留空位置 == 2
            int dx = mBgCircleProgressRadio - 2;
            canvas.translate(-dx, 0);

            //缩放画布使得风扇中心可以绘制在圆心上
            canvas.scale((float) (dx * 2) / (float) mFanWidth, (float) (dx * 2) / (float) mFanHeight);
            canvas.drawBitmap(mFanBitmap, 0, -mFanHeight / 2, mBitmapPaint);
        }

        canvas.restore();
    }

    //根据属性计算应绘制的进度条长度
    private int generateProgressWidth() {
        int result = 0;
        long deltaT = System.currentTimeMillis() - mProgressSetTime;

        if (deltaT > 0 && mIntervalDrawTime > deltaT) {
            float deltaWidth = (mProgress - mOldProgress) / mIntervalDrawTime * deltaT;
            result = (int) ((mOldProgress + deltaWidth) / 100f * mBgWidth);
        } else {
            result = (int) (mProgress / 100f * mBgWidth);
        }
        return result;
    }

    //计算树叶旋转角度
    private void generateLeafRotation(Leaf leaf) {
        long intervalTime = System.currentTimeMillis() - leaf.startT;

        if (intervalTime < 0) {
            return;
        }
//        else if (intervalTime > leaf.cycleTime) {
//            leaf.startT = System.currentTimeMillis()
//                    + new Random().nextInt(leaf.cycleTime);
//        }

//        float fraction = intervalTime % mRotateTime / (float) mRotateTime;
        float fraction = intervalTime % leaf.rotateCycle / (float) leaf.rotateCycle;
        float angle = fraction * 360;
        leaf.rotateAngle = (int) (leaf.rotateInit + leaf.rotateDirection * angle);

    }

    //计算树叶位置
    private void generateLeafLocation(Leaf leaf) {
        long intervalTime = System.currentTimeMillis() - leaf.startT;

        if (intervalTime < 0) {
            return;
        }
//        else if (intervalTime > leaf.cycleTime) {
//            leaf.startT = System.currentTimeMillis()
//                    + new Random().nextInt(leaf.cycleTime);
//        }

        float fraction = (float) intervalTime % leaf.cycleTime / leaf.cycleTime;
        leaf.x = (int) (-mBgWidth * fraction);
        leaf.y = calLocationY(leaf);
    }

    private boolean isFlyAway(Leaf leaf) {
        boolean result = false;
        long intervalTime = System.currentTimeMillis() - leaf.startT;

        if (intervalTime < 0 || leaf.startT == 0) {
            result = false;
        } else if (intervalTime > leaf.cycleTime) {
            result = true;
        }

        return result;
    }

    private int calLocationY(Leaf leaf) {
        // y = A Sin(w * x + Q) + k
        int A = leaf.amplitudeType * mApmlitudeDiff + mAmplitudeMid;
        double w = ((Math.PI * 2) / (mBgProgressWidth));
        return (int) (A * Math.sin(w * leaf.x + leaf.Q));
    }

    private static class Leaf {
        //y = A Sin(w * x + Q) + k

        // 叶子振幅 计算公式为：AmplitudeType(类型) * ApmlitudeDiff(振幅差值) + 默认值
        private static final int A_L = -1;
        private static final int A_M = 0;
        private static final int A_H = 1;

        // 旋转方向
        private static final int Rotate_D_ZH = -1;
        private static final int Rotate_D_F = 1;

        //振幅类型
        private int amplitudeType;
        //周期
        private int cycleTime;
        //位置
        private int x, y;
        //初始相位
        private float Q;

        //开始旋转的时间
        private long startT;
        //旋转初始角度
        private int rotateInit;
        //旋转角度
        private int rotateAngle;
        //旋转方向
        private int rotateDirection;
        //旋转周期
        private int rotateCycle;

    }

    private static class LeafFactory {
        private static final int DEFAULT_SIZE = 7;

        private static Leaf generateLeaf() {
            Random random = new Random();
            Leaf leaf = new Leaf();

            //随机值使叶子在产生时有先后顺序
            long addTime = random.nextInt(LEAF_CYCLE_TIME / 2);
            leaf.startT = System.currentTimeMillis() + addTime;
            //初始旋转角度
            leaf.rotateInit = random.nextInt(360);
            //随机初始旋转方向
            leaf.rotateDirection = (int) Math.pow(-1, random.nextInt(1));
            //随机振幅
            leaf.amplitudeType = random.nextInt(2) - 1;
            //随机周期
            leaf.cycleTime = random.nextInt(1500) + LEAF_CYCLE_TIME;
            //随机相位
            leaf.Q = (float) (Math.PI * random.nextInt(3) / 6);
            //随机旋转周期
            leaf.rotateCycle = random.nextInt(1000) + ROTATE_TIME;

            return leaf;
        }

        private static List<Leaf> generateLeaves(int size) {
            List<Leaf> leaves = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                Leaf leaf = generateLeaf();
                leaves.add(leaf);
            }
            return leaves;
        }

        private static List<Leaf> generateLeaves() {
            return generateLeaves(DEFAULT_SIZE);
        }
    }

    public void setProgress(float progress) {
        if (System.currentTimeMillis() - mProgressSetTime > mIntervalDrawTime
                || progress >= 100) {
            //保存旧进度
            this.mOldProgress = this.mProgress;
            //保存新进度
            this.mProgress = progress;
            //保存设置时间
            this.mProgressSetTime = System.currentTimeMillis();
            //添加叶子
            addLeaf();
            Log.i(TAG, "setProgress: delta=" + (mProgress - mOldProgress));
            postInvalidate();
        }
    }

    private void addLeaf() {
        float deltaProgress = mProgress - mOldProgress;
        if (deltaProgress > 0 && mLeafMax > mLeafInfo.size()) {
            int addNum = 1;

            if (8 > deltaProgress && deltaProgress > 5) {
                addNum = 2;
            } else if (deltaProgress > 8) {
                addNum = 3;
            }

            if (addNum > (mLeafMax - mLeafInfo.size())) {
                addNum = mLeafMax - mLeafInfo.size();
            }

            mLeafInfo.addAll(LeafFactory.generateLeaves(addNum));

        }
    }


}
