package edu.monash.service;

import edu.monash.repo.UsersRepo;


public class AuthService {
    private final UsersRepo users;

    public AuthService(UsersRepo users) {
        this.users = users;
    }

    
    public boolean loginCustomer(String email, String pwd) {
        var r = users.users.get(email);
        return r != null && r.role.equals("CUSTOMER") && r.password.equals(pwd);
    }

    
    public boolean loginAdmin(String email, String pwd) {
        var r = users.users.get(email);
        return r != null && r.role.equals("ADMIN") && r.password.equals(pwd);
    }
}
