/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadbalancer;
/**
 * @author eceak & duygugenc
 */

import java.io.*;
import java.net.Socket;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server implements Runnable {
    ServerSocket welcomeSocket;
    Socket client;
    Loads Loads;
    Thread serverThread = null;
    int currentServerPort;

    public static void main(String[] args) throws IOException {
        Server server=new Server();
        server.welcomeTheServer();

    }

    public void welcomeTheServer(){
        clientJoins();
        try {
            DataOutputStream outToServer = new DataOutputStream(this.client.getOutputStream());
            outToServer.writeBytes("Server says hi" + '\n');
        }
        catch(IOException e){
            throw new RuntimeException("problem, cannot connect",e);
        }

    }

    Server(Loads Loads, int port) {
        this.currentServerPort = port;
        this.Loads = Loads;
    }
    Server(){

    }
    public void clientJoins(){
        try {
            this.client=new Socket("localhost", 8001);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startServer(){
        try{
            this.welcomeSocket = new ServerSocket(this.currentServerPort);
        }
        catch(IOException e){
            throw new RuntimeException("Cannot open port",e);
        }

    }

    @Override
    public void run() {
        Socket connectionSocket = null;
        this.serverThread = Thread.currentThread();
        Thread.currentThread().setName(Integer.toString(this.currentServerPort));
        startServer();
        String clientSentence;
        String name;
        BufferedReader inFromClient = null;
        while (true) {
            try {
                connectionSocket = welcomeSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                name = inFromClient.readLine();
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
                clientSentence = inFromClient.readLine();
                //System.out.println(name + " wants: " + clientSentence);

                if ("list directory".equals(clientSentence)){
                    Loads.incrementLoad(currentServerPort,1);
                    System.out.println("Directory listing is about to start");
                    try {
                        serverThread.sleep( 5*1000);
                    }
                    catch(InterruptedException  e){
                        System.out.println("interrupted");
                    }
                    System.out.println("Directory is successfully listed at server:"+this.currentServerPort);
                    connectionSocket.close();
                    Loads.resetLoad(currentServerPort);

                }
                else if("transfer file".equals(clientSentence)) {
                    Loads.incrementLoad(currentServerPort,2);
                    System.out.println("File transfer is about to start");
                    try {
                        serverThread.sleep( 20*1000);
                    }
                    catch(InterruptedException  e){
                        System.out.println("interrupted");
                    }
                    System.out.println("File is successfully transferred to appropiate location at server:"+this.currentServerPort);
                    connectionSocket.close();
                    Loads.resetLoad(currentServerPort);

                }
                else if("compute".equals(clientSentence)){
                    Loads.incrementLoad(currentServerPort,3);
                    System.out.println("Server will be busy for computing");
                    try {
                        serverThread.sleep( 60*1000);
                    }
                    catch(InterruptedException  e){
                        System.out.println("interrupted");
                    }
                    System.out.println("compution is successful by server:"+this.currentServerPort);
                    connectionSocket.close();
                    Loads.resetLoad(currentServerPort);
                }
                else{
                    outToClient.println("Request is not understood.");
                    connectionSocket.close();


                }


                // Request processed, decrement load of selected server.
                //Loads.decrementLoad(currentServer);


            }catch (IOException e) {
                e.printStackTrace();
            }


        }


    }
}

