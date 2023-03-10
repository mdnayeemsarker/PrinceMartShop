package com.princemartbd.shop.model;

public class OffersModel {

    final String id;
    final String name;
    final String start;
    final String end;
    final String type;
    final String amount;
    final String is_active;
    final String date_created;

    public OffersModel(String id, String name, String start, String end, String type, String amount, String is_active, String date_created) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.amount = amount;
        this.is_active = is_active;
        this.date_created = date_created;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }

    public String getIs_active() {
        return is_active;
    }

    public String getDate_created() {
        return date_created;
    }
}
