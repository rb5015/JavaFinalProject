/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.bankingapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rohan Bhalla and Mirna Ashour
 */

//SERVER SIDE CODE


//ServerSide Code in this Application
public class BankingApplication {

    public static void main(String[] args) {
        System.out.println("Banking Server:");
        
        //Connect to the database and see if it works
        ConnectDatabase.connection();
        
        //Start server
        //Socket connections code
        try {
            ServerSocket ss = new ServerSocket(5050);
            while (true){
                Socket sock = ss.accept(); //Blocking system call
                System.out.println("Got a connection from: "+sock.getInetAddress());
                ProcessConnection pConnect = new ProcessConnection(sock);
                ProcessConnection.conn = ConnectDatabase.c;
                pConnect.start();
            }       
        } catch (IOException ex) {
            System.out.println("Unable to bind to port!");
        }    
        
        
    }
    
    
    
}



//Create classes for all the entities probably (corresponding to tables in DB)
class user{
    int account_num;
    String first_name;
    String last_name;
    String username;
    String password;
    int balance;
    
    //Can have a constructor
}


class transaction{
    int trans_id;
    int account_num;
    int amount;
    String type;
    Time time;
    Date date;
    
    //Can have a constructor

}

class ProcessConnection extends Thread{
    static Connection conn;
    String userName;
    Socket sock;
    //Account number variable will be set when the user logs in
    //Use this variable for the actions of depositing, transfering and withdrawing from the account
    int accountNum;
    
    //Socket variables to be used throughout the class
    BufferedReader sin;
    PrintStream sout;
    ProcessConnection(Socket newSock){
        sock = newSock;
    }
    @Override
    public void run(){
        try{
            sin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            sout = new PrintStream(sock.getOutputStream());
            
            //Line for testing purposes
            sout.println("Welcome to Punjab National Bank server");
            sout.println("Would you like to register or login?");     
            
            String action =  sin.readLine();
            System.out.println(action);
            if(action.equalsIgnoreCase("REGISTER"))
            {
                
                Boolean register = registerUser();
                if(register)
                {
                    sout.println("Registered Successfully");
                    
                }
                else
                {
                    sout.println("Error Registering!");   
                }
                
                
            } else if(action.equalsIgnoreCase("LOGIN"))
            {
                boolean loginSuccess = false;
                do{
                     loginSuccess = authenticateUser();
                }
                while(!loginSuccess);
                
            }
            
           
            //First get username and password
//            userName = sin.readLine();
//            String pword = sin.readLine();
            
            //Line for testing purposes
//            System.out.println("Got uname: " + userName +" and pword: " + pword);
            
           
        }catch(Exception e){
            System.out.println(sock.getInetAddress()+" disconnected");
        }
    }
    
    //Not sure how to organize the file yet so just going
    //to implement most functions here and then take care of it later one
    
