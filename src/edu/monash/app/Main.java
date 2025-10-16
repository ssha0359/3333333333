package edu.monash.app;

import edu.monash.domain.*;
import edu.monash.repo.*;
import edu.monash.service.*;
import edu.monash.usecase.*;

import java.util.*;


public class Main {

    private static final String DATA_DIR = "data";
    private static final Scanner sc = new Scanner(System.in);

    
    public static void main(String[] args) throws Exception {

        var users = new UsersRepo(DATA_DIR);
        var accounts = new AccountsRepo(DATA_DIR);
        var memberships = new MembershipsRepo(DATA_DIR);
        var products = new ProductsRepo(DATA_DIR);
        var stores = new StoresRepo(DATA_DIR);
        var promos = new PromosRepo(DATA_DIR);
        var orders = new OrdersRepo(DATA_DIR);
        var payments = new PaymentsRepo(DATA_DIR);
        var memberHist = new MembershipHistoryRepo(DATA_DIR);
        memberHist.ensure();
        users.ensure(); accounts.ensure(); memberships.ensure(); products.ensure(); stores.ensure(); promos.ensure(); orders.ensure(); payments.ensure();
        users.load(); accounts.load(); memberships.load(); products.load(); stores.load(); promos.load();

        var auth = new AuthService(users);
        var profile = new ProfileService(accounts, memberships);
        var pricing = new PricingService();
        var promo = new PromoService(promos, orders);
        var catalog = new CatalogService(products);
        var cartSvc = new CartService(products);
        var accountSvc = new AccountService(accounts);
        var memberSvc = new MembershipService(memberships, accountSvc);
        var adminSvc = new AdminService(products);
        var checkout = new CheckoutService(accounts, products, orders, payments, profile, pricing, promo);

        var loginUC = new LoginUser(auth);
        var viewProfileUC = new ViewProfile(profile);

        while (true) {
            System.out.println("\n=== MMOSS ===");
            System.out.println("1) Customer Login");
            System.out.println("2) Admin Login");
            System.out.println("0) Exit");
            System.out.print("> ");
            String choice = sc.nextLine().trim();
            if ("0".equals(choice)) break;
            switch (choice) {
                case "1": doCustomerFlow(loginUC, catalog, cartSvc, checkout, viewProfileUC, accountSvc, memberSvc, stores,products,
                         pricing,
                         profile, memberHist, orders); break;
                case "2": doAdminFlow(loginUC, adminSvc, catalog,users); break;
                default: System.out.println("Unknown option");
            }
        }
        System.out.println("Goodbye.");
    }

    private static void doCustomerFlow(LoginUser loginUC, CatalogService catalog, CartService cartSvc,
                                       CheckoutService checkout, ViewProfile viewProfileUC,
                                       AccountService accountSvc, MembershipService memberSvc,
                                       StoresRepo stores,ProductsRepo products,
                                       PricingService pricing,
                                       ProfileService profile, MembershipHistoryRepo memberHist,OrdersRepo orders
                                       ) throws Exception {
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        if (!loginUC.asCustomer(email, pwd)) { System.out.println("Login failed."); return; }
        System.out.println("Welcome, " + email);

        while (true) {
            System.out.println("\n-- Customer Menu --");
            System.out.println("1) Browse products");
            System.out.println("2) Search by keyword");
            System.out.println("3) Cart");
            System.out.println("4) Checkout");
            System.out.println("5) Profile");
            System.out.println("6) Top up");
            System.out.println("7) Membership");
            System.out.println("8) Visit History (orders count)");
            System.out.println("9) Filter (cat/brand/price/in-stock)");
            System.out.println("10) History (orders & membership)");

            System.out.println("0) Logout");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) {
                cartSvc.clear(email);
                System.out.println("Logged out. Cart cleared.");
                break;
            }
            switch (c) {
                case "1" -> {
                    System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                            "ID", "Name", "Price", "Member", "Stock");
                    catalog.listAll().forEach(p -> {
                        String nm = (p.name == null ? "Unknown" : p.name);
                        if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
                        System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                                p.id, nm, p.price, p.memberPrice, p.stock);
                    });
                }

