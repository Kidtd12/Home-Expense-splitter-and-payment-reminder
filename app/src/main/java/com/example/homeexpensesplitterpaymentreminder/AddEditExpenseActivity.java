package com.example.homeexpensesplitterpaymentreminder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.example.homeexpensesplitterpaymentreminder.utils.DataStorage;
import com.example.homeexpensesplitterpaymentreminder.utils.AlarmManagerUtil;
import java.util.Calendar;
import java.util.List;

public class AddEditExpenseActivity extends AppCompatActivity {

    private Spinner spinnerType;
    private Spinner spinnerPayer;
    private EditText etAmount;
    private Button btnSelectDate;
    private Button btnSave;
    private DataStorage dataStorage;
    private List<Member> members;
    private Calendar selectedDate;
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            expenseId = getIntent().getStringExtra("expense_id");
            if (expenseId != null) {
                getSupportActionBar().setTitle("Edit Expense");
            } else {
                getSupportActionBar().setTitle("Add Expense");
            }
        }

        dataStorage = new DataStorage(this);
        members = dataStorage.getMembers();
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, 1); // Default to tomorrow

        spinnerType = findViewById(R.id.spinner_type);
        spinnerPayer = findViewById(R.id.spinner_payer);
        etAmount = findViewById(R.id.et_amount);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnSave = findViewById(R.id.btn_save_expense);

        setupTypeSpinner();
        setupPayerSpinner();
        setupDatePicker();
        setupSaveButton();

        if (expenseId != null) {
            loadExpense();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupTypeSpinner() {
        String[] types = {"Rent", "Electricity", "Water", "Food", "WiFi", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupPayerSpinner() {
        String[] memberNames = new String[members.size()];
        for (int i = 0; i < members.size(); i++) {
            memberNames[i] = members.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayer.setAdapter(adapter);
    }

    private void setupDatePicker() {
        updateDateButton();
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateButton();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateButton() {
        String dateStr = String.format("%02d/%02d/%04d",
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.YEAR));
        btnSelectDate.setText("Due Date: " + dateStr);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem().toString();
            String amountStr = etAmount.getText().toString().trim();
            int payerIndex = spinnerPayer.getSelectedItemPosition();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, R.string.error_add_expense, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                String payerId = members.get(payerIndex).getId();
                long dueDateMillis = selectedDate.getTimeInMillis();

                if (expenseId != null) {
                    // Update existing expense
                    List<Expense> expenses = dataStorage.getExpenses();
                    for (Expense expense : expenses) {
                        if (expense.getId().equals(expenseId)) {
                            AlarmManagerUtil.cancelAlarm(AddEditExpenseActivity.this, expense);
                            expense.setType(type);
                            expense.setAmount(amount);
                            expense.setDueDateMillis(dueDateMillis);
                            expense.setPayerId(payerId);
                            dataStorage.updateExpense(expense);
                            AlarmManagerUtil.scheduleAlarm(AddEditExpenseActivity.this, expense);
                            break;
                        }
                    }
                } else {
                    // Create new expense
                    Expense expense = new Expense(type, amount, dueDateMillis, payerId);
                    dataStorage.addExpense(expense);
                    AlarmManagerUtil.scheduleAlarm(AddEditExpenseActivity.this, expense);
                }

                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpense() {
        List<Expense> expenses = dataStorage.getExpenses();
        for (Expense expense : expenses) {
            if (expense.getId().equals(expenseId)) {
                // Set type
                String[] types = {"Rent", "Electricity", "Water", "Food", "WiFi", "Other"};
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equals(expense.getType())) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }

                // Set amount
                etAmount.setText(String.valueOf(expense.getAmount()));

                // Set payer
                for (int i = 0; i < members.size(); i++) {
                    if (members.get(i).getId().equals(expense.getPayerId())) {
                        spinnerPayer.setSelection(i);
                        break;
                    }
                }

                // Set date
                selectedDate.setTimeInMillis(expense.getDueDateMillis());
                updateDateButton();

                break;
            }
        }
    }
}

