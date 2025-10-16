package edu.monash.domain;

import java.util.*;


public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    public static final int MAX_ITEMS = 20;
    public static final int MAX_QTY_PER_ITEM = 10;

    
    public List<CartItem> getItems() {
        items.sort(Comparator.comparingLong(i -> i.addedAtMillis));
        return items;
    }

    
    public String addOrMerge(String productId, int qty) {
        if (qty <= 0) return "Please enter a positive quantity";
        for (CartItem i : items) {
            if (i.productId.equals(productId)) {
                if (i.quantity + qty > MAX_QTY_PER_ITEM) return "Max 10 units per product";
                i.quantity += qty;
                return "OK";
            }
        }
        if (items.size() >= MAX_ITEMS) return "Cart can hold at most 20 distinct products";
        if (qty > MAX_QTY_PER_ITEM) return "Max 10 units per product";
        items.add(new CartItem(productId, qty));
        return "OK";
    }

    
    public boolean remove(String productId) {
        return items.removeIf(i -> i.productId.equals(productId));
    }

    
    public void clear() { items.clear(); }
}
