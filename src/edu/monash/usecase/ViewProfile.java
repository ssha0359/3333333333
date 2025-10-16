package edu.monash.usecase;

import edu.monash.service.ProfileService;


public class ViewProfile {
    private final ProfileService profile;
    public ViewProfile(ProfileService profile){ this.profile = profile; }

    
    public String execute(String email){
        return "Email: " + email + "\n" +
               "Balance: $" + String.format("%.2f", profile.getBalance(email)) + "\n" +
               "Membership: " + profile.membershipText(email);
    }
}
