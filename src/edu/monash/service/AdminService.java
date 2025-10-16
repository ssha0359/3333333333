package edu.monash.service;

import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;


public class AdminService {
    private final ProductsRepo products;
    public AdminService(ProductsRepo products){ this.products = products; }

    public String addProduct(edu.monash.domain.Product p) {
        if (p == null || p.id == null || p.id.isBlank()) return "Invalid product id";
        if (p.name == null || p.name.isBlank()) return "Invalid name";
        if (p.category == null || p.category.isBlank()) return "Invalid category";
        if (p.price < 0 || p.memberPrice < 0) return "Invalid price";
        if (p.memberPrice > p.price) return "memberPrice cannot be greater than price";
        if (p.stock < 0) return "Invalid stock";
        if (products.products.containsKey(p.id)) return "Product already exists";

        String newCatNorm = p.category.trim().toLowerCase();

        java.util.Set<String> cats = new java.util.HashSet<>();
        for (var x : products.products.values()) {
            if (x.category == null) continue;
            String c = x.category.trim().toLowerCase();
            if (!c.isBlank()) cats.add(c);
        }

        boolean introducesNewCategory = !newCatNorm.isBlank() && !cats.contains(newCatNorm);
        if (introducesNewCategory && cats.size() >= 10) {
            return "Add failed: category limit is 10.";
        }

        p.id = p.id.trim();
        p.name = p.name.trim();
        p.category = p.category.trim();
        if (p.subcategory != null) p.subcategory = p.subcategory.trim();
        if (p.brand != null) p.brand = p.brand.trim();
        if (p.description != null) p.description = p.description.trim();
        if (p.expiry != null && p.expiry.isBlank()) p.expiry = null;
        if (p.ingredients != null && p.ingredients.isBlank()) p.ingredients = null;
        if (p.storage != null && p.storage.isBlank()) p.storage = null;
        if (p.allergens != null && p.allergens.isBlank()) p.allergens = null;

        products.products.put(p.id, p);
        try { products.save(); } catch (Exception ignored) {}
        return "OK";
    }


    public String editProduct(Product p){
        if (p == null || p.id == null || p.id.isBlank()) return "Invalid product id";
        if (!products.products.containsKey(p.id)) return "Product not found";

        String newCat = (p.category == null ? "" : p.category.trim().toLowerCase());

        // count per-category, ignoring blank
        java.util.Map<String,Integer> count = new java.util.HashMap<>();
        for (var x : products.products.values()) {
            if (x.category == null) continue;
            String c = x.category.trim().toLowerCase();
            if (c.isBlank()) continue;
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        int distinct = count.size();

        var old = products.products.get(p.id);
        String oldCat = (old.category == null ? "" : old.category.trim().toLowerCase());

        boolean categoryChanged = !oldCat.equals(newCat); // true only if real change
        if (categoryChanged) {
            boolean newCatIsNonBlank = !newCat.isBlank();
            boolean newCatIsNew = newCatIsNonBlank && !count.containsKey(newCat);
            boolean oldCatNonBlank = !oldCat.isBlank();
            boolean oldCatWouldDisappear = oldCatNonBlank && count.getOrDefault(oldCat, 0) == 1;

            int projectedDistinct = distinct
                    + (newCatIsNew ? 1 : 0)
                    - (oldCatWouldDisappear ? 1 : 0);

            if (projectedDistinct > 10) {
                return "Edit failed: category limit is 10.";
            }
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

    // Count products per normalized (lower-cased, trimmed) category
    private java.util.Map<String, Integer> categoryCounts() {
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        for (var p : products.products.values()) {
            String c = p.category == null ? "" : p.category.trim().toLowerCase();
            if (c.isEmpty()) continue;
            m.put(c, m.getOrDefault(c, 0) + 1);
        }
        return m;
    }

    private String normCat(String c) {
        return (c == null ? "" : c.trim().toLowerCase());
    }

}
