
import java.util.LinkedList;
import java.util.Random;

public class Processo {

    private Thread utilizaRecurso = new Thread();
    private int idProcesso;
    private Conexao conexao = new Conexao();
    private boolean coordenadorAtual = false;

    private static final int usoMinimoDoProcesso = 10000;
    private static final int usoMaximoDoProcesso = 25000;

    private LinkedList<Processo> listaDeEspera;
    private boolean recursoEmUso;

    public Processo(int idProcesso) {
        this.idProcesso = idProcesso;
        setCoordenadorAtual(false);
    }

    public boolean isCoordenador() {
        return coordenadorAtual;
    }

    private void pararRecurso() {
        if (utilizaRecurso.isAlive()) {
            utilizaRecurso.interrupt();
        }
    }

    public boolean recursoEstaEmUso() {
        return encontraOCoordenador().recursoEmUso;
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
            coordenador = eleicao.realizaEleicao(this.getIdProcesso());
        }
        return coordenador;
    }

    public void acessaORecurso() {
        if (ControleProcesso.estaUsandoRecurso(this) || this.isCoordenador()) {
            return;
        }

        String resultado = conexao.fazerRequisicao("O processo " + this + " quer consumir o recurso!\n");

        System.out.println("O resultado da requisição do processo " + this + " é: " + resultado);

        if (resultado.equals(Conexao.permiteAcesso)) {
            utilizaRecurso(this);
        } else if (resultado.equals(Conexao.negaAcesso)) {
            adicionaNaListaDeEspera(this);
        }
    }

    private void adicionaNaListaDeEspera(Processo processoEmEspera) {
        getListaDeEspera().add(processoEmEspera);

        System.out.println("O processo " + this + " foi adicionado na lista de espera!");
        System.out.println("Os processos que estão na lista de espera são: " + getListaDeEspera());
    }

    private void utilizaRecurso(Processo processo) {
        Random random = new Random();
        int randomUsageTime = usoMinimoDoProcesso + random.nextInt(usoMaximoDoProcesso - usoMinimoDoProcesso);

        utilizaRecurso = new Thread(() -> {
            System.out.println("O processo " + processo + " está consumindo o recurso.");
            setRecursoEmUso(true, processo);

            try {
                Thread.sleep(randomUsageTime);
            } catch (InterruptedException e) {
            }

            System.out.println("O processo " + processo + " parou de consumir o recurso.");
            processo.liberaRecurso();
        });
        utilizaRecurso.start();
    }

    private void liberaRecurso() {
        setRecursoEmUso(false, this);

        if (!listaDeEsperaEstaVazia()) {
            Processo processoEmEspera = getListaDeEspera().removeFirst();
            processoEmEspera.acessaORecurso();
            System.out.println("O processo " + processoEmEspera + " foi removido da lista de espera.");
            System.out.println("Processos na lista de espera: " + getListaDeEspera());
        }
    }

    public void removeProcesso() {
        if (isCoordenador()) {
            conexao.encerraAConexao();
        } else {
            removerDaListaDeEspera(this);
            if (ControleProcesso.estaUsandoRecurso(this)) {
                pararRecurso();
                liberaRecurso();
            }
        }

        ControleProcesso.removeOProcesso(this);
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

    //Getters and Setters
    public int getIdProcesso() {
        return idProcesso;
    }

    public void setCoordenadorAtual(boolean ehCoordenador) {
        this.coordenadorAtual = ehCoordenador;
        if (this.coordenadorAtual) {
            listaDeEspera = new LinkedList<>();
            conexao.conectar(this);

            if (ControleProcesso.estaSendoConsumido()) {
                ControleProcesso.getConsumidor().pararRecurso();
            }

            recursoEmUso = false;
        }
    }

    public void setRecursoEmUso(boolean estaEmUso, Processo consumidor) {
        Processo coordenador = encontraOCoordenador();

        coordenador.recursoEmUso = estaEmUso;
        ControleProcesso.setConsumidor(estaEmUso ? consumidor : null);
    }

    private LinkedList<Processo> getListaDeEspera() {
        return encontraOCoordenador().listaDeEspera;
    }
}
