package edu.monash.repo;

import java.io.*;
import java.util.*;


public class UsersRepo {
    
    public static class Row {
        public String email; public String password; public String role;public String firstName;
        public String lastName;
        public String mobile;
        public Row(String e, String p, String r){ email=e; password=p; role=r; }
        public Row(String e, String p, String fn, String ln, String mb, String r) {
            this.email = e; this.password = p; this.firstName = fn; this.lastName = ln; this.mobile = mb; this.role = r;
        }
    }
    public Map<String, Row> users = new HashMap<>();
    private final File file;


    public UsersRepo(String dir){ this.file = new File(dir, "users.csv"); }

    
    public void ensure() throws Exception {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("email,password,firstName,lastName,mobile,role");
                pw.println("student@student.monash.edu,Monash1234!,Student,User,0400000001,CUSTOMER");
                pw.println("staff@monash.edu,Monash1234!,Staff,User,0400000002,CUSTOMER");
                pw.println("admin@monash.edu,Admin1234!,Admin,User,0400000000,ADMIN");
            }
        }
    }


    public void load() throws Exception {
        users.clear();
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) return;

            String[] cols = header.split(",", -1);
            int iEmail  = idx(cols, "email");
            int iPwd    = idx(cols, "password");
            int iRole   = idx(cols, "role");
            int iFN     = idx(cols, "firstname");
            int iLN     = idx(cols, "lastname");
            int iMobile = idx(cols, "mobile");

            String line;
            while ((line = br.readLine()) != null) {
                String[] a = line.split(",", -1);
                if (iEmail < 0 || iPwd < 0 || iRole < 0 || a.length < cols.length) continue;

                String email = a[iEmail];
                String pwd   = a[iPwd];
                String role  = a[iRole];
                String fn    = (iFN     >= 0 ? a[iFN]     : "");
                String ln    = (iLN     >= 0 ? a[iLN]     : "");
                String mb    = (iMobile >= 0 ? a[iMobile] : "");

                Row r = new Row(email, pwd, fn, ln, mb, role);
                users.put(email, r);
            }
        }
    }

    public void save() throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("email,password,firstName,lastName,mobile,role");
            for (Row r : users.values()) {
                pw.printf("%s,%s,%s,%s,%s,%s%n",
                        nz(r.email), nz(r.password), nz(r.firstName), nz(r.lastName), nz(r.mobile), nz(r.role));
            }
        }
    }

    private static String nz(String s){ return (s==null ? "" : s); }


    public static class AdminProfile {
        public final String email, password, firstName, lastName, mobile;
        public AdminProfile(String email, String password, String firstName, String lastName, String mobile) {
            this.email = email; this.password = password; this.firstName = firstName; this.lastName = lastName; this.mobile = mobile;
        }
        @Override public String toString() {
            return "Email: " + n(email) + "\n"
                    + "Password: " + n(password) + "\n"
                    + "First Name: " + n(firstName) + "\n"
                    + "Last  Name: " + n(lastName) + "\n"
                    + "Mobile: " + n(mobile);
        }
        private static String n(String s){ return (s==null||s.isBlank()) ? "(N/A)" : s; }
    }

    public AdminProfile getAdminProfileByEmail(String email) {
        java.io.File f = this.file;
        try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(
                new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return null;
            String[] cols = header.split(",", -1);
            int iEmail = idx(cols, "email"), iPwd = idx(cols, "password"),
                    iFn = idx(cols, "firstname"), iLn = idx(cols, "lastname"),
                    iMobile = idx(cols, "mobile"), iRole = idx(cols, "role");

            String line;
            while ((line = br.readLine()) != null) {
                String[] a = line.split(",", -1);
                if (a.length < cols.length) continue;
                if (!a[iEmail].equalsIgnoreCase(email)) continue;
                if (iRole >= 0 && !equalsIgnoreCase(a[iRole], "admin")) {
                }
                String pwd = (iPwd >= 0 ? a[iPwd] : "");
                String fn  = (iFn  >= 0 ? a[iFn]  : "");
                String ln  = (iLn  >= 0 ? a[iLn]  : "");
                String mb  = (iMobile >= 0 ? a[iMobile] : "");
                return new AdminProfile(a[iEmail], pwd, fn, ln, mb);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // helpers
    private static int idx(String[] cols, String want){
        for (int i=0;i<cols.length;i++){
            if (cols[i].trim().equalsIgnoreCase(want)) return i;
        }
        return -1;
    }
    private static boolean equalsIgnoreCase(String a, String b){ return a!=null && a.equalsIgnoreCase(b); }
}
