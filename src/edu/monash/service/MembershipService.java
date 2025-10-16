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
        var r = repo.membership.get(email);
        if (r == null) {
            r = new MembershipsRepo.Row(email, "VIP", today, today.plusYears(1), "ACTIVE");
        } else {
            var base = (r.endDate != null && !r.endDate.isBefore(today)) ? r.endDate : today;
            r.status = "ACTIVE";
            if (r.startDate == null) r.startDate = today;
            r.endDate = base.plusYears(1);
        }
        repo.membership.put(email, r);
        try { repo.save(); } catch (Exception ignored){}
        return "OK";
    }

    public String renew(String email){
        if (!accounts.deduct(email, 20.0)) return "Insufficient funds";
        var today = LocalDate.now();
        var r = repo.membership.get(email);
        if (r == null) {
            r = new MembershipsRepo.Row(email, "VIP", today, today.plusYears(1), "ACTIVE");
        } else {
            var base = (r.endDate != null && !r.endDate.isBefore(today)) ? r.endDate : today;
            r.status = "ACTIVE";
            if (r.startDate == null) r.startDate = today;
            r.endDate = base.plusYears(1);
        }
        repo.membership.put(email, r);
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
