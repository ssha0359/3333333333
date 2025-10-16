package edu.monash.repo;

import java.io.*;
import java.time.LocalDate;
import java.util.*;


public class MembershipsRepo {
    
    public static class Row {
        public String email; public String type; public LocalDate startDate; public LocalDate endDate; public String status;
        public Row(String email, String type, LocalDate startDate, LocalDate endDate, String status){
            this.email=email; this.type=type; this.startDate=startDate; this.endDate=endDate; this.status=status;
        }
    }
    public Map<String, Row> membership = new HashMap<>();
    private final File file;

    public MembershipsRepo(String dir){ this.file = new File(dir, "memberships.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,type,startDate,endDate,status");
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
                LocalDate sdate = s[2].isBlank()? null : LocalDate.parse(s[2]);
                LocalDate edate = s[3].isBlank()? null : LocalDate.parse(s[3]);
                membership.put(s[0], new Row(s[0], s[1], sdate, edate, s[4]));
            }
        }
    }

    
    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,type,startDate,endDate,status");
            for (var r : membership.values()) {
                pw.printf("%s,%s,%s,%s,%s%n", r.email, r.type, r.startDate, r.endDate, r.status);
            }
        }
    }
}
