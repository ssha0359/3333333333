package edu.monash.domain;


public class Product {
    public String id;
    public String name;
    public String category;
    public String subcategory;
    public String brand;
    public String description;
    public double price;
    public double memberPrice;
    public int stock;

    public String expiry;
    public String ingredients;
    public String storage;
    public String allergens;

    
    public Product(String id, String name, String category, String subcategory, String brand, String description,
                   double price, double memberPrice, int stock,
                   String expiry, String ingredients, String storage, String allergens) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.memberPrice = memberPrice;
        this.stock = stock;
        this.expiry = expiry;
        this.ingredients = ingredients;
        this.storage = storage;
        this.allergens = allergens;
        validate();
    }

    
    private void validate() throws IllegalArgumentException {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product id and name are required");
        }
        if (price < 0 || memberPrice < 0) throw new IllegalArgumentException("Price must be >= 0");
        if (stock < 0) throw new IllegalArgumentException("Stock must be >= 0");
    }

    
    public void adjustStock(int delta) {
        int next = this.stock + delta;
        if (next < 0) throw new IllegalStateException("Insufficient inventory");
        this.stock = next;
    }
}
