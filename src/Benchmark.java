import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Etapa 3 — medições.
 *
 *   java -Xss64m -cp out Benchmark [pasta_saida] [execucoes]
 *
 * Gera em pasta_saida (padrão "resultados"):
 *   medicoes_escala.csv    tempo x n para todos os algoritmos (Etapas 1 e 2)
 *   medicoes_cruzamento.csv  n pequenos: onde a força bruta vence a D&C
 *   medicoes_limiar.csv    D&C do par mais próximo variando o limiar
 *   maquina.txt            configuração da máquina / JVM
 *
 * Cada linha do CSV é UMA execução (dado bruto). Uma "execução" usa uma entrada
 * aleatória nova; para n pequenos, o algoritmo roda várias vezes seguidas sobre
 * a mesma entrada até somar >= 5 ms, e o tempo registrado é a média por chamada
 * (a resolução do relógio não mede bem chamadas de microssegundos).
 * Antes das medições há um aquecimento para o JIT da JVM compilar o código.
 */
public final class Benchmark {

    static final int[] TAMANHOS = {100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200};
    static final int[] TAMANHOS_CRUZ = {2, 4, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256};
    static final int[] LIMIARES = {2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 128, 256};
    static final int[] N_LIMIAR = {6400, 100000};
    static final long LOTE_MIN_NS = 5_000_000L;

    static volatile Object sumidouro;   // impede o JIT de descartar os resultados

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        Path saida = Path.of(args.length > 0 ? args[0] : "resultados");
        int exec = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        Files.createDirectories(saida);
        salvarMaquina(saida.resolve("maquina.txt"));

        System.out.println("Aquecendo a JVM...");
        aquecer();

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(saida.resolve("medicoes_escala.csv")))) {
            w.println("problema,algoritmo,n,execucao,tempo_ns,repeticoes");
            for (int n : TAMANHOS) {
                for (int e = 1; e <= exec; e++) {
                    Random r = new Random(1000L * n + e);
                    int[] v = vetor(r, n);
                    medir(w, "subarranjo", "forca_bruta", n, e, () -> SubarranjoMaximo.forcaBruta(v));
                    medir(w, "subarranjo", "divisao_conquista", n, e, () -> SubarranjoMaximo.divisaoConquista(v));
                    medir(w, "subarranjo", "kadane", n, e, () -> SubarranjoMaximo.kadane(v));
                    ParMaisProximo.Ponto[] p = pontos(r, n);
                    medir(w, "par", "forca_bruta", n, e, () -> ParMaisProximo.forcaBruta(p));
                    medir(w, "par", "divisao_conquista", n, e, () -> ParMaisProximo.divisaoConquista(p));
                }
                System.out.println("  escala n=" + n + " ok");
            }
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(saida.resolve("medicoes_cruzamento.csv")))) {
            w.println("problema,algoritmo,n,execucao,tempo_ns,repeticoes");
            for (int n : TAMANHOS_CRUZ) {
                for (int e = 1; e <= exec; e++) {
                    Random r = new Random(77L * n + e);
                    int[] v = vetor(r, n);
                    medir(w, "subarranjo", "forca_bruta", n, e, () -> SubarranjoMaximo.forcaBruta(v));
                    medir(w, "subarranjo", "divisao_conquista", n, e, () -> SubarranjoMaximo.divisaoConquista(v));
                    ParMaisProximo.Ponto[] p = pontos(r, n);
                    medir(w, "par", "forca_bruta", n, e, () -> ParMaisProximo.forcaBruta(p));
                    // limiar 2 = D&C "pura", recursão até o fim
                    medir(w, "par", "divisao_conquista_pura", n, e, () -> ParMaisProximo.divisaoConquista(p, 2));
                }
            }
            System.out.println("  cruzamento ok");
        }

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(saida.resolve("medicoes_limiar.csv")))) {
            w.println("n,limiar,execucao,tempo_ns,repeticoes");
            for (int n : N_LIMIAR) {
                for (int e = 1; e <= exec; e++) {
                    ParMaisProximo.Ponto[] p = pontos(new Random(31L * n + e), n);
                    for (int lim : LIMIARES) {
                        long[] t = cronometrar(() -> ParMaisProximo.divisaoConquista(p, lim));
                        w.printf("%d,%d,%d,%d,%d%n", n, lim, e, t[0], t[1]);
                    }
                }
                System.out.println("  limiar n=" + n + " ok");
            }
        }
        System.out.println("Pronto. Arquivos em " + saida.toAbsolutePath());
    }

    static void medir(PrintWriter w, String prob, String alg, int n, int e, Supplier<Object> f) {
        long[] t = cronometrar(f);
        w.printf("%s,%s,%d,%d,%d,%d%n", prob, alg, n, e, t[0], t[1]);
    }

    /** Retorna {tempo médio por chamada em ns, repetições}. */
    static long[] cronometrar(Supplier<Object> f) {
        long ini = System.nanoTime();
        sumidouro = f.get();
        long umaVez = System.nanoTime() - ini;
        if (umaVez >= LOTE_MIN_NS) return new long[]{umaVez, 1};
        int reps = (int) Math.min(1_000_000, Math.max(1, LOTE_MIN_NS / Math.max(1, umaVez)));
        ini = System.nanoTime();
        for (int k = 0; k < reps; k++) sumidouro = f.get();
        return new long[]{(System.nanoTime() - ini) / reps, reps};
    }

    static void aquecer() {
        Random r = new Random(1);
        for (int k = 0; k < 300; k++) {
            int n = 50 + r.nextInt(2000);
            int[] v = vetor(r, n);
            sumidouro = SubarranjoMaximo.forcaBruta(v);
            sumidouro = SubarranjoMaximo.divisaoConquista(v);
            sumidouro = SubarranjoMaximo.kadane(v);
            ParMaisProximo.Ponto[] p = pontos(r, n);
            sumidouro = ParMaisProximo.forcaBruta(p);
            sumidouro = ParMaisProximo.divisaoConquista(p);
            sumidouro = ParMaisProximo.divisaoConquista(p, 2);
        }
    }

    /** Variações diárias entre -1000 e 1000. */
    static int[] vetor(Random r, int n) {
        int[] v = new int[n];
        for (int i = 0; i < n; i++) v[i] = r.nextInt(2001) - 1000;
        return v;
    }

    /** Estações com coordenadas uniformes no quadrado [0, 1e6]². */
    static ParMaisProximo.Ponto[] pontos(Random r, int n) {
        ParMaisProximo.Ponto[] p = new ParMaisProximo.Ponto[n];
        for (int i = 0; i < n; i++) p[i] = new ParMaisProximo.Ponto(r.nextDouble() * 1e6, r.nextDouble() * 1e6, i);
        return p;
    }

    static void salvarMaquina(Path arq) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String txt = String.join("\n",
                "Sistema operacional: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")",
                "Processadores lógicos disponíveis: " + rt.availableProcessors(),
                "Memória máxima da JVM: " + (rt.maxMemory() / (1024 * 1024)) + " MB",
                "Java: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"),
                "Modelo do processador: preencher manualmente (ex.: Gerenciador de Tarefas > Desempenho, ou `lscpu`)",
                "");
        Files.writeString(arq, txt);
    }
}
