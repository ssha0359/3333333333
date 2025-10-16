package edu.monash.domain;


public class User {
    
    public final String email;
    
    public final String password;
    
    public final String role;

    
    public String phone;
    
    public String address;

    
    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
