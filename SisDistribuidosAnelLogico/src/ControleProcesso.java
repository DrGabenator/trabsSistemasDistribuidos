
import java.util.ArrayList;

public class ControleProcesso {

    private static ArrayList<Processo> processosAtivos = new ArrayList<Processo>();
    private static Recurso recurso = new Recurso();
    private static Processo consumidor = null;

    private ControleProcesso() {

    }

    public static ArrayList<Processo> getProcessosAtivos() {
        return processosAtivos;
    }

    public static Recurso getRecurso() {
        return recurso;
    }

    public static Processo getConsumidor() {
        return consumidor;
    }

    public static void setConsumidor(Processo novoConsumidor) {
        consumidor = novoConsumidor;
    }

    public static Processo getCoordenador() {
        for (Processo processo : processosAtivos) {
            if (processo.isCoordenador()) {
                return processo;
            }
        }
        return null;
    }

    public static void removerProcesso(Processo processo) {
        processosAtivos.remove(processo);
    }

    public static boolean isUsandoRecurso(Processo processo) {
        return processo.equals(consumidor);
    }

    public static boolean isSendoConsumido() {
        return consumidor != null;
    }
}
