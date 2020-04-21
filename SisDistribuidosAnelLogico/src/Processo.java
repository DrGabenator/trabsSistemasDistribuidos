//GRUPO 14

import java.util.LinkedList;
import java.util.Random;

public class Processo {

    private int idProcesso;
    private boolean coordenadorAtual = false;
    private Thread utilizaRecurso = new Thread();
    private Conexao conexao = new Conexao();

    private LinkedList<Processo> listaDeEspera;
    private boolean recursoEmUso;

    private static final int usoMinimoProcesso = 10000;
    private static final int usoMaximoProcesso = 20000;

    public Processo(int idProcesso) {
        setIdProcesso(idProcesso);
    }

    public Processo(int idProcesso, boolean ehCoordenador) {
        setIdProcesso(idProcesso);
        setCoordenadorAtual(ehCoordenador);
    }

    public boolean isCoordenadorAtual() {
        return coordenadorAtual;
    }

    public boolean novaRequisicao() {
        boolean resultadoDeRequisicao = false;
        for (Processo processo : AnelLogico.processosAtivos) {
            if (processo.isCoordenadorAtual()) {
                resultadoDeRequisicao = processo.recebeRequisicao(this.idProcesso);
            }
        }
        if (!resultadoDeRequisicao) {
            this.novaEleicao();
        }
        System.out.println("Requisição finalizada");
        return resultadoDeRequisicao;
    }

    private boolean recebeRequisicao(int pidOrigemRequisicao) {
        System.out.println("Requisição do processo " + pidOrigemRequisicao);
        return true;
    }

    private void novaEleicao() {
        System.out.println("-------------------- Eleição iniciada --------------------");

        LinkedList<Integer> idProcessosVerificados = new LinkedList<>();

        AnelLogico.processosAtivos.forEach((p) -> {
            p.consultarProcesso(idProcessosVerificados);
        });

        int idNovoCoordenador = this.getIdProcesso();

        for (Integer id : idProcessosVerificados) {
            if (id > idNovoCoordenador) {
                idNovoCoordenador = id;
            }
        }

        boolean resultadoAtualizacao = false;
        resultadoAtualizacao = atualizarCoordenador(idNovoCoordenador);

        if (resultadoAtualizacao) {
            System.out.println("Eleição FINALIZADA - Novo coordenador: " + idNovoCoordenador);
        } else {
            System.out.println("Eleição FINALIZADA - Sem novo coordenador");
        }
    }

    private void consultarProcesso(LinkedList<Integer> processosConsultados) {
        processosConsultados.add(this.getIdProcesso());
    }

    private boolean atualizarCoordenador(int idNovoCoordenador) {
        AnelLogico.processosAtivos.forEach((processo) -> {
            processo.setCoordenadorAtual(processo.getIdProcesso() == idNovoCoordenador);
        });
        return true;
    }

    private void interrompeAcessoRecurso() {
        if (utilizaRecurso.isAlive()) {
            utilizaRecurso.interrupt();
        }
    }

    public boolean isRecursoEmUso() {
        return encontrarCoordenador().recursoEmUso;
    }

    public void setRecursoEmUso(boolean estaEmUso, Processo consumidor) {
        Processo coordenador = encontrarCoordenador();

        coordenador.recursoEmUso = estaEmUso;
        ControleProcesso.setConsumidor(estaEmUso ? consumidor : null);
    }

    public boolean isListaDeEsperaVazia() {
        return getListaDeEspera().isEmpty();
    }

    private void removerDaListaDeEspera(Processo processo) {
        if (getListaDeEspera().contains(processo)) {
            getListaDeEspera().remove(processo);
        }
    }

    private Processo encontrarCoordenador() {
        Processo coordenador = ControleProcesso.getCoordenador();

        if (coordenador == null) {
            Eleicao eleicao = new Eleicao();
            coordenador = eleicao.realizarEleicao(this.getIdProcesso());
        }

        return coordenador;
    }

    public void acessarRecursoCompartilhado() {
        if (ControleProcesso.isUsandoRecurso(this) || this.isCoordenadorAtual()) {
            return;
        }

        String result = conexao.realizarRequisicao("Processo " + this + " quer consumir o recurso.\n");

        System.out.println("Resultado da requisição do processo " + this + ": " + result);

        if (result.equals(Conexao.PERMITIR_ACESSO)) {
            utilizarRecurso(this);
        } else if (result.equals(Conexao.NEGAR_ACESSO)) {
            adicionaNaListaDeEspera(this);
        }
    }

    private void adicionaNaListaDeEspera(Processo processoEmEspera) {
        getListaDeEspera().add(processoEmEspera);

        System.out.println("Processo " + this + " foi adicionado na lista de espera.");
        System.out.println("Lista de espera: " + getListaDeEspera());
    }

    private void utilizarRecurso(Processo processo) {
        Random random = new Random();
        int usoTempo = usoMinimoProcesso + random.nextInt(usoMaximoProcesso - usoMinimoProcesso);

        utilizaRecurso = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Processo " + processo + " está consumindo o recurso.");
                setRecursoEmUso(true, processo);

                try {
                    Thread.sleep(usoTempo);
                } catch (InterruptedException e) {
                }

                System.out.println("Processo " + processo + " parou de consumir o recurso.");
                processo.liberarRecurso();
            }
        });
        utilizaRecurso.start();
    }

    private void liberarRecurso() {
        setRecursoEmUso(false, this);

        if (!isListaDeEsperaVazia()) {
            Processo processoEmEspera = getListaDeEspera().removeFirst();
            processoEmEspera.acessarRecursoCompartilhado();
            System.out.println("Processo " + processoEmEspera + " foi removido da lista de espera.");
            System.out.println("Lista de espera: " + getListaDeEspera());
        }
    }

    public void destruir() {
        if (isCoordenadorAtual()) {
            conexao.encerraConexao();
        } else {
            removerDaListaDeEspera(this);
            if (ControleProcesso.isUsandoRecurso(this)) {
                interrompeAcessoRecurso();
                liberarRecurso();
            }
        }

        ControleProcesso.removeProcesso(this);
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

    //Getters e Setters:
    public int getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(int idProcesso) {
        this.idProcesso = idProcesso;
    }

    public void setCoordenadorAtual(boolean coordenadorAtual) {
        this.coordenadorAtual = coordenadorAtual;
        if (this.coordenadorAtual) {
            listaDeEspera = new LinkedList<>();
            conexao.conectar(this);

            if (ControleProcesso.isSendoConsumido()) {
                ControleProcesso.getConsumidor().interrompeAcessoRecurso();
            }

            recursoEmUso = false;
        }
    }

    public Thread getUtilizaRecurso() {
        return utilizaRecurso;
    }

    public void setUtilizaRecurso(Thread utilizaRecurso) {
        this.utilizaRecurso = utilizaRecurso;
    }

    public Conexao getConexao() {
        return conexao;
    }

    public void setConexao(Conexao conexao) {
        this.conexao = conexao;
    }

    public LinkedList<Processo> getListaDeEspera() {
        return encontrarCoordenador().listaDeEspera;
    }

    public void setListaDeEspera(LinkedList<Processo> listaDeEspera) {
        this.listaDeEspera = listaDeEspera;
    }

}
