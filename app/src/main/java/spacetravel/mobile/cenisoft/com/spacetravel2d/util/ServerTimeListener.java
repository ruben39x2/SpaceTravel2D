package spacetravel.mobile.cenisoft.com.spacetravel2d.util;

public interface ServerTimeListener {
   void onDidReceiveServerTime(long serverMillis);

   void onDidFailed();
}
