package edu.monash.domain;


public class CartItem {
    
    public String productId;
    
    public int quantity;
    
    public double snapshotUnitPrice;
    
    public long addedAtMillis;

    
    public CartItem(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        this.productId = productId;
        this.quantity = quantity;
        this.addedAtMillis = System.currentTimeMillis();
    }
}
