import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor implements Runnable {
    private static final int PORTA_MULTICAST = 1234;
    private static final String ENDERECO_MULTICAST = "239.255.255.250";
    private static final String GRUPO_PADRAO = "Geral";

    private MulticastSocket multicastSocket;
    private String nomeCliente;
    private String nomeGrupo;

    public Servidor(MulticastSocket multicastSocket, String nomeCliente, String nomeGrupo) {
        this.multicastSocket = multicastSocket;
        this.nomeCliente = nomeCliente;
        this.nomeGrupo = nomeGrupo;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[8196];
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(pacote);
                String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                String[] partes = mensagem.split(":", 3);

                if (partes.length == 3) {
                    String nomeRemetente = partes[0].trim();
                    String nomeGrupoRemetente = partes[1].trim();
                    String mensagemRemetente = partes[2].trim();

                    if (!nomeRemetente.equals(nomeCliente) && nomeGrupoRemetente.equals(nomeGrupo)) {
                        if (mensagemRemetente.startsWith("<<file>>")) {
                            String[] partesArquivo = mensagemRemetente.split(":", 2);
                            if (partesArquivo.length == 2) {
                                String nomeArquivo = partesArquivo[1].trim();
                                System.out.println(nomeRemetente + " enviou um arquivo: " + nomeArquivo);
                                receberArquivo(nomeArquivo);
                            }
                        } else {
                            System.out.println(nomeRemetente + ": " + mensagemRemetente);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receberArquivo(String nomeArquivo) {
        try {
            byte[] buf = new byte[8196];
            DatagramPacket pacoteArquivo = new DatagramPacket(buf, buf.length);
            multicastSocket.receive(pacoteArquivo);

            String caminhoSalvo = "./arquivos_recebidos/" + nomeArquivo;
            File diretorioRecebido = new File("./arquivos_recebidos");
            if (!diretorioRecebido.exists()) {
                diretorioRecebido.mkdirs();
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(caminhoSalvo)) {
                fileOutputStream.write(pacoteArquivo.getData(), 0, pacoteArquivo.getLength());
                System.out.println("Arquivo salvo como: " + caminhoSalvo);
            } catch (IOException e) {
                System.out.println("Erro ao salvar o arquivo: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Erro ao receber o arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(PORTA_MULTICAST);
            multicastSocket.joinGroup(InetAddress.getByName(ENDERECO_MULTICAST));

            Scanner scanner = new Scanner(System.in);

            System.out.print("Digite o nome do cliente: ");
            String nomeCliente = scanner.nextLine();

            System.out.print("Digite o grupo que deseja entrar (pressione Enter para o grupo padrão 'Geral'): ");
            String nomeGrupo = scanner.nextLine();
            if (nomeGrupo.trim().isEmpty()) {
                nomeGrupo = GRUPO_PADRAO;
            }

            ClientHandler clientHandler = new ClientHandler(multicastSocket, nomeCliente, nomeGrupo);
            Thread threadClientHandler = new Thread(clientHandler);
            threadClientHandler.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
