package edu.monash.domain;


public class Administrator extends User {
    
    public Administrator(String email, String password) {
        super(email, password, "ADMIN");
    }
}
