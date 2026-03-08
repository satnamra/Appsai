package com.example.quicknotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL_ID = "quicknotes_reminders";
    static final String EXTRA_TITLE = "note_title";
    static final String EXTRA_NOTE_ID = "note_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID, "Note Reminders", NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(channel);

        Intent openIntent = new Intent(context, NoteActivity.class);
        openIntent.putExtra("note_id", noteId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, noteId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Reminder: " + title)
            .setContentText("Tap to open your note")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(noteId, builder.build());
    }
}
