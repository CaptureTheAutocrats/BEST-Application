package com.best.models;

import java.io.Serializable;

public class Cart implements Serializable {
    public int cart_item_id;
    public int user_id;
    public int product_id;
    public int quantity;
    public String updated_at;
    public Product product;
}