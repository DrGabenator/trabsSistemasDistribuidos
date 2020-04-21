
import java.util.LinkedList;

public class Eleicao {

    public Processo realizarEleicao(int idProcessoIniciador) {
        LinkedList<Integer> idProcessosConsultados = new LinkedList<>();

        ControleProcesso.getProcessosAtivos().forEach((p) -> {
            consultarProcesso(p.getIdProcesso(), idProcessosConsultados);
        });

        int idNovoCoordenador = idProcessoIniciador;
        for (Integer id : idProcessosConsultados) {
            if (id > idNovoCoordenador) {
                idNovoCoordenador = id;
            }
        }

        Processo coordenador = atualizarCoordenador(idNovoCoordenador);

        if (coordenador == null) {
            for (Processo p : ControleProcesso.getProcessosAtivos()) {
                if (p.getIdProcesso() == idProcessoIniciador) {
                    return p;
                }
            }
        }

        return coordenador;
    }

    private void consultarProcesso(int idProcesso, LinkedList<Integer> processosConsultados) {
        processosConsultados.add(idProcesso);
    }

    private Processo atualizarCoordenador(int idNovoCoordenador) {
        Processo coordenador = null;
        for (Processo p : ControleProcesso.getProcessosAtivos()) {
            if (p.getIdProcesso() == idNovoCoordenador) {
                p.setCoordenadorAtual(true);
                coordenador = p;
            } else {
                p.setCoordenadorAtual(false);
            }
        }

        return coordenador;
    }

}
