package edu.monash.repo;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import edu.monash.domain.PromoCode;


public class PromosRepo {
    
    public Map<String, PromoCode> promos = new HashMap<>();
    private final File file;
    public PromosRepo(String dir){ this.file = new File(dir, "promos.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("code,percent,scope,expiry");
                pw.println("PROMO10,10,ALL,2099-12-31");
                pw.println("FIRST_PICKUP,15,PICKUP,2099-12-31");
                pw.println("NEWMONASH20,20,PICKUP,2099-12-31");
            }
        }
    }

    
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while((line=br.readLine())!=null){
                String[] s = line.split(",", 4);
                LocalDate exp = (s.length>3 && !s[3].isBlank()) ? LocalDate.parse(s[3]) : null;
                promos.put(s[0].toUpperCase(), new PromoCode(s[0], Integer.parseInt(s[1]), s[2], exp));
            }
        }
    }
}
