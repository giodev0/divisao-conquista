# Resultados das medições

Tempos em milissegundos: média de N execuções ± desvio-padrão. Dados brutos em `medicoes_*.csv`.


## Etapa 1 — Subarranjo de soma máxima

| n | Força bruta (ms) | Divisão e conquista (ms) | Kadane (desafio) (ms) | razão FB / D&C |
|---:|---:|---:|---:|---:|
| 100 | 0.0020 ± 0.0002 | 0.0035 ± 0.0044 | 0.0002 ± 0.0001 | 0.6× |
| 200 | 0.0075 ± 0.0001 | 0.0039 ± 0.0006 | 0.0003 ± 0.0002 | 1.9× |
| 400 | 0.0307 ± 0.0020 | 0.0108 ± 0.0051 | 0.0007 ± 0.0006 | 2.8× |
| 800 | 0.1230 ± 0.0052 | 0.0219 ± 0.0058 | 0.0009 ± 0.0002 | 5.6× |
| 1600 | 0.6468 ± 0.1334 | 0.0845 ± 0.0508 | 0.0035 ± 0.0019 | 7.7× |
| 3200 | 2.1878 ± 0.4370 | 0.1119 ± 0.0327 | 0.0038 ± 0.0014 | 19.6× |
| 6400 | 7.9625 ± 1.2517 | 0.2502 ± 0.0958 | 0.0069 ± 0.0026 | 31.8× |
| 12800 | 31.9 ± 2.8789 | 0.4828 ± 0.1041 | 0.0133 ± 0.0046 | 66.0× |
| 25600 | 128.8 ± 11.3 | 1.0453 ± 0.2328 | 0.0225 ± 0.0065 | 123.3× |
| 51200 | 532.7 ± 71.4 | 2.3959 ± 0.5527 | 0.0550 ± 0.0228 | 222.3× |

Execuções por tamanho: 10.

Expoente empírico (inclinação de log t × log n nos 5 maiores n):

- Força bruta: 1.99 (previsto: n²)
- Divisão e conquista: 1.09 (previsto: n log₂ n)
- Kadane (desafio): 0.94 (previsto: n)


## Etapa 2 — Par de pontos mais próximos

| n | Força bruta (ms) | Divisão e conquista (ms) | razão FB / D&C |
|---:|---:|---:|---:|
| 100 | 0.0053 ± 0.0009 | 0.0165 ± 0.0050 | 0.3× |
| 200 | 0.0190 ± 0.0002 | 0.0293 ± 0.0010 | 0.6× |
| 400 | 0.0771 ± 0.0027 | 0.0874 ± 0.0050 | 0.9× |
| 800 | 0.3055 ± 0.0042 | 0.2423 ± 0.0087 | 1.3× |
| 1600 | 1.6404 ± 0.2998 | 0.9082 ± 0.1898 | 1.8× |
| 3200 | 6.1824 ± 1.2101 | 1.8020 ± 0.4480 | 3.4× |
| 6400 | 23.5 ± 3.9784 | 3.6900 ± 0.5294 | 6.4× |
| 12800 | 94.7 ± 9.7795 | 8.1033 ± 1.0092 | 11.7× |
| 25600 | 395.3 ± 49.2 | 20.4 ± 3.1123 | 19.4× |
| 51200 | 1706.4 ± 110.6 | 42.6 ± 8.2853 | 40.1× |

Execuções por tamanho: 10.

Expoente empírico (inclinação de log t × log n nos 5 maiores n):

- Força bruta: 2.03 (previsto: n²)
- Divisão e conquista: 1.16 (previsto: n log₂ n)


## Entradas pequenas: força bruta × divisão e conquista


### Etapa 1

| n | Força bruta (µs) | Divisão e conquista (µs) | mais rápido |
|---:|---:|---:|---|
| 2 | 0.055 | 0.084 | Força bruta |
| 4 | 0.052 | 0.138 | Força bruta |
| 8 | 0.094 | 0.196 | Força bruta |
| 12 | 0.145 | 0.343 | Força bruta |
| 16 | 0.196 | 0.460 | Força bruta |
| 24 | 0.240 | 0.550 | Força bruta |
| 32 | 0.257 | 0.481 | Força bruta |
| 48 | 0.536 | 0.877 | Força bruta |
| 64 | 0.928 | 1.244 | Força bruta |
| 96 | 1.970 | 1.797 | Divisão e conquista |
| 128 | 5.112 | 3.513 | Divisão e conquista |
| 192 | 7.731 | 4.070 | Divisão e conquista |
| 256 | 14.124 | 6.806 | Divisão e conquista |

### Etapa 2 (D&C sem limiar)

| n | Força bruta (µs) | D&C sem limiar (limiar = 2) (µs) | mais rápido |
|---:|---:|---:|---|
| 2 | 0.069 | 1.247 | Força bruta |
| 4 | 0.050 | 2.558 | Força bruta |
| 8 | 0.105 | 1.173 | Força bruta |
| 12 | 0.184 | 2.028 | Força bruta |
| 16 | 0.267 | 2.570 | Força bruta |
| 24 | 0.381 | 3.291 | Força bruta |
| 32 | 0.494 | 3.669 | Força bruta |
| 48 | 1.107 | 5.779 | Força bruta |
| 64 | 2.205 | 9.336 | Força bruta |
| 96 | 4.614 | 14.306 | Força bruta |
| 128 | 11.209 | 30.145 | Força bruta |
| 192 | 19.416 | 43.030 | Força bruta |
| 256 | 35.978 | 64.180 | Força bruta |

## Etapa 2: escolha do limiar


### n = 6400

| limiar | tempo (ms) | relativo ao limiar 2 |
|---:|---:|---:|
| 2 | 5.2665 | 1.00 |
| 3 | 4.4237 | 0.84 |
| 4 | 4.3312 | 0.82 |
| 6 | 4.6715 | 0.89 |
| 8 | 4.3078 | 0.82 |
| 12 | 4.1752 | 0.79 |
| 16 | 4.0958 | 0.78 |
| 24 **(melhor)** | 4.0173 | 0.76 |
| 32 | 4.3898 | 0.83 |
| 48 | 4.1998 | 0.80 |
| 64 | 4.2940 | 0.82 |
| 128 | 4.0746 | 0.77 |
| 256 | 4.4445 | 0.84 |

### n = 100000

| limiar | tempo (ms) | relativo ao limiar 2 |
|---:|---:|---:|
| 2 | 116.0 | 1.00 |
| 3 | 110.6 | 0.95 |
| 4 | 113.1 | 0.97 |
| 6 | 98.3 | 0.85 |
| 8 | 104.0 | 0.90 |
| 12 | 101.8 | 0.88 |
| 16 | 105.1 | 0.91 |
| 24 | 102.4 | 0.88 |
| 32 | 97.4 | 0.84 |
| 48 | 94.4 | 0.81 |
| 64 **(melhor)** | 92.9 | 0.80 |
| 128 | 97.9 | 0.84 |
| 256 | 101.6 | 0.88 |
