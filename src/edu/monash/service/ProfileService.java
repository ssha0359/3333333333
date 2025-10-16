package edu.monash.service;

import edu.monash.repo.AccountsRepo;
import edu.monash.repo.MembershipsRepo;


public class ProfileService {
    private final AccountsRepo accounts; private final MembershipsRepo memberships;
    public ProfileService(AccountsRepo accounts, MembershipsRepo memberships){ this.accounts=accounts; this.memberships=memberships; }

    
    public double getBalance(String email){ return accounts.balance.getOrDefault(email,0.0); }

    
    public boolean isMemberActive(String email){
        var r = memberships.membership.get(email);
        if (r==null) return false;
        return "ACTIVE".equalsIgnoreCase(r.status) && (r.endDate==null || !r.endDate.isBefore(java.time.LocalDate.now()));
    }

    
    public String membershipText(String email){
        var r = memberships.membership.get(email);
        if (r==null) return "No membership";
        return r.status + " (" + r.startDate + " ~ " + r.endDate + ")";
    }
}
