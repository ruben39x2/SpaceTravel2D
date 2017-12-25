package spacetravel.mobile.cenisoft.com.spacetravel2d.main;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SpaceshipCrashChecker {
   private RelativeLayout relativeLayout;
   private ArrayList<View> harmlessViews;
   private Timer timer;
   private SpaceshipCrashListener listener;

   SpaceshipCrashChecker(RelativeLayout relativeLayout,
                         ArrayList<View> harmlessViews,
                         Timer timer,
                         SpaceshipCrashListener listener) {
      this.relativeLayout = relativeLayout;
      this.harmlessViews = harmlessViews;
      this.timer = timer;
      this.listener = listener;
   }

   public void startCheckingForCollisions(final ImageView spaceship) {
      timer.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run() {
            if (isCollisioning(spaceship)) {
               listener.onDidCrash();
            }
         }
      }, 1000, 50);
   }

   private boolean isCollisioning(ImageView spaceship) {
      int[] spaceshipCoordinates = new int[2];
      spaceship.getLocationOnScreen(spaceshipCoordinates);

      for (int i = 0; i < relativeLayout.getChildCount(); i++) {
         View subview = relativeLayout.getChildAt(i);
         if (harmlessViews.contains(subview)) {
            continue; /* Skip this subview. It's harmless */
         }
         if (subview != spaceship) {
            int[] asteroidCoordinates = new int[2];
            subview.getLocationOnScreen(asteroidCoordinates);
            /* Lazy comparison. First Y axis */
            if (asteroidCoordinates[1] + subview.getHeight() > spaceshipCoordinates[1]) {
               /* Then check if rects intersect */
               Rect spaceshipRect = new Rect();
               Rect subviewRect = new Rect();
               spaceship.getHitRect(spaceshipRect);
               subview.getHitRect(subviewRect);
               spaceshipRect = new Rect( /* Make the hitbox a bit smaller */
                       spaceshipRect.left + 10,
                       spaceshipRect.top + 10,
                       spaceshipRect.right - 10,
                       spaceshipRect.bottom - 10);
               subviewRect = new Rect(
                       subviewRect.left + (int)(subview.getWidth() * 0.2),
                       subviewRect.top + (int)(subview.getHeight() * 0.4),
                       subviewRect.right - (int)(subview.getWidth() * 0.2),
                       subviewRect.bottom - (int)(subview.getHeight() * 0.2)
               );
               if (spaceshipRect.intersect(subviewRect)) {
                  return true;
               }
            }
         }
      }
      return false;
   }
}
