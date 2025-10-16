package edu.monash.service;

import edu.monash.domain.ShoppingCart;
import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;
import java.util.*;


public class CartService {
    private final ProductsRepo products;
    private final Map<String, ShoppingCart> carts = new HashMap<>(); // email -> cart

    public CartService(ProductsRepo products){ this.products = products; }

    
    public ShoppingCart get(String email){
        return carts.computeIfAbsent(email, k -> new ShoppingCart());
    }

    
    public String add(String email, String productId, int qty){
        Product p = products.products.get(productId);
        if (p == null) return "Product not found";
        var cart = get(email);
        return cart.addOrMerge(productId, qty);
    }

    // CartService.java —— add this method inside the class
    public String editQuantity(String email, String productId, int newQty) {
        var cart = get(email);
        var p = products.products.get(productId);
        if (p == null) return "Product not found";

        if (newQty <= 0) {
            // treat as remove
            boolean removed = remove(email, productId);
            return removed ? "Removed" : "Not found";
        }

        // clamp to per-item limit 10
        if (newQty > 10) newQty = 10;

        // stock check
        if (newQty > p.stock) return "Insufficient stock (" + p.stock + " available)";

        // find existing item
        edu.monash.domain.CartItem target = null;
        for (var it : cart.getItems()) {
            if (productId.equals(it.productId)) { target = it; break; }
        }

        if (target == null) {
            // adding as new line: respect 20 distinct items cap
            if (cart.getItems().size() >= 20) return "Cart item limit reached (max 20 items)";
            target = new edu.monash.domain.CartItem(productId, newQty);
            cart.getItems().add(target);
        } else {
            target.quantity = newQty;
        }
        return "Updated: " + productId + " -> " + newQty;
    }

    
    public boolean remove(String email, String productId){
        return get(email).remove(productId);
    }

    
    public void clear(String email){ get(email).clear(); }
}
