/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bankingapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author ronballer
 */
public class BankCustomer {
    
    public static void main(String[] args) {
        
        //Put the socket stuff in a class/function and organize it better
        try {
            Socket s = new Socket("localhost", 5050);
//            Scanner input = new Scanner(s.getInputStream());
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter output = new PrintWriter(s.getOutputStream(), true);

            
            //Thread to display received messages
            ReponsesThread incomingMsg = new ReponsesThread(input);
            incomingMsg.start();
            
            //Receive and send user input
            String message = "";
            Scanner cin = new Scanner(System.in);
            while (!message.equals("EXIT")){
                cin = new Scanner(System.in);
                message = cin.nextLine();
                if(!message.equals("EXIT"))
                {
                    output.println(message);
                }
            }
            
            
        }
        catch (IOException ex) {
            System.out.println("Socket Problem");
        }
    }

}


//The ResponsesThread helps in being able to receive messages back from the server 
class ReponsesThread extends Thread {
    BufferedReader input;
    
    ReponsesThread(BufferedReader inputParam){
        input = inputParam;
    }
    
    @Override
        public void run() {
            try {
                // Read incoming messages from the server and print them to the console
                String message;
                while ((message = input.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (Exception e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }
}