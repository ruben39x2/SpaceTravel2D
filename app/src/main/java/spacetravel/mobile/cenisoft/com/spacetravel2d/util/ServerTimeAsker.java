package spacetravel.mobile.cenisoft.com.spacetravel2d.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerTimeAsker {

   public static final String TIME_SERVER = "http://time.akamai.com";

   public static void askTime(final ServerTimeListener listener) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               URL url = new URL(TIME_SERVER);
               HttpURLConnection connection = (HttpURLConnection)url.openConnection();
               connection.connect();
               InputStream inputStream = connection.getInputStream();
               BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
               final String akamaiTime = bufferedReader.readLine();
               connection.disconnect();
               listener.onDidReceiveServerTime(Long.valueOf(akamaiTime) * 1000);
            } catch (IOException | NumberFormatException e) {
               listener.onDidFailed();
            }
         }
      }).start();
   }
}
