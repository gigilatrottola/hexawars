package com.onebitheroes;

public class num extends javax.swing.JTextField {
    
    /** Az editBox tartalm�ban el�fordulhat� karakterek t�mbje */
    private static final char[] betuk =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    /** Az editBox tartalm�nak maxim�lis hossza */
    private int fieldLength = 255, maxVal=Integer.MAX_VALUE;
    
    public num() {
        super();
    }
    /** Kontruktor
     *  @param text Az editBox kezdeti tartalma
     *  @param length Az editBox tartalm�nak maxim�lis hossza
     */    
    public num(String text, int length) {
        super(text);
        fieldLength = length;
    }
    public void setMax(int max) {
        maxVal=max;
    }
    
    /** Be�ll�tja az editBox tartalm�nak maxim�lis hossz�t */
    public void setLength(int length) {
        fieldLength = length;
    }
    
    /** L�trehoz egy az editBox tartalm�t reprezent�l� oszt�ly */
    protected javax.swing.text.Document createDefaultModel() {
        return new NumericDocument();
    }
    
    /** Az editBox tartalm�t reprezent�l� priv�t oszt�ly */
    private class NumericDocument extends javax.swing.text.PlainDocument {
        
        /** Visszaadja, hogy a megadott karakter leg�lisan besz�rhat�-e
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
        
        /** Beilleszti a sz�veget a sz�vegdobozba ha az leg�lisan megtehet� */
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
