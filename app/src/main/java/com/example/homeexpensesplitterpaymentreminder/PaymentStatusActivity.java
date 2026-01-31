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
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import java.util.List;
import java.util.Locale;

public class PaymentStatusActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PaymentStatusAdapter adapter;
    private DataStorage dataStorage;
    private Expense expense;
    private List<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_status);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.payment_status);
        }

        dataStorage = new DataStorage(this);
        String expenseId = getIntent().getStringExtra("expense_id");

        List<Expense> expenses = dataStorage.getExpenses();
        for (Expense exp : expenses) {
            if (exp.getId().equals(expenseId)) {
                expense = exp;
                break;
            }
        }

        if (expense == null) {
            finish();
            return;
        }

        members = dataStorage.getMembers();

        TextView tvTitle = findViewById(R.id.tv_payment_title);
        String title = expense.getType() + " - " + String.format(Locale.getDefault(), "%.2f Birr", expense.getAmount());
        tvTitle.setText(title);

        TextView tvShare = findViewById(R.id.tv_share_per_person);
        double share = expense.calculateShare(members.size());
        tvShare.setText(getString(R.string.share_per_person, String.format(Locale.getDefault(), "%.2f", share)));

        recyclerView = findViewById(R.id.recycler_payment_status);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentStatusAdapter(members, expense);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class PaymentStatusAdapter extends RecyclerView.Adapter<PaymentStatusAdapter.ViewHolder> {
        private List<Member> memberList;
        private Expense expenseItem;

        public PaymentStatusAdapter(List<Member> memberList, Expense expenseItem) {
            this.memberList = memberList;
            this.expenseItem = expenseItem;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment_status, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Member member = memberList.get(position);
            boolean isPaid = expenseItem.isPaid(member.getId());

            holder.tvName.setText(member.getName());
            holder.tvIcon.setText("👤");

            if (isPaid) {
                holder.tvStatus.setText("✅ " + getString(R.string.paid));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.success_green, null));
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.light_gray, null));
            } else {
                holder.tvStatus.setText("❌ " + getString(R.string.not_paid));
                holder.tvStatus.setTextColor(getResources().getColor(R.color.warning_red, null));
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.soft_red, null));
            }

            holder.itemView.setOnClickListener(v -> {
                if (isPaid) {
                    expenseItem.markUnpaid(member.getId());
                } else {
                    expenseItem.markPaid(member.getId());
                }
                dataStorage.updateExpense(expenseItem);
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon;
            TextView tvName;
            TextView tvStatus;

            ViewHolder(View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_member_icon);
                tvName = itemView.findViewById(R.id.tv_member_name);
                tvStatus = itemView.findViewById(R.id.tv_payment_status);
            }
        }
    }
}