    //Register User Function
    boolean registerUser()
    {
        boolean registered = false;
        sout.println("Registration---------------");
        //Create a new user object
        user newUser = new user();
        
        //Give prompts and fill in its fields with user input
        try {
            sout.println("Enter First Name:");
            newUser.first_name = sin.readLine();
            sout.println("Enter Last Name:");
            newUser.last_name = sin.readLine();
            sout.println("Set Username:");
            newUser.username = sin.readLine();
            sout.println("Set Password:");
            newUser.password = sin.readLine();
            sout.println("Starting Balance with default amount: $200");
            newUser.balance = 200;            
        } catch (IOException ex) {
            System.out.println(sock.getInetAddress()+" disconnected");
        }
        //SQL query to insert into database
        //Name all the column names after auto incrment 
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO user (first_name, last_name, username, password, balance) VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, newUser.first_name);
            stmt.setString(2, newUser.last_name);
            stmt.setString(3, newUser.username);
            stmt.setString(4, newUser.password);
            stmt.setInt(5, newUser.balance);
            stmt.executeUpdate();
            registered = true;
          
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            System.out.println("SQL insert for registration didn't work");
        }
     
        return registered;
    }
    
    //Authenticate & Login Function for users
     Boolean authenticateUser()
    {
        String password = "";
        sout.println("Login---------------");
        //Get username and password from the user
        try {
            sout.println("Enter Username:");
            userName = sin.readLine();
            sout.println("Enter Password:");
            password = sin.readLine();
        } catch (IOException ex) {
            sout.println("Incorrect Username or Password!");
            Logger.getLogger(ProcessConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Connection c is the datbase connector
        //Need the socket of the client to be able to send it a message for connected or cancelled
        Boolean answer = false;
        try {
            //Query from the database and get            
            //Prepared statement is used to create parameterized queries
            PreparedStatement s = conn.prepareStatement("SELECT * FROM user WHERE username=? AND password=?");
            
            
            s.setString(1, userName);
            s.setString(2, password);
            ResultSet rs = s.executeQuery();
            answer = rs.next();
           
        } catch (SQLException ex) {
            System.out.println("Authentication problem!");
        }
        if(answer)
        {
            //reply with 200
            try {
                PrintStream sendMsg = new PrintStream(this.sock.getOutputStream());
                sendMsg.println("200");
//                System.out.println("200");
            } catch (IOException ex) {
                Logger.getLogger(ProcessConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            //reply with 500
            try {
                PrintStream sendMsg = new PrintStream(this.sock.getOutputStream());
                sendMsg.println("500");
//                System.out.println("500");
            } catch (IOException ex) {
                Logger.getLogger(ProcessConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //NOTE: Not yet making an entry in the logins table, fix the table format and call insertlogin with fixed query
        //Else: get account name using SQL query and then pass that into login table
        
        return answer;
    }
     
    void insertLogin()
    {
         
        try {
            //get current data/time for database field time
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            //Get ip address
            InetAddress ip = this.sock.getInetAddress();
            String ipString= ip.toString();
            //Get username and password --> have been currently passed in
            
            //SQL Query
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO login (login_id, account_num, date_time) VALUES (?, ?, ?)");
            stmt.setString(1, userName);
            stmt.setString(2, ipString);
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("SQL Logins table entry didn't work");
        }
    }
     
     //View Account Information Function
     void getAccInfo()
     {
         //SQL Query to get user's information
         
         //Get all the information send it using server output to client
         
         
     }
     
     //Deposit into Account Function 
     Boolean deposit(double amount)
     {
         Boolean deposited = false;
         PreparedStatement prepSelectStmt = null;
         PreparedStatement prepUpdateStmt = null;

         //
         try {
            // Select the user based on the account number
            String selectSql = "SELECT * FROM users WHERE account_num = ?";
            prepSelectStmt = conn.prepareStatement(selectSql);
            prepSelectStmt.setInt(1, accountNum);
            ResultSet resultSet = prepSelectStmt.executeQuery();

            //User existence check
            if (resultSet.next()) {
                // Get current balance from DB
                double currentBalance = resultSet.getDouble("balance");

                // Updated balance field insert into DB
                double newBalance = currentBalance + amount;

                // Update the user balance in DB
                String updateSql = "UPDATE users SET balance = ? WHERE account_num = ?";
                prepUpdateStmt = conn.prepareStatement(updateSql);
                prepUpdateStmt.setDouble(1, newBalance);
                prepUpdateStmt.setInt(2, accountNum);
                int rowsAffected = prepUpdateStmt.executeUpdate();
                System.out.println(rowsAffected + " rows updated");
                deposited = true;
                System.out.println("Deposited successfully. Account balance: " + newBalance);
            } else {
                System.out.println("Account number: " + accountNum + " is invalid.");
                
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } 
        return deposited;
    }
     //Withdraw from Account Function
     Boolean withdraw(double amount)
     {
         Boolean withdrawn = false;
         PreparedStatement prepSelectStmt = null;
         PreparedStatement prepUpdateStmt = null;

         //
         try {
            // Select the user based on the account number
            String selectSql = "SELECT * FROM users WHERE account_num = ?";
            prepSelectStmt = conn.prepareStatement(selectSql);
            prepSelectStmt.setInt(1, accountNum);
            ResultSet resultSet = prepSelectStmt.executeQuery();

            // User Existence check
            if (resultSet.next()) {
                // Get current balance from DB
                double currentBalance = resultSet.getDouble("balance");

                // updated balance for DB
                double newBalance = currentBalance - amount;

                //place new Balance into DB
                String updateSql = "UPDATE users SET balance = ? WHERE account_num = ?";
                prepUpdateStmt = conn.prepareStatement(updateSql);
                prepUpdateStmt.setDouble(1, newBalance);
                prepUpdateStmt.setInt(2, accountNum);
                int rowsAffected = prepUpdateStmt.executeUpdate();
                System.out.println(rowsAffected + " rows updated");
                //Current system doesn't prevent account balance from becoming negative
                withdrawn = true;
                System.out.println("Withdrawn successfully. Account balance: " + newBalance);
            } else {
                System.out.println("Account number: " + accountNum + " is invalid.");
               
            }
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        }
        return withdrawn;
    }
     
     //Transfer Between Accounts Function
     Boolean transfer(double amount, int transferToAcc)
     {
         Boolean transferred = false;
         PreparedStatement selectStmt = null;
         PreparedStatement updateStmt = null;
         try { 
             // Select the user to withdraw from their account
             String selectFromSql = "SELECT * FROM users WHERE account_num = ?";
             selectStmt = conn.prepareStatement(selectFromSql);
             selectStmt.setInt(1, accountNum);
             ResultSet fromResult = selectStmt.executeQuery();
 
             // User existence and balance checking in the if statements
             if (fromResult.next()) {
                 double fromBalance = fromResult.getDouble("balance");
                 if (fromBalance >= amount) {
                     // Generate updated balance after transfering 
                     double newFromBalance = fromBalance - amount;
 
                     // Update the "from" account's balance in the database
                     String updateFromSql = "UPDATE users SET balance = ? WHERE account_num = ?";
                     updateStmt = conn.prepareStatement(updateFromSql);
                     updateStmt.setDouble(1, newFromBalance);
                     updateStmt.setInt(2, accountNum);
                     int fromRowsAffected = updateStmt.executeUpdate();
                     System.out.println(fromRowsAffected + " rows updated for withdrawal");
 
                     // Select the user to whom to deposit
                     String selectToSql = "SELECT * FROM users WHERE account_num = ?";
                     selectStmt = conn.prepareStatement(selectToSql);
                     selectStmt.setInt(1, transferToAcc);
                     ResultSet toResult = selectStmt.executeQuery();
 
                     // Check if the "to" account exists
                     if (toResult.next()) {
                         double toBalance = toResult.getDouble("balance");
 
                         // Calculate the new balance for the "to" account
                         double newToBalance = toBalance + amount;
 
                         // Update the "to" account's balance in the database
                         String updateToSql = "UPDATE users SET balance = ? WHERE account_num = ?";
                         updateStmt = conn.prepareStatement(updateToSql);
                         updateStmt.setDouble(1, newToBalance);
                         updateStmt.setInt(2, transferToAcc);
                         int toRowsAffected = updateStmt.executeUpdate();
                         System.out.println(toRowsAffected + " rows updated for deposit");
 
                         System.out.println("Transfer successful. New balance for " + transferToAcc + ": " + newFromBalance);
                         System.out.println("New balance for " + transferToAcc + ": " + newToBalance);
                     } else {
                         System.out.println("User with account number " + transferToAcc + " not found.");
                     }
                 } else {
                     System.out.println("Insufficient balance in account " + accountNum + ".");
                 }
             } else {
                 System.out.println("User with account number " + accountNum + " not found.");
             }
         } catch (SQLException se) {
             // Handle errors for JDBC
             se.printStackTrace();
         } catch (Exception e) {
             // Handle errors for Class.forName
             e.printStackTrace();
         }
         return transferred;
         
     }


     //Generate PDF code (test first)
     Boolean generatePDF()
     {
        Boolean generated  = false;

        return generated;
     }


}

//Class and functions to connect to the database in the server side
class ConnectDatabase{
    static Connection c;
    static void connection()
    {
        try {
//            System.out.println("Chat server:");
            
            String username = "root";
            String password = "dab32borethalekalicar"; //
            String url = "jdbc:mysql://localhost:3306/banking_system";
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(url, username, password);
            
            //Add in later to attach to process connections
            //ProcessConnection.conn = c;
            if(c!= null)
            {
                //Connection successful then do the socket stuff
                System.out.print("DB Connection successful!\n");
            }
            else
            {
                System.out.print("Connection FAILED!");
            }
            
        } catch (ClassNotFoundException ex) {
            System.out.print("Class driver not working 1");
        } catch (SQLException ex) {
             System.out.print("SQL problem");
        }
        
    }
    
}







