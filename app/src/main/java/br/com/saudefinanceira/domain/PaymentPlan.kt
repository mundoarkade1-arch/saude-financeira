package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.BillEntity

object Priority {
    const val ESSENTIAL = 0
    const val IMPORTANT = 1
    const val FLEXIBLE = 2

    fun label(priority: Int): String = when (priority) {
        ESSENTIAL -> "Pagar primeiro"
        FLEXIBLE -> "Pode atrasar um pouco"
        else -> "Pagar depois"
    }

    private val essentialWords = listOf(
        "aluguel", "condom", "luz", "energia", "agua", "água", "gás", "gas", "mercado", "comida", "feira",
        "remédio", "remedio", "farmácia", "farmacia", "transporte", "passagem", "ônibus", "onibus", "escola", "creche",
    )
    private val flexibleWords = listOf("netflix", "spotify", "streaming", "assinatura", "youtube", "amazon", "disney", "hbo")

    /** Sugestão pelo nome da conta; a pessoa sempre pode trocar. */
    fun suggest(name: String): Int {
        val n = name.lowercase()
        return when {
            essentialWords.any { n.contains(it) } -> ESSENTIAL
            flexibleWords.any { n.contains(it) } -> FLEXIBLE
            else -> IMPORTANT
        }
    }
}

data class PaymentStep(val bill: BillEntity, val covered: Boolean, val missingCents: Long)

data class PaymentPlanResult(val steps: List<PaymentStep>, val leftoverCents: Long) {
    val coveredCount: Int get() = steps.count { it.covered }
    val uncoveredCount: Int get() = steps.size - coveredCount
}

object PaymentPlan {
    /** Essenciais primeiro; dentro de cada grupo, quem vence antes. */
    fun order(bills: List<BillEntity>): List<BillEntity> =
        bills.sortedWith(
            compareBy<BillEntity>({ it.priority }, { it.dueDay ?: 99 }, { it.name.lowercase() })
        )

    /**
     * Paga na ordem de prioridade até o dinheiro acabar. Na primeira conta que não cabe,
     * as seguintes também ficam sem cobertura (a ordem de prioridade é respeitada).
     */
    fun build(bills: List<BillEntity>, availableCents: Long): PaymentPlanResult {
        var left = availableCents.coerceAtLeast(0L)
        var broke = false
        val steps = order(bills).map { bill ->
            if (!broke && left >= bill.amountCents) {
                left -= bill.amountCents
                PaymentStep(bill, true, 0L)
            } else {
                val missing = bill.amountCents - (if (broke) 0L else left)
                broke = true
                PaymentStep(bill, false, missing)
            }
        }
        return PaymentPlanResult(steps, if (broke) 0L else left)
    }
}

data class SplitPart(val label: String, val cents: Long)

data class SalarySplit(val parts: List<SplitPart>, val freeCents: Long)

object SalarySplitCalc {
    /** Divide a renda em dinheiro entre contas por prioridade, parcelas, acordos, reserva e o que sobra. */
    fun compute(bills: List<BillEntity>, snapshot: Snapshot, reserveCents: Long): SalarySplit {
        fun sum(priority: Int) = bills.filter { it.priority == priority }.sumOf { it.amountCents }
        val reserve = reserveCents.coerceAtLeast(0L)
        val parts = listOf(
            SplitPart("Pagar primeiro", sum(Priority.ESSENTIAL)),
            SplitPart("Pagar depois", sum(Priority.IMPORTANT)),
            SplitPart("Pode atrasar um pouco", sum(Priority.FLEXIBLE)),
            SplitPart("Parcelas", snapshot.installmentsCents),
            SplitPart("Acordos de dívida", snapshot.debtPaymentsCents),
            SplitPart("Guardar um pouquinho", reserve),
        ).filter { it.cents > 0L }
        return SalarySplit(parts, snapshot.incomeCents - parts.sumOf { it.cents })
    }
}
