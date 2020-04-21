
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexao {

    private boolean conectado = true;
    public static final String permiteAcesso = "PERMITIR";
    public static final String negaAcesso = "NAO_PERMITIR";
    private static final int porta = 8000;
    private Socket sock;
    private ServerSocket listenSocket;

    public void encerraAConexao() {
        conectado = false;
        try {
            sock.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando a conexao: ");
        }
        try {
            listenSocket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando a conexao: ");
        }
    }

    public String fazerRequisicao(String mensagem) {
        String rBuf = "Erro!";
        try {
            Socket sock = new Socket("localhost", porta);

            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            dos.write(mensagem.getBytes("UTF-8"));

            InputStreamReader isr = new InputStreamReader(sock.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            rBuf = br.readLine();

            sock.close();
        } catch (IOException e) {
            System.out.println("A requisicao nao finalizou corretamente.");
        }
        return rBuf;
    }

    public void conectar(Processo coordenador) {
        System.out.println("Coordenador " + coordenador + " pronto para receber requisicoes.");
        new Thread(() -> {
            try {
                listenSocket = new ServerSocket(porta);

                while (conectado) {
                    sock = listenSocket.accept();

                    InputStreamReader isr = new InputStreamReader(sock.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String rBuf = br.readLine();
                    System.out.println(rBuf);

                    DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                    String sBuf = "Error!\n";

                    if (coordenador.isRecursoEmUso()) {
                        sBuf = negaAcesso + "\n";
                    } else {
                        sBuf = permiteAcesso + "\n";
                    }
                    dos.write(sBuf.getBytes("UTF-8"));
                }
                System.out.println("Conexao encerrada.");
            } catch (IOException e) {
                System.out.println("Conexao encerrada.");
            }
        }).start();
    }
}
