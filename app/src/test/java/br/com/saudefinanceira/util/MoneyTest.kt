package br.com.saudefinanceira.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun parsesBrazilianFormats() {
        assertEquals(350_050L, Money.parse("3500,50"))
        assertEquals(350_050L, Money.parse("3.500,50"))
        assertEquals(123_456L, Money.parse("R$ 1.234,56"))
        assertEquals(50L, Money.parse("0,5"))
        assertEquals(350_000L, Money.parse("3500"))
    }

    @Test
    fun dotAloneIsThousandsWhenItLooksLikeThousands() {
        assertEquals(350_000L, Money.parse("3.500"))
        assertEquals(123_456_700L, Money.parse("1.234.567"))
    }

    @Test
    fun dotIsDecimalOtherwise() {
        assertEquals(350_050L, Money.parse("3500.50"))
        assertEquals(1_250L, Money.parse("12.5"))
        assertEquals(50L, Money.parse("0.500")) // começa com zero: não é milhar
    }

    @Test
    fun rejectsInvalidInput() {
        assertNull(Money.parse(""))
        assertNull(Money.parse("   "))
        assertNull(Money.parse("abc"))
        assertNull(Money.parse("-5"))
        assertNull(Money.parse("1e5"))
        assertNull(Money.parse("1.2.3"))
        assertNull(Money.parse("1,2,3"))
    }

    @Test
    fun rejectsHugeValuesWithoutOverflow() {
        assertNull(Money.parse("999999999999999999999"))
    }

    @Test
    fun formatPlainKeepsTwoDecimals() {
        assertEquals("3500,50", Money.formatPlain(350_050))
        assertEquals("0,05", Money.formatPlain(5))
    }
}
