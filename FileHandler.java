import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileHandler {
    private static final int PORTA_MULTICAST = 1234;
    private static final String ENDERECO_MULTICAST = "239.255.255.250";
    private static final String GRUPO_PADRAO = "Geral";

    private MulticastSocket multicastSocket;
    private String nomeCliente;
    private String nomeGrupo;

    public FileHandler(MulticastSocket socket, String clientName, String groupName) {
        this.multicastSocket = socket;
        this.nomeCliente = clientName;
        this.nomeGrupo = groupName;
    }

    public void enviarArquivo(String caminhoArquivo) {
        try {
            File arquivo = new File(caminhoArquivo);
            if (!arquivo.exists() || !arquivo.isFile()) {
                System.out.println("Arquivo não encontrado: " + caminhoArquivo);
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(arquivo);
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                DatagramPacket pacoteArquivo = new DatagramPacket(buffer, bytesRead, InetAddress.getByName(ENDERECO_MULTICAST), PORTA_MULTICAST);
                multicastSocket.send(pacoteArquivo);
            }

            System.out.println("Arquivo enviado com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar o arquivo: " + e.getMessage());
        }
    }

    public void receberArquivo(String nomeArquivo) {
        try {
            byte[] buffer = new byte[8192];
            DatagramPacket pacoteArquivo = new DatagramPacket(buffer, buffer.length);

            multicastSocket.receive(pacoteArquivo);

            FileOutputStream fileOutputStream = new FileOutputStream(nomeArquivo);
            fileOutputStream.write(pacoteArquivo.getData(), 0, pacoteArquivo.getLength());
            fileOutputStream.close();

            System.out.println("Arquivo recebido e salvo como: " + nomeArquivo);
        } catch (IOException e) {
            System.out.println("Erro ao receber o arquivo: " + e.getMessage());
        }
    }
}
