package edu.monash.service;

import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;


public class AdminService {
    private final ProductsRepo products;
    public AdminService(ProductsRepo products){ this.products = products; }

    
    public String addProduct(Product p){
        if (products.products.containsKey(p.id)) return "Product already exists";
        products.products.put(p.id, p);
        try { products.save(); } catch (Exception ignored){}
        return "OK";
    }

    
    public String editProduct(Product p){
        if (!products.products.containsKey(p.id)) return "Product not found";
        products.products.put(p.id, p);
        try { products.save(); } catch (Exception ignored){}
        return "OK";
    }

    
    public String deleteProduct(String id){
        if (products.products.remove(id) == null) return "Product not found";
        try { products.save(); } catch (Exception ignored){}
        return "OK";
    }
}
