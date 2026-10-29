package br.com.saudefinanceira.domain

import br.com.saudefinanceira.data.GoalEntity
import java.time.YearMonth

data class GoalProgress(
    val fraction: Float,
    val remainingCents: Long,
    val done: Boolean,
    /** Mês estimado para concluir no ritmo informado (null se concluída ou sem ritmo positivo). */
    val eta: YearMonth?,
)

object GoalCalc {
    const val EMERGENCY_NAME = "Reserva de emergência"

    fun progress(goal: GoalEntity, monthlyPaceCents: Long, now: YearMonth): GoalProgress {
        val remaining = (goal.targetCents - goal.savedCents).coerceAtLeast(0L)
        val done = remaining == 0L
        val fraction = if (goal.targetCents <= 0L) 0f
        else (goal.savedCents.toFloat() / goal.targetCents).coerceIn(0f, 1f)
        val eta: YearMonth? = if (done || monthlyPaceCents <= 0L) null else {
            val months = (remaining + monthlyPaceCents - 1) / monthlyPaceCents
            if (months > 600L) null else now.plusMonths(months)
        }
        return GoalProgress(fraction, remaining, done, eta)
    }
}
