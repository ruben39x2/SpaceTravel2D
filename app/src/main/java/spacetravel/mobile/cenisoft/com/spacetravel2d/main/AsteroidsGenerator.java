package spacetravel.mobile.cenisoft.com.spacetravel2d.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import spacetravel.mobile.cenisoft.com.spacetravel2d.R;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.AnimatorEndListener;

public class AsteroidsGenerator {
   private AppCompatActivity activity;
   private RelativeLayout relativeLayout;
   private int screenWidth;
   private int screenHeight;
   private Random random;
   private Timer timer;

   AsteroidsGenerator(AppCompatActivity activity,
                      RelativeLayout relativeLayout,
                      Timer timer) {
      Display display = activity.getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      this.screenWidth = size.x;
      this.screenHeight = size.y;
      this.random = new Random(new Date().getTime());
      this.timer = timer;
      this.activity = activity;
      this.relativeLayout = relativeLayout;
   }

   public void startGeneratingAsteroids() {
      timer.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  generateNewAsteroid();
               }
            });
         }
      }, 500, 1400);
   }

   private void generateNewAsteroid() {
      final ImageView asteroid = new ImageView(activity);
      asteroid.setImageResource(randomAsteroidResource());
      asteroid.setAlpha(0.f);

      int randomSize = randomSize();
      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(randomSize, randomSize);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.setMargins(randomLeftMargin(), 0, 0, 0);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(asteroid, "alpha", 1.f);
      fadeAnimation.setDuration(1000);

      final ObjectAnimator translationAnimation =
              ObjectAnimator.ofFloat(asteroid, "translationY", screenHeight + 200);
      translationAnimation.setDuration(randomTranslationDuration());
      translationAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  relativeLayout.removeView(asteroid);
               }
            }, 500);
         }
      });

      relativeLayout.addView(asteroid, params);

      fadeAnimation.start();
      translationAnimation.start();
   }

   private int randomAsteroidResource() {
      if (random.nextInt(2) == 1) {
         return R.drawable.asteroid1;
      } else {
         return R.drawable.asteroid2;
      }
   }

   private int randomSize() {
      return random.nextInt(180) + 30;
   }

   private int randomLeftMargin() {
      return random.nextInt(screenWidth);
   }

   private int randomTranslationDuration() {
      return random.nextInt(4000) + 2000;
   }
}
