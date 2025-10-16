package edu.monash.service;

import edu.monash.repo.MembershipsRepo;
import java.time.LocalDate;


public class MembershipService {
    private final MembershipsRepo repo;
    private final AccountService accounts;
    public MembershipService(MembershipsRepo repo, AccountService accounts){ this.repo = repo; this.accounts = accounts; }

    
    public String purchase(String email){
        if (!accounts.deduct(email, 20.0)) return "Insufficient funds";
        var today = LocalDate.now();
        var end = today.plusYears(1);
        repo.membership.put(email, new MembershipsRepo.Row(email, "VIP", today, end, "ACTIVE"));
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }

    
    public String renew(String email){
        var r = repo.membership.get(email);
        if (r == null || !"ACTIVE".equalsIgnoreCase(r.status)) return "No active membership";
        if (!accounts.deduct(email, 20.0)) return "Insufficient funds";
        r.endDate = r.endDate==null ? LocalDate.now().plusYears(1) : r.endDate.plusYears(1);
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }

    
    public String cancel(String email){
        var r = repo.membership.get(email);
        if (r == null) return "No membership";
        r.status = "CANCELLED";
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }
}
