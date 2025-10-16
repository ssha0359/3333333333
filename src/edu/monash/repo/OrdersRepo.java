package edu.monash.repo;

import java.io.*;
import java.util.*;
import edu.monash.domain.CartItem;


public class OrdersRepo {
    private final File orders, items;

    public OrdersRepo(String dir){
        this.orders = new File(dir, "orders.csv");
        this.items = new File(dir, "order_items.csv");
    }

    
    public void ensure() throws Exception {
        if (!orders.exists()) { orders.getParentFile().mkdirs(); try(PrintWriter pw=new PrintWriter(orders)){ pw.println("orderId,email,fulfilment,where,promo,subtotal,discount,fee,total"); } }
        if (!items.exists()) { items.getParentFile().mkdirs(); try(PrintWriter pw=new PrintWriter(items)){ pw.println("orderId,productId,qty"); } }
    }

    
    public String appendOrder(String email, String fulfilment, String where, String promo,
                              double subtotal, double discount, double fee, double total,
                              java.util.List<CartItem> cartItems) throws Exception {
        String id = "O" + System.currentTimeMillis();
        try (FileWriter fw = new FileWriter(orders, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f%n", id, email, fulfilment, CsvUtil.esc(where), promo==null?"":promo, subtotal, discount, fee, total);
        }
        try (FileWriter fw = new FileWriter(items, true); PrintWriter pw = new PrintWriter(fw)) {
            for (CartItem i : cartItems) {
                pw.printf("%s,%s,%d%n", id, i.productId, i.quantity);
            }
        }
        return id;
    }

    
    public int countOrdersByEmailSafe(String email){
        try (BufferedReader br = new BufferedReader(new FileReader(orders))) {
            br.readLine();
            int c=0; String line;
            while((line=br.readLine())!=null){
                String[] s = line.split(",", 3);
                if (s.length>1 && s[1].equals(email)) c++;
            }
            return c;
        } catch(Exception e){ return 0; }
    }

    public static class OrderRow {
        public final String orderId;
        public final String email;
        public final String fulfilment;   // PICKUP / DELIVERY
        public final String where;
        public final String promo;
        public final double subtotal, discount, fee, total;

        public OrderRow(String orderId, String email, String fulfilment, String where, String promo,
                        double subtotal, double discount, double fee, double total) {
            this.orderId = orderId; this.email = email; this.fulfilment = fulfilment; this.where = where; this.promo = promo;
            this.subtotal = subtotal; this.discount = discount; this.fee = fee; this.total = total;
        }
    }

    public List<OrderRow> listOrdersByEmail(String email) {
        File f = this.orders;
        List<OrderRow> out = new ArrayList<>();
        try (var br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] a = line.split(",", -1);
                // Expected columns (based on appendOrder signature):
                // orderId,email,fulfilment,where,promo,subtotal,discount,fee,total
                if (a.length < 9) continue;
                if (!email.equalsIgnoreCase(a[1])) continue;
                double sub = parseDouble(a[5]);
                double dis = parseDouble(a[6]);
                double fee = parseDouble(a[7]);
                double tot = parseDouble(a[8]);
                out.add(new OrderRow(a[0], a[1], a[2], a[3], a[4], sub, dis, fee, tot));
            }
        } catch (Exception ignore) {}
        return out;
    }

    private static double parseDouble(String s){ try{ return Double.parseDouble(s.trim()); }catch(Exception e){ return 0.0; } }
}
