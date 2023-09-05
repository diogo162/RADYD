package teste;

import java.io.*;
import java.net.*;

public class MulticastServerProxy {
    public static void main(String[] args) {
        int serverPort = 12345; // Porta do servidor proxy
        
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Proxy server is listening on port " + serverPort);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Get client's username
                String username = in.readLine();
                System.out.println("Client " + username + " connected");

                // Handle incoming messages from the client and forward to multicast
                String message;
                while ((message = in.readLine()) != null) {
                    // Here, you can forward the received message to the appropriate multicast group
                    // using MulticastSocket, as shown in the previous examples
                    System.out.println(username + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
