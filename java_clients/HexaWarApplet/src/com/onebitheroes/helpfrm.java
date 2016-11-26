package com.onebitheroes;

import java.awt.event.ActionEvent;

public class helpfrm extends javax.swing.JFrame {
    public helpfrm(HexaWarApplet parent) {
        super();
        java.awt.Dimension ss=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int px=(int)ss.getWidth()/2-250;
        int py=(int)ss.getHeight()/2-200;
        if (px<0) {
            px=0;
        }
        if (py<0) {
            py=0;
        }
        setBounds(px, py, 500, 400);
        
        ok=new javax.swing.JButton(parent.lng.CLOSE_HELP);
        ok.setName("okbtn");
        ok.setFocusPainted(false);
        ok.setFont(parent.fontpsmall);
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        
        tarea=new javax.swing.JTextArea();
        tarea.setName("tarea");
        tarea.setText(parent.lng.rulesTxt+"\n\n"+parent.lng.howtoplayTxt);
        tarea.setLineWrap(true);
        tarea.setWrapStyleWord(true);
        tarea.setCaretPosition(0);
        tarea.setEditable(false);
        scrpane=new javax.swing.JScrollPane();
        scrpane.setName("scrpane");
        scrpane.getViewport().add(tarea);
        
        getContentPane().setLayout(new layout());
        getContentPane().add(ok);
        getContentPane().add(scrpane);
    }    
    
    private class layout implements java.awt.LayoutManager {
        public void removeLayoutComponent(final java.awt.Component comp) {}
        
        public java.awt.Dimension preferredLayoutSize(
                final java.awt.Container parent) {
            return new java.awt.Dimension(250, 150);
        }
        
        public java.awt.Dimension minimumLayoutSize(
                final java.awt.Container parent) {
            return new java.awt.Dimension(150, 100);
        }
        
        public void addLayoutComponent(String s, java.awt.Component component) {
        }
        
        public void layoutContainer(final java.awt.Container parent) {
            java.awt.Insets insets = parent.getInsets();
            java.awt.Dimension size = parent.getSize();
            int totalW = size.width - (insets.left + insets.right);
            int totalH = size.height - (insets.top + insets.bottom);
            
            if (parent.getComponentCount()==0) {
                return;
            }
            
            for (int i=0; i<parent.getComponentCount(); i++) {
                java.awt.Component c = parent.getComponent(i);
                if (c.getName().equals("okbtn")) {
                    c.setBounds( (totalW-buttonSizeX)>>1, totalH-gapY-buttonSizeY,
                            buttonSizeX, buttonSizeY);
                    continue;
                }
                if (c.getName().equals("scrpane")) {
                    c.setBounds( gapX, gapY, totalW-2*gapX, totalH-3*gapY-buttonSizeY);
                    continue;
                }
            }
        }
        private int gapX = 5;
        private int gapY = 5;
        private int buttonSizeX = 90;
        private int buttonSizeY = 20;
    }

    private javax.swing.JButton ok;
    private javax.swing.JScrollPane scrpane;
    private javax.swing.JTextArea tarea;
}
