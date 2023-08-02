package net.queencoder.springboot;

public class Base62 {
	private static final char[] digitsChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE = digitsChar.length;
    private static final int FAST_SIZE = 'z';
    private static final int[] digitsIndex = new int[FAST_SIZE + 1];


    static {
        for (int i = 0; i < FAST_SIZE; i++) {
            digitsIndex[i] = -1;
        }
        for (int i = 0; i < BASE; i++) {
            digitsIndex[digitsChar[i]] = i;
        }
    }

    public static long decode(String s) {
        long result = 0L;
        long multiplier = 1;
        for (int pos = s.length() - 1; pos >= 0; pos--) {
            int index = getIndex(s, pos);
            result += index * multiplier;
            multiplier *= BASE;
        }
        return result;
    }
    
    public static long decodeWithPadding(String s) {
        // Remove any leading '0' characters used for padding
        String trimmed = s.replaceFirst("^0+", "");
        if (trimmed.isEmpty()) {
            return 0L;
        }
        long result = 0L;
        long multiplier = 1;
        for (int pos = trimmed.length() - 1; pos >= 0; pos--) {
            int index = getIndex(trimmed, pos);
            result += index * multiplier;
            multiplier *= BASE;
        }
        return result;
    }


    public static String encode(long number) {
        if (number < 0) throw new IllegalArgumentException("Number(Base62) must be positive: " + number);
        if (number == 0) return "0";
        StringBuilder buf = new StringBuilder();
        while (number != 0) {
            buf.append(digitsChar[(int) (number % BASE)]);
            number /= BASE;
        }
        return buf.reverse().toString();
    }
    
    public static String encodeWithPadding(long number, int paddingLength) {
        if (number < 0) throw new IllegalArgumentException("Number(Base62) must be positive: " + number);
        if (number == 0) return "0";
        StringBuilder buf = new StringBuilder();
        while (number != 0) {
            buf.append(digitsChar[(int) (number % BASE)]);
            number /= BASE;
        }
        // Add padding to the encoded result if needed
        int paddingCount = paddingLength - buf.length();
        for (int i = 0; i < paddingCount; i++) {
            buf.append(digitsChar[0]); // Adding '0' character as padding
        }
        return buf.reverse().toString();
    }


    private static int getIndex(String s, int pos) {
        char c = s.charAt(pos);
        if (c > FAST_SIZE) {
            throw new IllegalArgumentException("Unknow character for Base62: " + s);
        }
        int index = digitsIndex[c];
        if (index == -1) {
            throw new IllegalArgumentException("Unknow character for Base62: " + s);
        }
        return index;
    }
    
    private static char[] customDigitsChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static void setCustomCharacterSet(char[] customCharacterSet) {
        if (customCharacterSet == null || customCharacterSet.length != BASE) {
            throw new IllegalArgumentException("Custom character set must be an array of length " + BASE);
        }
        System.arraycopy(customCharacterSet, 0, digitsChar, 0, BASE);
    }

}
