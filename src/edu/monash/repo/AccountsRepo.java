package edu.monash.repo;

import java.io.*;
import java.util.*;


public class AccountsRepo {
    
    public Map<String, Double> balance = new HashMap<>();
    private final File file;

    
    public AccountsRepo(String dir){ this.file = new File(dir, "accounts.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,balance");
                pw.println("student@student.monash.edu,1000");
                pw.println("staff@monash.edu,1000");
                pw.println("admin@monash.edu,1000");
            }
        }
    }

    
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 2);
                if (s.length < 2) continue;
                balance.put(s[0], Double.parseDouble(s[1]));
            }
        }
    }

    
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,balance");
            for (var e : balance.entrySet()) pw.printf("%s,%.2f%n", e.getKey(), e.getValue());
        }
    }
}
