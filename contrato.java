import java.io.IOException;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public interface contrato{
 public void SairDoGrupoAtual(MulticastSocket multicastSocket, String ENDERECO_MULTICAST) throws UnknownHostException, IOException ;
 public void EntrarNovoGrupo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST) throws UnknownHostException, IOException ;
 public void EntrouNoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST) throws IOException;
 public void SaiuDoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST) throws IOException;
 public void EnviarArquivo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST,String caminhoArquivo) throws IOException;
}