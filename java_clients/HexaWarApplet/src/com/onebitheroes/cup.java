package com.onebitheroes;

public class cup extends javax.swing.JTextField {
    
    /** Az editBox tartalmában elõfordulható karakterek tömbje */
    private static final char[] betuk =
    {'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E',
     'f', 'F', 'g', 'G', 'h', 'H', 'i', 'I', 'j', 'J',
     'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O',
     'p', 'P', 'q', 'Q', 'r', 'R', 's', 'S', 't', 'T', 'u', 'U',
     'v', 'V', 'w', 'W', 'x', 'X', 'y', 'Y',
     'z', 'Z', '.', '_', '@', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    /** Az editBox tartalmának maximális hossza */
    private int fieldLength = 255;
    
    public cup() {
        super();
    }
    /** Kontruktor
     *  @param text Az editBox kezdeti tartalma
     *  @param length Az editBox tartalmának maximális hossza
     */    
    public cup(String text, int length) {
        super(text);
        fieldLength = length;
    }
    
    /** Beállítja az editBox tartalmának maximális hosszát */
    public void setLength(int length) {
        fieldLength = length;
    }
    
    /** Létrehoz egy az editBox tartalmát reprezentáló osztály */
    protected javax.swing.text.Document createDefaultModel() {
        return new NumericDocument();
    }
    
    /** Az editBox tartalmát reprezentáló privát osztály */
    private class NumericDocument extends javax.swing.text.PlainDocument {
        
        /** Visszaadja, hogy a megadott karakter legálisan beszúrható-e
         *  az editBox-ba
         */
        public boolean validateKeystroke(int key) {
            boolean found = false;
            int ofs = 0;
            
            if (key == 8 || key == 10 || key == 127) return true;
            while (ofs < betuk.length && !found) {
                if (key == betuk[ofs])
                    found = true;
                ofs++;
            }
            
            try {
                if (found) {
                    if (this.getText(0, getLength()).length() < fieldLength) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        
        /** Beilleszti a szöveget a szövegdobozba ha az legálisan megtehetõ */
        public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
        throws javax.swing.text.BadLocationException {
            if (str == null) return;
            str=str.toUpperCase();
            String newstr = new String("");
            for (int i = 0; i < str.length(); i++) {
                if (validateKeystroke(str.charAt(i))) {
                    newstr += str.substring(i, i + 1);
                }
            }
            super.insertString(offs, newstr, a);
        }
    }    
}
