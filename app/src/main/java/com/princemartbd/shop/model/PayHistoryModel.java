package com.princemartbd.shop.model;

public class PayHistoryModel {

    String id, user_id, seller_id, tran_id, status, discount, amount, date_created, cashback, payable;

    public PayHistoryModel(String id, String user_id, String seller_id, String tran_id, String status, String discount, String amount, String date_created, String cashback, String payable) {
        this.id = id;
        this.user_id = user_id;
        this.seller_id = seller_id;
        this.tran_id = tran_id;
        this.status = status;
        this.discount = discount;
        this.amount = amount;
        this.date_created = date_created;
        this.cashback = cashback;
        this.payable = payable;
    }

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public String getTran_id() {
        return tran_id;
    }

    public String getStatus() {
        return status;
    }

    public String getDiscount() {
        return discount;
    }

    public String getAmount() {
        return amount;
    }

    public String getDate_created() {
        return date_created;
    }

    public String getCashback() {
        return cashback;
    }

    public String getPayable() {
        return payable;
    }
}
