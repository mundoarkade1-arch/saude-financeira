package br.com.saudefinanceira.update

import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class UpdateInfo(
    val tag: String,
    val versionCode: Long,
    val apkName: String,
    val apkUrl: String,
    val sumsUrl: String,
    val sizeBytes: Long,
)

class IntegrityException(message: String) : IOException(message)

/** Consulta a última Release do GitHub, baixa o APK e confere o SHA-256. */
object Updater {
    private const val OWNER_REPO = "mundoarkade1-arch/saude-financeira"
    private const val API_URL = "https://api.github.com/repos/$OWNER_REPO/releases/latest"
    private const val DOWNLOAD_PREFIX = "https://github.com/$OWNER_REPO/releases/download/"
    private val SAFE_APK_NAME = Regex("^[A-Za-z0-9._-]+\\.apk$")

    /** "v0.1.12" -> 12 (o último número da tag é o versionCode). */
    fun parseVersionCode(tag: String): Long? = tag.trim().substringAfterLast('.').toLongOrNull()

    fun isSafeApkName(name: String): Boolean = SAFE_APK_NAME.matches(name)

    /** Procura o hash do arquivo em um SHA256SUMS.txt ("<hash>  <arquivo>"). */
    fun findHash(sums: String, fileName: String): String? =
        sums.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split(Regex("\\s+"), limit = 2)
                if (parts.size == 2 && parts[1].trimStart('*') == fileName) parts[0].lowercase() else null
            }
            .firstOrNull()

    fun fetchLatest(): UpdateInfo {
        val json = JSONObject(getText(API_URL, json = true))
        val tag = json.getString("tag_name")
        val code = parseVersionCode(tag) ?: throw IOException("Tag inválida: $tag")
        val assets = json.getJSONArray("assets")

        var apkName: String? = null
        var apkUrl: String? = null
        var size = 0L
        var sumsUrl: String? = null
        for (i in 0 until assets.length()) {
            val a = assets.getJSONObject(i)
            val name = a.getString("name")
            val url = a.getString("browser_download_url")
            if (name.endsWith(".apk")) {
                apkName = name; apkUrl = url; size = a.optLong("size")
            } else if (name == "SHA256SUMS.txt") {
                sumsUrl = url
            }
        }
        if (apkName == null || apkUrl == null || sumsUrl == null) {
            throw IOException("A Release não contém o APK e o SHA256SUMS.txt.")
        }
        if (!isSafeApkName(apkName)) throw IOException("Nome de arquivo inválido.")
        if (!apkUrl.startsWith(DOWNLOAD_PREFIX) || !sumsUrl.startsWith(DOWNLOAD_PREFIX)) {
            throw IOException("Origem do download não confiável.")
        }
        return UpdateInfo(tag, code, apkName, apkUrl, sumsUrl, size)
    }

    /** Baixa o APK para [dir], confere o SHA-256 e devolve o arquivo. Apaga o arquivo se a conferência falhar. */
    fun downloadVerified(dir: File, info: UpdateInfo, onProgress: (Float) -> Unit): File {
        dir.deleteRecursively()
        dir.mkdirs()
        val expected = findHash(getText(info.sumsUrl, json = false), info.apkName)
            ?: throw IntegrityException("Hash do APK não encontrado no SHA256SUMS.txt.")
        val file = File(dir, info.apkName)
        download(info.apkUrl, file, onProgress)
        val actual = sha256(file)
        if (!actual.equals(expected, ignoreCase = true)) {
            file.delete()
            throw IntegrityException("O arquivo baixado não confere com o hash publicado.")
        }
        return file
    }

    private fun open(url: String, json: Boolean): HttpURLConnection {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15_000
        c.readTimeout = 30_000
        c.setRequestProperty("User-Agent", "SaudeFinanceira-Updater")
        if (json) c.setRequestProperty("Accept", "application/vnd.github+json")
        return c
    }

    private fun getText(url: String, json: Boolean): String {
        val c = open(url, json)
        try {
            if (c.responseCode != 200) throw IOException("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    private fun download(url: String, dest: File, onProgress: (Float) -> Unit) {
        val c = open(url, json = false)
        try {
            if (c.responseCode != 200) throw IOException("HTTP ${c.responseCode}")
            val total = c.contentLengthLong
            var done = 0L
            var lastPct = -1
            c.inputStream.use { input ->
                dest.outputStream().use { out ->
                    val buf = ByteArray(32 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        if (total > 0) {
                            val pct = (done * 100 / total).toInt()
                            if (pct != lastPct) { lastPct = pct; onProgress(done.toFloat() / total) }
                        }
                    }
                }
            }
            if (dest.length() == 0L) throw IOException("Arquivo vazio.")
        } finally {
            c.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(32 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
