// Author: 陳予瑞

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {
    public static final int cacheSize=50;
    public static final Map<String, Account> cacheMap = new HashMap<>();
    public static final LinkedList<String> orderList=new LinkedList<>();
    public static final ReentrantReadWriteLock lock=new ReentrantReadWriteLock();


    public static Account getInstance(String key) {
            Account account;


            // Read lock
            lock.readLock().lock();

            try{
                account = cacheMap.get(key);
                if (account!=null)      // Check if account exist If so...
                {
                    System.out.println("Cache hit for: "+key);  // Logs that it reaches cache
                    moveToRecent(key);   // And move to recent key
                    return account;   // Get cached account
                }
            }
            finally {
                lock.readLock().unlock();   // Release read lock
            }

            // Write Lock: create new accounts, and modify cache
            lock.writeLock().lock();
            try {
                // Check if another thread adds account waiting for lock
                account = cacheMap.get(key);
                if(account == null) {
                    System.out.println("Cache missed: creating new account: "+key);
                    account = new Account(key);  // Add account using key
                    account.init();   // Retrieve data
                    cacheMap.put(key, account);  // Add to order list
    
                    //Remove recently used account if cache exceed size
                    if(cacheMap.size() > cacheSize)
                    {
                        String oldestKey=orderList.removeFirst(); // Remove oldest and least used account.
                        cacheMap.remove(oldestKey);   // Remove oldest and least used account from cache
                        System.out.println("Removing recently used account");
                    }
                }
    
            }
            finally {
                lock.writeLock().unlock();  // Unlock when finished
            }

        return account;
    }

    public static void moveToRecent(String key)
    {
        lock.writeLock().lock();   //Write lock before modifying list

        try
        {
            orderList.remove(key);    // Remove key if exists in order list
            orderList.addLast(key);   // Readd ket
        }
        finally 
        {
            lock.writeLock().unlock();  //Release lock after modifying list
        }
    }


    private String key;
    private String cachedData;

    
    public String getCachedData()
    {
        return cachedData;   // Return cached data
    }




    public Account(String key) {
        this.key = key;
    }

    private void init()
    {
        this.cachedData="Account data: "+key; 
    }
    

    // Main Function
    public static void main (String[] args)
    {
        System.out.println("Welcome");

        //Create array of 50 accounts
        String accounts[]=new String[50];
        for(int i=0;i<accounts.length;i++)
            accounts[i]="Account_"+(i+1);

        Runnable task=()->{
            String accName=accounts[(int)(Math.random()*50)];   // Randomly select account
            Account account=Account.getInstance(accName);       // Get account
            System.out.println("Fetched: "+accName + " | Cached data: "+(account!=null ? account.getCachedData(): accName + " Evicted"));
        };

        //Initialize threads

        for (int i=0;i<50;i++)
        {
            Thread taskThread=new Thread(task);
            taskThread.start();
        }
 
        // // Create account 1
        // String Acc1Name=accounts[0];
        // Account Account1=Account.getInstance(Acc1Name);
        // System.out.println("Cached data: "+Account1.getCachedData());

        // //Create account 2
        // String Acc2Name=accounts[1];
        // Account Account2=Account.getInstance(Acc2Name);
        // System.out.println("Cached data: "+Account2.getCachedData());

        // //Get and create 50 accounts. The last one gets cache hit.
        // for (int i=0;i<50;i++)
        // {
        //     System.out.println("Account "+i);
        //     Account.getInstance(accounts[i]);
        // }
        
        // // Calling Account2 if it's evicted already
        // System.out.println("Let's call Account_2 again!");
        // String Acc1AName=accounts[1];
        // Account account1A=Account.getInstance(Acc1AName);
        // System.out.println("Cached data: "+(account1A!=null ? account1A.getCachedData(): Acc1AName + " Evicted"));
        // String Acc1BName=accounts[1];
        // Account account1B=Account.getInstance(Acc1BName);
        // System.out.println("Cached data: "+(account1B!=null ? account1B.getCachedData(): Acc1BName + " Evicted"));
    }
}

