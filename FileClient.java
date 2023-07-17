import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                writer.println(message);
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
