package com.example.homeexpensesplitterpaymentreminder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SummaryAdapter adapter;
    private DataStorage dataStorage;
    private TextView tvTotalExpenses;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.summary_title);
        }

        dataStorage = new DataStorage(this);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        btnReset = findViewById(R.id.btn_reset_month);
        recyclerView = findViewById(R.id.recycler_summary);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnReset.setOnClickListener(v -> showResetDialog());

        loadSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showResetDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.start_new_month)
                .setMessage(R.string.reset_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dataStorage.resetPaymentStatus();
                    loadSummary();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void loadSummary() {
        List<Expense> expenses = dataStorage.getExpenses();
        List<Member> members = dataStorage.getMembers();

        double totalExpenses = 0;
        for (Expense expense : expenses) {
            totalExpenses += expense.getAmount();
        }

        tvTotalExpenses.setText(String.format(Locale.getDefault(), "%.2f Birr", totalExpenses));

        // Calculate who paid and who owes
        Map<String, Double> paidAmounts = new HashMap<>();
        Map<String, Double> owedAmounts = new HashMap<>();

        for (Member member : members) {
            paidAmounts.put(member.getId(), 0.0);
            owedAmounts.put(member.getId(), 0.0);
        }

        for (Expense expense : expenses) {
            double share = expense.calculateShare(members.size());
            String payerId = expense.getPayerId();

            // Payer paid the full amount upfront
            paidAmounts.put(payerId, paidAmounts.get(payerId) + expense.getAmount());

            // Calculate what each member owes
            for (Member member : members) {
                if (expense.isPaid(member.getId())) {
                    // Member has paid their share
                    paidAmounts.put(member.getId(), paidAmounts.get(member.getId()) + share);
                } else {
                    // Member still owes their share
                    owedAmounts.put(member.getId(), owedAmounts.get(member.getId()) + share);
                }
            }
        }

        // Calculate net balances
        List<BalanceItem> balanceItems = new ArrayList<>();
        for (Member member : members) {
            double paid = paidAmounts.get(member.getId());
            double owed = owedAmounts.get(member.getId());
            double net = paid - owed;
            balanceItems.add(new BalanceItem(member, paid, owed, net));
        }

        adapter = new SummaryAdapter(balanceItems, members, expenses);
        recyclerView.setAdapter(adapter);

        TextView emptyView = findViewById(R.id.tv_empty_summary);
        if (expenses.isEmpty() || members.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private static class BalanceItem {
        Member member;
        double paid;
        double owed;
        double net;

        BalanceItem(Member member, double paid, double owed, double net) {
            this.member = member;
            this.paid = paid;
            this.owed = owed;
            this.net = net;
        }
    }

    private class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ViewHolder> {
        private List<BalanceItem> balanceItems;
        private List<Member> memberList;
        private List<Expense> expenseList;

        public SummaryAdapter(List<BalanceItem> balanceItems, List<Member> memberList, List<Expense> expenseList) {
            this.balanceItems = balanceItems;
            this.memberList = memberList;
            this.expenseList = expenseList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_summary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BalanceItem item = balanceItems.get(position);

            holder.tvName.setText(item.member.getName());
            holder.tvIcon.setText("👤");
            holder.tvPaid.setText(String.format(Locale.getDefault(), "Paid: %.2f Birr", item.paid));
            holder.tvOwed.setText(String.format(Locale.getDefault(), "Owes: %.2f Birr", item.owed));

            if (item.net > 0) {
                holder.tvNet.setText(String.format(Locale.getDefault(), "Balance: +%.2f Birr", item.net));
                holder.tvNet.setTextColor(getResources().getColor(R.color.success_green, null));
            } else if (item.net < 0) {
                holder.tvNet.setText(String.format(Locale.getDefault(), "Balance: %.2f Birr", item.net));
                holder.tvNet.setTextColor(getResources().getColor(R.color.warning_red, null));
            } else {
                holder.tvNet.setText("Balance: 0.00 Birr");
                holder.tvNet.setTextColor(getResources().getColor(R.color.dark_gray, null));
            }

            // Show who owes to whom
            if (item.net < 0) {
                // This person owes money
                for (BalanceItem otherItem : balanceItems) {
                    if (otherItem.net > 0 && otherItem.member.getId().equals(item.member.getId()) == false) {
                        double amount = Math.min(Math.abs(item.net), otherItem.net);
                        holder.tvOwesTo.setText(String.format(Locale.getDefault(),
                                getString(R.string.owes_message), item.member.getName(),
                                String.format(Locale.getDefault(), "%.2f", amount),
                                otherItem.member.getName()));
                        holder.tvOwesTo.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            } else {
                holder.tvOwesTo.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return balanceItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon;
            TextView tvName;
            TextView tvPaid;
            TextView tvOwed;
            TextView tvNet;
            TextView tvOwesTo;

            ViewHolder(View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_member_icon);
                tvName = itemView.findViewById(R.id.tv_member_name);
                tvPaid = itemView.findViewById(R.id.tv_paid);
                tvOwed = itemView.findViewById(R.id.tv_owed);
                tvNet = itemView.findViewById(R.id.tv_net);
                tvOwesTo = itemView.findViewById(R.id.tv_owes_to);
            }
        }
    }
}

