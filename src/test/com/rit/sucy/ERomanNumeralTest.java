package com.rit.sucy;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Diemex
 */
public class ERomanNumeralTest {

    /**
     * Test if we get correct values when inputting valid input
     */
    @Test
    public void valid_getNumeralValueTest()
    {
        assertEquals(100, ERomanNumeral.getNumeralValue('C'));
        assertEquals(500, ERomanNumeral.getNumeralValue('D'));
    }

    /**
     * Invalid characters will return 0
     */
    @Test
    public void invalid_getNumeralValueTest()
    {
        assertEquals(ERomanNumeral.getNumeralValue('E'), 0);
    }

    /**
     * Parse roman number string into decimal
     */
    @Test
    public void valid_getValueOfTest()
    {
        //Normal Numbers with simple Addition
        assertEquals(161, ERomanNumeral.getValueOf("CLXI"));
        assertEquals(1631, ERomanNumeral.getValueOf("MDCXXXI"));

        //Numbers with subtraction
        assertEquals(4, ERomanNumeral.getValueOf("IV"));
        assertEquals(99, ERomanNumeral.getValueOf("IC"));
        assertEquals(54, ERomanNumeral.getValueOf("LIV"));
    }

    /**
     * Get the roman representation of an integer
     */
    @Test
    public void valid_numeralOfTest()
    {
        assertEquals("CI", ERomanNumeral.numeralOf(101));
        assertEquals("CLI",ERomanNumeral.numeralOf(151));
        assertEquals("IX", ERomanNumeral.numeralOf(9));
        assertEquals("IV", ERomanNumeral.numeralOf(4));
        assertEquals("VI", ERomanNumeral.numeralOf(6));
    }
}
