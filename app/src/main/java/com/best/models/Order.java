package com.best.models;

import android.hardware.usb.UsbRequest;

import java.io.Serializable;

public class Order implements Serializable {
    public int order_id;
    public int buyer_id;
    public int seller_id;
    public int product_id;
    public int quantity;
    public int total_amount;
    public String status;
    public String box_id;
    public String created_at;
    public String updated_at;
    public Product product;
    public User seller;
    public User buyer;
}
