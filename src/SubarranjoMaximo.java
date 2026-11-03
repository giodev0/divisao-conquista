/**
 * Etapa 1 — Subarranjo de Soma Máxima.
 *
 * Regra de desempate (igual nas três implementações): entre os intervalos com a
 * soma máxima, escolhe-se o de MENOR índice de início; se ainda houver empate,
 * o de MENOR índice de fim (ou seja, o menor par (inicio, fim) em ordem
 * lexicográfica). Índices são 0-based. Somas usam long para evitar overflow.
 * O subarranjo nunca é vazio: se todos os valores forem negativos, a resposta
 * é o maior elemento.
 */
public final class SubarranjoMaximo {

    private SubarranjoMaximo() {}

    public record Resultado(long soma, int inicio, int fim) {
        @Override
        public String toString() {
            return soma + " " + inicio + " " + fim;
        }
    }

    /** true se o intervalo (soma, ini, fim) é melhor que o resultado atual pela regra de desempate. */
    private static boolean melhor(long soma, int ini, int fim, Resultado atual) {
        if (atual == null) return true;
        if (soma != atual.soma()) return soma > atual.soma();
        if (ini != atual.inicio()) return ini < atual.inicio();
        return fim < atual.fim();
    }

    // ------------------------------------------------------------------
    // 1. Força bruta — O(n²): testa todos os intervalos [i..j].
    // ------------------------------------------------------------------
    public static Resultado forcaBruta(int[] a) {
        exigirNaoVazio(a);
        long melhorSoma = Long.MIN_VALUE;
        int bi = 0, bj = 0;
        for (int i = 0; i < a.length; i++) {
            long soma = 0;
            for (int j = i; j < a.length; j++) {
                soma += a[j];
                // estritamente maior: como i e j crescem, o primeiro encontrado
                // já é o menor (inicio, fim) lexicográfico
                if (soma > melhorSoma) {
                    melhorSoma = soma;
                    bi = i;
                    bj = j;
                }
            }
        }
        return new Resultado(melhorSoma, bi, bj);
    }

    // ------------------------------------------------------------------
    // 2. Divisão e conquista — T(n) = 2T(n/2) + Θ(n) = Θ(n log n)
    // ------------------------------------------------------------------
    public static Resultado divisaoConquista(int[] a) {
        exigirNaoVazio(a);
        return dc(a, 0, a.length - 1);
    }

    private static Resultado dc(int[] a, int lo, int hi) {
        if (lo == hi) return new Resultado(a[lo], lo, lo);       // caso base: 1 elemento
        int mid = (lo + hi) >>> 1;
        Resultado esq = dc(a, lo, mid);                          // T(n/2)
        Resultado dir = dc(a, mid + 1, hi);                      // T(n/2)
        Resultado cruz = cruzandoMeio(a, lo, mid, hi);           // Θ(n)

        Resultado r = esq;
        if (melhor(cruz.soma(), cruz.inicio(), cruz.fim(), r)) r = cruz;
        if (melhor(dir.soma(), dir.inicio(), dir.fim(), r)) r = dir;
        return r;
    }

    /**
     * Melhor subarranjo que contém a[mid] e a[mid+1]. Como soma = L(i) + R(j),
     * maximizar a soma é maximizar L e R separadamente; para o desempate
     * escolhe-se o menor i (>=) e o menor j (>).
     */
    private static Resultado cruzandoMeio(int[] a, int lo, int mid, int hi) {
        long soma = 0, melhorEsq = Long.MIN_VALUE;
        int bi = mid;
        for (int i = mid; i >= lo; i--) {
            soma += a[i];
            if (soma >= melhorEsq) { melhorEsq = soma; bi = i; }
        }
        soma = 0;
        long melhorDir = Long.MIN_VALUE;
        int bj = mid + 1;
        for (int j = mid + 1; j <= hi; j++) {
            soma += a[j];
            if (soma > melhorDir) { melhorDir = soma; bj = j; }
        }
        return new Resultado(melhorEsq + melhorDir, bi, bj);
    }

    // ------------------------------------------------------------------
    // 3. Questão desafio — Kadane, Θ(n)
    // ------------------------------------------------------------------
    public static Resultado kadane(int[] a) {
        exigirNaoVazio(a);
        long atual = a[0];
        int iniAtual = 0;
        Resultado melhor = new Resultado(a[0], 0, 0);
        for (int j = 1; j < a.length; j++) {
            // estende se o prefixo acumulado não é negativo (>= 0 mantém o
            // início mais à esquerda em caso de empate)
            if (atual >= 0) {
                atual += a[j];
            } else {
                atual = a[j];
                iniAtual = j;
            }
            if (melhor(atual, iniAtual, j, melhor)) melhor = new Resultado(atual, iniAtual, j);
        }
        return melhor;
    }

    private static void exigirNaoVazio(int[] a) {
        if (a == null || a.length == 0)
            throw new IllegalArgumentException("o vetor precisa ter pelo menos 1 elemento");
    }
}
