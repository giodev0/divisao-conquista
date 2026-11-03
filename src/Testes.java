import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Executor de testes (sem dependências externas).
 *
 *   java -cp out Testes [pasta_dos_testes]
 *
 * 1. Casos fixos: lê testes/casos_subarranjo.txt e testes/casos_par.txt e
 *    confere TODAS as implementações contra a saída esperada.
 * 2. Verificação cruzada aleatória: milhares de entradas pequenas (com muitos
 *    zeros, empates, duplicados e colineares), comparando D&C e Kadane com a
 *    força bruta, que serve de oráculo.
 *
 * Termina com código 1 se algum teste falhar.
 */
public final class Testes {

    private static int ok = 0, falhas = 0;
    private static final double EPS = 1e-6;
    private static final int[] LIMIARES_TESTE = {2, 3, 4, ParMaisProximo.LIMIAR_PADRAO};

    public static void main(String[] args) throws IOException {
        Path pasta = Path.of(args.length > 0 ? args[0] : "testes");
        System.out.println("== Etapa 1: casos fixos ==");
        casosSubarranjo(pasta.resolve("casos_subarranjo.txt"));
        System.out.println("== Etapa 2: casos fixos ==");
        casosPar(pasta.resolve("casos_par.txt"));
        System.out.println("== Verificação cruzada aleatória ==");
        cruzadoSubarranjo();
        cruzadoPar();
        System.out.printf("%nResultado: %d verificações OK, %d falhas%n", ok, falhas);
        if (falhas > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ casos fixos

    record Caso(String nome, String justificativa, List<String> entrada, String[] esperado) {}

    static List<Caso> lerCasos(Path arq) throws IOException {
        List<Caso> casos = new ArrayList<>();
        String nome = null, just = null;
        List<String> ent = null;
        for (String linha : Files.readAllLines(arq)) {
            linha = linha.strip();
            if (linha.isEmpty() || linha.startsWith("#")) continue;
            if (linha.startsWith("caso:")) {
                String[] p = linha.substring(5).split("\\|", 2);
                nome = p[0].strip();
                just = p.length > 1 ? p[1].strip() : "";
            } else if (linha.startsWith("entrada:")) {
                ent = List.of(linha.substring(8).strip().split("\\s+"));
            } else if (linha.startsWith("esperado:")) {
                casos.add(new Caso(nome, just, ent, linha.substring(9).strip().split("\\s+")));
            }
        }
        return casos;
    }

    static void casosSubarranjo(Path arq) throws IOException {
        for (Caso c : lerCasos(arq)) {
            int[] v = Main.lerVetor(c.entrada());
            String esp = String.join(" ", c.esperado());
            conferir(c.nome() + " [forca]", esp, SubarranjoMaximo.forcaBruta(v).toString());
            conferir(c.nome() + " [dc]", esp, SubarranjoMaximo.divisaoConquista(v).toString());
            conferir(c.nome() + " [kadane]", esp, SubarranjoMaximo.kadane(v).toString());
        }
    }

    static void casosPar(Path arq) throws IOException {
        for (Caso c : lerCasos(arq)) {
            ParMaisProximo.Ponto[] p = Main.lerPontos(c.entrada());
            conferirPar(c.nome() + " [forca]", c.esperado(), p, ParMaisProximo.forcaBruta(p));
            for (int lim : LIMIARES_TESTE)
                conferirPar(c.nome() + " [dc limiar=" + lim + "]", c.esperado(), p,
                        ParMaisProximo.divisaoConquista(p, lim));
        }
    }

    static void conferir(String nome, String esperado, String obtido) {
        if (esperado.equals(obtido)) { ok++; System.out.println("  OK    " + nome); }
        else { falhas++; System.out.println("  FALHA " + nome + ": esperado [" + esperado + "], obtido [" + obtido + "]"); }
    }

    static void conferirPar(String nome, String[] esp, ParMaisProximo.Ponto[] p, ParMaisProximo.Resultado r) {
        double d = Double.parseDouble(esp[0]);
        boolean certo = Math.abs(r.distancia() - d) < EPS && parValido(p, r);
        if (certo && !esp[1].equals("*"))
            certo = r.i() == Integer.parseInt(esp[1]) && r.j() == Integer.parseInt(esp[2]);
        if (certo) { ok++; System.out.println("  OK    " + nome); }
        else { falhas++; System.out.println("  FALHA " + nome + ": esperado [" + String.join(" ", esp) + "], obtido [" + r + "]"); }
    }

    /** O par devolvido existe e tem mesmo a distância informada. */
    static boolean parValido(ParMaisProximo.Ponto[] p, ParMaisProximo.Resultado r) {
        if (r.i() < 0 || r.j() >= p.length || r.i() >= r.j()) return false;
        return Math.abs(Math.sqrt(ParMaisProximo.dist2(p[r.i()], p[r.j()])) - r.distancia()) < EPS;
    }

    // ------------------------------------------------------------------ aleatórios

    static void cruzadoSubarranjo() {
        Random rnd = new Random(42);
        int erros = 0, total = 0;
        for (int t = 0; t < 20000; t++) {
            int n = 1 + rnd.nextInt(t < 10000 ? 12 : 300);
            int amp = 1 + rnd.nextInt(t % 3 == 0 ? 3 : 1000);   // amplitude pequena => muitos empates
            int[] v = new int[n];
            for (int i = 0; i < n; i++) v[i] = rnd.nextInt(2 * amp + 1) - amp;
            String bf = SubarranjoMaximo.forcaBruta(v).toString();
            String dc = SubarranjoMaximo.divisaoConquista(v).toString();
            String kd = SubarranjoMaximo.kadane(v).toString();
            total++;
            if (!bf.equals(dc) || !bf.equals(kd)) {
                if (erros++ < 5) System.out.println("  FALHA vetor=" + java.util.Arrays.toString(v) + " bf=" + bf + " dc=" + dc + " kadane=" + kd);
            }
        }
        registrar("subarranjo: " + total + " vetores aleatórios (força bruta = D&C = Kadane, inclusive índices)", erros);
    }

    static void cruzadoPar() {
        Random rnd = new Random(7);
        int erros = 0, total = 0;
        for (int t = 0; t < 4000; t++) {
            int n = 2 + rnd.nextInt(t < 3000 ? 60 : 1500);
            int grade = 1 + rnd.nextInt(t % 2 == 0 ? 10 : 100000);   // grade pequena => duplicados/colineares
            boolean vertical = t % 17 == 0;
            ParMaisProximo.Ponto[] p = new ParMaisProximo.Ponto[n];
            for (int i = 0; i < n; i++) {
                double x = vertical ? 3 : rnd.nextInt(grade);
                double y = rnd.nextInt(grade) + (t % 5 == 0 ? rnd.nextDouble() : 0);
                p[i] = new ParMaisProximo.Ponto(x, y, i);
            }
            ParMaisProximo.Resultado bf = ParMaisProximo.forcaBruta(p);
            for (int lim : LIMIARES_TESTE) {
                ParMaisProximo.Resultado dc = ParMaisProximo.divisaoConquista(p, lim);
                total++;
                if (Math.abs(bf.distancia() - dc.distancia()) > EPS || !parValido(p, dc)) {
                    if (erros++ < 5) System.out.println("  FALHA n=" + n + " limiar=" + lim + " bf=" + bf + " dc=" + dc);
                }
            }
        }
        registrar("par: " + total + " execuções aleatórias (D&C com limiares 2,3,4,16 = força bruta)", erros);
    }

    static void registrar(String desc, int erros) {
        if (erros == 0) { ok++; System.out.println("  OK    " + desc); }
        else { falhas++; System.out.println("  FALHA " + desc + " — " + erros + " divergências"); }
    }

    static { Locale.setDefault(Locale.ROOT); }
}
