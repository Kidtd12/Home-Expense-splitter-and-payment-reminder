package com.example.homeexpensesplitterpaymentreminder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import java.util.ArrayList;
import java.util.List;

public class MembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MembersAdapter adapter;
    private DataStorage dataStorage;
    private List<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.members_title);
        }

        dataStorage = new DataStorage(this);
        recyclerView = findViewById(R.id.recycler_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addButton = findViewById(R.id.btn_add_member);
        addButton.setOnClickListener(v -> showAddMemberDialog(null));

        loadMembers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadMembers() {
        members = dataStorage.getMembers();
        adapter = new MembersAdapter(members);
        recyclerView.setAdapter(adapter);

        TextView emptyView = findViewById(R.id.tv_empty_members);
        if (members.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddMemberDialog(Member memberToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_member_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        if (memberToEdit != null) {
            etName.setText(memberToEdit.getName());
        }

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.error_add_member, Toast.LENGTH_SHORT).show();
                return;
            }

            if (memberToEdit != null) {
                memberToEdit.setName(name);
                dataStorage.updateMember(memberToEdit);
            } else {
                Member newMember = new Member(name);
                dataStorage.addMember(newMember);
            }

            dialog.dismiss();
            loadMembers();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
        private List<Member> memberList;

        public MembersAdapter(List<Member> memberList) {
            this.memberList = memberList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Member member = memberList.get(position);
            holder.tvName.setText(member.getName());
            holder.tvIcon.setText("👤");

            // Click on item to edit
            holder.itemView.setOnClickListener(v -> showAddMemberDialog(member));

            // Delete button click
            holder.btnDelete.setOnClickListener(v -> {
                // Check if member is involved in any expenses
                List<Expense> expenses = dataStorage.getExpenses();
                List<Expense> relatedExpenses = new ArrayList<>();
                for (Expense expense : expenses) {
                    if (expense.getPayerId().equals(member.getId())) {
                        relatedExpenses.add(expense);
                    }
                }

                String message;
                if (relatedExpenses.isEmpty()) {
                    message = "Delete " + member.getName() + "?";
                } else {
                    message = "Delete " + member.getName() + "?\n\n" +
                            "Warning: This member is the payer for " + relatedExpenses.size() + 
                            " expense(s). Those expenses will also be deleted.";
                }

                new AlertDialog.Builder(MembersActivity.this)
                        .setTitle("Delete Member")
                        .setMessage(message)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            // Delete related expenses first
                            for (Expense expense : relatedExpenses) {
                                dataStorage.deleteExpense(expense.getId());
                            }
                            // Then delete the member
                            dataStorage.deleteMember(member.getId());
                            loadMembers();
                            if (!relatedExpenses.isEmpty()) {
                                Toast.makeText(MembersActivity.this, 
                                    "Member and " + relatedExpenses.size() + " related expense(s) deleted", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });

            // Long press also shows delete dialog (keeping for convenience)
            holder.itemView.setOnLongClickListener(v -> {
                // Check if member is involved in any expenses
                List<Expense> expenses = dataStorage.getExpenses();
                List<Expense> relatedExpenses = new ArrayList<>();
                for (Expense expense : expenses) {
                    if (expense.getPayerId().equals(member.getId())) {
                        relatedExpenses.add(expense);
                    }
                }

                String message;
                if (relatedExpenses.isEmpty()) {
                    message = "Delete " + member.getName() + "?";
                } else {
                    message = "Delete " + member.getName() + "?\n\n" +
                            "Warning: This member is the payer for " + relatedExpenses.size() + 
                            " expense(s). Those expenses will also be deleted.";
                }

                new AlertDialog.Builder(MembersActivity.this)
                        .setTitle("Delete Member")
                        .setMessage(message)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            // Delete related expenses first
                            for (Expense expense : relatedExpenses) {
                                dataStorage.deleteExpense(expense.getId());
                            }
                            // Then delete the member
                            dataStorage.deleteMember(member.getId());
                            loadMembers();
                            if (!relatedExpenses.isEmpty()) {
                                Toast.makeText(MembersActivity.this, 
                                    "Member and " + relatedExpenses.size() + " related expense(s) deleted", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIcon;
            TextView tvName;
            MaterialButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvIcon = itemView.findViewById(R.id.tv_member_icon);
                tvName = itemView.findViewById(R.id.tv_member_name);
                btnDelete = itemView.findViewById(R.id.btn_delete_member);
            }
        }
    }
}

