import java.util.HashMap;
import java.util.Scanner;

public class conta {
    private static HashMap<String, String> contasUsuarios = new HashMap<>();

    private static void criarNovaConta() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite um nome de usuário: ");
        String novoUsuario = scanner.nextLine();
        System.out.print("Digite uma senha: ");
        String novaSenha = scanner.nextLine();
        contasUsuarios.put(novoUsuario, novaSenha);
        System.out.println("Conta criada com sucesso!");
    }

    private static boolean fazerLogin(String nomeCliente) {

        Scanner scanner = new Scanner(System.in);
        if (contasUsuarios.containsKey(nomeCliente)) {
            String senhaArmazenada = contasUsuarios.get(nomeCliente);
            System.out.print("Digite sua senha: ");
            String senhaDigitada = scanner.nextLine();
            return senhaArmazenada.equals(senhaDigitada);
        }
        return false;
    }
}
