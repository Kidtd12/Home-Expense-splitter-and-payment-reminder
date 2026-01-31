package com.example.homeexpensesplitterpaymentreminder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import com.example.homeexpensesplitterpaymentreminder.utils.AlarmManagerUtil;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Hide action bar on home screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        requestNotificationPermission();
        AlarmManagerUtil.rescheduleAllAlarms(this);

        CardView membersCard = findViewById(R.id.card_members);
        CardView expensesCard = findViewById(R.id.card_expenses);
        CardView remindersCard = findViewById(R.id.card_reminders);
        CardView summaryCard = findViewById(R.id.card_summary);

        membersCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MembersActivity.class);
            startActivity(intent);
        });

        expensesCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ExpensesActivity.class);
            startActivity(intent);
        });

        remindersCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RemindersActivity.class);
            startActivity(intent);
        });

        summaryCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
}

