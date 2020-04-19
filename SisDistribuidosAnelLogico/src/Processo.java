//GRUPO 14
import java.util.LinkedList;

public class Processo {

    private int idProcesso;
    private boolean coordenadorAtual;

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

    //Getters e Setters:
    public int getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(int idProcesso) {
        this.idProcesso = idProcesso;
    }

    public void setCoordenadorAtual(boolean coordenadorAtual) {
        this.coordenadorAtual = coordenadorAtual;
    }
}
