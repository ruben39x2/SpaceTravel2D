package spacetravel.mobile.cenisoft.com.spacetravel2d.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import spacetravel.mobile.cenisoft.com.spacetravel2d.R;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.AnimatorEndListener;

public class EnemiesGenerator {
   private AppCompatActivity activity;
   private RelativeLayout relativeLayout;
   private int screenWidth;
   private int screenHeight;
   private Random random;
   private Timer timer;
   private EnemiesPhase phase;
   private EnemiesPhasesListener listener;

   private TimerTask yellowSpaceshipsTask;
   private int yellowSpaceshipsGenerated = 0;

   private TimerTask purpleBossesTask;
   private int purpleBossesGenerated = 0;

   private TimerTask greenSpaceshipsTask;
   private int greenSpaceshipsGenerated = 0;

   private TimerTask blueHiveTask;
   private boolean shouldGenerateTinyBlues = false;

   EnemiesGenerator(AppCompatActivity activity,
                    RelativeLayout relativeLayout,
                    Timer timer,
                    EnemiesPhasesListener listener) {
      Display display = activity.getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      this.screenWidth = size.x;
      this.screenHeight = size.y;
      this.random = new Random(new Date().getTime());
      this.timer = timer;
      this.activity = activity;
      this.relativeLayout = relativeLayout;
      this.listener = listener;
   }

   public void startGeneratingEnemies() {
      startPhase(randomPhase());
   }

