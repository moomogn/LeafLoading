package com.arno.support;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.text.TextUtils;
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
    private static final String TAG = "LeafLoading";

    //叶子图片信息
    private Bitmap mLeafBitmap;
    private int mLeafWidth;
    private int mLeafHeight;

    //风扇图片信息
    private Bitmap mFanBitmap;
    private int mFanWidth;
    private int mFanHeight;

    //浅黄色
    private String DEFAULT_COLOR_BG = "#fce48b";
    //橙色
    private String DEFAULT_COLOR_PROGRESS = "#ffa800";
    //文字大小
    private static final int DEFAULT_TEXT_SIZE = 40;

    //背景色
    private int mColorBg;
    //进度色
    private int mColorProgress;

    // 外 - 高度
    private int mBgHeight;
    // 外 - 宽度
    private int mBgWidth;
    // 外 - 半圆半径
    private int mBgCircleRadio;
    // 外内之间的空隙
    private int mOutBoundWidth;

    // 内 - 高度
    private int mBgProgressHeight;
    // 内 - 宽度
    private int mBgProgressWidth;
    // 内 - 半圆半径
    private int mBgCircleProgressRadio;

    // 叶子信息
    private List<Leaf> mLeafInfo;
    // 叶子默认运动周期
    private static int mLeafCycleTime;
    // 叶子默认旋转周期
    private static int mLeafRotateTime;
    // 叶子运动振幅 - 中
    private int mLeafAmplitudeMid;
    // 叶子振幅差
    private int mLeafAmplitudeDiff;
    // 叶子数量
    private int mLeafMaxSize;

    //风扇默认旋转周期
    private int mFanCycleTime;
    //风扇转过的角度
    private int mFanRotate;
    //风扇内外边距
    private int mFanOutBoundWidth;
    //风扇缩小动画时间
    private float mFanAnimTime;

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
    private float mProgressDrawTime;
    //进度完成标志
    private boolean bFinishFlag;
    //进度完成时间
    private long mFinishTime;
    //进度完成时显示的文字
    private String mTextComplete;

    //progress 绘制录像
    private Picture mProgressPicture;

    //progress 路径
    private Path mPath;

    public LeafLoadingView(Context context) {
        super(context);
        initDefault(context);
        init(context);
    }

    private void initDefault(Context context) {
        mColorBg = Color.parseColor(DEFAULT_COLOR_BG);
        mColorProgress = Color.parseColor(DEFAULT_COLOR_PROGRESS);

        mOutBoundWidth = 15;
        mProgressDrawTime = 500;

        mLeafCycleTime = 1500;
        mLeafRotateTime = 1500;
        mLeafAmplitudeMid = 50;
        mLeafAmplitudeDiff = 20;
        mLeafMaxSize = 7;

        mFanOutBoundWidth = 10;
        mFanAnimTime = 500;
        mFanCycleTime = 2000;

        mTextComplete = "100%";

        mBgWidth = Utils.dip2px(context,200);
    }

    public LeafLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.LeafLoadingView);
        try {
            mColorBg = ta.getColor(R.styleable.LeafLoadingView_colorBg, Color.parseColor(DEFAULT_COLOR_BG));
            mColorProgress = ta.getColor(R.styleable.LeafLoadingView_colorProgress, Color.parseColor(DEFAULT_COLOR_PROGRESS));

            mOutBoundWidth = ta.getInt(R.styleable.LeafLoadingView_progressOutBoundWidth,15);
            mProgressDrawTime = ta.getInt(R.styleable.LeafLoadingView_progressDrawTime, 500);

            mLeafCycleTime = ta.getInt(R.styleable.LeafLoadingView_leafCycleTime,1500);
            mLeafRotateTime = ta.getInt(R.styleable.LeafLoadingView_leafRotateTime,1500);
            mLeafAmplitudeMid = ta.getInt(R.styleable.LeafLoadingView_leafAmplitudeMid,50);
            mLeafAmplitudeDiff = ta.getInt(R.styleable.LeafLoadingView_leafAmplitudeDiff,20);
            mLeafMaxSize = ta.getInt(R.styleable.LeafLoadingView_leafMaxSize,7);

            mFanOutBoundWidth = ta.getInt(R.styleable.LeafLoadingView_fanOutBoundWidth,10);
            mFanAnimTime = ta.getInt(R.styleable.LeafLoadingView_fanAnimTime,500);
            mFanCycleTime = ta.getInt(R.styleable.LeafLoadingView_fanCycleTime,2000);

            mTextComplete = ta.getString(R.styleable.LeafLoadingView_fanCompleteText);
            if (TextUtils.isEmpty(mTextComplete)) {
                mTextComplete = "100%";
            }

            mBgWidth = Utils.dip2px(context,ta.getDimension(R.styleable.LeafLoadingView_loadingWidth,200));
        }finally {
            if (ta!=null) {
                ta.recycle();
            }
        }

        init(context);
    }

    public void init(Context context) {
        //初始化尺寸
        initDimens();
        //初始化画笔
        initPaint();
        //获取叶子
        initLeafInfo();
        //初始化图片
        initBitmap(context);
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
            mBgPaint.setColor(mColorBg);
        }
        if (mProgressPaint == null) {
            mProgressPaint = new Paint(mBasePaint);
            mProgressPaint.setColor(mColorProgress);
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
            mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
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
        mBgHeight = (int) (mBgWidth / 4.5f);

        mBgCircleRadio = mBgHeight / 2;
        mBgCircleProgressRadio = mBgCircleRadio - mOutBoundWidth;

        mBgProgressHeight = mBgHeight - 2 * mOutBoundWidth;
        mBgProgressWidth = mBgWidth - 2 * mOutBoundWidth;

        mProgressSetTime = System.currentTimeMillis();
        mFanRotate = (int) (System.currentTimeMillis() % mFanCycleTime / mFanCycleTime);
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
        mFanBgPaint.setColor(Color.parseColor(DEFAULT_COLOR_PROGRESS));
        canvas.drawCircle(0, 0, mBgCircleRadio - mFanOutBoundWidth, mFanBgPaint);

        if (generateProgressWidth() >= mBgWidth) {//完成文字
            if (!bFinishFlag) {//记录完成时间
                bFinishFlag = true;
                mFinishTime = System.currentTimeMillis();
            }

            drawCompleteFan(canvas);
            drawCompleteText(canvas);

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

    private void drawCompleteFan(Canvas canvas) {
        long deltaT = System.currentTimeMillis() - mFinishTime;

        if (deltaT > mFanAnimTime || !bFinishFlag) {
            return;
        }

        canvas.save();
        canvas.rotate(mFanRotate, 0, 0);

        float dx = (int) ((mBgCircleProgressRadio - 2) * (1 - 1 / mFanAnimTime * deltaT));
        canvas.translate(-dx, 0);

        //缩放画布使得风扇中心可以绘制在圆心上
        canvas.scale((dx * 2) / (float) mFanWidth, (dx * 2) / (float) mFanHeight);
        canvas.drawBitmap(mFanBitmap, 0, -mFanHeight / 2, mBitmapPaint);
        canvas.restore();
    }

    private void drawCompleteText(Canvas canvas) {
        long deltaT = System.currentTimeMillis() - mFinishTime;

        if (!bFinishFlag) {
            return;
        }

        if (deltaT < mFanAnimTime) {
            int textSize = (int) ((DEFAULT_TEXT_SIZE) / mFanAnimTime * deltaT);
            mTextPaint.setTextSize(textSize);
        }

        //获取文字尺寸
        Rect rect = new Rect();
        mTextPaint.getTextBounds(mTextComplete, -0, mTextComplete.length(), rect);
        canvas.drawText(mTextComplete, -(rect.right - rect.left) / 2, (rect.bottom - rect.top) / 2, mTextPaint);

    }

    //根据属性计算应绘制的进度条长度
    private int generateProgressWidth() {
        int result;
        long deltaT = System.currentTimeMillis() - mProgressSetTime;

        if (deltaT > 0 && mProgressDrawTime > deltaT) {
            float deltaWidth = (mProgress - mOldProgress) / mProgressDrawTime * deltaT;
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
        int A = leaf.amplitudeType * mLeafAmplitudeDiff + mLeafAmplitudeMid;
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

        private static Leaf generateLeaf() {
            Random random = new Random();
            Leaf leaf = new Leaf();

            //随机值使叶子在产生时有先后顺序
            long addTime = random.nextInt(mLeafCycleTime / 2);
            leaf.startT = System.currentTimeMillis() + addTime;
            //初始旋转角度
            leaf.rotateInit = random.nextInt(360);
            //随机初始旋转方向
            leaf.rotateDirection = (int) Math.pow(-1, random.nextInt(1));
            //随机振幅
            leaf.amplitudeType = random.nextInt(2) - 1;
            //随机周期
            leaf.cycleTime = random.nextInt(1500) + mLeafCycleTime;
            //随机相位
            leaf.Q = (float) (Math.PI * random.nextInt(3) / 6);
            //随机旋转周期
            leaf.rotateCycle = random.nextInt(1000) + mLeafRotateTime;

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
    }

    public void setProgress(float progress) {
        if (System.currentTimeMillis() - mProgressSetTime > mProgressDrawTime
                && System.currentTimeMillis() - mProgressSetTime > mFanAnimTime) {
            //保存旧进度
            this.mOldProgress = this.mProgress;
            //保存新进度
            this.mProgress = progress;
            //保存设置时间
            this.mProgressSetTime = System.currentTimeMillis();
            //添加叶子
            addLeaf();

            //完成标志
            if (100 > progress) {
                bFinishFlag = false;
            }

            postInvalidate();
        }
    }

    private void addLeaf() {
        float deltaProgress = mProgress - mOldProgress;
        if (deltaProgress > 0 && mLeafMaxSize > mLeafInfo.size()) {
            int addNum = 1;

            if (8 > deltaProgress && deltaProgress > 5) {
                addNum = 2;
            } else if (deltaProgress > 8) {
                addNum = 3;
            }

            if (addNum > (mLeafMaxSize - mLeafInfo.size())) {
                addNum = mLeafMaxSize - mLeafInfo.size();
            }

            mLeafInfo.addAll(LeafFactory.generateLeaves(addNum));

        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.w(TAG, "onSizeChanged: w="+ w+",h="+h);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int resultWidth;
        int resultHeight ;
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d(TAG, "onMeasure: specW=" + specWidthSize + ",specH=" + specHeightSize);
        if (specWidthMode == MeasureSpec.AT_MOST) {
            resultWidth = mBgWidth + mBgCircleRadio;
        } else {
            resultWidth = specWidthSize;
        }

        if (specHeightMode == MeasureSpec.AT_MOST) {
            resultHeight = mBgHeight;
        } else {
            resultHeight = specHeightSize;
        }
        Log.d(TAG, "onMeasure: resultWidth=" + resultWidth + ",resultHeight=" + resultHeight);
        setMeasuredDimension(resultWidth, resultHeight);
    }
}
