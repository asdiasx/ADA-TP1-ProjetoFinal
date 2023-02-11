package impl;

import dominio.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class CampeonatoBrasileiroImpl implements EstatisticasCampeonato {

    private final List<Jogo> jogos;
    private final Predicate<Jogo> filtro;

    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = lerArquivo(arquivo);
        this.filtro = filtro;
    }

    @Override
    public List<Jogo> lerArquivo(Path file) throws IOException {
        return Files.readAllLines(file).stream()
                .skip(1)
                .map((linha) -> {
                    var dados = linha.split(";");
                    Integer rodada = Integer.parseInt(dados[0]);

                    LocalDate dataJogo = LocalDate.parse(dados[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime horario = dados[2].equals("") ? null : LocalTime.parse(dados[2].replace('h', ':')); // uso do 'if' devido à linha 2196 no csv estar com o horário vazio
                    DayOfWeek dia = dataJogo.getDayOfWeek();

                    DataDoJogo data = new DataDoJogo(dataJogo, horario, dia);

                    Time mandante = new Time(dados[4]);

                    Time visitante = new Time(dados[5]);

                    Time vencedor = new Time(dados[6]);

                    String arena = dados[7];
                    Integer mandantePlacar = Integer.parseInt(dados[8]);
                    Integer visitantePlacar = Integer.parseInt(dados[9]);
                    String estadoMandante = dados[10];
                    String estadoVisitante = dados[11];
                    String estadoVencedor = dados[12];

                    return new Jogo(rodada, data, mandante, visitante, vencedor, arena, mandantePlacar, visitantePlacar,
                            estadoMandante, estadoVisitante, estadoVencedor);
                })
                .toList();
    }

    @Override
    public IntSummaryStatistics getEstatisticasPorJogo() {

        return jogos.stream()
                .filter(filtro)
                .collect(Collectors.summarizingInt(jogo -> jogo.mandantePlacar()
                        + jogo.visitantePlacar()));
    }

    @Override
    public Long getTotalVitoriasEmCasa() {
        return jogos.stream()
                .filter(filtro
                        .and(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar()))
                .count();
    }

    @Override
    public Long getTotalVitoriasForaDeCasa() {
        return jogos.stream().filter(filtro.and(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar())).count();
    }

    @Override
    public Long getTotalEmpates() {
        return jogos.stream().filter(filtro.and(jogo -> jogo.mandantePlacar().equals(jogo.visitantePlacar()))).count();
    }

    @Override
    public Long getTotalJogosComMenosDe3Gols() {
        return jogos.stream().filter(filtro).map(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar()).filter(n -> n < 3).count();
    }

    @Override
    public Long getTotalJogosCom3OuMaisGols() {
        return jogos.stream().filter(filtro).map(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar()).filter(n -> n > 3).count();
    }

    @Override
    public Map.Entry<Resultado, Long> getPlacarMaisRepetido() {
        return jogos.stream()
                .filter(filtro)
                .map(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()))
                .collect(groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    @Override
    public Map.Entry<Resultado, Long> getPlacarMenosRepetido() {
        return jogos.stream()
                .filter(filtro)
                .map(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()))
                .collect(groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);
    }

    @Override
    public Set<PosicaoTabela> getTabela() {

        return jogos.stream()
                .filter(filtro)
                .map(Jogo::mandante)
                .distinct()
                .map(time -> new PosicaoTabela(time,
                        getVitoriasPorTime(time),
                        getDerrotasPorTime(time),
                        getEmpatesPorTime(time),
                        getGolsPositivosPorTime(time),
                        getGolsSofridosPorTime(time),
                        getSaldoDeGolsPorTime(time),
                        getQuantidadeJogosPorTime(time)
                ))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private long getQuantidadeJogosPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                .count();
    }

    private long getGolsPositivosPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                .map(jogo -> {
                    if (jogo.mandante().equals(time)) {
                        return jogo.mandantePlacar();
                    }
                    return jogo.visitantePlacar();
                })
                .reduce(0, Integer::sum);
    }

    private long getGolsSofridosPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                .map(jogo -> {
                    if (jogo.mandante().equals(time)) {
                        return jogo.visitantePlacar();
                    }
                    return jogo.mandantePlacar();
                })
                .reduce(0, Integer::sum);
    }

    private long getSaldoDeGolsPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                .map(jogo -> {
                    if (jogo.mandante().equals(time)) {
                        return jogo.mandantePlacar() - jogo.visitantePlacar();
                    }
                    return jogo.visitantePlacar() - jogo.mandantePlacar();
                })
                .reduce(0, Integer::sum);
    }

    private long getVitoriasPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> (jogo.mandante().equals(time) && jogo.mandantePlacar() > jogo.visitantePlacar()) ||
                        (jogo.visitante().equals(time) && (jogo.visitantePlacar() > jogo.mandantePlacar())))
                .count();
    }

    private long getDerrotasPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> (jogo.mandante().equals(time) && jogo.mandantePlacar() < jogo.visitantePlacar()) ||
                        (jogo.visitante().equals(time) && (jogo.visitantePlacar() < jogo.mandantePlacar())))
                .count();
    }

    private long getEmpatesPorTime(Time time) {
        return jogos.stream()
                .filter(filtro)
                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                .filter(jogo -> jogo.mandantePlacar().equals(jogo.visitantePlacar()))
                .count();
    }
}