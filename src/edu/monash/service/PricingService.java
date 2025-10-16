package edu.monash.service;

import edu.monash.domain.Product;


public class PricingService {
    
    public double unitPrice(Product p, boolean isMember) {
        return isMember ? p.memberPrice : p.price;
    }

    
    public double fulfilmentFee(String email, String fulfilment) {
        if ("PICKUP".equalsIgnoreCase(fulfilment)) return 0;
        boolean isStudent = email != null && (email.endsWith("@student.monash.edu") || email.endsWith("@monash.edu"));
        return isStudent ? 0.0 : 20.0;
    }

    
    public double studentPickupDiscountRate(String email, String fulfilment) {
        boolean isStudent = email != null && (email.endsWith("@student.monash.edu") || email.endsWith("@monash.edu"));
        if (isStudent && "PICKUP".equalsIgnoreCase(fulfilment)) return 0.05;
        return 0.0;
    }
}
