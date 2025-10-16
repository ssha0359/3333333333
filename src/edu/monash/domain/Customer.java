package edu.monash.domain;


public class Customer extends User {
    
    public Customer(String email, String password) {
        super(email, password, "CUSTOMER");
    }
}
