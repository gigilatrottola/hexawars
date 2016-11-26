package com.onebitheroes;

public class num extends javax.swing.JTextField {
    
    /** Az editBox tartalmában elõfordulható karakterek tömbje */
    private static final char[] betuk =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    /** Az editBox tartalmának maximális hossza */
    private int fieldLength = 255, maxVal=Integer.MAX_VALUE;
    
    public num() {
        super();
    }
    /** Kontruktor
     *  @param text Az editBox kezdeti tartalma
     *  @param length Az editBox tartalmának maximális hossza
     */    
    public num(String text, int length) {
        super(text);
        fieldLength = length;
    }
    public void setMax(int max) {
        maxVal=max;
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
                if (key == betuk[ofs]) {
                    found = true;
                } else {
                    ofs++;
                }
            }
            
            try {
                if (found) {
                    if (this.getText(0, getLength()).length() < fieldLength ) {
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
