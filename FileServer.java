import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class FileServer {
    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        int port = 1234;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new Handler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writers.add(writer);
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("/sendfile")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 2) {
                            String filename = parts[1];
                            InputStream inputStream = socket.getInputStream();
                            DataInputStream dataInputStream = new DataInputStream(inputStream);
                            int length = dataInputStream.readInt();
                            byte[] fileContent = new byte[length];
                            dataInputStream.readFully(fileContent);
                            Path path = Paths.get(filename);
                            Files.write(path, fileContent);
                            for (PrintWriter w : writers) {
                                w.println("File received: " + filename);
                            }
                        }
                    } else {
                        for (PrintWriter w : writers) {
                            w.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
