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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class LoadBalancer {
    private static void startLoadBalancer(String schedAlgo) throws IOException {
        ServerSocket welcomeSocket=new ServerSocket(8001);
        String clientRequest;
        int minload;
        int port=6900;

        try {
            ArrayList<Server> servers = new ArrayList<>();
            BufferedReader serverfile = null;
            // serverLoad object consists of a list of server loads, one int load for each server.
            int currentServer = 0;
            while (!Thread.interrupted()) {
                Loads serverLoad = new Loads(servers.size());
                System.out.println("Current server size:"+servers.size());

                // Accept a new client connection.
                Socket clientSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(),true);
                // getting client name to set up thread
                clientRequest = inFromClient.readLine();
                System.out.println(clientRequest);

                if (clientRequest.equals("Server says hi")){
                    servers.add(new Server(serverLoad,port));
                    System.out.println("new server is here with port number :" + port);
                    new Thread(new Server(serverLoad,port)).start();
                    port++;
                    servers.add(new Server(serverLoad,port));
                    port++;
                    servers.add(new Server(serverLoad,port));
                    port++;
                    clientSocket.close();
                }
                if (schedAlgo.equals("RR")) {
                    // When Round Robin" selected, select servers in a circular fashion.
                    currentServer = (currentServer + 1) % servers.size();
                    System.out.println("Selected server " + servers.get(currentServer).currentServerPort + " for request "+clientRequest);
                }
                else if (schedAlgo.equals("LC")) {
                    // When Least Connections selected, select servers with least active connections/requests, and
                    // increment its load
                    if(clientRequest.equals("Request from Client")){
                        inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        currentServer = serverLoad.getMinLoadServer();
                        int currLoad = serverLoad.getLoad(currentServer);
                        System.out.println("Selected server " + servers.get(currentServer).currentServerPort + " with load: " + currLoad + " for request "+clientRequest);
                        String port_ = Integer.toString(servers.get(currentServer).currentServerPort);
                        outToClient.println(port_);
                        System.out.println("assigned port is: "+ port_);


                        clientSocket.close();
                    }

                }

                // Open connection to selected server.
                //Socket runningServerSocket = new Socket(servers.get(currentServer).getHost(), servers.get(currentServer).getPort());


                // Start a new thread to serve this request.
                Thread lbRequestServer = new Thread(new Server(serverLoad, currentServer));
                currentServer++;
                lbRequestServer.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        // args[0] has the scheduling algorithm parameter(RR and LC, for Round-Robin and Least-Connections respectively).
        String schedAlgo = "LC";
        startLoadBalancer(schedAlgo);
    }
}


class Loads {
    private ArrayList<Integer> Loads = new ArrayList<>();

   Loads(int num_servers) {
        // Initialize loads of all servers to 0.
        for (int i = 0; i < num_servers; i++)
            Loads.add(0);
    }

    int getLoad(int index){
        return Loads.get(index);
    }

    // Find servers with minimum load.
    synchronized int getMinLoadServer() {
        int minLoad = Loads.get(0), min_ind = 0;
        for (int i = 1; i < Loads.size(); i++) {
            int thisLoad = Loads.get(i);
            if (thisLoad < minLoad) {
                minLoad = thisLoad;
                min_ind = i;
            }
        }
        return min_ind;
    }


    synchronized void incrementLoad(int index,int increment){
        Loads.set(index, Loads.get(index) + increment);
    }
    synchronized void resetLoad(int index){
        Loads.set(index, 0);
    }

    synchronized void decrementLoad(int index){
        Loads.set(index, Loads.get(index) - 1);
    }

}
