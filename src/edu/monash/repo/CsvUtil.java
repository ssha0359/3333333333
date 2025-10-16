package edu.monash.repo;


public class CsvUtil {
    
    public static String esc(String s){
        if (s == null) return "";
        boolean need = s.contains(",") || s.contains("\"") || s.contains("\n");
        if (!need) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
