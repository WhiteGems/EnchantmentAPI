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
     * What happens if we input a literal which isn't a valid number
     */
    @Test (expected = IllegalArgumentException.class)
    public void invalid_getNumeralValueTest()
    {
        ERomanNumeral.getNumeralValue('E');
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
        //This doesn't work:
        //assertEquals(33, ERomanNumeral.getValueOf("IIVXL"));
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

    /**
     * Roman numbers don't support negative values
     */
    @Test(expected = IllegalArgumentException.class)
    public void negative_numeralOfTest()
    {
        ERomanNumeral.numeralOf(-1);
    }

    /**
     * Roman numbers can't represent zero
     */
    @Test(expected = IllegalArgumentException.class)
    public void zero_numeralOfTest()
    {
        ERomanNumeral.numeralOf(0);
    }
}
