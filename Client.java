/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadbalancer;



/**
 *
 * @author eceak & duygugenc
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable{
    String name;


    public static void main(String argv[]) throws Exception {
        String client;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the client name:");
        client = sc.next();
        new Thread(new Client(client)).start();

    }

    public Client(String name){
        this.name=name;
    }
    @Override
    public void run() {
        Thread.currentThread().setName(this.name);
        while(!Thread.currentThread().isInterrupted()) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcome "+this.name+"! Here are the lists of operations you can perform: ");
            System.out.println("Type(list) for list directories ");
            System.out.println("Type(transfer) for transferring a file ");
            System.out.println("Type(math) for basic math calculations ");
            System.out.println("Type(exit) to exit ");
            String clientAnswer = sc.next();


            if ("exit".equals(clientAnswer) ){
                System.out.println("Exited" );
                Thread.currentThread().interrupt();
            }
            if("list".equals(clientAnswer) || "transfer".equals(clientAnswer)  || "math".equals(clientAnswer) ) {
                try {
                    String request = "Request from Client";
                    Socket Clientcurrent = new Socket("localhost", 8001);
                    DataOutputStream outToServer = new DataOutputStream(Clientcurrent.getOutputStream());
                    outToServer.writeBytes(request + '\n');
                    System.out.println(request);
                    try {
                        System.out.println("sleeping ");
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread sleep interrupted");
                    }

                    outToServer = new DataOutputStream(Clientcurrent.getOutputStream());
                    outToServer.writeBytes(String.valueOf(clientAnswer + '\n'));

                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(Clientcurrent.getInputStream()));
                    int port = Integer.parseInt(inFromServer.readLine());
                    System.out.println(port);
                    System.out.println("Client is about to be connect to Port : "+port );
                    if("list".equals(clientAnswer) ){
                        Clientcurrent= new Socket("localhost",port);
                        outToServer = new DataOutputStream( Clientcurrent.getOutputStream());
                        outToServer.writeBytes(name + '\n');
                        try {
                            Thread.currentThread().sleep(1000);
                        } catch (InterruptedException e) {
                            System.out.println("Thread sleep interrupted");
                        }

                        outToServer = new DataOutputStream(Clientcurrent.getOutputStream());
                        outToServer.writeBytes("listing directories" + '\n');
                        System.out.println("hi, wait a little listing operation will be done in sec");
                    }
                    else if("transfer".equals(clientAnswer)){
                        Clientcurrent= new Socket("localhost",port);
                        outToServer = new DataOutputStream(Clientcurrent.getOutputStream());
                        outToServer.writeBytes("transferring files" + '\n');
                        System.out.println("just a min, file transfering operation will be done in sec");


                    }else if("math".equals(clientAnswer)) {
                        Clientcurrent= new Socket("localhost",port);
                        outToServer = new DataOutputStream(Clientcurrent.getOutputStream());
                        outToServer.writeBytes("computing" + '\n');
                        System.out.println("be patient, logical operations usually take longer time...");

                    }
                } catch (IOException e) {
                    System.out.println(e.getCause());
                }
            }
        }

    }
}

