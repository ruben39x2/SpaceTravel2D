package spacetravel.mobile.cenisoft.com.spacetravel2d.main;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import spacetravel.mobile.cenisoft.com.spacetravel2d.R;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.ServerTimeAsker;
import spacetravel.mobile.cenisoft.com.spacetravel2d.util.ServerTimeListener;

public class AlarmReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(final Context context, Intent intent) {
      ServerTimeAsker.askTime(new ServerTimeListener() {
         @Override
         public void onDidReceiveServerTime(long serverMillis) {
            SharedPreferences prefs =
                    context.getSharedPreferences(FullscreenActivity.PREFS_NAME, Context.MODE_PRIVATE);
            long storedMillis = prefs.getLong(FullscreenActivity.PREFS_CRASHED_DATE_KEY, 0);
            if (serverMillis > storedMillis + FullscreenActivity.REPAIR_INTERVAL) {
               showNotification(context);
            }
         }

         @Override
         public void onDidFailed() {
         }
      });
   }

   private static void showNotification(final Context context) {
      NotificationManager manager =
              (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      NotificationCompat.Builder builder =
              new NotificationCompat.Builder(context)
                      .setSmallIcon(R.drawable.spaceship)
                      .setContentTitle(context.getString(R.string.notification_title))
                      .setContentText(context.getString(R.string.notification_body))
                      .setAutoCancel(true);

      Intent targetIntent = new Intent(context, FullscreenActivity.class);
      PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentIntent(contentIntent);
      manager.notify(123456, builder.build());
   }
}
