
import java.util.ArrayList;
import java.util.Random;

public class MainClass {

    private static final int adicionaProcesso = 40000;
    private static final int inativaProcesso = 80000;
    private static final int inativaCoordenador = 60000;
    private static final int consumoMinimoRecurso = 5000;
    private static final int consumoMaximoRecurso = 15000;

    private static final Object lockToThread = new Object();

    public static void main(String[] args) {
        criaProcessos(ControleProcesso.getProcessosAtivos());
        inativaCoordenador(ControleProcesso.getProcessosAtivos());
        inativaProcesso(ControleProcesso.getProcessosAtivos());
        acessaRecurso(ControleProcesso.getProcessosAtivos());
    }

    private static int geraIdUnico(ArrayList<Processo> processosAtivos) {
        Random random = new Random();
        int idRandom = random.nextInt(10000);

        for (Processo p : processosAtivos) {
            if (p.getIdProcesso() == idRandom) {
                return geraIdUnico(processosAtivos);
            }
        }

        return idRandom;
    }

    public static void criaProcessos(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                synchronized (lockToThread) {
                    Processo processo = new Processo(geraIdUnico(processosAtivos));

                    if (processosAtivos.isEmpty()) {
                        processo.setCoordenadorAtual(true);
                    }

                    processosAtivos.add(processo);
                }

                espera(adicionaProcesso);

            }
        }).start();
    }

    public static void acessaRecurso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            Random random = new Random();
            int intervalo = 0;
            while (true) {
                intervalo = random.nextInt(consumoMaximoRecurso - consumoMinimoRecurso);
                espera(consumoMinimoRecurso + intervalo);

                synchronized (lockToThread) {
                    if (!processosAtivos.isEmpty()) {
                        int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());

                        Processo processoConsumidor = processosAtivos.get(indexProcessoAleatorio);
                        processoConsumidor.acessaORecurso();
                    }
                }
            }
        }).start();
    }

    public static void inativaCoordenador(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                espera(inativaCoordenador);

                synchronized (lockToThread) {
                    Processo coordenador = null;
                    for (Processo p : processosAtivos) {
                        if (p.isCoordenador()) {
                            coordenador = p;
                        }
                    }
                    if (coordenador != null) {
                        coordenador.removeProcesso();
                        System.out.println("O processo do coordenador " + coordenador + " foi finalizado.");
                    }
                }
            }
        }).start();
    }

    public static void inativaProcesso(ArrayList<Processo> processosAtivos) {
        new Thread(() -> {
            while (true) {
                espera(inativaProcesso);

                synchronized (lockToThread) {
                    if (!processosAtivos.isEmpty()) {
                        int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());
                        Processo pRemover = processosAtivos.get(indexProcessoAleatorio);
                        if (pRemover != null && !pRemover.isCoordenador()) {
                            pRemover.removeProcesso();
                        }
                    }
                }
            }
        }).start();
    }

    private static void espera(int segundos) {
        try {
            Thread.sleep(segundos);
        } catch (Exception e) {
        }
    }
}
