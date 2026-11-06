package br.com.saudefinanceira

import br.com.saudefinanceira.update.Updater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdaterTest {
    @Test
    fun parsesVersionCodeFromTag() {
        assertEquals(12L, Updater.parseVersionCode("v0.1.12"))
        assertEquals(7L, Updater.parseVersionCode("0.1.7"))
        assertNull(Updater.parseVersionCode("latest"))
    }

    @Test
    fun findsHashByFileName() {
        val sums = "ABC123  SaudeFinanceira-v0.1.5.apk\nfff000  outro.apk\n"
        assertEquals("abc123", Updater.findHash(sums, "SaudeFinanceira-v0.1.5.apk"))
        assertNull(Updater.findHash(sums, "inexistente.apk"))
    }

    @Test
    fun acceptsOnlySafeApkNames() {
        assertTrue(Updater.isSafeApkName("SaudeFinanceira-v0.1.5.apk"))
        assertFalse(Updater.isSafeApkName("../x.apk"))
        assertFalse(Updater.isSafeApkName("a/b.apk"))
        assertFalse(Updater.isSafeApkName("arquivo.txt"))
    }
}
