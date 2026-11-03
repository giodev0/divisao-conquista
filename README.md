# Trabalho Prático — Divisão e Conquista (Java)

Etapa 1: subarranjo de soma máxima (força bruta, divisão e conquista e Kadane O(n)).
Etapa 2: par de pontos mais próximos (força bruta e divisão e conquista O(n log n) com limiar).
Etapa 3: medições, CSV bruto, tabelas e gráficos.

## Estrutura

```
src/
  SubarranjoMaximo.java   Etapa 1: forcaBruta, divisaoConquista, kadane
  ParMaisProximo.java     Etapa 2: forcaBruta, divisaoConquista(pontos, limiar)
  Main.java               execução pela linha de comando (lê da entrada padrão)
  Testes.java             bateria de testes (sem dependências externas)
  Benchmark.java          medições de tempo -> CSV
testes/
  casos_subarranjo.txt    casos da Etapa 1: entrada, saída esperada e justificativa
  casos_par.txt           casos da Etapa 2: entrada, saída esperada e justificativa
scripts/
  analise.py              gera tabelas.md e os gráficos a partir dos CSVs
resultados/               CSVs brutos, maquina.txt, tabelas.md, graficos/
```

## Requisitos

- JDK 17 ou superior (testado com OpenJDK 21)
- Para os gráficos: Python 3 com `numpy` e `matplotlib` (`pip install numpy matplotlib`)

## Compilar

```bash
javac -encoding UTF-8 -d out src/*.java
```

## Executar

Etapa 1 — entrada: `n` seguido de `n` inteiros. Saída: `soma inicio fim`.

```bash
echo "8 2 -3 4 -1 -2 1 5 -3" | java -cp out Main subarranjo dc       # 7 2 6
java -cp out Main subarranjo forca  < entrada.txt
java -cp out Main subarranjo kadane < entrada.txt
```

Etapa 2 — entrada: `n` seguido de `n` pares `x y`. Saída: `distancia i j` (6 casas decimais).

```bash
echo "4 0 0 3 4 1 1 7 7" | java -cp out Main par dc          # 1.414214 0 2
java -cp out Main par forca < pontos.txt
java -cp out Main par dc 32 < pontos.txt                      # limiar 32
```

Convenções:
- Índices começam em 0. Para começar em 1, acrescente `--base1`.
- Etapa 1, desempate: entre intervalos de mesma soma máxima, vence o de menor início e, depois, o de menor fim. O subarranjo nunca é vazio (só negativos → maior elemento). Somas em `long`.
- Etapa 2: `i < j` na ordem da entrada. Se vários pares empatam na distância mínima, qualquer um deles é resposta válida.

## Testes

```bash
java -cp out Testes
```

Roda todos os casos de `testes/` contra **todas** as implementações (na Etapa 2, a D&C também com limiares 2, 3 e 4, para forçar a recursão nos casos pequenos) e depois faz uma verificação cruzada aleatória: 20.000 vetores e 16.000 execuções de pontos (com muitos empates, zeros, duplicados e colineares), comparando cada versão rápida com a força bruta. Sai com código 1 se algo falhar.

## Medições

```bash
java -Xss64m -cp out Benchmark resultados 10      # 10 execuções por tamanho (~1 min)
python scripts/analise.py resultados
```

Gera `resultados/medicoes_escala.csv`, `medicoes_cruzamento.csv`, `medicoes_limiar.csv` (uma linha por execução), `maquina.txt`, `tabelas.md` e `graficos/*.png`. Tamanhos: 100, 200, …, 6.400 (como no enunciado) e mais três dobras até 51.200 para a tendência ficar clara. Para n pequenos, cada execução repete o algoritmo até somar ≥ 5 ms e registra o tempo médio por chamada (coluna `repeticoes`).

