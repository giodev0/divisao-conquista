"""
Gera tabelas e gráficos a partir dos CSVs brutos do Benchmark.

    python scripts/analise.py [pasta_resultados]

Requer: numpy, matplotlib  (pip install numpy matplotlib)
Saída: resultados/graficos/*.png e resultados/tabelas.md
"""
import csv
import math
import sys
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

PASTA = Path(sys.argv[1] if len(sys.argv) > 1 else "resultados")
GRAF = PASTA / "graficos"
GRAF.mkdir(parents=True, exist_ok=True)

NOMES = {
    "forca_bruta": "Força bruta",
    "divisao_conquista": "Divisão e conquista",
    "divisao_conquista_pura": "D&C sem limiar (limiar = 2)",
    "kadane": "Kadane (desafio)",
}
CORES = {"forca_bruta": "#c0392b", "divisao_conquista": "#2471a3",
         "divisao_conquista_pura": "#2471a3", "kadane": "#1e8449"}
COMPLEX = {  # função g(n) da complexidade prevista, para normalizar
    ("subarranjo", "forca_bruta"): ("n²", lambda n: n * n),
    ("subarranjo", "divisao_conquista"): ("n log₂ n", lambda n: n * math.log2(n)),
    ("subarranjo", "kadane"): ("n", lambda n: n),
    ("par", "forca_bruta"): ("n²", lambda n: n * n),
    ("par", "divisao_conquista"): ("n log₂ n", lambda n: n * math.log2(n)),
}

plt.rcParams.update({"figure.dpi": 150, "font.size": 9, "axes.grid": True, "grid.alpha": 0.3})


def ler(arq, chave):
    dados = defaultdict(list)
    with open(arq, newline="") as f:
        for linha in csv.DictReader(f):
            dados[chave(linha)].append(int(linha["tempo_ns"]))
    return dados


def resumo(lista):
    a = np.array(lista, dtype=float)
    return a.mean(), a.std(ddof=1) if len(a) > 1 else 0.0, len(a)


def fmt_ms(ns):
    return f"{ns / 1e6:.4f}" if ns < 1e7 else f"{ns / 1e6:.1f}"


md = ["# Resultados das medições\n",
      "Tempos em milissegundos: média de N execuções ± desvio-padrão. "
      "Dados brutos em `medicoes_*.csv`.\n"]

# ------------------------------------------------------------------ escala
esc = ler(PASTA / "medicoes_escala.csv", lambda l: (l["problema"], l["algoritmo"], int(l["n"])))
for prob, titulo, algs in [
    ("subarranjo", "Etapa 1 — Subarranjo de soma máxima", ["forca_bruta", "divisao_conquista", "kadane"]),
    ("par", "Etapa 2 — Par de pontos mais próximos", ["forca_bruta", "divisao_conquista"]),
]:
    ns = sorted({k[2] for k in esc if k[0] == prob})
    md.append(f"\n## {titulo}\n")
    md.append("| n | " + " | ".join(f"{NOMES[a]} (ms)" for a in algs) + " | razão FB / D&C |")
    md.append("|---:|" + "---:|" * len(algs) + "---:|")
    medias = {a: [] for a in algs}
    for n in ns:
        cel = []
        for a in algs:
            m, s, k = resumo(esc[(prob, a, n)])
            medias[a].append(m)
            cel.append(f"{fmt_ms(m)} ± {fmt_ms(s)}")
        razao = medias["forca_bruta"][-1] / medias["divisao_conquista"][-1]
        md.append(f"| {n} | " + " | ".join(cel) + f" | {razao:.1f}× |")
    md.append(f"\nExecuções por tamanho: {k}.\n")

    # inclinação log-log (expoente empírico) nos 5 maiores n
    md.append("Expoente empírico (inclinação de log t × log n nos 5 maiores n):\n")
    for a in algs:
        x, y = np.log(ns[-5:]), np.log(medias[a][-5:])
        b = np.polyfit(x, y, 1)[0]
        md.append(f"- {NOMES[a]}: {b:.2f} (previsto: {COMPLEX[(prob, a)][0]})")
    md.append("")

    # gráfico 1: tempo x n (log-log)
    fig, ax = plt.subplots(figsize=(6.4, 4.2))
    for a in algs:
        ax.plot(ns, [m / 1e6 for m in medias[a]], "o-", color=CORES[a], label=NOMES[a], ms=4)
    ax.set_xscale("log", base=2); ax.set_yscale("log")
    ax.set_xlabel("n (tamanho da entrada)"); ax.set_ylabel("tempo médio (ms, escala log)")
    ax.set_title(titulo); ax.legend()
    fig.tight_layout(); fig.savefig(GRAF / f"{prob}_tempo.png"); plt.close(fig)

    # gráfico 2: tempo / g(n) — fica ~constante se a complexidade prevista vale
    fig, axs = plt.subplots(1, len(algs), figsize=(3.2 * len(algs), 3.0))
    for ax, a in zip(axs, algs):
        nome_g, g = COMPLEX[(prob, a)]
        ax.plot(ns, [m / g(n) for m, n in zip(medias[a], ns)], "o-", color=CORES[a], ms=4)
        ax.set_xscale("log", base=2); ax.set_ylim(bottom=0)
        ax.set_title(f"{NOMES[a]}\ntempo / {nome_g}", fontsize=8.5)
        ax.set_xlabel("n"); ax.set_ylabel("ns por unidade")
    fig.suptitle(f"{titulo}: tempo normalizado pela complexidade prevista", fontsize=9)
    fig.tight_layout(); fig.savefig(GRAF / f"{prob}_normalizado.png"); plt.close(fig)

