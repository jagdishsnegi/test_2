public class AccountTriggerHandler {
    public static void CreateAccounts(List<Account> acctlist) {
        for (Account a : acctlist) {
            if(a.ShippingState != a.BillingState){
                a.ShippingState = a.BillingState ;
            }
        }
    }
    
    
}