**Importante:** os resultados incluídos foram gerados numa máquina virtual de 2 núcleos (Intel Xeon 2,8 GHz, OpenJDK 21, Linux). Rode o benchmark no computador de vocês antes de entregar e preencha o modelo do processador em `maquina.txt`.

## Notas para o relatório

**Recorrências**

| Algoritmo | Recorrência | Solução |
|---|---|---|
| Subarranjo — força bruta | — (dois laços: Σ (n−i) = n(n+1)/2) | Θ(n²) |
| Subarranjo — D&C | T(n) = 2T(n/2) + Θ(n), T(1) = Θ(1) | Θ(n log n) (Mestre, caso 2: a = 2, b = 2, f(n) = Θ(n^log₂2)) |
| Subarranjo — Kadane | T(n) = T(n−1) + Θ(1) | Θ(n) |
| Par — força bruta | — (n(n−1)/2 pares) | Θ(n²) |
| Par — D&C | T(n) = 2T(n/2) + Θ(n), T(n ≤ L) = Θ(L²) | Θ(n log n), mais Θ(n log n) da ordenação inicial |

No par mais próximo o Θ(n) da combinação vem de: separar a lista ordenada por y em esquerda/direita (sem reordenar), montar a faixa e fazer no máximo 7 comparações por ponto da faixa. Com limiar L, as folhas custam (n/L)·Θ(L²) = Θ(nL), que é linear: o limiar muda a constante, não a ordem.

**Por que bastam 7 comparações por ponto na faixa.** Seja δ a menor distância dos dois lados. Se p e q estão na faixa e d(p, q) < δ, então |y_q − y_p| < δ; logo q está no retângulo de largura 2δ e altura δ que começa na altura de p. Dividindo esse retângulo em 8 quadrados de lado δ/2 (4 de cada lado da linha), cada quadrado tem no máximo 1 ponto: dois pontos no mesmo quadrado ficariam do mesmo lado e a distância entre eles seria no máximo a diagonal δ/√2 < δ, contradizendo δ. Então o retângulo tem no máximo 8 pontos, um deles o próprio p, e na ordem por y basta olhar os 7 seguintes. O código ainda para antes quando a diferença em y chega a δ.

**Quem vence em cada tamanho** (ver `medicoes_cruzamento.csv` e `graficos/cruzamento.png`). Para n pequeno a força bruta ganha: tem laços simples, sem chamadas recursivas, sem alocar vetores e sem ordenar; a constante dela é bem menor. Nas medições incluídas, na Etapa 1 a D&C passa a ganhar perto de n ≈ 96; na Etapa 2 a D&C completa (com ordenação) só ganha a partir de algo entre 400 e 800 pontos, porque paga a ordenação inicial e a alocação dos subvetores. Para n grande a diferença de ordem domina: com 51.200 elementos a D&C foi ~220× mais rápida na Etapa 1 e ~40× na Etapa 2.

**Limiar da Etapa 2** (ver `medicoes_limiar.csv` e `graficos/limiar.png`). Sem limiar (L = 2) é o mais lento; de L ≈ 12 a L ≈ 128 a curva fica praticamente plana, uns 15–20% mais rápida. O padrão no código é **16**: está no platô e é o menor valor dele, o que mantém pequeno o custo quadrático de cada folha. Se nas medições de vocês o mínimo cair em outro ponto do platô, ajustem `LIMIAR_PADRAO` em `ParMaisProximo.java` e citem os números.

**Observação sobre as curvas normalizadas.** Em `graficos/*_normalizado.png`, tempo/g(n) deve ficar aproximadamente constante. Há um degrau perto de n ≈ 1.600 na Etapa 2: é quando os dados deixam de caber na cache do processador, não uma mudança de complexidade. Os expoentes empíricos (inclinação no gráfico log-log) ficaram 1,99 e 2,03 para as forças brutas, 1,09 e 1,16 para as D&C (n log n entre 3.200 e 51.200 tem inclinação local ≈ 1,07–1,1) e 0,94 para Kadane.
