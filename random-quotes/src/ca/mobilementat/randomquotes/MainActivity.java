package ca.mobilementat.randomquotes;

import java.lang.reflect.Field;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final long ANIMATION_DURATION = 8000;
	private static final String TAG = "RandomQuotesTag";
	
	private Field[] images;
	private String[] texts;
	private int indexImage = 0;
	private int indexText = 0;
	private ImageView imageView;
	private TextView textView;
	private Handler cycleHandler = new Handler();
	private Handler hideHandler = new Handler();
	private AnimationSet imageAnimationSet;
	private AnimationSet textAnimationSet;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.image);
        textView = (TextView)findViewById(R.id.text);
        images = R.drawable.class.getFields();
        texts = getResources().getStringArray(R.array.quotes);
        
        configureAnimationSets();
        hideNavigation();
        
        // register a listener for when the navigation bar re-appears
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
        	new OnSystemUiVisibilityChangeListener() {
	
		        @Override
		        public void onSystemUiVisibilityChange(int visibility) {
			        if(visibility == 0) {
				        // the navigation bar re-appears, let’s hide it
				        // after 2 seconds
			        	hideHandler.postDelayed(mHideRunnable, 2000);
			        }
		        }
        	}
        );
        
        cycleHandler.postDelayed(run, 1000);        
    }

	Runnable run = new Runnable() {
		@Override
		public void run() {
			try {
				int idImage = images[indexImage++].getInt(null);
			
				if(R.drawable.ic_launcher == idImage){
					idImage = images[indexImage++].getInt(null);
				}
				
				imageView.setBackground(getResources().getDrawable(idImage));
				textView.setText(texts[indexText++]);
				textView.setVisibility(View.VISIBLE);
				
				if (indexImage == images.length) {
					indexImage = 0;
				}

				if (indexText == texts.length) {
					indexText = 0;
				}

				imageView.startAnimation(imageAnimationSet);
				textView.startAnimation(textAnimationSet);
				
				cycleHandler.postDelayed(run, ANIMATION_DURATION);				
			}
			catch (IllegalAccessException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
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
		Animation zoomin = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomin.setDuration(ANIMATION_DURATION);
        
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f) ; 
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f) ;
    	fadeIn.setDuration(ANIMATION_DURATION/10);
    	fadeIn.setFillAfter(true);
    	fadeOut.setDuration(ANIMATION_DURATION/10);
    	fadeOut.setFillAfter(true);
    	fadeOut.setStartOffset(ANIMATION_DURATION-ANIMATION_DURATION/10 + fadeIn.getStartOffset());
    	
    	imageAnimationSet = new AnimationSet(true);
    	imageAnimationSet.setInterpolator(new AccelerateInterpolator()); 
    	imageAnimationSet.addAnimation(zoomin);
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
