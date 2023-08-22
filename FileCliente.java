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
    private static HashMap<String, String> contasUsuarios = new HashMap<>();

    public static void main(String[] args) {
        carregarContas();
        try {
            multicastSocket = new MulticastSocket(PORTA_MULTICAST);
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.joinGroup(grupo);
            Scanner scanner = new Scanner(System.in);

            System.out.print("Criar uma nova conta? (S/N): ");
            String resposta = scanner.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                criarNovaConta();
                salvarContas();
            }

            System.out.print("Digite seu nome de usuário: ");
            nomeCliente = scanner.nextLine();

            if (!fazerLogin()) {
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

    private static void criarNovaConta() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite um nome de usuário: ");
        String novoUsuario = scanner.nextLine();
        System.out.print("Digite uma senha: ");
        String novaSenha = scanner.nextLine();
        contasUsuarios.put(novoUsuario, novaSenha);
        System.out.println("Conta criada com sucesso!");
    }

    private static boolean fazerLogin() {
        Scanner scanner = new Scanner(System.in);
        if (contasUsuarios.containsKey(nomeCliente)) {
            String senhaArmazenada = contasUsuarios.get(nomeCliente);
            System.out.print("Digite sua senha: ");
            String senhaDigitada = scanner.nextLine();
            return senhaArmazenada.equals(senhaDigitada);
        }
        return false;
    }

    private static void carregarContas() {
        String ARQUIVO_CONTAS = "contas.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(ARQUIVO_CONTAS))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(":");
                if (partes.length == 2) {
                    String usuario = partes[0];
                    String senha = partes[1];
                    contasUsuarios.put(usuario, senha);
                }
            }
        } catch (IOException e) {
            // Arquivo ainda não existe ou ocorreu um erro ao lê-lo.
        }
    }

    private static void salvarContas() {
        String ARQUIVO_CONTAS = "contas.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_CONTAS))) {
            for (String usuario : contasUsuarios.keySet()) {
                String senha = contasUsuarios.get(usuario);
                writer.write(usuario + ":" + senha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar contas: " + e.getMessage());
        }
    }

    private static void receberArquivo(String nomeArquivo) {
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

}
