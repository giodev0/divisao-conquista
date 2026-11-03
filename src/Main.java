import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Uso (entrada pela entrada padrão):
 *
 *   java -cp out Main subarranjo [forca|dc|kadane]   < entrada.txt
 *       entrada: n seguido de n inteiros
 *       saída:   soma inicio fim        (índices 0-based)
 *
 *   java -cp out Main par [forca|dc] [limiar]        < entrada.txt
 *       entrada: n seguido de n pares "x y"
 *       saída:   distancia i j          (i < j, índices 0-based na ordem da entrada)
 *
 * Algoritmo padrão: dc. Acrescente --base1 para imprimir índices começando em 1.
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("uso: Main subarranjo [forca|dc|kadane] | Main par [forca|dc] [limiar]  [--base1]");
            System.exit(2);
        }
        boolean base1 = false;
        List<String> a = new ArrayList<>();
        for (String s : args) { if (s.equals("--base1")) base1 = true; else a.add(s); }
        int off = base1 ? 1 : 0;

        String problema = a.get(0);
        String alg = a.size() > 1 ? a.get(1) : "dc";
        List<String> tokens = lerTokens(System.in);

        switch (problema) {
            case "subarranjo" -> {
                int[] v = lerVetor(tokens);
                SubarranjoMaximo.Resultado r = switch (alg) {
                    case "forca" -> SubarranjoMaximo.forcaBruta(v);
                    case "dc" -> SubarranjoMaximo.divisaoConquista(v);
                    case "kadane" -> SubarranjoMaximo.kadane(v);
                    default -> throw new IllegalArgumentException("algoritmo desconhecido: " + alg);
                };
                System.out.println(r.soma() + " " + (r.inicio() + off) + " " + (r.fim() + off));
            }
            case "par" -> {
                ParMaisProximo.Ponto[] p = lerPontos(tokens);
                int limiar = a.size() > 2 ? Integer.parseInt(a.get(2)) : ParMaisProximo.LIMIAR_PADRAO;
                ParMaisProximo.Resultado r = switch (alg) {
                    case "forca" -> ParMaisProximo.forcaBruta(p);
                    case "dc" -> ParMaisProximo.divisaoConquista(p, limiar);
                    default -> throw new IllegalArgumentException("algoritmo desconhecido: " + alg);
                };
                System.out.printf(java.util.Locale.ROOT, "%.6f %d %d%n", r.distancia(), r.i() + off, r.j() + off);
            }
            default -> throw new IllegalArgumentException("problema desconhecido: " + problema);
        }
    }

    // ---------------- leitura ----------------

    static List<String> lerTokens(InputStream in) throws IOException {
        String txt = new String(new BufferedInputStream(in).readAllBytes());
        List<String> t = new ArrayList<>();
        for (String s : txt.trim().split("\\s+")) if (!s.isEmpty()) t.add(s);
        return t;
    }

    static int[] lerVetor(List<String> t) {
        int n = Integer.parseInt(t.get(0));
        if (t.size() - 1 < n) throw new IllegalArgumentException("esperados " + n + " valores, lidos " + (t.size() - 1));
        int[] v = new int[n];
        for (int i = 0; i < n; i++) v[i] = Integer.parseInt(t.get(i + 1));
        return v;
    }

    static ParMaisProximo.Ponto[] lerPontos(List<String> t) {
        int n = Integer.parseInt(t.get(0));
        if (t.size() - 1 < 2 * n) throw new IllegalArgumentException("esperadas " + n + " coordenadas (x y)");
        ParMaisProximo.Ponto[] p = new ParMaisProximo.Ponto[n];
        for (int i = 0; i < n; i++)
            p[i] = new ParMaisProximo.Ponto(Double.parseDouble(t.get(1 + 2 * i)), Double.parseDouble(t.get(2 + 2 * i)), i);
        return p;
    }
}
