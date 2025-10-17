package com.model;

public class CartItem {
    private String phoneNumber;
    private String networkProvider;
    private String type; // domestic, foreign, prepaid, postpaid
    private boolean selected;

    public CartItem() {
    }

    public CartItem(String phoneNumber, String networkProvider, String type, boolean selected) {
        this.phoneNumber = phoneNumber;
        this.networkProvider = networkProvider;
        this.type = type;
        this.selected = selected;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNetworkProvider() {
        return networkProvider;
    }

    public void setNetworkProvider(String networkProvider) {
        this.networkProvider = networkProvider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}