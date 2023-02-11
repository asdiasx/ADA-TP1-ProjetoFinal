package dominio;

import java.util.Comparator;
import java.util.Objects;

public record PosicaoTabela(Time time,
                            Long vitorias,
                            Long derrotas,
                            Long empates,
                            Long golsPositivos,
                            Long golsSofridos,
                            Long saldoDeGols,
                            Long jogos) implements Comparable<PosicaoTabela> {

    @Override
    public String toString() {
        return time +
                ", pontos=" + getPontos() + // desenvolver forma de obter a pontuação
                ", vitorias=" + vitorias +
                ", derrotas=" + derrotas +
                ", empates=" + empates +
                ", golsPositivos=" + golsPositivos +
                ", golsSofridos=" + golsSofridos +
                ", saldoDeGols=" + saldoDeGols +
                ", jogos=" + jogos +
                '}';
    }

    public Long getPontos() {
        return 3 * vitorias + empates;
    }

    @Override
    public int compareTo(PosicaoTabela posTab) {
        return Objects.compare(this, posTab,
                Comparator.comparing(PosicaoTabela::getPontos)
                        .thenComparing(PosicaoTabela::vitorias)
                        .thenComparing(PosicaoTabela::saldoDeGols).reversed());
    }
}
