package com.example.homeexpensesplitterpaymentreminder.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.homeexpensesplitterpaymentreminder.AlarmReceiver;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import java.util.Calendar;
import java.util.List;

public class AlarmManagerUtil {

    public static void scheduleAlarm(Context context, Expense expense) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("expense_id", expense.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                expense.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(expense.getDueDateMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9); // 9 AM reminder
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the date has passed, schedule for next month
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public static void cancelAlarm(Context context, Expense expense) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                expense.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    public static void rescheduleAllAlarms(Context context) {
        DataStorage dataStorage = new DataStorage(context);
        List<Expense> expenses = dataStorage.getExpenses();

        for (Expense expense : expenses) {
            scheduleAlarm(context, expense);
        }
    }
}

