package edu.monash.service;

import java.util.*;
import java.util.stream.Collectors;
import edu.monash.domain.Product;
import edu.monash.repo.ProductsRepo;


public class CatalogService {
    private final ProductsRepo products;
    public CatalogService(ProductsRepo products){ this.products = products; }

    
    public List<Product> listAll(){
        return sortByStock(new ArrayList<>(products.products.values()));
    }

    
    public List<Product> search(String keyword){
        String k = keyword==null? "": keyword.toLowerCase();
        return sortByStock(products.products.values().stream().filter(p ->
                p.name.toLowerCase().contains(k) ||
                (p.brand!=null && p.brand.toLowerCase().contains(k)) ||
                (p.category!=null && p.category.toLowerCase().contains(k)) ||
                (p.subcategory!=null && p.subcategory.toLowerCase().contains(k))
        ).collect(Collectors.toList()));
    }

    
    public List<Product> filter(String category, String brand, Double minPrice, Double maxPrice, Boolean inStockOnly){
        return sortByStock(products.products.values().stream().filter(p -> {
            if (category!=null && !category.isBlank() && !category.equalsIgnoreCase(p.category)) return false;
            if (brand!=null && !brand.isBlank() && (p.brand==null || !brand.equalsIgnoreCase(p.brand))) return false;
            if (minPrice!=null && p.price < minPrice) return false;
            if (maxPrice!=null && p.price > maxPrice) return false;
            if (inStockOnly!=null && inStockOnly && p.stock<=0) return false;
            return true;
        }).collect(Collectors.toList()));
    }

    private List<Product> sortByStock(List<Product> list){
        list.sort(Comparator.<Product>comparingInt(p -> p.stock<=0?1:0).thenComparing(p -> p.name.toLowerCase()));
        return list;
    }

    
    public Product getById(String id){ return products.products.get(id); }
}