                case "2" -> {
                    System.out.print("keyword: "); String k = sc.nextLine();
                    System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                            "ID", "Name", "Price", "Member", "Stock");
                    catalog.search(k).forEach(p -> {
                        String nm = (p.name == null ? "Unknown" : p.name);
                        if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
                        System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                                p.id, nm, p.price, p.memberPrice, p.stock);
                    });
                }

                case "3" -> doCart(email, cartSvc, products, pricing, profile);
                case "4" -> doCheckout(email, cartSvc, checkout, stores);
                case "5" -> System.out.println(viewProfileUC.execute(email));
                case "6" -> { System.out.print("amount: "); double amt = Double.parseDouble(sc.nextLine()); System.out.println(accountSvc.topUp(email, amt)); }
                case "7" -> doMembership(email, memberSvc,memberHist);
                case "8" -> System.out.println("Orders placed: " + new OrdersRepo("data").countOrdersByEmailSafe(email));
                case "9" -> doFilter(catalog);
                case "10" -> doHistory(email,orders, memberHist);
                default -> System.out.println("Unknown option");
            }
        }
    }

    private static void doCart(String email,
                               CartService cartSvc,
                               ProductsRepo products,
                               PricingService pricing,
                               ProfileService profile) {
        boolean isMember = false;
        try { isMember = profile.isMemberActive(email); } catch (Exception ignored) {}

        while (true) {
            var cart = cartSvc.get(email);

            System.out.println("-- Cart --");
            // Header: ID | Name | Qty | Unit | Line | VIP Unit
            System.out.printf(java.util.Locale.US, "%-6s %-24s %6s %10s %12s %12s%n",
                    "ID", "Name", "Qty", "Unit", "Line", "VIP Unit");

            double subtotal = 0.0;
            for (var i : cart.getItems()) {
                var p = products.products.get(i.productId);
                if (p == null || i.quantity <= 0) continue;

                String nm = (p.name == null ? "Unknown" : p.name);
                if (nm.length() > 24) nm = nm.substring(0, 23) + "…";

                double unit = pricing.unitPrice(p, isMember);   // effective unit (member or regular)
                double line = unit * i.quantity;
                subtotal += line;

                System.out.printf(java.util.Locale.US, "%-6s %-24s %6d %10.2f %12.2f %12.2f%n",
                        p.id, nm, i.quantity, unit, line, p.memberPrice);
            }

            System.out.println("---------------------------------------------------------------------");
            System.out.printf(java.util.Locale.US, "%-6s %-24s %6s %10s %12.2f %12s%n",
                    "", "Subtotal", "", "", subtotal, "");

            System.out.println("1) Add");
            System.out.println("2) Edit quantity");
            System.out.println("3) Remove");
            System.out.println("4) Clear");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();

            if ("0".equals(c)) break;

            switch (c) {
                case "1" -> {
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.print("qty: "); int q = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(cartSvc.add(email, pid, q));
                }
                case "2" -> { // NEW: edit quantity
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.print("new qty (0 = remove): "); int q = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(cartSvc.editQuantity(email, pid, q));
                }
                case "3" -> {
                    System.out.print("productId: "); String pid = sc.nextLine().trim();
                    System.out.println(cartSvc.remove(email, pid) ? "Removed" : "Not found");
                }
                case "4" -> {
                    cartSvc.clear(email);
                    System.out.println("Cleared");
                }
                default -> { /* ignore */ }
            }
        }
    }



    private static void doCheckout(String email, CartService cartSvc, CheckoutService checkout, StoresRepo stores) throws Exception {
        var cart = cartSvc.get(email);
        System.out.print("Fulfilment (PICKUP/DELIVERY): "); String f = sc.nextLine().trim().toUpperCase();
        String where = "";
        if ("PICKUP".equalsIgnoreCase(f)) {
            System.out.println("Available stores:");
            for (var s : stores.stores.values()) {
                System.out.println(s.id + " | " + s.name + " | " + s.address + " | " + s.phone + " | " + s.hours);
            }
            System.out.print("Enter store id: "); String sid = sc.nextLine().trim();
            var st = stores.stores.getOrDefault(sid, null);
            where = (st==null) ? "Unknown store" : (st.name + " - " + st.address);
        } else {
            where = "Default Delivery Address";
        }
        System.out.print("Promo code (blank if none): "); String promo = sc.nextLine().trim();

        String summary = checkout.buildOrderSummary(null, email, cart, f, where, promo);
        System.out.println(summary);

        var res = checkout.checkout(email, cart, f, where, promo);
        if (!"OK".equals(res.message)) System.out.println(res.message);
        else {
            System.out.println("Order Confirmed: " + res.orderId);
            System.out.println("Total: $" + String.format("%.2f", res.total));
            System.out.println("New Balance: $" + String.format("%.2f", res.newBalance));
        }
    }

    private static void doMembership(String email,
                                     MembershipService memberSvc,
                                     MembershipHistoryRepo memberHist) {
        while (true) {
            System.out.println("-- Membership --");
            System.out.println("1) Purchase (multi-year)");
            System.out.println("2) Renew (multi-year)");
            System.out.println("3) Cancel");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) break;

            switch (c) {
                case "1" -> {
                    Integer years = askYears();
                    if (years == null) break; // back
                    int success = 0;
                    String lastMsg = "";
                    for (int i = 0; i < years; i++) {
                        lastMsg = memberSvc.purchase(email);
                        if (lastMsg == null || lastMsg.toLowerCase().startsWith("insufficient")) break;
                        success++;
                    }
                    if (success == years) {
                        System.out.println("Purchased " + years + " year(s).");
                        if (memberHist != null) {
                            memberHist.append(email, "PURCHASE", years, 20.0 * years, java.time.LocalDate.now());
                        }
                    } else if (success > 0) {
                        System.out.println("Partially purchased " + success + " year(s). Balance may be low.");
                        if (memberHist != null) {
                            memberHist.append(email, "PURCHASE", success, 20.0 * success, java.time.LocalDate.now());
                        }
                    } else {
                        System.out.println(lastMsg == null ? "Failed" : lastMsg);
                    }
                }
                case "2" -> {
                    Integer years = askYears();
                    if (years == null) break; // back
                    int success = 0;
                    String lastMsg = "";
                    for (int i = 0; i < years; i++) {
                        lastMsg = memberSvc.renew(email);
                        if (lastMsg == null || lastMsg.toLowerCase().startsWith("insufficient")) break;
                        success++;
                    }
                    if (success == years) {
                        System.out.println("Renewed " + years + " year(s).");
                        if (memberHist != null) {
                            memberHist.append(email, "RENEW", years, 20.0 * years, java.time.LocalDate.now());
                        }
                    } else if (success > 0) {
                        System.out.println("Partially renewed " + success + " year(s). Balance may be low.");
                        if (memberHist != null) {
                            memberHist.append(email, "RENEW", success, 20.0 * success, java.time.LocalDate.now());
                        }
                    } else {
                        System.out.println(lastMsg == null ? "Failed" : lastMsg);
                    }
                }
                case "3" -> {
                    String msg = memberSvc.cancel(email);
                    System.out.println(msg);
                    if (msg != null && !msg.toLowerCase().startsWith("insufficient")) {
                        if (memberHist != null) {
                            memberHist.append(email, "CANCEL", 0, 0.0, java.time.LocalDate.now());
                        }
                    }
                }
                default -> { /* ignore */ }
            }
        }
    }

    private static Integer askYears() {
        System.out.print("Years (enter 'b' to back): ");
        String s = sc.nextLine().trim();
        if (s.equalsIgnoreCase("b")) return null;
        try {
            int y = Integer.parseInt(s);
            if (y <= 0) { System.out.println("Years must be >= 1"); return null; }
            // 可选：上限保护，比如最多 10 年
            if (y > 10) { System.out.println("Max 10 years allowed; using 10."); y = 10; }
            return y;
        } catch (Exception e) {
            System.out.println("Invalid number");
            return null;
        }
    }




    private static void doFilter(CatalogService catalog){
        System.out.print("category (blank for all): "); String cat = sc.nextLine();
        System.out.print("brand (blank for all): "); String brand = sc.nextLine();
        System.out.print("min price (blank for none): "); String minS = sc.nextLine();
        System.out.print("max price (blank for none): "); String maxS = sc.nextLine();
        System.out.print("in-stock only? (y/n): "); String io = sc.nextLine().trim().toLowerCase();
        Double min = (minS.isBlank()? null : Double.parseDouble(minS));
        Double max = (maxS.isBlank()? null : Double.parseDouble(maxS));
        Boolean instock = ("y".equals(io)? Boolean.TRUE : ("n".equals(io)? Boolean.FALSE : null));
        System.out.printf(java.util.Locale.US, "%-6s %-28s %10s %12s %8s%n",
                "ID", "Name", "Price", "Member", "Stock");
        catalog.filter(cat, brand, min, max, instock).forEach(p -> {
            String nm = (p.name == null ? "Unknown" : p.name);
            if (nm.length() > 28) nm = nm.substring(0, 27) + "…";
            System.out.printf(java.util.Locale.US, "%-6s %-28s %10.2f %12.2f %8d%n",
                    p.id, nm, p.price, p.memberPrice, p.stock);
        });

    }

    private static void doAdminFlow(LoginUser loginUC, AdminService adminSvc, CatalogService catalog, UsersRepo users){
        System.out.print("Admin email: "); String email = sc.nextLine().trim();
        System.out.print("Password: "); String pwd = sc.nextLine().trim();
        if (!loginUC.asAdmin(email, pwd)) { System.out.println("Login failed."); return; }
        System.out.println("Welcome admin.");

        while (true) {
            System.out.println("\n-- Admin Panel --");
            System.out.println("1) List products");
            System.out.println("2) Add product");
            System.out.println("3) Edit product");
            System.out.println("4) Delete product");
            System.out.println("5) View profile");
            System.out.println("0) Back");
            System.out.print("> ");
            String c = sc.nextLine().trim();
            if ("0".equals(c)) break;
            switch (c) {
                case "1" -> catalog.listAll().forEach(p -> System.out.println(p.id + " | " + p.name + " | " + p.brand + " | $" + p.price + " | stock:" + p.stock));
                case "2" -> { var p = readProduct(); System.out.println(adminSvc.addProduct(p)); }
                case "3" -> { var p = readProduct(); System.out.println(adminSvc.editProduct(p)); }
                case "4" -> { System.out.print("productId: "); String id = sc.nextLine(); System.out.println(adminSvc.deleteProduct(id)); }
                case "5" -> {
                    var profile = users.getAdminProfileByEmail(email);
                    if (profile == null) System.out.println("(Profile not found)");
                    else {
                        System.out.println("\n-- Administrator Profile --");
                        System.out.println(profile.toString());
                    }
                }
                default -> System.out.println("Unknown option");
            }
        }
    }

    private static void doHistory(String email, OrdersRepo orders, MembershipHistoryRepo memberHist) {
        System.out.println("\n-- Order History --");
        var list = orders.listOrdersByEmail(email);
        if (list.isEmpty()) {
            System.out.println("(No orders)");
        } else {
            System.out.printf(java.util.Locale.US, "%-10s %-9s %-10s %-10s %-10s %-10s%n",
                    "OrderID", "Mode", "Subtotal", "Discount", "Fee", "Total");
            for (var r : list) {
                System.out.printf(java.util.Locale.US, "%-10s %-9s %10.2f %10.2f %10.2f %10.2f%n",
                        r.orderId, r.fulfilment, r.subtotal, r.discount, r.fee, r.total);
            }
        }

        System.out.println("\n-- Membership History --");
        var mh = memberHist.listByEmail(email);
        if (mh.isEmpty()) {
            System.out.println("(No membership records)");
        } else {
            System.out.printf(java.util.Locale.US, "%-10s %-8s %-8s %-12s%n", "Action", "Years", "Amount", "Date");
            for (var row : mh) {
                System.out.printf(java.util.Locale.US, "%-10s %-8d %-8.2f %-12s%n",
                        row.action, row.years, row.amount, row.date.toString());
            }
        }
    }


    private static Product readProduct() {
        String id = askNonBlank("id");
        String name = askNonBlank("name");
        String category = askNonBlank("category");

        System.out.print("subcategory (optional): ");
        String subcategory = sanitize(sc.nextLine());

        System.out.print("brand (optional): ");
        String brand = sanitize(sc.nextLine());

        System.out.print("description (optional): ");
        String description = sanitize(sc.nextLine());

        double price = askDouble("price (>=0)");
        double mprice = askDouble("memberPrice (>=0, <= price)");
        while (mprice > price) {
            System.out.println("memberPrice cannot be greater than price.");
            mprice = askDouble("memberPrice (>=0, <= price)");
        }

        int stock = askInt("stock (>=0)");

        System.out.print("expiry (YYYY-MM-DD, blank if N/A): ");
        String expiry = blankToNull(sc.nextLine().trim());

        System.out.print("ingredients (blank if N/A): ");
        String ingredients = blankToNull(sanitize(sc.nextLine()));

        System.out.print("storage (blank if N/A): ");
        String storage = blankToNull(sanitize(sc.nextLine()));

        System.out.print("allergens (blank if N/A): ");
        String allergens = blankToNull(sanitize(sc.nextLine()));

        return new Product(id, name, category, subcategory, brand, description,
                price, mprice, stock, expiry, ingredients, storage, allergens);
    }


    private static String askNonBlank(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine();
            if (s != null && !s.trim().isBlank()) return sanitize(s);
            System.out.println(label + " is required.");
        }
    }

    private static double askDouble(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v < 0) { System.out.println("Must be >= 0."); continue; }
                return v;
            } catch (Exception e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private static int askInt(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < 0) { System.out.println("Must be >= 0."); continue; }
                return v;
            } catch (Exception e) {
                System.out.println("Invalid integer.");
            }
        }
    }

    private static String sanitize(String s) {
        return (s == null) ? "" : s.replace(",", " ").trim();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
