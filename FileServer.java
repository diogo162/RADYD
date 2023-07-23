import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class FileServer {
    private static final int MULTICAST_PORT = 1234;
    private static final String MULTICAST_ADDRESS = "239.255.255.250";
    private static final String FILES_FOLDER = "received_files/";

    private static MulticastSocket multicastSocket;

    public static void main(String[] args) {
        try {
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            System.out.println("File server listening on port " + MULTICAST_PORT);

            // Create the directory to save received files if it does not exist
            File filesDir = new File(FILES_FOLDER);
            if (!filesDir.exists()) {
                if (filesDir.mkdir()) {
                    System.out.println("Directory created: " + FILES_FOLDER);
                } else {
                    System.out.println("Failed to create directory: " + FILES_FOLDER);
                }
            }

            HashMap<String, String> fileReceivers = new HashMap<>();

            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                String[] parts = message.split(":", 3);

                if (parts.length == 3) {
                    String senderName = parts[0].trim();
                    String senderGroupName = parts[1].trim();
                    String senderMessage = parts[2].trim();

                    // Check if the received message is a file acknowledgment message
                    if (senderMessage.startsWith("/file-start ")) {
                        String fileName = senderMessage.substring(12);
                        String receiver = senderName;
                        fileReceivers.put(fileName, receiver);
                        System.out.println("File transfer started from " + senderName + ": " + fileName);
                    } else if (senderMessage.startsWith("/file-end ")) {
                        String fileName = senderMessage.substring(10);
                        String receiver = fileReceivers.get(fileName);
                        if (receiver != null && receiver.equals(senderName)) {
                            // File transfer completed, save the received file
                            receiveFile(packet, fileName);
                            System.out.println("File received from " + senderName + ": " + fileName);
                            // Send an acknowledgment message back to the client
                            byte[] ackMessageBytes = "File received".getBytes();
                            DatagramPacket ackPacket = new DatagramPacket(ackMessageBytes, ackMessageBytes.length, packet.getAddress(), packet.getPort());
                            multicastSocket.send(ackPacket);
                            // Remove the entry from fileReceivers
                            fileReceivers.remove(fileName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(DatagramPacket packet, String fileName) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            File file = new File(FILES_FOLDER + timestamp + "_" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(packet.getData(), 0, packet.getLength());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
