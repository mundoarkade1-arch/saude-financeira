package br.com.saudefinanceira.domain

import br.com.saudefinanceira.util.Money

enum class TipLevel { INFO, GOOD, WARNING, ALERT }

data class Tip(val title: String, val message: String, val level: TipLevel)

data class Snapshot(
    val incomeCents: Long,
    val fixedCents: Long,
    val variableCents: Long,
    val installmentsCents: Long,
) {
    val spentCents: Long get() = fixedCents + variableCents + installmentsCents
    val balanceCents: Long get() = incomeCents - spentCents
}

/** Regras locais: nenhum dado sai do aparelho. */
object TipEngine {
    fun generate(s: Snapshot, risingBills: List<Pair<String, Int>> = emptyList()): List<Tip> {
        if (s.incomeCents <= 0) {
            return listOf(
                Tip("Comece pelo salário", "Informe sua renda mensal na tela Início para receber dicas personalizadas.", TipLevel.INFO)
            )
        }
        val tips = mutableListOf<Tip>()
        val income = s.incomeCents.toDouble()

        if (s.balanceCents < 0) {
            tips += Tip(
                "Gastos acima da renda",
                "Seus gastos previstos superam a renda em ${Money.format(-s.balanceCents)}. Priorize cortar despesas variáveis e evite novas parcelas até equilibrar.",
                TipLevel.ALERT,
            )
        }

        val committed = (s.fixedCents + s.installmentsCents) / income
        when {
            committed > 0.5 -> tips += Tip(
                "Compromissos muito altos",
                "Contas fixas e parcelas consomem ${(committed * 100).toInt()}% da renda. Acima de 50% fica difícil absorver imprevistos.",
                TipLevel.ALERT,
            )
            committed > 0.35 -> tips += Tip(
                "Atenção aos compromissos",
                "Contas fixas e parcelas somam ${(committed * 100).toInt()}% da renda. O ideal é ficar abaixo de 35%.",
                TipLevel.WARNING,
            )
        }

        val saveRate = s.balanceCents / income
        if (saveRate >= 0.2) {
            tips += Tip("Ótima sobra", "Você prevê guardar ${(saveRate * 100).toInt()}% da renda. Direcione para a reserva de emergência.", TipLevel.GOOD)
        } else if (s.balanceCents >= 0) {
            tips += Tip("Aumente a sobra", "Sua sobra prevista é ${(saveRate * 100).toInt()}% da renda. Tente chegar a 20%.", TipLevel.INFO)
        }

        risingBills.firstOrNull()?.let { (name, pct) ->
            tips += Tip(
                "Conta em alta",
                "$name está $pct% acima da sua média recente. Vale revisar o consumo.",
                TipLevel.WARNING,
            )
        }

        tips += Tip(
            "Regra 50/30/20 para sua renda",
            "Necessidades: ${Money.format((income * 0.5).toLong())} · Desejos: ${Money.format((income * 0.3).toLong())} · Poupança: ${Money.format((income * 0.2).toLong())}.",
            TipLevel.INFO,
        )

        val base = s.fixedCents + s.variableCents
        if (base > 0) {
            tips += Tip(
                "Reserva de emergência",
                "Meta de 3 a 6 meses de gastos essenciais: ${Money.format(base * 3)} a ${Money.format(base * 6)}.",
                TipLevel.INFO,
            )
        }
        return tips
    }
}
