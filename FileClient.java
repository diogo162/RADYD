import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileClient {
    public static void main(String[] args) {
        String hostname = "Diogo";
        int port = 1234;
        try (Socket socket = new Socket(hostname, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            System.out.println("Connected to server at " + hostname + " on port " + port);
            new Thread(new ReaderThread(reader)).start();
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = stdIn.readLine()) != null) {
                if (message.startsWith("/sendfile")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 2) {
                        String filename = parts[1];
                        Path path = Paths.get(filename);
                        if (Files.exists(path)) {
                            writer.println("/sendfile " + path.getFileName());
                            byte[] fileContent = Files.readAllBytes(path);
                            OutputStream outputStream = socket.getOutputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            dataOutputStream.writeInt(fileContent.length);
                            dataOutputStream.write(fileContent);
                        } else {
                            System.out.println("File not found: " + filename);
                        }
                    } else {
                        System.out.println("Usage: /sendfile <filename>");
                    }
                } else {
                    writer.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ReaderThread implements Runnable {
        private BufferedReader reader;

        public ReaderThread(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
