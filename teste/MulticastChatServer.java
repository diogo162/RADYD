package teste;

import java.io.*;
import java.net.*;
import java.util.*;

class MulticastChatServer {
    public static void main(String[] args) {
        int port = 12345; // Porta para comunicação multicast
        String multicastGroup = "239.255.255.250"; // Endereço do grupo multicast

        try (MulticastSocket multicastSocket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(multicastGroup);
            multicastSocket.joinGroup(group);
            
            System.out.println("Server is listening on multicast group " + multicastGroup);
            
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);
                
                // Aqui você pode adicionar lógica para encaminhar a mensagem para os clientes adequados
                // com base no grupo do remetente, etc.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

