package com.model;

public class CartItem {
    private String name;
    private String country;
    private String company;
    private String role;
    private String color;
    private boolean selected;

    public CartItem() {
    }

    public CartItem(String name, String country, String company, String role, String color, boolean selected) {
        this.name = name;
        this.country = country;
        this.company = company;
        this.role = role;
        this.color = color;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