   private void startPhase(EnemiesPhase phase) {
      this.phase = phase;
      switch (phase) {
         case YellowSpaceships:
         {
            yellowSpaceshipsTask = new TimerTask() {
               @Override
               public void run() {
                  activity.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        generateYellowSpaceship();
                        yellowSpaceshipsGenerated++;

                        if (yellowSpaceshipsGenerated > randomYellowQuantity()) {
                           changePhase();
                        }
                     }
                  });
               }
            };
            timer.scheduleAtFixedRate(yellowSpaceshipsTask,
                    randomInterphaseDelay(),
                    randomYellowTiming());
            break;
         }
         case PurpleBosses:
         {
            purpleBossesTask = new TimerTask() {
               @Override
               public void run() {
                  activity.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        generatePurpleBoss();
                        purpleBossesGenerated++;

                        if (purpleBossesGenerated > randomPurpleQuantity()) {
                           changePhase();
                        }
                     }
                  });
               }
            };
            timer.scheduleAtFixedRate(purpleBossesTask,
                    randomInterphaseDelay(),
                    7000);
            break;
         }
         case GreenSpaceships:
         {
            greenSpaceshipsTask = new TimerTask() {
               @Override
               public void run() {
                  activity.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        generateGreenSpaceship();
                        greenSpaceshipsGenerated++;

                        if (greenSpaceshipsGenerated > randomGreenQuantity()) {
                           changePhase();
                        }
                     }
                  });
               }
            };
            timer.scheduleAtFixedRate(greenSpaceshipsTask,
                    randomInterphaseDelay(),
                    randomGreenTiming());
            break;
         }
         case BlueHive:
         {
            final ImageView blueQueen = generateBlueQueen();

            blueHiveTask = new TimerTask() {
               @Override
               public void run() {
                  activity.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        if (shouldGenerateTinyBlues) {
                           generateTinyBlue(blueQueen);
                        } else {
                           changePhase();
                        }
                     }
                  });
               }
            };
            timer.scheduleAtFixedRate(blueHiveTask, 3000, randomHiveTiming());
            break;
         }
      }
   }

   private void changePhase() {
      switch (phase) {
         case YellowSpaceships:
         {
            yellowSpaceshipsGenerated = 0;
            yellowSpaceshipsTask.cancel();
            yellowSpaceshipsTask = null;
            break;
         }
         case PurpleBosses:
         {
            purpleBossesGenerated = 0;
            purpleBossesTask.cancel();
            purpleBossesTask = null;
            break;
         }
         case GreenSpaceships:
         {
            greenSpaceshipsGenerated = 0;
            greenSpaceshipsTask.cancel();
            greenSpaceshipsTask = null;
            break;
         }
         case BlueHive:
         {
            blueHiveTask.cancel();
            blueHiveTask = null;
            break;
         }
      }
      timer.purge();
      startPhase(randomPhase());
      listener.onDidChangePhase();
   }

   private void generateYellowSpaceship() {
      final int SPACESHIP_WIDTH = 70;
      final int SPACESHIP_HEIGHT = 97;
      final ImageView yellowSpaceship = new ImageView(activity);
      yellowSpaceship.setImageResource(randomYellowResource());
      yellowSpaceship.setAlpha(0.f);

      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
      params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(yellowSpaceship, "alpha", 1.f);
      fadeAnimation.setDuration(500);

      final ObjectAnimator translationXAnimation =
              ObjectAnimator.ofFloat(yellowSpaceship, "translationX",
                      -(screenWidth - SPACESHIP_WIDTH));
      translationXAnimation.setDuration(2000);
      translationXAnimation.setInterpolator(new LinearInterpolator());
      translationXAnimation.setRepeatCount(ValueAnimator.INFINITE);
      translationXAnimation.setRepeatMode(ValueAnimator.REVERSE);

      final ObjectAnimator translationYAnimation =
              ObjectAnimator.ofFloat(yellowSpaceship, "translationY", screenHeight + 200);
      translationYAnimation.setDuration(6000);
      translationYAnimation.setInterpolator(new LinearInterpolator());
      translationYAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  relativeLayout.removeView(yellowSpaceship);
               }
            }, 500);
         }
      });

      relativeLayout.addView(yellowSpaceship, params);

      fadeAnimation.start();
      translationXAnimation.start();
      translationYAnimation.start();
   }

   private void generatePurpleBoss() {
      final int SPACESHIP_WIDTH = 163;
      final int SPACESHIP_HEIGHT = 250;
      final ImageView purpleBoss = new ImageView(activity);
      purpleBoss.setImageResource(R.drawable.purple_boss);
      purpleBoss.setAlpha(0.f);

      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.setMargins(randomLeftMargin(), 0, 0, 0);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(purpleBoss, "alpha", 1.f);
      fadeAnimation.setDuration(2000);

      final ObjectAnimator translationYAnimation =
              ObjectAnimator.ofFloat(purpleBoss, "translationY", screenHeight + 200);
      translationYAnimation.setDuration(12000);
      translationYAnimation.setInterpolator(new LinearInterpolator());
      translationYAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  relativeLayout.removeView(purpleBoss);
               }
            }, 500);
         }
      });

      relativeLayout.addView(purpleBoss, params);

      fadeAnimation.start();
      translationYAnimation.start();
   }

   private void generateGreenSpaceship() {
      final int SPACESHIP_WIDTH = 79;
      final int SPACESHIP_HEIGHT = 70;
      final ImageView greenSpaceship = new ImageView(activity);
      greenSpaceship.setImageResource(R.drawable.green_spaceship);
      greenSpaceship.setAlpha(0.f);

      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.setMargins(randomLeftMargin(), 0, 0, 0);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(greenSpaceship, "alpha", 1.f);
      fadeAnimation.setDuration(2000);

      final ObjectAnimator translationYAnimation =
              ObjectAnimator.ofFloat(greenSpaceship, "translationY", screenHeight + 200);
      translationYAnimation.setDuration(randomGreenYDuration());
      translationYAnimation.setInterpolator(new AccelerateInterpolator(1.8f));
      translationYAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  relativeLayout.removeView(greenSpaceship);
               }
            }, 500);
         }
      });

      relativeLayout.addView(greenSpaceship, params);

      fadeAnimation.start();
      translationYAnimation.start();
   }

   private ImageView generateBlueQueen() {

      shouldGenerateTinyBlues = true;

      final int SPACESHIP_WIDTH = 174;
      final int SPACESHIP_HEIGHT = 256;
      final ImageView blueQueen = new ImageView(activity);
      blueQueen.setImageResource(R.drawable.big_blue_spaceship);
      blueQueen.setAlpha(0.f);

      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.setMargins(randomLeftMargin(), 30, 0, 0);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(blueQueen, "alpha", 1.f);
      fadeAnimation.setDuration(5000);

      final ObjectAnimator translationYAnimation =
              ObjectAnimator.ofFloat(blueQueen, "translationY", 200);
      translationYAnimation.setDuration(randomBlueQueenDuration());
      translationYAnimation.setInterpolator(new LinearInterpolator());
      translationYAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  final ObjectAnimator accelerateAnimation =
                          ObjectAnimator.ofFloat(blueQueen, "translationY", screenHeight + 200);
                  accelerateAnimation.setDuration(2000);
                  accelerateAnimation.setInterpolator(new AccelerateInterpolator(2.f));
                  accelerateAnimation.addListener(new AnimatorEndListener() {
                     @Override
                     public void onAnimationEnd(Animator animation) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                           @Override
                           public void run() {
                              relativeLayout.removeView(blueQueen);
                           }
                        }, 500);
                     }
                  });
                  accelerateAnimation.start();
                  shouldGenerateTinyBlues = false;
               }
            });
         }
      });

      relativeLayout.addView(blueQueen, params);

      fadeAnimation.start();
      translationYAnimation.start();

      return blueQueen;
   }

   private void generateTinyBlue(ImageView queen) {
      final int SPACESHIP_WIDTH = 40;
      final int SPACESHIP_HEIGHT = 61;
      final ImageView tinyBlue = new ImageView(activity);
      tinyBlue.setImageResource(R.drawable.tiny_blue_spaceship);
      tinyBlue.setAlpha(0.f);

      final RelativeLayout.LayoutParams params =
              new RelativeLayout.LayoutParams(SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      params.setMargins(
              queen.getLeft() + queen.getWidth() / 2,
              queen.getTop() + queen.getHeight(),
              0, 0);

      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(tinyBlue, "alpha", 1.f);
      fadeAnimation.setInterpolator(new AccelerateInterpolator(3.f));
      fadeAnimation.setDuration(800);

      final ObjectAnimator translationXAnimation =
              ObjectAnimator.ofFloat(tinyBlue, "translationX", randomXDisplacement());
      translationXAnimation.setDuration(3000);
      translationXAnimation.setInterpolator(new AccelerateInterpolator(1.5f));

      final ObjectAnimator translationYAnimation =
              ObjectAnimator.ofFloat(tinyBlue, "translationY", screenHeight + 200);
      translationYAnimation.setDuration(3000);
      translationYAnimation.setInterpolator(new AccelerateInterpolator(1.5f));
      translationYAnimation.addListener(new AnimatorEndListener() {
         @Override
         public void onAnimationEnd(Animator animation) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  relativeLayout.removeView(tinyBlue);
               }
            }, 500);
         }
      });

      relativeLayout.addView(tinyBlue, params);

      fadeAnimation.start();
      translationXAnimation.start();
      translationYAnimation.start();
   }

   private EnemiesPhase randomPhase() {
      return EnemiesPhase.values()[random.nextInt(EnemiesPhase.values().length)];
   }

   private int randomLeftMargin() {
      return random.nextInt(screenWidth - 180);
   }

   private int randomXDisplacement() {
      return random.nextInt(screenWidth * 4) - (screenWidth * 2);
   }

   private int randomYellowTiming() {
      return random.nextInt(1000) + 1500;
   }

   private int randomYellowResource() {
      if (random.nextInt(2) == 1) {
         return R.drawable.yellow_spaceship;
      } else {
         return R.drawable.yellow_spaceship2;
      }
   }

   private int randomYellowQuantity() {
      return random.nextInt(10) + 10;
   }

   private int randomPurpleQuantity() {
      return random.nextInt(6) + 2;
   }

   private int randomGreenYDuration() {
      return random.nextInt(1500) + 2000;
   }

   private int randomGreenQuantity() {
      return random.nextInt(20) + 5;
   }

   private int randomGreenTiming() {
      return random.nextInt(1000) + 100;
   }

   private int randomBlueQueenDuration() {
      return random.nextInt(15000) + 6000;
   }

   private int randomInterphaseDelay() {
      return random.nextInt(5000) + 500;
   }

   private int randomHiveTiming() {
      return random.nextInt(300) + 100;
   }
}
