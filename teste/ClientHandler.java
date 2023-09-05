package teste;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private static final int PORTA_MULTICAST = 1234;
    private static final String ENDERECO_MULTICAST = "239.255.255.250";
    private static final String GRUPO_PADRAO = "Geral";

    private MulticastSocket multicastSocket;
    private String nomeCliente;
    private String nomeGrupo;

    public ClientHandler(MulticastSocket socket, String clientName, String groupName) {
        this.multicastSocket = socket;
        this.nomeCliente = clientName;
        this.nomeGrupo = groupName;
    }

    @Override
    public void run() {
        try {
            EntrouNoGrupoMensagem();
            BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String mensagem = leitor.readLine();

                if (mensagem.equalsIgnoreCase("/sair")) {
                    SaiuDoGrupoMensagem();
                    SairDoGrupoAtual();
                    System.out
                            .print("Digite o grupo que deseja entrar (pressione Enter para o grupo padrão 'Geral'): ");
                    nomeGrupo = leitor.readLine().trim();
                    if (nomeGrupo.isEmpty()) {
                        nomeGrupo = GRUPO_PADRAO;
                    }
                    EntrarNovoGrupo();
                    EntrouNoGrupoMensagem();
                } else if (mensagem.startsWith("/enviararquivo")) {
                    String[] partes = mensagem.split(" ", 2);
                    if (partes.length == 2) {
                        String caminhoArquivo = partes[1];
                        EnviarArquivo(caminhoArquivo);
                    } else {
                        System.out.println("Comando inválido. Uso: /enviararquivo <caminho_arquivo>");
                    }
                } else {
                    byte[] buffer = (nomeCliente + ":" + nomeGrupo + ":" + mensagem).getBytes();
                    InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_MULTICAST);
                    multicastSocket.send(pacote);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SairDoGrupoAtual() throws IOException {
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        multicastSocket.leaveGroup(grupo);
    }

    private void EntrarNovoGrupo() throws IOException {
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        multicastSocket.joinGroup(grupo);
    }

    private void EntrouNoGrupoMensagem() throws IOException {
        System.out.println("Cliente '" + nomeCliente + "' entrou no grupo " + nomeGrupo);
        byte[] buffer = (nomeCliente + ":" + nomeGrupo + ":" + "entrou no grupo").getBytes();
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_MULTICAST);
        multicastSocket.send(pacote);
    }

    private void SaiuDoGrupoMensagem() throws IOException {
        byte[] buffer = (nomeCliente + ":" + nomeGrupo + ":" + "saiu do grupo").getBytes();
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_MULTICAST);
        multicastSocket.send(pacote);
    }

    private void EnviarArquivo(String caminhoArquivo) throws IOException {
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("Arquivo não encontrado: " + caminhoArquivo);
            return;
        }

        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(arquivo))) {
            byte[] buffer = new byte[1024];
            int bytesLidos;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while ((bytesLidos = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesLidos);
            }

            byte[] dadosDoArquivo = byteArrayOutputStream.toByteArray();
            byte[] buf = (nomeCliente + ":" + nomeGrupo + ":<<file>>" + ":" + arquivo.getName()).getBytes();
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            DatagramPacket pacote = new DatagramPacket(buf, buf.length, grupo, PORTA_MULTICAST);
            multicastSocket.send(pacote);

            DatagramPacket pacoteArquivo = new DatagramPacket(dadosDoArquivo, dadosDoArquivo.length, grupo,
                    PORTA_MULTICAST);
            multicastSocket.send(pacoteArquivo);
            System.out.println("Arquivo enviado com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar o arquivo: " + e.getMessage());
        }
    }
}