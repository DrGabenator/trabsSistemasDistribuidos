
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexao {

    private boolean conectado = true;
    public static final String permiteAcesso = "Acesso permitido!";
    public static final String negaAcesso = "Acesso negado!";
    private static final int porta = 8000;
    private Socket socket;
    private ServerSocket ouvirSocket;

    public void encerraAConexao() {
        conectado = false;
        try {
            socket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando o socket!");
        }
        try {
            ouvirSocket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando o socket que está ouvindo!");
        }
    }

    public String fazerRequisicao(String mensagem) {
        String rBuf = "Erro!";
        try {
            Socket s = new Socket("localhost", porta);

            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.write(mensagem.getBytes("UTF-8"));

            InputStreamReader isr = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            rBuf = br.readLine();

            s.close();
        } catch (IOException e) {
            System.out.println("A requisição não finalizou corretamente!");
        }
        return rBuf;
    }

    public void conectar(Processo coordenador) {
        System.out.println("-------------Conectando-------------");
        System.out.println("Coordenador " + coordenador + " está pronto para receber as requisições!");
        new Thread(() -> {
            try {
                ouvirSocket = new ServerSocket(porta);

                while (conectado) {
                    socket = ouvirSocket.accept();

                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String rBuf = br.readLine();
                    System.out.println(rBuf);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    String sBuf = "Erro!\n";

                    if (coordenador.recursoEstaEmUso()) {
                        sBuf = negaAcesso + "\n";
                    } else {
                        sBuf = permiteAcesso + "\n";
                    }
                    dos.write(sBuf.getBytes("UTF-8"));
                }
                System.out.println("Conexão encerrada!");
            } catch (IOException e) {
            }
        }).start();
    }
}
