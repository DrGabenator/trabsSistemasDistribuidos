
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexao {

    private boolean conectado = true;
    public static final String PERMITIR_ACESSO = "Permitir";
    public static final String NEGAR_ACESSO = "Negar";
    private static final int porta = 8000;
    private Socket socket;
    private ServerSocket listen;

    public void conectar(Processo coordenador) {
        System.out.println("Coordenador " + coordenador + " está pronto para receber as requisições.");
        new Thread(() -> {
            try {
                listen = new ServerSocket(porta);

                while (conectado) {
                    socket = listen.accept();

                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String rBuf = br.readLine();
                    System.out.println(rBuf);

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    String wBuf = "Erro!\n";
                    if (coordenador.isRecursoEmUso()) {
                        wBuf = NEGAR_ACESSO + "\n";
                    } else {
                        wBuf = PERMITIR_ACESSO + "\n";
                    }
                    dos.write(wBuf.getBytes("UTF-8"));
                }
                System.out.println("Conexão finalizada.");
            } catch (IOException e) {
                System.out.println("Conexão finalizada.");
            }
        }).start();
    }

    public String realizarRequisicao(String mensagem) {
        String wBuf = "Erro!\n";
        try {
            Socket socket = new Socket("localhost", porta);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.write(mensagem.getBytes("UTF-8"));

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            wBuf = br.readLine();

            socket.close();
        } catch (Exception e) {
            System.out.println("A requisição não foi finalizada corretamente.");
        }
        return wBuf;
    }

    public void encerraConexao() {
        conectado = false;
        try {
            socket.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando conexão.");
        }

        try {
            listen.close();
        } catch (IOException | NullPointerException e) {
            System.out.println("Erro encerrando conexão.");
        }
    }
}
