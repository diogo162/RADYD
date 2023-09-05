package teste;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MessageClient {
    public static void main(String[] args) {
    final int MULTICAST_PORT = 12345; // Multicast port
    final String MULTICAST_ADDRESS = "230.0.0.1"; // Multicast address
    final String GRUPO_PADRAO = "Geral";

        
        try {MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter group name: ");
            String nomeGrupo = scanner.nextLine();
            if (nomeGrupo.trim().isEmpty()) {
                nomeGrupo = GRUPO_PADRAO;
            }

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    while (true) {
                        multicastSocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            String userInputMessage;
            while ((userInputMessage = scanner.nextLine()) != null) {
                String fullMessage = username + ": " + userInputMessage;
                byte[] buffer = fullMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
                multicastSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
}
    }}