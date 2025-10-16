package edu.monash.repo;

import java.io.*;
import java.util.Date;


public class PaymentsRepo {
    private final File file;
    public PaymentsRepo(String dir){ this.file = new File(dir, "payments.csv"); }

    
    public void ensure() throws Exception { if(!file.exists()){ file.getParentFile().mkdirs(); try(PrintWriter pw=new PrintWriter(file)){ pw.println("orderId,pre,post,paidAt"); } } }

    
    public void append(String orderId, double pre, double post, Date paidAt) throws Exception {
        try (FileWriter fw = new FileWriter(file, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%.2f,%.2f,%d%n", orderId, pre, post, paidAt.getTime());
        }
    }
}
