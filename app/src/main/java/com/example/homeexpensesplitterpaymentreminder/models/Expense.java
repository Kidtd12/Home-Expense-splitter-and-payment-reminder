package com.example.homeexpensesplitterpaymentreminder.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Expense implements Serializable {
    private String id;
    private String type; // Rent, Electricity, Water, Food, WiFi, Other
    private double amount;
    private long dueDateMillis;
    private String payerId; // Who pays first
    private Map<String, Boolean> paymentStatus; // memberId -> paid/unpaid

    public Expense() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.paymentStatus = new HashMap<>();
    }

    public Expense(String type, double amount, long dueDateMillis, String payerId) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.type = type;
        this.amount = amount;
        this.dueDateMillis = dueDateMillis;
        this.payerId = payerId;
        this.paymentStatus = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public Map<String, Boolean> getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Map<String, Boolean> paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void markPaid(String memberId) {
        if (paymentStatus == null) {
            paymentStatus = new HashMap<>();
        }
        paymentStatus.put(memberId, true);
    }

    public void markUnpaid(String memberId) {
        if (paymentStatus == null) {
            paymentStatus = new HashMap<>();
        }
        paymentStatus.put(memberId, false);
    }

    public boolean isPaid(String memberId) {
        if (paymentStatus == null) {
            return false;
        }
        Boolean status = paymentStatus.get(memberId);
        return status != null && status;
    }

    public double calculateShare(int totalMembers) {
        if (totalMembers == 0) return 0;
        return amount / totalMembers;
    }
}

