import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class metodos implements contrato{
    

    public void SairDoGrupoAtual(MulticastSocket multicastSocket, String ENDERECO_MULTICAST) throws IOException {
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        multicastSocket.leaveGroup(grupo);
    }

    public void EntrarNovoGrupo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST) throws IOException {
            InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
            multicastSocket.joinGroup(grupo);
    }

    public void EntrouNoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST) throws IOException {
        System.out.println("Cliente '" + nomeCliente + "' entrou no grupo " + nomeGrupo);
        byte[] buffer = (nomeCliente + ":" + nomeGrupo + ":" + "entrou no grupo").getBytes();
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_MULTICAST);
        multicastSocket.send(pacote);        
    }

    public void SaiuDoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST) throws IOException {
        byte[] buffer = (nomeCliente + ":" + nomeGrupo + ":" + "saiu do grupo").getBytes();
        InetAddress grupo = InetAddress.getByName(ENDERECO_MULTICAST);
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_MULTICAST);
        multicastSocket.send(pacote);        
    }


    public void EnviarArquivo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST, String caminhoArquivo) throws IOException {
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

            DatagramPacket pacoteArquivo = new DatagramPacket(dadosDoArquivo, dadosDoArquivo.length, grupo, PORTA_MULTICAST);
            multicastSocket.send(pacoteArquivo);
            System.out.println("Arquivo enviado com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar o arquivo: " + e.getMessage());
        }
    }
}
