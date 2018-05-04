package de.ostfalia.mobile.orgelhelfer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.PlayerActivity;
import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.model.Constants;
import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;

import static de.ostfalia.mobile.orgelhelfer.model.Constants.START_PLAYING_RECORDING;

/**
 * http://www.tutorialsface.com/2015/09/simple-android-foreground-service-example/
 */
public class MidiPlayerService extends Service {
    public static boolean IS_SERVICE_RUNNING = false;
    private final String LOG_TAG = MidiPlayerService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.MAIN_ACTION)) {
            Log.d(LOG_TAG, "Received Start Foreground Intent ");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotification(createNotificationChannel());
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                showNotification();
            }

            IS_SERVICE_RUNNING = true;
        } else if (intent.getAction().equals(
                Constants.STOP_PLAYING_RECORDING)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            IS_SERVICE_RUNNING = false;
            stopForeground(true);
            stopSelf();
        } else if (intent.getAction().equals(START_PLAYING_RECORDING)) {
            Log.d(LOG_TAG, "Start Playing the Recording");
            long startingTimestamp = PlayerActivity.recording.getStartingTimestamp();
            final MidiRecording recording = PlayerActivity.recording;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<MidiNote> notes = recording.getRecordingList();
                    long lastTimestamp = 0;
                    long nextTimestamp = 0;
                    for (int i = 0; i < notes.size(); i++) {
                        if (!(i + 1 < notes.size())) {
                            MidiDataManager.getInstance().sendEvent(notes.get(i));
                            return;
                        }

                        MidiDataManager.getInstance().sendEvent(notes.get(i));
                        nextTimestamp = notes.get(i + 1).getTimestamp();
                        lastTimestamp = notes.get(i).getTimestamp();
                        try {
                            Thread.sleep(nextTimestamp - lastTimestamp);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "rip");
                        }
                    }
                }
            });
        } else {
            Log.d(LOG_TAG, "Unkown Intent: " + intent.getAction().toString());
        }
        return START_STICKY;
    }

    private void showNotification() {
        showNotification(null);
    }

    private void showNotification(String channelid) {
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setAction(Constants.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        Intent playIntent = new Intent(this, MidiPlayerService.class);
        playIntent.setAction(START_PLAYING_RECORDING);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent stopIntent = new Intent(this, MidiPlayerService.class);
        stopIntent.setAction(Constants.STOP_PLAYING_RECORDING);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.orgelhelper_icon);

        Notification notification = new NotificationCompat.Builder(this, channelid)
                .setContentTitle("Midi Music Player")
                .setTicker("TutorialsFace Music Player")
                .setContentText("Playing Midi Recording")
                .setSmallIcon(R.mipmap.orgelhelper_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_pause, "Stop",
                        pstopIntent).build();
        startForeground(Constants.NOTIFICATION_ID,
                notification);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "my_service";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        IS_SERVICE_RUNNING = false;
    }
}
