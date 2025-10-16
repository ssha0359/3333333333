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

    
    public boolean remove(String email, String productId){
        return get(email).remove(productId);
    }

    
    public void clear(String email){ get(email).clear(); }
}
