package com.example.calendar2;

public class DeleteInfo {

    String date;
    String name;
    String count;
    String price;

    public DeleteInfo(String date, String name, String count, String price) {
        this.date = date;
        this.name = name;
        this.count = count;
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount() {
        this.count = count;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
