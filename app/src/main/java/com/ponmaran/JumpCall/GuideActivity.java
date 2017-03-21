package com.ponmaran.JumpCall;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

public class GuideActivity extends Activity {
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private int imageIndex =0;
    Button buttonNext;
    Button buttonPrev;
    Button buttonSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView = findViewById(R.id.baseFrame);
        mHideHandler.post(mHidePart2Runnable);
        buttonNext = (Button) findViewById(R.id.button_next);
        buttonNext.setOnClickListener(listener);
        buttonPrev = (Button) findViewById(R.id.button_prev);
        buttonPrev.setOnClickListener(listener);
        buttonPrev.setTextColor(Color.GRAY);
        buttonSkip = (Button) findViewById(R.id.button_skip);
        buttonSkip.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.button_next:
                    switch (imageIndex){
                        case 0:
                            mContentView.setBackground(ContextCompat.getDrawable(GuideActivity.this,R.drawable.guide_page_2));
                            buttonPrev.setTextColor(Color.BLACK);
                            imageIndex++;
                            break;
                        case 1:
                            mContentView.setBackground(ContextCompat.getDrawable(GuideActivity.this,R.drawable.guide_page_3));
                            buttonNext.setText(R.string.guide_button_done);
                            imageIndex++;
                            break;
                        case 2:
                            finish();
                    }
                    break;
                case R.id.button_prev:
                    switch (imageIndex){
                        case 1:
                            mContentView.setBackground(ContextCompat.getDrawable(GuideActivity.this, R.drawable.guide_page_1));
                            buttonPrev.setTextColor(Color.GRAY);
                            imageIndex--;
                            break;
                        case 2:
                            mContentView.setBackground(ContextCompat.getDrawable(GuideActivity.this, R.drawable.guide_page_2));
                            buttonNext.setText(getText(R.string.guide_button_next));
                            buttonPrev.setTextColor(Color.BLACK);
                            imageIndex--;
                            break;
                    }
                    break;
                case R.id.button_skip:
                    finish();
                    break;
            }
        }
    };
}