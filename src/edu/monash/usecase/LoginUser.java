package edu.monash.usecase;

import edu.monash.service.AuthService;


public class LoginUser {
    private final AuthService auth;
    public LoginUser(AuthService auth){ this.auth = auth; }

    
    public boolean asCustomer(String email, String pwd){ return auth.loginCustomer(email, pwd); }
    
    public boolean asAdmin(String email, String pwd){ return auth.loginAdmin(email, pwd); }
}
