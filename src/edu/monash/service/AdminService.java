package edu.monash.service;

import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;


public class AdminService {
    private final ProductsRepo products;
    public AdminService(ProductsRepo products){ this.products = products; }

    public String addProduct(Product p){
        if (products.products.containsKey(p.id)) return "Product already exists";

        String newCat = (p.category == null ? "" : p.category.trim().toLowerCase());

        java.util.Set<String> cats = new java.util.HashSet<>();
        for (var x : products.products.values()) {
            if (x.category != null && !x.category.isBlank()) {
                cats.add(x.category.trim().toLowerCase());
            }
        }

        if (!newCat.isBlank() && !cats.contains(newCat) && cats.size() >= 10) {
            return "Add failed: category limit is 10.";
        }

        products.products.put(p.id, p);
        try { products.save(); } catch (Exception ignored){}
        return "OK";
    }


    public String editProduct(Product p){
        if (!products.products.containsKey(p.id)) return "Product not found";

        String newCat = (p.category == null ? "" : p.category.trim().toLowerCase());

        java.util.Map<String,Integer> count = new java.util.HashMap<>();
        for (var x : products.products.values()) {
            String c = (x.category == null ? "" : x.category.trim().toLowerCase());
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        int distinct = count.size();

        var old = products.products.get(p.id);
        String oldCat = (old.category == null ? "" : old.category.trim().toLowerCase());

        boolean introducesNewCategory = newCat.length() > 0 && !count.containsKey(newCat);
        boolean oldCatWillDisappear = oldCat.equals(newCat) || count.getOrDefault(oldCat,0) == 1;

        if (introducesNewCategory && !(oldCatWillDisappear || distinct < 10)) {
            return "Edit failed: category limit is 10.";
        }

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
