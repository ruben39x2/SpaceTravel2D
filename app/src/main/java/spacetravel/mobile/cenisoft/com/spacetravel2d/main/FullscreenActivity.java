package spacetravel.mobile.cenisoft.com.spacetravel2d.main;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import spacetravel.mobile.cenisoft.com.spacetravel2d.R;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.ServerTimeAsker;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.ServerTimeListener;

public class FullscreenActivity extends AppCompatActivity
        implements SpaceshipCrashListener, EnemiesPhasesListener, ServerTimeListener {

   public static final String PREFS_NAME = "AsteroidsShips3";
   public static final String PREFS_SCORE_KEY = "score";
   public static final String PREFS_CRASHED_DATE_KEY = "dateCrashed";
   public static final long REPAIR_INTERVAL = (long) 10800000; /* 3 hours */
   public static final long ANIMATION_DURATION = 2000; /* Spaceship velocity */

   private RelativeLayout layout;
   private ImageView spaceship;
   private TextView backgroundTextView;
   private TextView scoreTextView;
   private AlertDialog connectingDialog;
   private Button button;
   private Timer timer;
   private Random random;
   private ObjectAnimator moveSpaceshipLeftRightAnimation;
   private ObjectAnimator rotateSpaceshipAnimation;
   private AsteroidsGenerator asteroidsGenerator;
   private EnemiesGenerator enemiesGenerator;
   private SpaceshipCrashChecker crashChecker;
   private boolean gameStarted = false;
   private TimerTask scoreTask;
   private long score = 0;
   private long serverTime = 0;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_fullscreen);

      scoreTextView = (TextView) findViewById(R.id.text_score);
      backgroundTextView = (TextView) findViewById(R.id.text_background);
      button = (Button) findViewById(R.id.button);
      layout = (RelativeLayout) findViewById(R.id.main_layout);
      spaceship = (ImageView) findViewById(R.id.spaceship);
      timer = new Timer();
      random = new Random(new Date().getTime());
      ArrayList<View> harmlessViews = new ArrayList<>(1);
      harmlessViews.add(backgroundTextView);
      harmlessViews.add(scoreTextView);
      crashChecker = new SpaceshipCrashChecker(layout, harmlessViews, timer, this);
      asteroidsGenerator = new AsteroidsGenerator(this, layout, timer);
      enemiesGenerator = new EnemiesGenerator(this, layout, timer, this);

      setupSpaceshipAnimations(getScreenWidth());
      setupTouchRecognizer();
      setupScore();
      asteroidsGenerator.startGeneratingAsteroids();
      enemiesGenerator.startGeneratingEnemies();
      crashChecker.startCheckingForCollisions(spaceship);

      moveSpaceshipLeftRightAnimation.start();

      connectingDialog = displayAlert("", getString(R.string.asking_time_msg), false, false);
      ServerTimeAsker.askTime(this);
   }

   @Override
   public void onDestroy() {
      /* If this is not called due to memory needs, timer will be killed anyway */
      timer.cancel();
      super.onDestroy();
   }

   public void onDidCrash() {
      if (gameStarted) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               scoreTask.cancel();
               moveSpaceshipLeftRightAnimation.cancel();
               rotateSpaceshipAnimation.start();
               setGameOverBackgroundText();
               gameStarted = false;
               showEndGameAfterDelay();
               saveData();
            }
         });
      }
   }

   public void onDidChangePhase() {
      if (gameStarted) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
               changeBackgroundText();
            }
         });
      }
   }

   public void onDidReceiveServerTime(final long serverMillis) {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            connectingDialog.dismiss();
            handleServerTime(serverMillis);
         }
      });
   }

   public void onDidFailed() {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            connectingDialog.dismiss();
            displayAlert(getString(R.string.ops_title),
                    getString(R.string.connection_failure_msg),
                    true, true);
         }
      });
   }

   private void handleServerTime(long serverMillis) {
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      long storedMillis = prefs.getLong(PREFS_CRASHED_DATE_KEY, 0);
      if (storedMillis == 0) { /* First time */
         serverTime = serverMillis;
         enableStartButton();
      } else {
         if (serverMillis > storedMillis + REPAIR_INTERVAL) {
            serverTime = serverMillis;
            enableStartButton();
         } else {
            final long MILLIS_IN_ONE_HOUR = 60 * 60 * 1000;
            final long MILLIS_IN_ONE_MINUTE = 60 * 1000;
            long timeLeftInMillis = REPAIR_INTERVAL - (serverMillis - storedMillis);
            long timeLeftHours = timeLeftInMillis / MILLIS_IN_ONE_HOUR;
            long timeLeftMinutes = (timeLeftInMillis % MILLIS_IN_ONE_HOUR) / MILLIS_IN_ONE_MINUTE;
            displayAlert(getString(R.string.repairing_title),
                    getString(R.string.not_repaired_msg, timeLeftHours, timeLeftMinutes),
                    true, false);
            setNoticeButton();
         }
      }
   }

   private void setNoticeButton() {
      button.setText(R.string.repairing_notice);
   }

   private void enableStartButton() {
      setupButtonAnimation();
      button.setText(getString(R.string.start));
      button.setEnabled(true);
      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            layout.removeView(v);
            startGame();
         }
      });
   }

   private void startGame() {
      changeBackgroundText();
      scoreTask = new TimerTask() {
         @Override
         public void run() {
            score++;
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  scoreTextView.setText(String.valueOf(score));
               }
            });
         }
      };
      timer.scheduleAtFixedRate(scoreTask, 0, 100);
      gameStarted = true;
   }

   private int getScreenWidth() {
      Display display = getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      return size.x;
   }

   private void setupSpaceshipAnimations(int screenWidth) {
      final int MARGINS = 10;
      final int SPACESHIP_WIDTH = 80;

      moveSpaceshipLeftRightAnimation =
              ObjectAnimator.ofFloat(spaceship, "translationX",
                      -(screenWidth - MARGINS - SPACESHIP_WIDTH));
      moveSpaceshipLeftRightAnimation.setInterpolator(new LinearInterpolator());
      moveSpaceshipLeftRightAnimation.setDuration(ANIMATION_DURATION);
      moveSpaceshipLeftRightAnimation.setRepeatCount(ValueAnimator.INFINITE);
      moveSpaceshipLeftRightAnimation.setRepeatMode(ValueAnimator.REVERSE);

      rotateSpaceshipAnimation = ObjectAnimator.ofFloat(spaceship, "rotation", 360);
      rotateSpaceshipAnimation.setInterpolator(new LinearInterpolator());
      rotateSpaceshipAnimation.setDuration(500);
      rotateSpaceshipAnimation.setRepeatCount(ValueAnimator.INFINITE);
      rotateSpaceshipAnimation.setRepeatMode(ValueAnimator.RESTART);
   }

   private void setupButtonAnimation() {
      final ObjectAnimator fadeAnimation = ObjectAnimator.ofFloat(button, "alpha", 1.f);
      fadeAnimation.setDuration(300);
      fadeAnimation.setRepeatCount(ValueAnimator.INFINITE);
      fadeAnimation.setRepeatMode(ValueAnimator.REVERSE);
      fadeAnimation.start();
   }

   private void setupTouchRecognizer() {
      layout.setOnTouchListener(new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
            if (gameStarted) {
               moveSpaceshipLeftRightAnimation.reverse();
               return false;
            } else {
               return true;
            }
         }
      });
   }

   private void setupScore() {
      SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
      long score = prefs.getLong(PREFS_SCORE_KEY, 0);
      this.score = score;
      scoreTextView.setText(String.valueOf(score));
   }

   private void saveData() {
      SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
      editor.putLong(PREFS_SCORE_KEY, this.score);
      /* Save the server time only after game ended (spaceship crashed). This way users can
       * play again if the game crashed or was interrupted and didn't finalize */
      editor.putLong(PREFS_CRASHED_DATE_KEY, this.serverTime);
      editor.apply();
      editor.apply();
   }

   private void showEndGameAfterDelay() {
      final FullscreenActivity activity = this;
      timer.schedule(new TimerTask() {
         @Override
         public void run() {
            timer.cancel();
            activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  displayAlert(getString(R.string.gameover_title),
                          getString(R.string.gameover_message),
                          true, true);
               }
            });
         }
      }, 1500);
   }

   private AlertDialog displayAlert(String title,
                                    String message,
                                    boolean showOkButton,
                                    final boolean finishActivityOnDismiss) {
      final Activity activity = this;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(title);
      builder.setMessage(message);
      builder.setCancelable(false);
      if (showOkButton) {
         builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
               if (finishActivityOnDismiss) {
                  activity.finish();
               }
            }
         });
      }
      if (!isFinishing()) {
         AlertDialog dialog = builder.create();
         dialog.show();
         return dialog;
      }
      return null;
   }

   private void changeBackgroundText() {
      String[] messages = {"HAVE\nFUN", "WUBBA\nLUBBA", "HOWDY", "YEEHA", "OH\nNO!", "MAMMA\nMIA",
              "SORRY\nFOR\nTHIS", "LIFE IS\nA LOOP", "MONEY\nIS A LIE", "LOVE\nTRULY", "CONTROL\nIS AN\nILLUSION",
              "TAKE IT\nEASY", "YOU ARE\nA NUMBER", "EASY\nPEASY", "TRICKY", "HOT\nROD", "3, 2, 1", "HEY",
              "PEACE\nAND\nLOVE", "LOVE\nMUSIC", "BE\nPATIENT", "RELAX", "LOVE\nLIFE", "EASY"};
      backgroundTextView.setText(messages[random.nextInt(messages.length - 1)]);
   }

   private void setGameOverBackgroundText(){
      backgroundTextView.setText(R.string.boom);
      backgroundTextView.setTextColor(getResources().getColor(R.color.red_boom));
   }
}
