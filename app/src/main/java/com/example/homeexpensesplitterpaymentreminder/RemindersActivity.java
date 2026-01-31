package com.example.homeexpensesplitterpaymentreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RemindersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RemindersAdapter adapter;
    private DataStorage dataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.reminders_title);
        }

        dataStorage = new DataStorage(this);
        recyclerView = findViewById(R.id.recycler_reminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadReminders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadReminders() {
        List<Expense> allExpenses = dataStorage.getExpenses();
        List<Expense> upcomingExpenses = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);

        for (Expense expense : allExpenses) {
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTimeInMillis(expense.getDueDateMillis());
            dueDate.set(Calendar.HOUR_OF_DAY, 0);
            dueDate.set(Calendar.MINUTE, 0);
            dueDate.set(Calendar.SECOND, 0);
            dueDate.set(Calendar.MILLISECOND, 0);

            if (dueDate.after(today) || dueDate.equals(today)) {
                if (dueDate.before(nextMonth) || dueDate.equals(nextMonth)) {
                    upcomingExpenses.add(expense);
                }
            }
        }

        adapter = new RemindersAdapter(upcomingExpenses);
        recyclerView.setAdapter(adapter);

        TextView emptyView = findViewById(R.id.tv_empty_reminders);
        if (upcomingExpenses.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ViewHolder> {
        private List<Expense> expenseList;

        public RemindersAdapter(List<Expense> expenseList) {
            this.expenseList = expenseList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reminder, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Expense expense = expenseList.get(position);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTimeInMillis(expense.getDueDateMillis());
            dueDate.set(Calendar.HOUR_OF_DAY, 0);
            dueDate.set(Calendar.MINUTE, 0);
            dueDate.set(Calendar.SECOND, 0);
            dueDate.set(Calendar.MILLISECOND, 0);

            String icon = getIconForType(expense.getType());
            holder.tvIcon.setText(icon);
            holder.tvType.setText(expense.getType());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(expense.getDueDateMillis()));

            if (dueDate.equals(today)) {
                holder.tvMessage.setText(getString(R.string.payment_due_today, expense.getType()));
                holder.tvMessage.setTextColor(getResources().getColor(R.color.warning_red, null));
                holder.tvDate.setText("⏰ Today");
            } else {
                holder.tvMessage.setText(getString(R.string.payment_due_soon, expense.getType(), dateStr));
                holder.tvMessage.setTextColor(getResources().getColor(R.color.dark_gray, null));
                holder.tvDate.setText("📅 " + dateStr);
            }

            holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f Birr", expense.getAmount()));
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }

        private String getIconForType(String type) {
            switch (type) {
                case "Rent": return "🏠";
                case "Electricity": return "⚡";
                case "Water": return "💧";
                case "Food": return "🍽️";
                case "WiFi": return "📶";
                default: return "💰";
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon;
            TextView tvType;
            TextView tvMessage;
            TextView tvDate;
            TextView tvAmount;

            ViewHolder(View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_reminder_icon);
                tvType = itemView.findViewById(R.id.tv_reminder_type);
                tvMessage = itemView.findViewById(R.id.tv_reminder_message);
                tvDate = itemView.findViewById(R.id.tv_reminder_date);
                tvAmount = itemView.findViewById(R.id.tv_reminder_amount);
            }
        }
    }
}

