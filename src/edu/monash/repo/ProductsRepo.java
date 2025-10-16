package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.Product;


public class ProductsRepo {
    
    public Map<String, Product> products = new LinkedHashMap<>();
    private final File file;

    public ProductsRepo(String dir){
        this.file = new File(dir, "products.csv");
    }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("id,name,category,subcategory,brand,description,price,memberPrice,stock,expiry,ingredients,storage,allergens");

                pw.println("P1001,Apple iPhone,Electronics,Phone,Apple,Smart phone,1299.0,1199.0,5,,,,");
                pw.println("P2001,Almond Milk,Food,Beverages,Monash,1L almond milk,3.5,3.0,10,2026-12-31,Almonds;Water,Keep refrigerated,Tree nuts");
            }
        }
    }

    
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = split(line);
                if (s.length < 13) continue;
                Product p = new Product(s[0], s[1], s[2], s[3], s[4], s[5],
                        Double.parseDouble(s[6]), Double.parseDouble(s[7]), Integer.parseInt(s[8]),
                        s[9], s[10], s[11], s[12]);
                products.put(p.id, p);
            }
        }
    }

    
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("id,name,category,subcategory,brand,description,price,memberPrice,stock,expiry,ingredients,storage,allergens");
            for (Product p : products.values()) {
                pw.printf("%s,%s,%s,%s,%s,%s,%.2f,%.2f,%d,%s,%s,%s,%s%n",
                        CsvUtil.esc(p.id), CsvUtil.esc(p.name), CsvUtil.esc(p.category), CsvUtil.esc(p.subcategory),
                        CsvUtil.esc(p.brand), CsvUtil.esc(p.description),
                        p.price, p.memberPrice, p.stock,
                        CsvUtil.esc(p.expiry), CsvUtil.esc(p.ingredients), CsvUtil.esc(p.storage), CsvUtil.esc(p.allergens));
            }
        }
    }

    
    private static String[] split(String line){
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q=false;
        for (int i=0;i<line.length();i++){
            char c = line.charAt(i);
            if (c=='"'){ q = !q; continue; }
            if (c==',' && !q){ out.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
