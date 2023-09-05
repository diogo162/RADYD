import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final int PORTA_MULTICAST = 1234;
    private static final String ENDERECO_MULTICAST = "239.255.255.250";
    private static String nomeCliente;
    private static String nomeGrupo;

    public static void main(String[] args) {
        try {
            
            MulticastSocket multicastSocket = new MulticastSocket(PORTA_MULTICAST);
            multicastSocket.joinGroup(InetAddress.getByName(ENDERECO_MULTICAST));

            final Scanner scanner = new Scanner(System.in);

            System.out.print("Criar uma nova conta? (S/N): ");
            String resposta = scanner.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                conta.criarNovaConta();
                conta.salvarContas();
            }

            System.out.print("Digite seu nome de usuário: ");
            nomeCliente = scanner.nextLine();

            if (!conta.fazerLogin(nomeCliente)) {
                System.out.println("Login falhou. Verifique seu nome de usuário e senha.");
                return;
            }
            
            Servidor servidor = new Servidor(multicastSocket, nomeCliente, nomeGrupo);
            Thread threadServidor = new Thread(servidor);
            threadServidor.start();
        
            
            // Interagir com o usuário e enviar mensagens
            while (true) {
                String mensagem = scanner.nextLine();
                // Lógica para enviar a mensagem
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
