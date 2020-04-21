
import java.util.ArrayList;
import java.util.Random;

//GRUPO 14
public class MainClass {

    private static final int ADICIONA = 4000;
    private static final int INATIVA_PROCESSO = 8000;
    private static final int INATIVA_COORDENADOR = 30000;
    private static final int CONSOME_RECURSO_MINIMO = 5000;
    private static final int CONSOME_RECURSO_MAXIMO = 10000;

    private static final Object lockToThread = new Object();

    public static void main(String[] args) {
        criarProcessos(ControleProcesso.getProcessosAtivos());
        inativarCoordenador(ControleProcesso.getProcessosAtivos());
        inativarProcesso(ControleProcesso.getProcessosAtivos());
        acessarRecurso(ControleProcesso.getProcessosAtivos());
    }

    public static void criarProcessos(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                synchronized (lockToThread) {
                    Processo processo = new Processo(gerarIdUnico(processosAtivos));

                    if (processosAtivos.isEmpty()) {
                        processo.setCoordenadorAtual(true);

                        processosAtivos.add(processo);
                    }

                    esperar(ADICIONA);
                }
            }
        }).start();
    }

    private static int gerarIdUnico(ArrayList<Processo> processosAtivos) {
        Random random = new Random();
        int idRandom = random.nextInt(1000);

        for (Processo p : processosAtivos) {
            if (p.getIdProcesso() == idRandom) {
                return gerarIdUnico(processosAtivos);
            }
        }

        return idRandom;
    }

    public static void inativarProcesso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                esperar(INATIVA_PROCESSO);

                synchronized (lockToThread) {
                    if (!processosAtivos.isEmpty()) {
                        int processoAleatorio = new Random().nextInt(processosAtivos.size());

                        Processo remove = processosAtivos.get(processoAleatorio);

                        if (remove != null && !remove.isCoordenadorAtual()) {
                            remove.destruir();
                        }
                    }
                }
            }
        }).start();
    }

    public static void inativarCoordenador(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                esperar(INATIVA_COORDENADOR);

                synchronized (lockToThread) {
                    Processo coordenador = null;
                    for (Processo p : processosAtivos) {
                        if (p.isCoordenadorAtual()) {
                            coordenador = p;
                        }
                    }

                    if (coordenador != null) {
                        coordenador.destruir();
                        System.out.println("Processo coordenador " + coordenador + " finalizado.");
                    }
                }
            }
        }).start();
    }

    public static void acessarRecurso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            Random random = new Random();
            int intervalo = 0;

            while (true) {
                intervalo = random.nextInt(CONSOME_RECURSO_MAXIMO - CONSOME_RECURSO_MINIMO);
                esperar(CONSOME_RECURSO_MINIMO + intervalo);

                synchronized (lockToThread) {
                    if (!processosAtivos.isEmpty()) {
                        int processoAleatorio = new Random().nextInt(processosAtivos.size());

                        Processo consumidor = processosAtivos.get(processoAleatorio);
                        consumidor.acessarRecursoCompartilhado();
                    }
                }
            }
        }).start();
    }

    private static void esperar(int segundos) {
        try {
            Thread.sleep(segundos);
        } catch (InterruptedException e) {
        }
    }
}
