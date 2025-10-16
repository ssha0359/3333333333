package edu.monash.service;

import edu.monash.repo.AccountsRepo;


public class AccountService {
    private final AccountsRepo accounts;
    public AccountService(AccountsRepo accounts){ this.accounts = accounts; }

    
    public String topUp(String email, double amount){
        if (amount <= 0) return "Please enter a positive amount";
        if (amount > 1000) return "Please enter a smaller amount (max $1000 per top-up)";
        double cur = accounts.balance.getOrDefault(email, 0.0);
        accounts.balance.put(email, cur + amount);
        try { accounts.save(); } catch (Exception ignored){}
        return "OK";
    }

    
    public boolean deduct(String email, double amount){
        double cur = accounts.balance.getOrDefault(email, 0.0);
        if (cur < amount) return false;
        accounts.balance.put(email, cur - amount);
        try { accounts.save(); } catch (Exception ignored){}
        return true;
    }
}
