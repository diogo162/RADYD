import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    private static final int MULTICAST_PORT = 1234;
    private static final String MULTICAST_ADDRESS = "239.255.255.250";
    private static final String DEFAULT_GROUP = "General";

    private static MulticastSocket multicastSocket;
    private static String clientName;
    private static String groupName;

    public static void main(String[] args) {
        try {
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);

            System.out.print("Enter your name: ");
            Scanner scanner = new Scanner(System.in);
            clientName = scanner.nextLine();

            System.out.print("Enter the group you want to join (press Enter for default group 'General'): ");
            groupName = scanner.nextLine();
            if (groupName.trim().isEmpty()) {
                groupName = DEFAULT_GROUP;
            }

            System.out.println("Client '" + clientName + "' listening on group '" + groupName + "' and port " + MULTICAST_PORT);

            new Thread(new ClientHandler()).start();

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
                    if (!senderName.equals(clientName) && senderGroupName.equals(groupName)) {
                        if (senderMessage.startsWith("<<file>>")) {
                            // Received a file
                            String[] fileParts = senderMessage.split(":", 3);
                            if (fileParts.length == 3) {
                                String fileName = fileParts[1].trim();
                                String fileExtension = fileParts[2].trim();
                                System.out.println(senderName + " sent a file: " + fileName + "." + fileExtension);
                                receiveFile(packet.getAddress(), fileName, fileExtension);
                            }
                            
                        } else {
                            System.out.println(senderName + ": " + senderMessage);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(InetAddress senderAddress, String fileName, String fileExtension) {
        String savePath = "./received_files/" + fileName + "." + fileExtension;
        File receivedDir = new File("./received_files");
        if (!receivedDir.exists()) {
            receivedDir.mkdirs(); // Create the directory if it doesn't exist
        }
   
        try (FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket filePacket = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(filePacket);
   
                String receivedData = new String(filePacket.getData(), 0, filePacket.getLength());
                if (receivedData.equals("<<endfile>>")) {
                    break; // Received the end of the file, exit the loop
                }
   
                fileOutputStream.write(filePacket.getData(), 0, filePacket.getLength());
            }
   
            System.out.println("File saved as: " + savePath);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
    
    
    

    private static void sendFile(String receiverName, String filePath) throws IOException {
        // Read the file and send it to the receiver
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found: " + filePath);
            return;
        }
    
        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file))) {
            String fileName = file.getName();
            String fileExtension = getFileExtension(fileName);
    
            byte[] buf = (clientName + ":" + groupName + ":<<file>>" + receiverName + ":" + fileName + ":" + fileExtension).getBytes();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTICAST_PORT);
            multicastSocket.send(packet);
    
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buf)) != -1) {
                DatagramPacket filePacket = new DatagramPacket(buf, bytesRead, group, MULTICAST_PORT);
                multicastSocket.send(filePacket);
            }
    
            // Send the "end of file" marker
            String endOfFileMarker = "<<endfile>>";
            byte[] endOfFileBuf = endOfFileMarker.getBytes();
            DatagramPacket endOfFilePacket = new DatagramPacket(endOfFileBuf, endOfFileBuf.length, group, MULTICAST_PORT);
            multicastSocket.send(endOfFilePacket);
    
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            System.out.println("Error sending file: " + e.getMessage());
        }
    }
    
    
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return ""; // Return an empty string if there's no file extension
    }

    private static class ClientHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String message = reader.readLine();

                    if (message.equalsIgnoreCase("exit")) {
                        leaveCurrentGroup();
                        System.out.print("Enter the group you want to join (press Enter for default group 'General'): ");
                        groupName = reader.readLine().trim();
                        if (groupName.isEmpty()) {
                            groupName = DEFAULT_GROUP;
                        }
                        joinNewGroup();
                    } else if (message.startsWith("sendfile")) {
                        // Format: sendfile <receiver_name> <file_path>
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String receiverName = parts[1];
                            String filePath = parts[2];
                            sendFile(receiverName, filePath);
                        } else {
                            System.out.println("Invalid command. Usage: sendfile <receiver_name> <file_path>");
                        }
                    } else {
                        byte[] buf = (clientName + ":" + groupName + ":" + message).getBytes();
                        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTICAST_PORT);
                        multicastSocket.send(packet);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void leaveCurrentGroup() throws IOException {
            // Leave the current group
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.leaveGroup(group);
        }

        private void joinNewGroup() throws IOException {
            // Join the new group
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(group);
        }
    }
}
