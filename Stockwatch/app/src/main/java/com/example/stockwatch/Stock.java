package com.example.stockwatch;

public class Stock {
    private String name;
    private String symbol;
    private double price;
    private double change;
    private double percentage;
    private String id;

    public Stock(String name, String symbol, double price, double change, double percentage, String id){
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.percentage = percentage;
        this.id = id;
    }

    public Stock (String name, String symbol, String id){
        this.name = name;
        this.symbol = symbol;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getChange() {
        return change;
    }

    public double getPercentage() {
        return percentage;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", price=" + price +
                ", change=" + change +
                ", percentage=" + percentage +
                '}';
    }
}
