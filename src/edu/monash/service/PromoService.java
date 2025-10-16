package edu.monash.service;

import edu.monash.repo.PromosRepo;
import edu.monash.repo.OrdersRepo;


public class PromoService {
    private final PromosRepo promos;
    private final OrdersRepo orders;

    public PromoService(PromosRepo promos, OrdersRepo orders){ this.promos = promos; this.orders = orders; }

    
    public double getDiscountPercent(String email, String code, boolean isPickup) {
        if (code == null || code.isBlank()) return 0.0;
        var r = promos.promos.get(code.toUpperCase());
        if (r == null) return 0.0;
        if (r.isExpired()) return 0.0;
        boolean first = orders.countOrdersByEmailSafe(email) == 0;
        if (("FIRST_PICKUP".equalsIgnoreCase(code) || "NEWMONASH20".equalsIgnoreCase(code))) {
            if (!isPickup || !first) return 0.0;
        }
        int pct = Math.max(0, Math.min(90, r.percent));
        return pct / 100.0;
    }
}
