package com.example.homeexpensesplitterpaymentreminder.models;

import java.io.Serializable;

public class Member implements Serializable {
    private String id;
    private String name;

    public Member() {
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public Member(String name) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

