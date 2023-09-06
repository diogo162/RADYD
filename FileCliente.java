import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class FileCliente {
    private static final int PORTA_MULTICAST = 1234;
    private static final String ENDERECO_MULTICAST = "239.255.255.250";
    private static final String GRUPO_PADRAO = "Geral";

    private static MulticastSocket multicastSocket;
    private static String nomeCliente;
    private static String nomeGrupo;
    private static interfaceContas accountService;
    private static FileHandler filehandler;

    public static void main(String[] args) {
        accountService = new ServicosContas();
        accountService.carregarContas(); 
        try {
            multicastSocket = new MulticastSocket(PORTA_MULTICAST);
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.joinGroup(grupo);
            Scanner scanner = new Scanner(System.in);

            System.out.print("Criar uma nova conta? (S/N): ");
            String resposta = scanner.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                accountService.criarNovaConta();
                accountService.salvarContas();
            }

            System.out.print("Digite seu nome de usuário: ");
            nomeCliente = scanner.nextLine();

            if (!accountService.fazerLogin(nomeCliente)) {
                System.out.println("Login falhou. Verifique seu nome de usuário e senha.");
                return;
            }

            System.out.print("Digite o grupo que deseja entrar (pressione Enter para o grupo padrão 'Geral'): ");
            nomeGrupo = scanner.nextLine();
            if (nomeGrupo.trim().isEmpty()) {
                nomeGrupo = GRUPO_PADRAO;
            }
            ClientHandler clientHandler = new ClientHandler(multicastSocket, nomeCliente, nomeGrupo);
            new Thread(clientHandler).start();

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
                                filehandler.receberArquivo(nomeArquivo);
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

}
