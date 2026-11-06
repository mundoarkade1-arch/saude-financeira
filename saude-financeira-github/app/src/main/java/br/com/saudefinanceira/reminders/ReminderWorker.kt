package br.com.saudefinanceira.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.saudefinanceira.FinanceApp
import br.com.saudefinanceira.data.BillEntity
import br.com.saudefinanceira.data.BillType
import br.com.saudefinanceira.domain.DueBill
import br.com.saudefinanceira.domain.ReminderLogic
import br.com.saudefinanceira.util.Money
import java.time.LocalDate

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = ReminderPrefs(ctx)
        if (!prefs.enabled || !Notifier.hasPermission(ctx)) return Result.success()

        val today = LocalDate.now()
        val dao = (ctx as FinanceApp).db.dao()
        val debtBills = dao.getDebts().filter { it.balanceCents > 0L }.mapNotNull { debt ->
            val day = debt.dueDay ?: return@mapNotNull null
            BillEntity(
                id = -debt.id,
                name = "Dívida • ${debt.creditor}",
                type = BillType.FIXED,
                amountCents = debt.plannedPaymentCents.takeIf { it > 0 } ?: debt.balanceCents,
                dueDay = day,
            )
        }
        val bills = dao.getBills() + debtBills
        val due = ReminderLogic.dueSoon(bills, today, prefs.alertDays)
            .filter { prefs.markIfNew("${it.bill.id}|${it.dueDate}|${it.daysUntil}", today) }
        if (due.isEmpty()) return Result.success()

        val title = if (due.size == 1) "Conta perto do vencimento" else "${due.size} contas perto do vencimento"
        Notifier.show(ctx, 2001, title, due.map { describe(it) })
        return Result.success()
    }

    private fun describe(d: DueBill): String {
        val whenText = when (d.daysUntil) {
            0L -> "vence hoje"
            1L -> "vence amanhã"
            else -> "vence em ${d.daysUntil} dias (dia ${d.dueDate.dayOfMonth})"
        }
        return "${d.bill.name} $whenText · ${Money.format(d.bill.amountCents)}"
    }
}

