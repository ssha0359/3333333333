package edu.monash.repo;

import java.io.*;
import java.util.*;


public class UsersRepo {
    
    public static class Row {
        public String email; public String password; public String role;
        public Row(String e, String p, String r){ email=e; password=p; role=r; }
    }
    public Map<String, Row> users = new HashMap<>();
    private final File file;

    
    public UsersRepo(String dir){ this.file = new File(dir, "users.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,password,role");
                pw.println("student@student.monash.edu,Monash1234!,CUSTOMER");
                pw.println("staff@monash.edu,Monash1234!,CUSTOMER");
                pw.println("admin@monash.edu,Admin1234!,ADMIN");
            }
        }
    }

    
    public void load() throws Exception {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",", 3);
                if (s.length < 3) continue;
                users.put(s[0], new Row(s[0], s[1], s[2]));
            }
        }
    }

    
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,password,role");
            for (Row r : users.values()) pw.printf("%s,%s,%s%n", r.email, r.password, r.role);
        }
    }
}
