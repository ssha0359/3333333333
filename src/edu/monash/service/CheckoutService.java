package edu.monash.service;

import edu.monash.domain.*;
import edu.monash.repo.*;
import java.util.*;


public class CheckoutService {
    private final AccountsRepo accounts;
    private final ProductsRepo products;
    private final OrdersRepo orders;
    private final PaymentsRepo payments;
    private final ProfileService profile;
    private final PricingService pricing;
    private final PromoService promo;

    public CheckoutService(AccountsRepo accounts, ProductsRepo products, OrdersRepo orders, PaymentsRepo payments,
                           ProfileService profile, PricingService pricing, PromoService promo){
        this.accounts = accounts; this.products = products; this.orders = orders; this.payments = payments;
        this.profile = profile; this.pricing = pricing; this.promo = promo;
    }


    public static class Result {
        public String orderId; public double total; public double newBalance; public String message;
        public Result(String m){ this.message = m; }
        public Result(String id, double t, double nb){ this.orderId=id; this.total=t; this.newBalance=nb; this.message="OK"; }
    }


    public Result checkout(String email, ShoppingCart cart, String fulfilment, String where, String promoCode) throws Exception {
        if (cart==null || cart.getItems().isEmpty()) return new Result("Cart is empty");
        boolean isMember = profile.isMemberActive(email);
        boolean isPickup = "PICKUP".equalsIgnoreCase(fulfilment);

        double subtotal = 0.0;
        for (CartItem i : cart.getItems()) {
            Product p = products.products.get(i.productId);
            if (p == null) return new Result("Invalid product in cart");
            if (i.quantity > p.stock) return new Result("Insufficient stock for " + p.name);
            double unit = pricing.unitPrice(p, isMember);
            i.snapshotUnitPrice = unit;
            subtotal += unit * i.quantity;
        }

        /**
         * Builds a detailed order summary for the given cart and fulfilment options.
         * The summary includes the buyer's email, item list with quantities and line prices,
         * subtotal, discount, fulfilment fee, total and fulfilment information.
         */
        /**
         * Builds a detailed order summary for the given cart and fulfilment options.
         * The summary includes the buyer's email, item list with quantities and line prices,
         * subtotal, discount, fulfilment fee, total and fulfilment information.
         */

        double promoRate = promo.getDiscountPercent(email, promoCode, isPickup);
        double studentPickupRate = (promoCode==null || promoCode.isBlank()) ? pricing.studentPickupDiscountRate(email, fulfilment) : 0.0;
        double discountRate = Math.min(0.9, promoRate + studentPickupRate);
        double discount = subtotal * discountRate;

        double fee = pricing.fulfilmentFee(email, fulfilment);
        double total = Math.max(0, subtotal - discount + fee);

        double bal = accounts.balance.getOrDefault(email, 0.0);
        if (bal < total) return new Result("Insufficient funds, please top up!");

        accounts.balance.put(email, bal - total); accounts.save();
        for (CartItem i : cart.getItems()) {
            Product p = products.products.get(i.productId);
            p.adjustStock(-i.quantity);
        }
        products.save();

        String orderId = orders.appendOrder(email, fulfilment, where, promoCode, subtotal, discount, fee, total, cart.getItems());
        payments.append(orderId, bal, bal-total, new java.util.Date());
        cart.getItems().clear();
        return new Result(orderId, total, bal-total);
    }
    // Build an order summary string for preview/printing before checkout.
// It uses your existing pricing rules and promo logic.
    public String buildOrderSummary(
            String customerName,
            String email,
            ShoppingCart cart,
            String fulfilment,   // "PICKUP" or "DELIVERY"
            String where,        // store name/address (pickup) or delivery address
            String promoCode
    ) {
        StringBuilder sb = new StringBuilder(256);
        boolean isPickup = "PICKUP".equalsIgnoreCase(fulfilment);
        boolean isMember = false;
        try { isMember = profile.isMemberActive(email); } catch (Exception ignored) {}

        sb.append("===== Order Summary =====\n");
        sb.append("Customer: ").append((customerName == null || customerName.isBlank()) ? "(N/A)" : customerName).append("\n");
        sb.append("Email: ").append(email == null ? "(N/A)" : email).append("\n");
        sb.append("Fulfilment: ").append(isPickup ? "Pickup" : "Delivery").append("\n");
        sb.append(isPickup ? "Pickup Location: " : "Delivery Address: ")
                .append((where == null || where.isBlank()) ? "(N/A)" : where).append("\n\n");

        // Items and subtotal
        sb.append("Items:\n");
        sb.append(String.format(java.util.Locale.US, "%-28s %6s %10s %12s\n", "Name", "Qty", "Unit", "Line Total"));
        sb.append("----------------------------------------------------------------\n");

        double subtotal = 0.0;
        if (cart != null && cart.getItems() != null) {
            for (CartItem i : cart.getItems()) {
                Product p = products.products.get(i.productId);
                if (p == null || i.quantity <= 0) continue;

                double unit = pricing.unitPrice(p, isMember);
                double line = unit * i.quantity;
                subtotal += line;

                String nm = (p.name == null ? "Unknown" : p.name);
                if (nm.length() > 28) nm = nm.substring(0, 27) + "…";

                sb.append(String.format(java.util.Locale.US, "%-28s %6d %10.2f %12.2f\n", nm, i.quantity, unit, line));
            }
        }
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(java.util.Locale.US, "%-28s %6s %10s %12.2f\n\n", "Subtotal", "", "", subtotal));

        // Discounts (promo + student pickup, non-stacking as per spec)
        double promoRate = 0.0;
        if (promoCode != null && !promoCode.isBlank()) {
            try { promoRate = promo.getDiscountPercent(email, promoCode, isPickup); } catch (Exception ignored) {}
        }
        double studentPickupRate = (promoCode == null || promoCode.isBlank())
                ? pricing.studentPickupDiscountRate(email, fulfilment) : 0.0;

        // non-stacking: if promo applies, student pickup discount is disabled
        if (promoRate > 0.0) studentPickupRate = 0.0;

        double discountRate = Math.min(0.90, Math.max(0.0, promoRate + studentPickupRate));
        double discountAmt = subtotal * discountRate;

        if (promoRate > 0.0) {
            sb.append(String.format(java.util.Locale.US, "Promo (%s): -%.2f\n",
                    promoCode.toUpperCase(java.util.Locale.ROOT), subtotal * promoRate));
        }
        if (studentPickupRate > 0.0) {
            sb.append(String.format(java.util.Locale.US, "Student Pickup 5%%: -%.2f\n", subtotal * studentPickupRate));
        }

        // Fulfilment fee
        double fee = 0.0;
        try { fee = pricing.fulfilmentFee(email, fulfilment); } catch (Exception ignored) {}
        sb.append(String.format(java.util.Locale.US, "%s: +%.2f\n", isPickup ? "Pickup Fee" : "Delivery Fee", fee));

        // Total
        double total = Math.max(0.0, subtotal - discountAmt + fee);
        sb.append("----------------------------------------\n");
        sb.append(String.format(java.util.Locale.US, "Total Payable: %.2f\n", total));
        sb.append("========================================\n");

        return sb.toString();
    }

}
