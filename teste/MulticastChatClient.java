package teste;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MulticastChatClient {
    private static final int MULTICAST_PORT = 12345;
    private static final String MULTICAST_ADDRESS = "230.0.0.1"; // Example multicast address

    public static void main(String[] args) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);


            Scanner scanner = new Scanner(System.in);
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
                byte[] buffer = (username + ": " + userInputMessage).getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
                multicastSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
