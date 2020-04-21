
import java.util.LinkedList;
import java.util.Random;

public class Processo {

    private Thread utilizaRecurso = new Thread();
    private int idProcesso;
    private Conexao conexao = new Conexao();
    private boolean coordenadorAtual = false;

    private LinkedList<Processo> listaDeEspera;
    private boolean recursoEmUso;

    private static final int USO_PROCESSO_MIN = 10000;
    private static final int USO_PROCESSO_MAX = 20000;

    public Processo(int idProcesso) {
        this.idProcesso = idProcesso;
        setCoordenadorAtual(false);
    }

    public int getIdProcesso() {
        return idProcesso;
    }

    public boolean isCoordenador() {
        return coordenadorAtual;
    }

    public void setCoordenadorAtual(boolean ehCoordenador) {
        this.coordenadorAtual = ehCoordenador;
        if (this.coordenadorAtual) {
            listaDeEspera = new LinkedList<>();
            conexao.conectar(this);

            if (ControleProcesso.isSendoConsumido()) {
                ControleProcesso.getConsumidor().pararRecurso();
            }

            recursoEmUso = false;
        }
    }

    private void pararRecurso() {
        if (utilizaRecurso.isAlive()) {
            utilizaRecurso.interrupt();
        }
    }

    public boolean isRecursoEmUso() {
        return encontraOCoordenador().recursoEmUso;
    }

    public void setRecursoEmUso(boolean estaEmUso, Processo consumidor) {
        Processo coordenador = encontraOCoordenador();

        coordenador.recursoEmUso = estaEmUso;
        ControleProcesso.setConsumidor(estaEmUso ? consumidor : null);
    }

    private LinkedList<Processo> getListaDeEspera() {
        return encontraOCoordenador().listaDeEspera;
    }

    public boolean listaDeEsperaEstaVazia() {
        return getListaDeEspera().isEmpty();
    }

    private void removerDaListaDeEspera(Processo processo) {
        if (getListaDeEspera().contains(processo)) {
            getListaDeEspera().remove(processo);
        }
    }

    private Processo encontraOCoordenador() {
        Processo coordenador = ControleProcesso.getCoordenador();

        if (coordenador == null) {
            Eleicao eleicao = new Eleicao();
            coordenador = eleicao.realizarEleicao(this.getIdProcesso());
        }
        return coordenador;
    }

    public void acessaORecurso() {
        if (ControleProcesso.isUsandoRecurso(this) || this.isCoordenador()) {
            return;
        }

        String resultado = conexao.fazerRequisicao("Processo " + this + " quer consumir o recurso.\n");

        System.out.println("Resultado da requisicao do processo " + this + ": " + resultado);

        if (resultado.equals(Conexao.permiteAcesso)) {
            utilizarRecurso(this);
        } else if (resultado.equals(Conexao.negaAcesso)) {
            adicionarNaListaDeEspera(this);
        }
    }

    private void adicionarNaListaDeEspera(Processo processoEmEspera) {
        getListaDeEspera().add(processoEmEspera);

        System.out.println("O processo " + this + " foi adicionado na lista de espera!");
        System.out.println("Os processos que estão na lista de espera são: " + getListaDeEspera());
    }

    private void utilizarRecurso(Processo processo) {
        Random random = new Random();
        int randomUsageTime = USO_PROCESSO_MIN + random.nextInt(USO_PROCESSO_MAX - USO_PROCESSO_MIN);

        utilizaRecurso = new Thread(() -> {
            System.out.println("O processo " + processo + " está consumindo o recurso.");
            setRecursoEmUso(true, processo);
            
            try {
                Thread.sleep(randomUsageTime);
            } catch (InterruptedException e) {
            }
            
            System.out.println("O processo " + processo + " parou de consumir o recurso.");
            processo.liberarRecurso();
        });
        utilizaRecurso.start();
    }

    private void liberarRecurso() {
        setRecursoEmUso(false, this);

        if (!listaDeEsperaEstaVazia()) {
            Processo processoEmEspera = getListaDeEspera().removeFirst();
            processoEmEspera.acessaORecurso();
            System.out.println("Processo " + processoEmEspera + " foi removido da lista de espera.");
            System.out.println("Lista de espera: " + getListaDeEspera());
        }
    }

    public void removeProcesso() {
        if (isCoordenador()) {
            conexao.encerraAConexao();
        } else {
            removerDaListaDeEspera(this);
            if (ControleProcesso.isUsandoRecurso(this)) {
                pararRecurso();
                liberarRecurso();
            }
        }

        ControleProcesso.removerProcesso(this);
    }

    @Override
    public boolean equals(Object objeto) {
        Processo processo = (Processo) objeto;
        if (processo == null) {
            return false;
        }

        return this.idProcesso == processo.idProcesso;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getIdProcesso());
    }
}
