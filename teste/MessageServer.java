package teste;

import java.io.*;
import java.net.*;
import java.util.*;

public class MessageServer {
    private static Map<String, List<PrintWriter>> groupMap = new HashMap<>();

    public static void main(String[] args) {
        int multicastPort = 12345; 
        final String ENDERECO_MULTICAST = "239.255.255.250";
        
        try {MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
            InetAddress group = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.joinGroup(group);

            System.out.println("Message server is listening on multicast group " + group + " and port " + multicastPort);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);
                new ClientHandler().start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String groupName;
        private String username;

        public ClientHandler() {
            
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                groupName = in.readLine();
                username = in.readLine();
                System.out.println(username + " joined group " + groupName);

                synchronized (groupMap) {
                    groupMap.putIfAbsent(groupName, new ArrayList<>());
                    groupMap.get(groupName).add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    List<PrintWriter> groupWriters = groupMap.get(groupName);
                    synchronized (groupWriters) {
                        for (PrintWriter writer : groupWriters) {
                            writer.println(username + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (groupName != null) {
                    List<PrintWriter> groupWriters = groupMap.get(groupName);
                    if (groupWriters != null) {
                        synchronized (groupWriters) {
                            groupWriters.remove(out);
                        }
                    }
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int multicastPort = 12345; 
        MulticastSocket multicastSocket;
        final String ENDERECO_MULTICAST = "239.255.255.250";
        private void SairDoGrupoAtual() throws IOException {
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.leaveGroup(grupo);
        }
    
        private void EntrarNovoGrupo() throws IOException {
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.joinGroup(grupo);
        }
    }
}
