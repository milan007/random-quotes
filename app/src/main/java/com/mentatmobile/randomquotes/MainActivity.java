package com.mentatmobile.randomquotes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.mentatmobile.randomquotes.data.DatabaseHandler;
import com.mentatmobile.randomquotes.data.Quote;

import java.io.InputStream;

public class MainActivity extends Activity {

    private static final String LOG_TAG = "RandomQuotesTag";
    private static final int ANIMATION_DURATION = 8000;
    private static final int RESET_IMAGE_ID = 1;

    private int indexImage = RESET_IMAGE_ID + 1;
    private ImageView imageView;
    private TextView textView;
    private Handler animationHandler = new Handler();
    private AnimationSet imageAnimationSet;
    private AnimationSet textAnimationSet;
    private Bitmap currentBitmap;
    private DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.image);
        textView = (TextView)findViewById(R.id.text);
        currentBitmap = setInitialBitmap();

        setImmersiveMode();
        clearStorage();
        configureDatabase();
        configureAnimationSets();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
        }
    }

    private void setImmersiveMode(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void clearStorage(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer maxBackgrounds = preferences.getInt("maxBackgrounds", 10);

        for(int imageId = maxBackgrounds; imageId < 50; imageId++){
            deleteFile(String.format("image%03d.png", imageId));
        }
    }

    @Override
    protected void onPause() {
        animationHandler.removeCallbacks(runAnimation);
        super.onPause();
    }

    @Override
    protected void onResume() {
        animationHandler.postDelayed(runAnimation, 1000);
        super.onResume();
    }

    Runnable runAnimation = new Runnable() {
        @Override
        public void run() {
            try {
                BitmapDrawable ob = new BitmapDrawable(getResources(), currentBitmap);
                imageView.setBackground(ob);

                Quote quote = databaseHandler.getRandomQuote();
                String text = quote.getQuote() + "\n" + quote.getAuthor();
                textView.setText(text);

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
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Integer maxBackgrounds = preferences.getInt("maxBackgrounds", 10);

                if(indexImage > maxBackgrounds){
                    indexImage = RESET_IMAGE_ID;
                }

                currentBitmap = new GetImage().execute(indexImage++, getApplicationContext()).get();

                if(currentBitmap == null){
                    indexImage = RESET_IMAGE_ID + 1;
                    currentBitmap =  setInitialBitmap();
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    };

    private void configureDatabase(){
        try {
            databaseHandler.createDataBase();
            databaseHandler.openDataBase();
        }
        catch(Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
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

    private Bitmap setInitialBitmap(){
        Bitmap bitmap = null;
        InputStream inputStream = null;

        try {
            inputStream = getAssets().open("images/image001.png");
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        catch(Exception e){
        }
        finally{
            if(inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (Exception e){
                }
            }
        }

        return bitmap;
    }
}
