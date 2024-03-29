@isTest
private class AccountTriggerTest {
    @isTest static void TestCreate200Records() {
       
        List<Account> accts = new List<Account>();
        for(Integer i=0; i < 200; i++) {
            Account acct = new Account( Name = 'TEST NAME' + i, BillingState='CA');
            accts.add(acct);
           
        }              
       
        Test.startTest();
        insert accts;                               
        Test.stopTest();
        
        List<Account> lstAccount  = [SELECT ShippingState FROM Account];
        for(Account a:lstAccount){
            system.assertEquals('CA', a.ShippingState , 'ERROR');
        }
        
    }
}