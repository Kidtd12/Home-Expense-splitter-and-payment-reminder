package com.example.homeexpensesplitterpaymentreminder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.homeexpensesplitterpaymentreminder.models.Expense;
import com.example.homeexpensesplitterpaymentreminder.models.Member;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String PREFS_NAME = "HomeExpenseSplitterPrefs";
    private static final String KEY_MEMBERS = "members";
    private static final String KEY_EXPENSES = "expenses";
    private static final String KEY_LAST_RESET = "last_reset";

    private SharedPreferences prefs;
    private Gson gson;

    public DataStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Members
    public void saveMembers(List<Member> members) {
        String json = gson.toJson(members);
        prefs.edit().putString(KEY_MEMBERS, json).apply();
    }

    public List<Member> getMembers() {
        String json = prefs.getString(KEY_MEMBERS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Member>>(){}.getType();
        List<Member> members = gson.fromJson(json, type);
        return members != null ? members : new ArrayList<>();
    }

    public void addMember(Member member) {
        List<Member> members = getMembers();
        members.add(member);
        saveMembers(members);
    }

    public void updateMember(Member member) {
        List<Member> members = getMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(member.getId())) {
                members.set(i, member);
                break;
            }
        }
        saveMembers(members);
    }

    public void deleteMember(String memberId) {
        List<Member> members = getMembers();
        members.removeIf(m -> m.getId().equals(memberId));
        saveMembers(members);
    }

    // Expenses
    public void saveExpenses(List<Expense> expenses) {
        String json = gson.toJson(expenses);
        prefs.edit().putString(KEY_EXPENSES, json).apply();
    }

    public List<Expense> getExpenses() {
        String json = prefs.getString(KEY_EXPENSES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Expense>>(){}.getType();
        List<Expense> expenses = gson.fromJson(json, type);
        return expenses != null ? expenses : new ArrayList<>();
    }

    public void addExpense(Expense expense) {
        List<Expense> expenses = getExpenses();
        expenses.add(expense);
        saveExpenses(expenses);
    }

    public void updateExpense(Expense expense) {
        List<Expense> expenses = getExpenses();
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(expense.getId())) {
                expenses.set(i, expense);
                break;
            }
        }
        saveExpenses(expenses);
    }

    public void deleteExpense(String expenseId) {
        List<Expense> expenses = getExpenses();
        expenses.removeIf(e -> e.getId().equals(expenseId));
        saveExpenses(expenses);
    }

    // Monthly Reset
    public void resetPaymentStatus() {
        List<Expense> expenses = getExpenses();
        for (Expense expense : expenses) {
            expense.setPaymentStatus(new java.util.HashMap<>());
        }
        saveExpenses(expenses);
        prefs.edit().putLong(KEY_LAST_RESET, System.currentTimeMillis()).apply();
    }

    public long getLastResetTime() {
        return prefs.getLong(KEY_LAST_RESET, 0);
    }
}

