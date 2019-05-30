package com.example.grocery.Model;

public class Rating {
    private String userPhone; //both key and value
    private String groceryId;
    private String rateValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String groceryId, String rateValue, String comment) {
        this.userPhone = userPhone;
        this.groceryId = groceryId;
        this.rateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getGroceryId() {
        return groceryId;
    }

    public void setGroceryId(String groceryId) {
        this.groceryId = groceryId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
