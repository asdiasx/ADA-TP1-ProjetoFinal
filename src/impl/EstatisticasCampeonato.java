package impl;

import dominio.Jogo;
import dominio.PosicaoTabela;
import dominio.Resultado;

import java.io.IOException;
import java.nio.file.Path;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EstatisticasCampeonato {
    List<Jogo> lerArquivo(Path file) throws IOException;

    IntSummaryStatistics getEstatisticasPorJogo();

    Long getTotalVitoriasEmCasa();

    Long getTotalVitoriasForaDeCasa();

    Long getTotalEmpates();

    Long getTotalJogosComMenosDe3Gols();

    Long getTotalJogosCom3OuMaisGols();

    Map.Entry<Resultado, Long> getPlacarMaisRepetido();

    Map.Entry<Resultado, Long> getPlacarMenosRepetido();

    Set<PosicaoTabela> getTabela();
}
