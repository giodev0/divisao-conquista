import java.util.Arrays;
import java.util.Comparator;

/**
 * Etapa 2 — Par de Pontos Mais Próximos.
 *
 * Os pontos são identificados pelo índice na entrada (0-based). A saída é a
 * distância mínima e os índices i < j do par. Em caso de empate na distância,
 * qualquer par com a distância mínima é uma resposta válida.
 */
public final class ParMaisProximo {

    private ParMaisProximo() {}

    /** Limiar padrão: subproblemas com até LIMIAR pontos são resolvidos por força bruta. */
    public static final int LIMIAR_PADRAO = 16;

    /** Máximo de vizinhos comparados por ponto na faixa (ver justificativa no relatório). */
    public static final int VIZINHOS_FAIXA = 7;

    public record Ponto(double x, double y, int id) {}

    public record Resultado(double distancia, int i, int j) {
        static final Resultado INFINITO = new Resultado(Double.POSITIVE_INFINITY, -1, -1);

        static Resultado de(Ponto p, Ponto q, double d2) {
            int a = Math.min(p.id(), q.id()), b = Math.max(p.id(), q.id());
            return new Resultado(Math.sqrt(d2), a, b);
        }

        @Override
        public String toString() {
            return String.format(java.util.Locale.ROOT, "%.6f %d %d", distancia, i, j);
        }
    }

    static double dist2(Ponto p, Ponto q) {
        double dx = p.x() - q.x(), dy = p.y() - q.y();
        return dx * dx + dy * dy;
    }

    // ------------------------------------------------------------------
    // 1. Força bruta — Θ(n²): compara os n(n-1)/2 pares.
    // ------------------------------------------------------------------
    public static Resultado forcaBruta(Ponto[] pts) {
        exigirDois(pts);
        return forcaBruta(pts, 0, pts.length);
    }

    private static Resultado forcaBruta(Ponto[] p, int lo, int hi) {
        double melhor = Double.POSITIVE_INFINITY;
        int bi = -1, bj = -1;
        for (int i = lo; i < hi; i++) {
            for (int j = i + 1; j < hi; j++) {
                double d = dist2(p[i], p[j]);
                if (d < melhor) { melhor = d; bi = i; bj = j; }
            }
        }
        return bi < 0 ? Resultado.INFINITO : Resultado.de(p[bi], p[bj], melhor);
    }

    // ------------------------------------------------------------------
    // 2. Divisão e conquista — T(n) = 2T(n/2) + Θ(n) = Θ(n log n)
    //    (mais Θ(n log n) da ordenação inicial, feita uma única vez)
    // ------------------------------------------------------------------
    public static Resultado divisaoConquista(Ponto[] pts) {
        return divisaoConquista(pts, LIMIAR_PADRAO);
    }

    public static Resultado divisaoConquista(Ponto[] pts, int limiar) {
        exigirDois(pts);
        if (limiar < 2) throw new IllegalArgumentException("limiar deve ser >= 2");
        int n = pts.length;

        // Ordenações iniciais com a biblioteca padrão (permitido pelo enunciado)
        Ponto[] px = pts.clone();
        Arrays.sort(px, Comparator.comparingDouble(Ponto::x).thenComparingDouble(Ponto::y));
        Ponto[] py = pts.clone();
        Arrays.sort(py, Comparator.comparingDouble(Ponto::y).thenComparingDouble(Ponto::x));

        // posição de cada ponto em px, para dividir py sem reordenar
        // (funciona mesmo com vários pontos de mesmo x)
        int[] posX = new int[maxId(pts) + 1];
        for (int k = 0; k < n; k++) posX[px[k].id()] = k;

        return rec(px, py, 0, n, limiar, posX, new Ponto[n]);
    }

    /**
     * Resolve px[lo..hi). py contém exatamente esses pontos, ordenados por y.
     */
    private static Resultado rec(Ponto[] px, Ponto[] py, int lo, int hi, int limiar,
                                 int[] posX, Ponto[] faixa) {
        int m = hi - lo;
        if (m <= limiar) return forcaBruta(px, lo, hi);        // caso base

        // DIVIDIR: linha vertical na mediana de x
        int mid = lo + m / 2;
        double xMeio = px[mid].x();
        Ponto[] pyEsq = new Ponto[mid - lo];
        Ponto[] pyDir = new Ponto[hi - mid];
        int e = 0, d = 0;
        for (Ponto p : py) {                                   // Θ(n), mantém a ordem por y
            if (posX[p.id()] < mid) pyEsq[e++] = p; else pyDir[d++] = p;
        }

        // CONQUISTAR
        Resultado esq = rec(px, pyEsq, lo, mid, limiar, posX, faixa);
        Resultado dir = rec(px, pyDir, mid, hi, limiar, posX, faixa);
        Resultado melhor = esq.distancia() <= dir.distancia() ? esq : dir;
        double delta = melhor.distancia();

        // COMBINAR: faixa de largura 2δ em torno da linha, já ordenada por y
        int k = 0;
        for (Ponto p : py) if (Math.abs(p.x() - xMeio) < delta) faixa[k++] = p;

        double melhor2 = delta * delta;
        Ponto ba = null, bb = null;
        for (int i = 0; i < k; i++) {
            Ponto p = faixa[i];
            for (int t = i + 1; t < k && t <= i + VIZINHOS_FAIXA; t++) {
                Ponto q = faixa[t];
                if (q.y() - p.y() >= delta) break;             // fora do retângulo δ × 2δ
                double d2 = dist2(p, q);
                if (d2 < melhor2) { melhor2 = d2; ba = p; bb = q; }
            }
        }
        return ba == null ? melhor : Resultado.de(ba, bb, melhor2);
    }

    private static int maxId(Ponto[] pts) {
        int m = 0;
        for (Ponto p : pts) {
            if (p.id() < 0) throw new IllegalArgumentException("id de ponto negativo");
            m = Math.max(m, p.id());
        }
        return m;
    }

    private static void exigirDois(Ponto[] pts) {
        if (pts == null || pts.length < 2)
            throw new IllegalArgumentException("são necessários pelo menos 2 pontos");
    }
}
