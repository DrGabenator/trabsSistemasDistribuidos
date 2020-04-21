
import java.util.ArrayList;

public class ControleProcesso {
    
    private static ArrayList<Processo> processosAtivos = new ArrayList<Processo>();
    private static Recurso recurso = new Recurso();
    private static Processo consumidor = null;
    
    public ControleProcesso() {
        
    }
        
    public static void removeProcesso(Processo processo) {
        processosAtivos.remove(processo);
    }
    
    public static boolean isUsandoRecurso(Processo processo) {
        return processo.equals(consumidor);
    }
    
    public static boolean isSendoConsumido() {
        return consumidor != null;
    }
    
    public static ArrayList<Processo> getProcessosAtivos() {
        return processosAtivos;
    }

    public static void setProcessosAtivos(ArrayList<Processo> processosAtivos) {
        ControleProcesso.processosAtivos = processosAtivos;
    }

    public static Recurso getRecurso() {
        return recurso;
    }

    public static void setRecurso(Recurso recurso) {
        ControleProcesso.recurso = recurso;
    }

    public static Processo getConsumidor() {
        return consumidor;
    }

    public static void setConsumidor(Processo consumidor) {
        ControleProcesso.consumidor = consumidor;
    }
    
    public static Processo getCoordenador() {
        for (Processo processo : processosAtivos) {
            if (processo.isCoordenadorAtual())
                return processo;
        }
        return null;
    }
}
