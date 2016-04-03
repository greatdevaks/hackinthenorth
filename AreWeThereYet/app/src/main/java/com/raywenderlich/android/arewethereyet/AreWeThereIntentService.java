package com.raywenderlich.android.arewethereyet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AreWeThereIntentService extends IntentService {

  // region Properties

  private final String TAG = AreWeThereIntentService.class.getName();

  private SharedPreferences prefs;
  private Gson gson;

  // endregion

  // region Constructors

  public AreWeThereIntentService() {
    super("AreWeThereIntentService");
  }

  // endregion

  // region Overrides

  @Override
  protected void onHandleIntent(Intent intent) {
    prefs = getApplicationContext().getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
    gson = new Gson();

    GeofencingEvent event = GeofencingEvent.fromIntent(intent);
    if (event != null) {
      if (event.hasError()) {
        onError(event.getErrorCode());
      } else {
        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
          List<String> geofenceIds = new ArrayList<>();
          for (Geofence geofence : event.getTriggeringGeofences()) {
            geofenceIds.add(geofence.getRequestId());
          }
          if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            onEnteredGeofences(geofenceIds);
          }
        }
      }
    }
  }

  // endregion

  // region Private

  private void onEnteredGeofences(List<String> geofenceIds) {
    for (String geofenceId : geofenceIds) {
      String geofenceName = "";

      // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
      Map<String, ?> keys = prefs.getAll();
      for (Map.Entry<String, ?> entry : keys.entrySet()) {
        String jsonString = prefs.getString(entry.getKey(), null);
        NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
        if (namedGeofence.id.equals(geofenceId)) {
          geofenceName = namedGeofence.name;
          break;
        }
      }

      // Set the notification text and send the notification
      String contextText = String.format(this.getResources().getString(R.string.Notification_Text), geofenceName);

      Intent i;
      PackageManager manager = getPackageManager();
      try {
        i = manager.getLaunchIntentForPackage("com.stripe.example");
        if (i == null)
          throw new PackageManager.NameNotFoundException();
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);
      } catch (PackageManager.NameNotFoundException e) {

      }

      NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
      Intent intent = new Intent(this, AllGeofencesActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

      Notification notification = new NotificationCompat.Builder(this)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentTitle(this.getResources().getString(R.string.Notification_Title))
              .setContentText(contextText)
              .setContentIntent(pendingNotificationIntent)
              .setStyle(new NotificationCompat.BigTextStyle().bigText(contextText))
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setAutoCancel(true)
              .build();
      notificationManager.notify(0, notification);



    }
  }

  private void onError(int i) {
    Log.e(TAG, "Geofencing Error: " + i);
  }

  // endregion
}

