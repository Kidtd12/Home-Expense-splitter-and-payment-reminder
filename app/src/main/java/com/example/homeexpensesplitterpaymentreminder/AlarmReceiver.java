package com.example.homeexpensesplitterpaymentreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "payment_reminder_channel";
    private static final int NOTIFICATION_ID_BASE = 1000;

    /**
     * Get the alarm sound URI. Uses custom sound if available, otherwise uses default notification sound.
     * To add a custom alarm sound:
     * 1. Create folder: app/src/main/res/raw/
     * 2. Add your sound file (alarm_sound.mp3, alarm_sound.ogg, or alarm_sound.wav)
     * 3. Name it exactly: alarm_sound (without extension in code, Android will find it)
     */
    private Uri getAlarmSoundUri(Context context) {
        try {
            // Check if custom alarm sound exists in res/raw folder
            Resources resources = context.getResources();
            int soundId = resources.getIdentifier("alarm_sound", "raw", context.getPackageName());
            if (soundId != 0) {
                // Custom sound found - use it
                return Uri.parse("android.resource://" + context.getPackageName() + "/" + soundId);
            }
        } catch (Exception e) {
            // If there's any error, fall back to default
        }
        // If custom sound doesn't exist, use default notification sound
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String expenseId = intent.getStringExtra("expense_id");
        if (expenseId == null) return;

        DataStorage dataStorage = new DataStorage(context);
        List<Expense> expenses = dataStorage.getExpenses();

        Expense expense = null;
        for (Expense exp : expenses) {
            if (exp.getId().equals(expenseId)) {
                expense = exp;
                break;
            }
        }

        if (expense == null) return;

        createNotificationChannel(context);
        showNotification(context, expense);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Payment Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for payment due dates");
            channel.enableVibration(true);
            // Use custom alarm sound if available, otherwise use default notification sound
            channel.setSound(getAlarmSoundUri(context), null);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(Context context, Expense expense) {
        Intent intent = new Intent(context, RemindersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get payer name
        DataStorage dataStorage = new DataStorage(context);
        List<Member> members = dataStorage.getMembers();
        String payerName = "Unknown";
        for (Member member : members) {
            if (member.getId().equals(expense.getPayerId())) {
                payerName = member.getName();
                break;
            }
        }

        String title = "⚠️ " + expense.getType() + " payment due today";
        String message = String.format(Locale.getDefault(), 
            "Amount: %.2f Birr\nPayer: %s\nPlease pay on time!", 
            expense.getAmount(), payerName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Use custom alarm sound if available, otherwise use default notification sound
                .setSound(getAlarmSoundUri(context))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            int notificationId = NOTIFICATION_ID_BASE + expense.getId().hashCode();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}

