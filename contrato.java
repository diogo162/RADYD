import java.net.MulticastSocket;

public interface contrato{
 public void SairDoGrupoAtual(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST);
 public void EntrarNovoGrupo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST);
 public void EntrouNoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST);
 public void SaiuDoGrupoMensagem(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST);
 public void EnviarArquivo(MulticastSocket multicastSocket, String ENDERECO_MULTICAST, String nomeCliente, String nomeGrupo, int PORTA_MULTICAST,String caminhoArquivo);
}