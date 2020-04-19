//GRUPO 14
import java.util.ArrayList;
import java.util.Random;

public class AnelLogico {

    private final int processoNovoTimer = 30000;
    private final int eleicaoNovaTimer = 25000;
    private final int inativaCoordenadorTimer = 100000;
    private final int inativaProcessoTimer = 80000;

    public static ArrayList<Processo> processosAtivos;
    private final Object lockToThread = new Object();

    public AnelLogico() {
        processosAtivos = new ArrayList<Processo>();
    }

    public void novoProcesso() {
        new Thread(() -> {
            while (true) {
                synchronized (lockToThread) {
                    if (verificaProcessos()) {
                        processosAtivos.add(new Processo(1, true));
                    } else {
                        processosAtivos.add(new Processo(processosAtivos.get(processosAtivos.size() - 1).getIdProcesso() + 1, false));
                    }
                    System.out.println("Processo criado: " + processosAtivos.get(processosAtivos.size() - 1).getIdProcesso());
                }
                try {
                    Thread.sleep(processoNovoTimer);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    public void realizaRequisicao() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(eleicaoNovaTimer);
                } catch (InterruptedException e) {
                }
                synchronized (lockToThread) {
                    if (!verificaProcessos()) {
                        int index = new Random().nextInt(processosAtivos.size());
                        Processo processoRequisicao = processosAtivos.get(index);
                        processoRequisicao.novaRequisicao();
                        System.out.println("Processo que fez requisição: " + processoRequisicao.getIdProcesso());
                    }
                }
            }
        }).start();
    }

    private boolean verificaProcessos() {
        return processosAtivos.isEmpty();
    }

    public void inativarProcesso() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(inativaProcessoTimer);
                } catch (InterruptedException e) {
                }
                synchronized (lockToThread) {
                    if (!verificaProcessos()) {
                        int index = new Random().nextInt(processosAtivos.size());
                        Processo remocao = processosAtivos.get(index);
                        if (remocao != null && !remocao.isCoordenadorAtual()) {
                            processosAtivos.remove(remocao);
                            System.out.println("Processo inativado: " + remocao.getIdProcesso());
                        }
                    }
                }
            }
        }).start();
    }

    public void inativarCoordenador() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(inativaCoordenadorTimer);
                } catch (InterruptedException e) {
                }
                synchronized (lockToThread) {
                    Processo coordenador = null;
                    for (Processo processo : processosAtivos) {
                        if (processo.isCoordenadorAtual()) {
                            coordenador = processo;
                        }
                    }
                    if (coordenador != null) {
                        processosAtivos.remove(coordenador);
                        System.out.println("Coordenador inativado:  " + coordenador.getIdProcesso());
                    }
                }
            }
        }).start();
    }
}
