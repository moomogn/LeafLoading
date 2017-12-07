package com.arno.leafloading;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.arno.support.LeafLoadingView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean b = false;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_PROGRESS:
                    if (b) {
                        mProgress = 0;
                        b= false;
                    }

                    if (mProgress < 20) {
                        mProgress += 3;

                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(800));
                    } else if (mProgress < 40) {
                        mProgress += 4;

                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(800));
                    } else if (mProgress < 60) {
                        mProgress += 10;

                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(800));
                    } else if (mProgress < 100){
                        mProgress += 8;
                        // 随机1200ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(1200));

                    } else{
                        b = true;
                        // 随机500ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                10000);
                    }
                    mLeafLoadingView.setProgress(mProgress);
                    break;

                default:
                    break;
            }
        }
    };

    private static final int REFRESH_PROGRESS = 999;

    private LeafLoadingView mLeafLoadingView;
    private int mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLeafLoadingView = ((LeafLoadingView) findViewById(R.id.leafloading));
        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                new Random().nextInt(800));

    }
}
