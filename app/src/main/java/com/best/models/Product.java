package com.best.models;

import java.io.Serializable;

public class Product implements Serializable {
    public String product_id;
    public int user_id;
    public String name;
    public String description;
    public int price;
    public String image_path;
    public int stock;
    public String product_condition;
    public String created_at;
    public String updated_at;
}