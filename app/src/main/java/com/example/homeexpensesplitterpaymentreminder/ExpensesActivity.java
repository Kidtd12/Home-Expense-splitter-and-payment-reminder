package com.example.homeexpensesplitterpaymentreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpensesAdapter adapter;
    private DataStorage dataStorage;
    private List<Expense> expenses;
    private List<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.expenses_title);
        }

        dataStorage = new DataStorage(this);
        recyclerView = findViewById(R.id.recycler_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add_expense);
        fab.setOnClickListener(v -> {
            if (dataStorage.getMembers().isEmpty()) {
                android.widget.Toast.makeText(this, R.string.error_no_members, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ExpensesActivity.this, AddEditExpenseActivity.class);
            startActivity(intent);
        });

        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadExpenses() {
        expenses = dataStorage.getExpenses();
        members = dataStorage.getMembers();
        adapter = new ExpensesAdapter(expenses, members);
        recyclerView.setAdapter(adapter);

        TextView emptyView = findViewById(R.id.tv_empty_expenses);
        if (expenses.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> {
        private List<Expense> expenseList;
        private List<Member> memberList;

        public ExpensesAdapter(List<Expense> expenseList, List<Member> memberList) {
            this.expenseList = expenseList;
            this.memberList = memberList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_expense, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Expense expense = expenseList.get(position);
            
            String icon = getIconForType(expense.getType());
            holder.tvIcon.setText(icon);
            holder.tvType.setText(expense.getType());
            holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f Birr", expense.getAmount()));
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.tvDueDate.setText("Due: " + sdf.format(new Date(expense.getDueDateMillis())));
            
            String payerName = getMemberName(expense.getPayerId());
            holder.tvPayer.setText("Pays first: " + payerName);
            
            int totalMembers = memberList.size();
            double share = expense.calculateShare(totalMembers);
            holder.tvShare.setText(String.format(Locale.getDefault(), "Share: %.2f Birr/person", share));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ExpensesActivity.this, PaymentStatusActivity.class);
                intent.putExtra("expense_id", expense.getId());
                startActivity(intent);
            });
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

        private String getMemberName(String memberId) {
            for (Member member : memberList) {
                if (member.getId().equals(memberId)) {
                    return member.getName();
                }
            }
            return "Unknown";
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon;
            TextView tvType;
            TextView tvAmount;
            TextView tvDueDate;
            TextView tvPayer;
            TextView tvShare;

            ViewHolder(View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_expense_icon);
                tvType = itemView.findViewById(R.id.tv_expense_type);
                tvAmount = itemView.findViewById(R.id.tv_expense_amount);
                tvDueDate = itemView.findViewById(R.id.tv_due_date);
                tvPayer = itemView.findViewById(R.id.tv_payer);
                tvShare = itemView.findViewById(R.id.tv_share);
            }
        }
    }
}

