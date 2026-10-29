package br.com.saudefinanceira.data

import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

/** Exporta/importa todos os dados em JSON. A importação valida tudo ANTES de alterar o banco. */
object Backup {
    private const val APP_ID = "saude-financeira"
    private const val FORMAT = 1
    private const val MAX_ITEMS = 20_000
    private const val MAX_CENTS = 10_000_000_000_000L

    data class Data(
        val income: List<IncomeEntity>,
        val bills: List<BillEntity>,
        val installments: List<InstallmentEntity>,
        val entries: List<BillEntryEntity>,
    )

    suspend fun snapshot(db: AppDatabase): Data {
        val d = db.dao()
        return Data(d.getIncome(), d.getBills(), d.getInstallments(), d.getEntries())
    }

    suspend fun replaceAll(db: AppDatabase, data: Data) {
        db.withTransaction {
            val d = db.dao()
            d.clearEntries()
            d.clearBills()
            d.clearInstallments()
            d.clearIncome()
            d.insertIncomes(data.income)
            d.insertBills(data.bills)
            d.insertInstallments(data.installments)
            d.insertEntries(data.entries)
        }
    }

    fun toJson(data: Data): String {
        val root = JSONObject()
        root.put("app", APP_ID)
        root.put("format", FORMAT)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("income", JSONArray().also { arr ->
            data.income.forEach {
                arr.put(JSONObject().put("id", it.id).put("description", it.description).put("amountCents", it.amountCents))
            }
        })
        root.put("bills", JSONArray().also { arr ->
            data.bills.forEach {
                arr.put(
                    JSONObject()
                        .put("id", it.id).put("name", it.name).put("type", it.type.name)
                        .put("amountCents", it.amountCents).put("dueDay", it.dueDay ?: JSONObject.NULL)
                )
            }
        })
        root.put("installments", JSONArray().also { arr ->
            data.installments.forEach {
                arr.put(
                    JSONObject()
                        .put("id", it.id).put("description", it.description).put("totalCents", it.totalCents)
                        .put("installments", it.installments).put("firstYear", it.firstYear)
                        .put("firstMonth", it.firstMonth).put("card", it.card ?: JSONObject.NULL)
                )
            }
        })
        root.put("entries", JSONArray().also { arr ->
            data.entries.forEach {
                arr.put(
                    JSONObject()
                        .put("id", it.id).put("billId", it.billId).put("year", it.year)
                        .put("month", it.month).put("amountCents", it.amountCents)
                )
            }
        })
        return root.toString(2)
    }

    private fun JSONArray.objects(): List<JSONObject> {
        require(length() <= MAX_ITEMS) { "Backup grande demais." }
        return (0 until length()).map { getJSONObject(it) }
    }

    private fun checkCents(v: Long) = require(v in 0..MAX_CENTS) { "Valor fora do limite." }
    private fun checkText(v: String) = require(v.length <= 200) { "Texto longo demais." }

    /** Lança exceção (JSONException ou IllegalArgumentException) se o arquivo for inválido. */
    fun parse(text: String): Data {
        val root = JSONObject(text)
        require(root.getString("app") == APP_ID) { "Não é um backup do Saúde Financeira." }
        require(root.getInt("format") == FORMAT) { "Versão de backup não suportada." }

        val income = root.getJSONArray("income").objects().map {
            IncomeEntity(it.getLong("id"), it.getString("description").also(::checkText), it.getLong("amountCents").also(::checkCents))
        }
        val bills = root.getJSONArray("bills").objects().map {
            val day = if (it.isNull("dueDay")) null else it.getInt("dueDay")
            require(day == null || day in 1..31) { "Dia inválido." }
            BillEntity(
                id = it.getLong("id"),
                name = it.getString("name").also(::checkText),
                type = BillType.valueOf(it.getString("type")),
                amountCents = it.getLong("amountCents").also(::checkCents),
                dueDay = day,
            )
        }
        val installments = root.getJSONArray("installments").objects().map {
            val n = it.getInt("installments")
            val month = it.getInt("firstMonth")
            val year = it.getInt("firstYear")
            require(n in 1..120 && month in 1..12 && year in 1990..2200) { "Parcela inválida." }
            InstallmentEntity(
                id = it.getLong("id"),
                description = it.getString("description").also(::checkText),
                totalCents = it.getLong("totalCents").also(::checkCents),
                installments = n,
                firstYear = year,
                firstMonth = month,
                card = if (it.isNull("card")) null else it.getString("card").also(::checkText),
            )
        }
        val entries = root.getJSONArray("entries").objects().map {
            val month = it.getInt("month")
            val year = it.getInt("year")
            require(month in 1..12 && year in 1990..2200) { "Histórico inválido." }
            BillEntryEntity(
                id = it.getLong("id"),
                billId = it.getLong("billId"),
                year = year,
                month = month,
                amountCents = it.getLong("amountCents").also(::checkCents),
            )
        }

        require(income.map { it.id }.toSet().size == income.size) { "IDs repetidos." }
        require(bills.map { it.id }.toSet().size == bills.size) { "IDs repetidos." }
        require(installments.map { it.id }.toSet().size == installments.size) { "IDs repetidos." }
        require(entries.map { it.id }.toSet().size == entries.size) { "IDs repetidos." }
        val billIds = bills.map { it.id }.toSet()
        require(entries.all { it.billId in billIds }) { "Histórico sem conta correspondente." }
        require(entries.map { Triple(it.billId, it.year, it.month) }.toSet().size == entries.size) { "Histórico repetido." }
        return Data(income, bills, installments, entries)
    }
}
