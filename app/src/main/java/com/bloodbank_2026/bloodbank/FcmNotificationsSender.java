package com.bloodbank_2026.bloodbank;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    Context mContext;

    // IMPORTANT: আপনি অবশ্যই Firebase Console থেকে আপনার আসল Server Key এখানে বসাবেন।
    // Project Settings -> Cloud Messaging -> Cloud Messaging API (Legacy) -> Server Key
    private final String SERVER_KEY = "YOUR_ACTUAL_SERVER_KEY_HERE"; 

    public FcmNotificationsSender(String userFcmToken, String title, String body, Context mContext) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
    }

    public void SendNotifications() {
        Log.d("FCM_SEND", "Sending to: " + userFcmToken);
        AsyncTask.execute(() -> {
            try {
                JSONObject mainObj = new JSONObject();
                mainObj.put("to", userFcmToken);
                mainObj.put("priority", "high");
                
                JSONObject notificationObj = new JSONObject();
                notificationObj.put("title", title);
                notificationObj.put("body", body);
                notificationObj.put("icon", "ic_blood_drop");
                notificationObj.put("android_channel_id", "blood_bank_notifications");
                notificationObj.put("sound", "default");
                
                mainObj.put("notification", notificationObj);
                
                JSONObject dataObj = new JSONObject();
                dataObj.put("title", title);
                dataObj.put("body", body);
                mainObj.put("data", dataObj);

                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(mainObj.toString());
                wr.flush();
                wr.close();
                
                int responseCode = conn.getResponseCode();
                Log.d("FCM_SEND", "Response Code: " + responseCode);
                
                if (responseCode != 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.e("FCM_SEND", "Error Response: " + response.toString());
                } else {
                    Log.d("FCM_SEND", "Notification sent successfully!");
                }

            } catch (Exception e) {
                Log.e("FCM_SEND", "Exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
