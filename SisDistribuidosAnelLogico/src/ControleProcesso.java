
import java.util.ArrayList;

public class ControleProcesso {

    private static ArrayList<Processo> processosAtivos = new ArrayList<Processo>();
    private static Processo consumidor = null;
    private static Recurso recurso = new Recurso();

    private ControleProcesso() {

    }

    public static void removeOProcesso(Processo processo) {
        processosAtivos.remove(processo);
    }

    public static boolean estaUsandoRecurso(Processo processo) {
        return processo.equals(consumidor);
    }

    public static boolean estaSendoConsumido() {
        return consumidor != null;
    }

    //Getters e Setters
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
}
