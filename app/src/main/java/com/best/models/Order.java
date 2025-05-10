package com.best.models;

public class Order {
    public int order_id;
    public int buyer_id;
    public int seller_id;
    public int product_id;
    public int quantity;
    public int total_amount;
    public String status;
    public String box_id;
    public String pickup_code;
    public String created_at;
    public String updated_at;
    public Product product;
}
