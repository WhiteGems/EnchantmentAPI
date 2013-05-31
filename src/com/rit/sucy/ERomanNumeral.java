package com.rit.sucy;

class ERomanNumeral {

    static final char[] numerals = new char[] {  'M', 'D', 'C', 'L', 'X', 'V', 'I' };
    static final short[] values = new short[] { 1000, 500, 100,  50,  10,   5,   1 };

    static String numeralOf(int value) {
        String numeralString = "";
        for (int i = 0; i < numerals.length; i++) {
            while (value >= values[i]) {
                value -= values[i];
                numeralString += numerals[i];
            }
            if (i < numerals.length - 1) {
                int index = i - i % 2 + 2;
                if (value >= values[i] - values[index]) {
                    value -= values[i] - values[index];
                    numeralString += numerals[index] + "" + numerals[i];
                }
            }
        }
        return numeralString;
    }

    static int getValueOf(String romanNumeral) {
        char[] numerals = romanNumeral.toCharArray();
        int total = 0;

        for (int i = 0; i < numerals.length; i++) {
            int value = getNumeralValue(numerals[i]);
            if (i < numerals.length - 1) {
                if (getNumeralValue(numerals[i + 1]) > value) value = -value;
            }
            if (value == 0) return 0;
            total += value;
        }
        return total;
    }

    static int getNumeralValue(char numeral) {
        for (int i = 0; i < numerals.length; i++) {
            if (numerals[i] == numeral) return values[i];
        }
        return 0;
    }
}