# ------------------------------------------------------------------ cruzamento
cz = ler(PASTA / "medicoes_cruzamento.csv", lambda l: (l["problema"], l["algoritmo"], int(l["n"])))
fig, axs = plt.subplots(1, 2, figsize=(9, 3.4))
md.append("\n## Entradas pequenas: força bruta × divisão e conquista\n")
for ax, (prob, algs, titulo) in zip(axs, [
    ("subarranjo", ["forca_bruta", "divisao_conquista"], "Etapa 1"),
    ("par", ["forca_bruta", "divisao_conquista_pura"], "Etapa 2 (D&C sem limiar)"),
]):
    ns = sorted({k[2] for k in cz if k[0] == prob})
    md.append(f"\n### {titulo}\n")
    md.append("| n | " + " | ".join(f"{NOMES[a]} (µs)" for a in algs) + " | mais rápido |")
    md.append("|---:|---:|---:|---|")
    for a in algs:
        ax.plot(ns, [resumo(cz[(prob, a, n)])[0] / 1e3 for n in ns], "o-", color=CORES[a], label=NOMES[a], ms=4)
    for n in ns:
        ms = [resumo(cz[(prob, a, n)])[0] for a in algs]
        md.append(f"| {n} | {ms[0] / 1e3:.3f} | {ms[1] / 1e3:.3f} | {NOMES[algs[int(np.argmin(ms))]]} |")
    ax.set_xscale("log", base=2); ax.set_yscale("log")
    ax.set_xlabel("n"); ax.set_ylabel("tempo médio (µs)"); ax.set_title(titulo); ax.legend(fontsize=7.5)
fig.tight_layout(); fig.savefig(GRAF / "cruzamento.png"); plt.close(fig)

# ------------------------------------------------------------------ limiar
lm = ler(PASTA / "medicoes_limiar.csv", lambda l: (int(l["n"]), int(l["limiar"])))
fig, ax = plt.subplots(figsize=(6.4, 3.6))
md.append("\n## Etapa 2: escolha do limiar\n")
for n in sorted({k[0] for k in lm}):
    lims = sorted({k[1] for k in lm if k[0] == n})
    med = [resumo(lm[(n, l)])[0] for l in lims]
    base = med[0]
    ax.plot(lims, [m / base for m in med], "o-", ms=4, label=f"n = {n}")
    melhor = lims[int(np.argmin(med))]
    md.append(f"\n### n = {n}\n")
    md.append("| limiar | tempo (ms) | relativo ao limiar 2 |")
    md.append("|---:|---:|---:|")
    for l, m in zip(lims, med):
        md.append(f"| {l}{' **(melhor)**' if l == melhor else ''} | {fmt_ms(m)} | {m / base:.2f} |")
ax.set_xscale("log", base=2)
ax.set_xlabel("limiar (subproblemas com ≤ limiar pontos vão para a força bruta)")
ax.set_ylabel("tempo relativo ao limiar 2")
ax.set_title("Etapa 2 — efeito do limiar na divisão e conquista"); ax.legend()
fig.tight_layout(); fig.savefig(GRAF / "limiar.png"); plt.close(fig)

(PASTA / "tabelas.md").write_text("\n".join(md) + "\n", encoding="utf-8")
print("Gráficos em", GRAF, "| tabelas em", PASTA / "tabelas.md")
