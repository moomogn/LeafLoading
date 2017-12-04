package com.arno.leafloading;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.arno.support.LeafLoadingView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AAAAA";
    private boolean b = false;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_PROGRESS:
                    if (b) {
                        mProgress = 0;
                        b = false;
                    }

                    if (mProgress < 20) {
                        mProgress += 1;

                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(500));
                    } else if (mProgress < 60) {
                        mProgress += 2;

                        // 随机800ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(500));
                    } else if (mProgress < 100){
                        mProgress += 4;
                        // 随机1200ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                new Random().nextInt(500));

                    } else{
                        b = true;
                        // 随机500ms以内刷新一次
                        mHandler.sendEmptyMessageDelayed(REFRESH_PROGRESS,
                                3000);
                    }
//                    Log.i(TAG, "handleMessage: mProgress=="+mProgress);
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
