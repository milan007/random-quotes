package com.mentatmobile.randomquotes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "RandomQuotesTag";
    private static final int ANIMATION_DURATION = 8000;
    private static final int RESET_IMAGE_ID = 1;
    private static final int MAX_IMAGE_ID = 10;

    private String[] texts;
    private int indexImage = RESET_IMAGE_ID + 1;
    private int indexText = 0;
    private ImageView imageView;
    private TextView textView;
    private Handler animationHandler = new Handler();
    private Handler hideHandler = new Handler();
    private AnimationSet imageAnimationSet;
    private AnimationSet textAnimationSet;
    private Bitmap currentBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.image);
        textView = (TextView)findViewById(R.id.text);
        texts = getResources().getStringArray(R.array.quotes);
        currentBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.image001);

        clearStorage();

        configureAnimationSets();
        hideNavigation();

        // register a listener for when the navigation bar re-appears
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
            new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0) {
                        // the navigation bar re-appears, let’s hide it
                        // after 2 seconds
                        hideHandler.postDelayed(mHideRunnable, 2000);
                    }
                }
            }
        );

        animationHandler.postDelayed(runAnimation, 1000);
    }

    private void clearStorage(){

        for(int imageId = RESET_IMAGE_ID; imageId < MAX_IMAGE_ID + 1; imageId++){
            deleteFile(String.format("image%03d.png", imageId));
        }

    }

    Runnable runAnimation = new Runnable() {
        @Override
        public void run() {
            try {
                BitmapDrawable ob = new BitmapDrawable(getResources(), currentBitmap);

                imageView.setBackground(ob);
                textView.setText(texts[indexText++]);

                if (indexText == texts.length) {
                    indexText = 0;
                }

                imageView.startAnimation(imageAnimationSet);
                textView.startAnimation(textAnimationSet);

                new Thread(runGetBitmap).start();
                animationHandler.postDelayed(runAnimation, ANIMATION_DURATION);
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    };

    Runnable runGetBitmap = new Runnable() {
        @Override
        public void run() {
            try {
                if(indexImage > MAX_IMAGE_ID) {
                    indexImage = RESET_IMAGE_ID;
                }

                currentBitmap = new GetImage().execute(indexImage++, getApplicationContext()).get();

                if(currentBitmap == null){
                    indexImage = RESET_IMAGE_ID + 1;
                    currentBitmap =  BitmapFactory.decodeResource(getResources(), R.mipmap.image001);
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    };

    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideNavigation();
        }
    };

    private void configureAnimationSets(){
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomIn.setDuration(ANIMATION_DURATION);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f) ;
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f) ;
        fadeIn.setDuration(ANIMATION_DURATION/10);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(ANIMATION_DURATION/10);
        fadeOut.setFillAfter(true);
        fadeOut.setStartOffset(ANIMATION_DURATION-ANIMATION_DURATION/10 + fadeIn.getStartOffset());

        imageAnimationSet = new AnimationSet(true);
        imageAnimationSet.setInterpolator(new AccelerateInterpolator());
        imageAnimationSet.addAnimation(zoomIn);
        imageAnimationSet.addAnimation(fadeIn);
        imageAnimationSet.addAnimation(fadeOut);

        textAnimationSet = new AnimationSet(true);
        textAnimationSet.setInterpolator(new AccelerateInterpolator());
        textAnimationSet.addAnimation(fadeIn);
        textAnimationSet.addAnimation(fadeOut);
    }

    private void hideNavigation(){
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
