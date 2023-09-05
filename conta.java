import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class conta {
    private static Map<String, String> contasUsuarios = new HashMap<>();

    public static void criarNovaConta() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite um nome de usuário: ");
        String novoUsuario = scanner.nextLine();
        System.out.print("Digite uma senha: ");
        String novaSenha = scanner.nextLine();
        contasUsuarios.put(novoUsuario, novaSenha);
        System.out.println("Conta criada com sucesso!");
    }

    public static boolean fazerLogin(String nomeCliente) {
        Scanner scanner = new Scanner(System.in);
        if (contasUsuarios.containsKey(nomeCliente)) {
            String senhaArmazenada = contasUsuarios.get(nomeCliente);
            System.out.print("Digite sua senha: ");
            String senhaDigitada = scanner.nextLine();
            return senhaArmazenada.equals(senhaDigitada);
        }
        return false;
    }

    public static void salvarContas() {
        String ARQUIVO_CONTAS = "contas.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARQUIVO_CONTAS, true))) {
            for (Map.Entry<String, String> entry : contasUsuarios.entrySet()) {
                String usuario = entry.getKey();
                String senha = entry.getValue();
                writer.write(usuario + ":" + senha);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar contas: " + e.getMessage());
        }
    }

    public static void carregarContas() {
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
}
