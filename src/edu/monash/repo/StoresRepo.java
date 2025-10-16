package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.Store;


public class StoresRepo {
    
    public Map<String, Store> stores = new LinkedHashMap<>();
    private final File file;

    public StoresRepo(String dir) { this.file = new File(dir, "stores.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("id,name,address,hours,phone");
                pw.println("S1,Clayton Campus Store,21 College Walk,9am-5pm,+61 3 9905 0000");
                pw.println("S2,Caulfield Campus Store,24 Sir John Monash Dr,9am-5pm,+61 3 9903 0000");
            }
        }
    }

    
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 5);
                stores.put(s[0], new Store(s[0], s[1], s[2], s[3], s.length>4?s[4]:""));
            }
        }
    }
}
