/*
 * HexaWarApplet.java
 *
 * Created on 2006. augusztus 5., 11:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.onebitheroes;

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.io.*;
import com.onebitheroes.*;
import java.util.*;

/**
 *
 * @author Eper
 */
public class HexaWarApplet extends javax.swing.JApplet implements Runnable, KeyListener, MouseListener {
    final static byte SQUAD_H = 8;
    final static byte BALTASOK_SZAMA_0 = 0;
    final static byte KARDOSOK_SZAMA_1 = 1;
    final static byte LANDZSASOK_SZAMA_2 = 2;
    final static byte POS_X = 3;
    final static byte POS_Y = 4;
    final static byte JATEKOS = 5;
    final static byte MOVE_DIR = 6;
    final static byte MOVE_CNT = 7;
    
    final static byte EZUST = 0;
    final static byte KEK = 1;
    final static byte LILA = 2;
    final static byte NARANCS = 3;
    final static byte PIROS = 4;
    final static byte SARGA = 5;
    final static byte SZURKE = 6;
    final static byte ZOLD = 7;
    final byte players[] = {EZUST, SZURKE, PIROS, LILA, NARANCS, ZOLD, KEK, SARGA };
    final byte castleSizeX[] = {34, 34, 34};
    final byte castleSizeY[] = {31, 31, 31};
    final byte castleSizeX_p2[] = {17, 17, 17};
    final byte castleSizeY_p2[] = {15, 15, 15};
    final int[] anim_phase_had = {0*32, 1*32, 2*32, 3*32};
    final byte[] move_offset_x = { 27,   0, -27,-27,  0, 27};
    final byte[] move_offset_y = {-18, -38, -18, 18, 38, 18};
    final static byte MOVE_ANIM_PHASE = 4;
    final static byte TILE_MERET_36 = 48;
    final static byte TILE_MERET_36_SZER3PER4 = (TILE_MERET_36*3/4);
    final static byte TILE_MERET_PER_2_18 = TILE_MERET_36>>1;    
    
    public class BGPainter extends JComponent {
        public void paintComponent(Graphics g) {
            paint_offscreen();
            g.drawImage(offscr, 0, 0, null);
        }
    }
    
    public void background(boolean logo) {
        g2.setClip(0,0,SW,SH);
        for (int y=0; y<SH; y+=img[0].getHeight(null)) {
            for (int x=0; x<SW; x+=img[0].getWidth(null)) {
                g2.drawImage(img[0], x, y, null);
            }
        }
        if (logo) {
            g2.drawImage(img[1], 0, 0, null);
        }
    }
    
    public void fill_shaded_rect(Color color, int x, int y, int w, int h) {
        g2.setColor(Color.black);
        g2.fillRect(x+w, y+2, 2, h);
        g2.fillRect(x+2, y+h, w, 2);
        g2.setColor(color);
        g2.fillRect(x, y, w, h);
    }
    
    public void paint_shaded(String s, Color color, int x, int y, boolean center) {
        int _x=x;
        if (center) {
            _x=x-((g2.getFontMetrics().stringWidth(s))>>1);
        }
        g2.setColor(Color.black);
        g2.drawString(s, _x+1, y+1);
        g2.setColor(color);
        g2.drawString(s, _x, y);
    }
    
    public void paint_connection_panel(String s, int _pos) {
        int size=Math.max(200, g2.getFontMetrics().stringWidth(s)+30);
        fill_shaded_rect(new Color(0x35, 0x50, 0x46), _pos-(size>>1), SH-80, size, 25);
        int pos=_pos-(g2.getFontMetrics().stringWidth(s)>>1);
        for (int i=0; i<(frames/5)%4; ++i) {
            s+=".";
        }
        paint_shaded(s, Color.white, pos, SH-63, false);
    }
    
    public void paint_numbers(int x, int y, int i) {
        g2.setClip(x, y, SQUAD_ANIM_X, SQUAD_ANIM_Y);
        g2.drawImage(img[19], x-anim_phase_had[frames%4], y-SQUAD_ANIM_Y*players[i], null);
        g2.setClip(0,0,SW,SH);
        x+=35;
        paint_shaded(""+server_sereg_vegeredmeny[i], Color.white, x, y+18, false);
        x+=35;
        g2.setClip(x, y, castleSizeX[0], castleSizeY[0]);
        g2.drawImage(img[6], x-players[i]*castleSizeX[0], y, null);
        x+=35;
        g2.setClip(0,0,SW,SH);
        paint_shaded(""+server_varak_vegeredmeny[i], Color.white, x, y+18, false);
    }
    
    public void paint_name(int x, int y, int i) {
        g2.setClip(x, y, SQUAD_ANIM_X, SQUAD_ANIM_Y);
        g2.drawImage(img[19], x-anim_phase_had[frames%4], y-SQUAD_ANIM_Y*players[i], null);
        g2.setClip(0,0,SW,SH);
        paint_shaded(server_jatekosnev[i], Color.white, x+33+10, y+17, false);
    }
    
    public void paint_num(String s, int x, int y) {
        int tmp;
        for (int i=0; i<s.length(); ++i) {
            g2.setClip(x, y, 5, 6);
            try {
                tmp=Integer.parseInt(s.substring(i, i+1))*5;
            } catch (Exception e) {
                tmp=-5000; // hack
            }
            g2.drawImage(img[14], x-tmp, y, null);
            x+=5;
        }
        g2.setClip(0,0,SW,SH);
    }
    
    final int guipos[] = {20, 50, 80};
    public void paint_gui(boolean onlyTime, boolean whole) {
        g2.setClip(0,0,SW,SH);
        int h=0;
        String s;
        if (whole) {
            g2.drawImage(img[8], 0, 0, null);
            g2.drawImage(img[8], img[8].getWidth(null), 0, null);
            
            g2.setClip(0,0,SW,SH);
            g2.drawImage(img[7], SW-img[7].getWidth(null), 0, null);
            //g2.drawImage(img[12], 0, h, null);
            s=""+(roundEnd-System.currentTimeMillis()/1000);
            paint_num(s, SW-5*(s.length())-4, 2);
        } else {
            h=img[24].getHeight(null);
            g2.setClip(img[23].getWidth(null), h, SW-img[25].getWidth(null)-img[23].getWidth(null), SH);
            g2.drawImage(img[8], img[23].getWidth(null), h, null);
            g2.drawImage(img[8], img[23].getWidth(null)+img[8].getWidth(null), h, null);
            
            g2.setClip(0,0,SW,SH);
            g2.drawImage(img[7], SW-img[7].getWidth(null)-img[25].getWidth(null), h, null);
            //g2.drawImage(img[12], 0, h, null);
            s=""+(roundEnd-System.currentTimeMillis()/1000);
            paint_num(s, SW-5*(s.length())-4-img[25].getWidth(null), 2+h);
        }
        if (onlyTime) {
            return;
        }
        int val=-1;
        if (selectedUnit!=-1) {
            val=selectedUnit;
        } else {
            if (canSelectUnit!=-1) {
                val=canSelectUnit;
            }
        }
        if (val!=-1) {
            for (int i=0; i<3; ++i) {
                g2.setClip(guipos[i]-6-1, 1+h, 6, 8);
                g2.drawImage(img[18], guipos[i]-6*i-6-1, 1+h, null);
                
                g2.setClip(0,0,SW,SH);
                g2.drawImage(img[13], guipos[i], 1+h, null);
                
                int t=squads[val*SQUAD_H+i];
                s=""+(t>999?999:t);
                while (s.length()<3) {
                    s="-"+s;
                }
                paint_num(s, guipos[i]+3, 2+h);
            }
        }
        
        g2.setClip(0,0,SW,SH);
        g2.setColor(Color.red);
    }
    
    final static int GUI_HEIGHT = 12;
    //              JF
    final int sx[]={37, 13, 37, 37, 13, 37};
    final int sy[]={21, 36, 21, 36, 36, 36};
    final int startOfs[]={0, 37, 37+13, 37+13+37+37+13, 37+13+37+37, 37+13+37};
    
    public void paint_arrow(int posx, int posy, int direction, int color) {
        int xp=(posx-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx;
        int yp=(posy-kamera_eposy)*TILE_MERET_36+(posx&1)*TILE_MERET_PER_2_18-kamera_rposy;
        yp+=GUI_HEIGHT;
        --direction;
        switch (direction) {
            case 0: // jobbra fel
                xp+=TILE_MERET_PER_2_18-2;
                yp+=3;
                break;
            case 1: // fel
                yp-=TILE_MERET_PER_2_18;
                xp+=TILE_MERET_PER_2_18-6;
                break;
            case 2: // balra fel
                xp-=TILE_MERET_PER_2_18-2;
                yp+=3;
                break;
            case 3: // balra le
                xp-=TILE_MERET_PER_2_18-8;
                yp+=TILE_MERET_PER_2_18-12;
                break;
            case 4: // le
                yp+=TILE_MERET_PER_2_18+(TILE_MERET_PER_2_18>>1);
                xp+=TILE_MERET_PER_2_18-6;
                break;
            case 5: // jobbra le
                xp+=TILE_MERET_PER_2_18-2;
                yp+=TILE_MERET_PER_2_18-12;
                break;
        }
        g2.setClip(xp, yp, sx[direction], sy[direction]);
        g2.drawImage(img[15], xp-startOfs[direction], yp-color*(img[15].getHeight(null)>>3), null);
    }
    
    final static int POTYI_MERET = 15;
    final static int POTYI_MERET_p2 = POTYI_MERET>>1;
    final static int POTYI_MERET_szer_3_per_4 = (POTYI_MERET*3)/4;
    public void paint_game_scene(boolean paintSelection, boolean paintFooter, boolean paintBorder) {
        g2.setClip(0,0,SW,SH);
        int xp=0, yp=0, val=0, ofs=0, tmp=0, i=0;
        
        if (scene==RUNNING_GAME_SCENE && keypressed[MY_KEY_POUND]) {
            background(false);
            ofs=0;
            int startx=SW_p2-45-(POTYI_MERET>>2)-(map_meret_x*POTYI_MERET_szer_3_per_4)/2;
            int starty=SH_p2-(map_meret_y*POTYI_MERET)/2;
            for (int y=0; y<map_meret_y; ++y) {
                ++ofs;
                for (int x=1; x<map_meret_x; ++x, ++ofs) {
                    tmp=map[ofs];
                    val=(tmp&0x180)>>7;
                    xp=startx+x*POTYI_MERET_szer_3_per_4;
                    yp=starty+y*POTYI_MERET+(x&1)*POTYI_MERET_p2;
                    g2.setClip(xp, yp, POTYI_MERET, POTYI_MERET);
                    g2.drawImage(img[16], xp-(val!=0?players[(tmp&0xE00)>>9]:8)*POTYI_MERET, yp, null);
                }
            }
            for (ofs=0; ofs<squads.length; ofs+=SQUAD_H) {
                if (squads[ofs+JATEKOS]==-1) {
                    continue;
                }
                xp=startx+squads[ofs+POS_X]*POTYI_MERET_szer_3_per_4+5;
                yp=starty+squads[ofs+POS_Y]*POTYI_MERET+(squads[ofs+POS_X]&1)*POTYI_MERET_p2+6;
                g2.setClip(xp, yp, 6, 6);
                g2.drawImage(img[17], xp-players[squads[ofs+JATEKOS]]*6, yp, null);
            }
            
            g2.setClip(0,0,SW,SH);
            g2.drawImage(img[23], 0, 0, null);
            g2.drawImage(img[24], 0, 0, null);
            g2.drawImage(img[25], SW-img[25].getWidth(null), 0, null);
            g2.drawImage(img[26], 0, SH-img[26].getHeight(null), null);
            
            paint_gui(true, false);
            return;
        }
        
        g2.setColor(Color.black);
        g2.fillRect(0,0,SW,SH);
        
        for (int y=-2; y<TILE_VISIBLE_Y; ++y) {
            for (int x=-2; x<TILE_VISIBLE_X; ++x) {
                if (y+kamera_eposy<0 || y+kamera_eposy>=map_meret_y
                        || x+kamera_eposx<1 || x+kamera_eposx>=map_meret_x) {
                    ofs=-1;
                } else {
                    ofs=(y+kamera_eposy)*map_meret_x+x+kamera_eposx;
                    val=map[ofs];
                }
                xp=x*TILE_MERET_36_SZER3PER4-kamera_rposx;
                yp=y*TILE_MERET_36+((x+kamera_eposx)&1)*TILE_MERET_PER_2_18-kamera_rposy;
                
                yp+=GUI_HEIGHT;
                g2.setClip(xp, yp, TILE_MERET_36, TILE_MERET_36);
                g2.drawImage(img[backgr?3:4], xp-(ofs==-1?7*TILE_MERET_36:(val&0xF)*TILE_MERET_36), yp, null);
            }
        }
        
        if (server_hg_mozgasirany!=null && server_hg_mozgasiranyhely!=null && server_hg_mozgasiranyhely_szin!=null) {
            int sx, sy, cx, cy;
            for (i=0; i<Math.min(server_hg_mozgasirany.length, server_hg_mozgasiranyhely.length); ++i) {
                if (server_hg_mozgasirany[i]==-1) { // just for sure...
                    continue;
                }
                sx=server_hg_mozgasiranyhely[i]%server_koord_map_width;
                sy=server_hg_mozgasiranyhely[i]/server_koord_map_width;
                cx=this.ServerCSToClientCSX(sx, sy);
                cy=this.ServerCSToClientCSY(sy);
/*                int color=0;
                for (ofs=0; ofs<squads.length; ofs+=SQUAD_H) {
                    if (squads[ofs+JATEKOS]==-1) {
                        continue;
                    }
                    if (squads[ofs+POS_X]==cx && squads[ofs+POS_Y]==cy) {
                        color=players[squads[ofs+JATEKOS]];
                        break;
                    }
                }//*/
                //                0  1  2  3  4  5
                final int conv[]={1, 6, 5, 4, 3, 2};
                paint_arrow(cx, cy, conv[server_hg_mozgasirany[i]], server_hg_mozgasiranyhely_szin[i]);
            }
        }
        for (i=0; i<LEPES_SZAM*PATH_H; i+=PATH_H) {
            if (path[i]!=-1) {
                paint_arrow(path[i+PATH_POS_X], path[i+PATH_POS_Y], path[i+PATH_DIRECTION],
                        players[server_hg_sajatsorszam]);
            }
        }
        for (int y=-1; y<TILE_VISIBLE_Y; ++y) {
            for (int x=-1; x<TILE_VISIBLE_X; ++x) {
                if (y+kamera_eposy<0 || y+kamera_eposy>=map_meret_y
                        || x+kamera_eposx<0 || x+kamera_eposx>=map_meret_x) {
                    continue;
                }
                
                ofs=(y+kamera_eposy)*map_meret_x+x+kamera_eposx;
                tmp=map[ofs];
                val=(tmp&0x180)>>7;
                if (val!=0)  {
                    xp=x*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18-castleSizeX_p2[val-1];
                    yp=y*TILE_MERET_36+((x+kamera_eposx)&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18-castleSizeY_p2[val-1];
                    yp+=GUI_HEIGHT;
                    yp-=5;
                    g2.setClip(xp, yp, castleSizeX[val-1], castleSizeY[val-1]);
                    g2.drawImage(img[5+val], xp-players[(tmp&0xE00)>>9]*castleSizeX[val-1], yp, null);
                }
            }
        }
        
        for (i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
            if (squads[ofs+JATEKOS]==-1 || squads[ofs+JATEKOS]==server_hg_sajatsorszam) {
                continue;
            }
            xp=(squads[ofs+POS_X]-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18-(SQUAD_ANIM_X>>1);
            yp=(squads[ofs+POS_Y]-kamera_eposy)*TILE_MERET_36+((squads[ofs+POS_X])&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18-(SQUAD_ANIM_Y>>1);
            if (squads[ofs+MOVE_CNT]>0) {
                xp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_x[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                yp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_y[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
            } else {
            }
            yp+=GUI_HEIGHT;
            yp+=5;
            g2.setClip(xp, yp, SQUAD_ANIM_X, SQUAD_ANIM_Y);
            if (squads[ofs+MOVE_CNT]!=0 || selectedUnit==i) {
                xp-=anim_phase_had[frames%4];
                
            }
            g2.drawImage(img[19], xp, yp-SQUAD_ANIM_Y*players[squads[ofs+JATEKOS]], null);
        }
        
        for (i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
            if (squads[ofs+JATEKOS]==-1 || squads[ofs+JATEKOS]!=server_hg_sajatsorszam) {
                continue;
            }
            xp=(squads[ofs+POS_X]-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18-(SQUAD_ANIM_X>>1);
            yp=(squads[ofs+POS_Y]-kamera_eposy)*TILE_MERET_36+((squads[ofs+POS_X])&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18-(SQUAD_ANIM_Y>>1);
            if (squads[ofs+MOVE_CNT]>0) {
                xp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_x[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                yp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_y[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
            } else {
            }
            yp+=GUI_HEIGHT;
            yp+=5;
            g2.setClip(xp, yp, SQUAD_ANIM_X, SQUAD_ANIM_Y);
            if (squads[ofs+MOVE_CNT]!=0 || selectedUnit==i) {
                xp-=anim_phase_had[frames%4];
                
            }
            g2.drawImage(img[19], xp, yp-SQUAD_ANIM_Y*players[squads[ofs+JATEKOS]], null);
        }
        
        if (selectedUnit==-1 && paintSelection) {
            xp=(currentSelectionX-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx-1;
            yp=(currentSelectionY-kamera_eposy)*TILE_MERET_36+((currentSelectionX)&1)*TILE_MERET_PER_2_18-kamera_rposy-1;
            yp+=GUI_HEIGHT;
            g2.setClip(xp, yp, TILE_MERET_36+2, TILE_MERET_36+2);
            if (canSelectUnit!=-1 && squads[canSelectUnit*SQUAD_H+JATEKOS]!=server_hg_sajatsorszam) {
                xp-=img[9].getWidth(null)>>1;
            }
            g2.drawImage(img[9], xp, yp, null);
        }
        
        if (paintBorder) {
            g2.setClip(0,0,SW,SH);
            g2.drawImage(img[23], 0, 0, null);
            g2.drawImage(img[24], 0, 0, null);
            g2.drawImage(img[25], SW-img[25].getWidth(null), 0, null);
            g2.drawImage(img[26], 0, SH-img[26].getHeight(null), null);
        }
        boolean divide=false;
        if (paintFooter) {
            g2.setClip(0,0,SW,SH);
            if (selectedUnit!=-1) {
                lsk.setText(lng.UNDO_LABEL);
            } else if (canSelectUnit!=-1 && squads[canSelectUnit*SQUAD_H+JATEKOS]==
                    server_hg_sajatsorszam) {
                divide=true;
            } else {
                
            }
        }
        if (gamemenubtn!=null) {
            gamemenubtn[0].setVisible(paintFooter);
            gamemenubtn[1].setVisible(divide);
            gamemenubtn[2].setVisible(divide);
            for (i=3; i<gamemenubtn.length-1; ++i) {
                gamemenubtn[i].setVisible(selectedUnit!=-1&&paintFooter);
            }
            if (!gamemenubtn[12].isVisible()) {
                gamemenubtn[12].setVisible(true);
            }
        }
    }
    
    public void paint_divide_panel() {
        g2.setClip(0,0,SW,SH);
        fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-60, 140, 120);
        
        for (int i=0; i<3; ++i) {
            g2.setClip(SW_p2-40-45, SH_p2-8+i*20, 6, 8);
            g2.drawImage(img[18], SW_p2-40-6*i-45, SH_p2-8+i*20, null);
            g2.setClip(0,0,SW,SH);
/*            g2.setColor(Color.black);
            g2.fillRect(SW_p2-25-2, SH_p2-10+i*17-1, 30, 15);
            if (darabolOfs==i) {
                g2.setColor(Color.red);
                g2.drawRect(SW_p2-25-2, SH_p2-10+i*17-1, 30, 15);
            }//*/
//            paint_shaded(""+darabolEgyseg[i], Color.white, SW_p2-25, SH_p2-10+i*17, false);
            paint_shaded(lng.divide[1], Color.white, SW_p2+5-45, SH_p2+i*20, false);
            paint_shaded(""+squads[canSelectUnit*SQUAD_H+i], Color.white, SW_p2+25-45, SH_p2+i*20, false);
        }
        paint_shaded(lng.divide[0], Color.white, SW_p2-45, SH_p2-40, true);
    }
    
    public void paint_offscreen() {        
        int x, y, i;
        lsk.setText("");
        rsk.setText("");

        if (scene!=MAIN_MENU_SCENE && scene!=REG_SCENE) {
            loginName.setVisible(false);
            pass[0].setVisible(false);
            pass[1].setVisible(false);
            mail.setVisible(false);
            loginBtn.setVisible(false);
        }
        switch (scene) {
            case REG_OK_SCENE:
                background(false);
                paint_shaded(lng.regInfo[1], Color.white, SW_p2, SH_p2, true);
                lsk.setText(lng.yesno[1]);
                rsk.setText(lng.yesno[0]);
                break;            
            case GET_REG_INFO_SCENE:
                background(false);
                paint_connection_panel(lng.other_txt[11], SW_p2);
                break;            
            case DIVIDE_SCENE:
                paint_game_scene(true, false, true);
                g2.setClip(0,0,SW,SH);
/*				for (i=0; i<4; ++i) {
                                        g.drawImage(img[11], (i&1)*img[11].getWidth(), (i>>1)*img[11].getHeight(), Graphics.TOP|Graphics.LEFT);
                                }*/
                paint_gui(false, false);
                paint_divide_panel();
                lsk.setText(lng.divide[2]);
                rsk.setText(lng.divide[3]);
                move_kamera(true);
                break;
            case PAUSE_SCENE:
                paint_game_scene(true, false, true);
                paint_gui(false, false);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-60, 140, 120);
                g2.setFont(fontpsmall);
                paint_shaded(lng.other_txt[4], Color.white, SW_p2-45, SH_p2-45, true);
                break;
            case END_OF_GAME_SCENE_2:
                background(false);
                paint_shaded(lng.endofgame[0], Color.white, SW_p2, 25, true);
                y=55;
                paint_shaded(lng.endofgame[1], Color.white, SW_p2, y, true); y+=15;
                paint_shaded(server_jatekosnev[server_id_vegeredmeny[0]], Color.white, SW_p2, y, true); y+=15;
                y+=15;
                paint_shaded(lng.endofgame[2], Color.white, SW_p2, y, true); y+=15;
                int v=0;
                while (server_id_vegeredmeny[v]!=server_hg_sajatsorszam) {
                    ++v;
                }
                paint_shaded(lng.endofgame[3+v], Color.white, SW_p2, y, true); y+=15;
                rsk.setText(lng.pause_txt[0]);
                break;
            case END_OF_GAME_SCENE_1:
                if (gamemenubtn[12].isVisible()) {
                    gamemenubtn[12].setVisible(false);
                }
                background(false);
                paint_shaded(lng.endofgame[0], Color.white, SW_p2, 25, true);
                y=55;
                for (i=0; i<Math.min(server_sereg_vegeredmeny.length, server_varak_vegeredmeny.length); ++i) {
                    x=SW_p2-60;
                    g2.setClip(x, y, SQUAD_ANIM_X, SQUAD_ANIM_Y);
                    g2.drawImage(img[19], x-anim_phase_had[frames%4], y-SQUAD_ANIM_Y*players[server_id_vegeredmeny[i]], null);
                    g2.setClip(0,0,SW,SH);
                    x+=40;
                    paint_shaded(""+server_sereg_vegeredmeny[i], Color.white, x, y+19, false);
                    x+=45;
                    g2.setClip(x, y, castleSizeX[0], castleSizeY[0]);
                    g2.drawImage(img[6], x-players[server_id_vegeredmeny[i]]*castleSizeX[0], y, null);
                    x+=45;
                    g2.setClip(0,0,SW,SH);
                    paint_shaded(""+server_varak_vegeredmeny[i], Color.white, x, y+19, false);
                    y+=25;
                }
                rsk.setText(lng.pause_txt[0]);
                break;
            case DISPLAY_END_OF_TURN_INFO_0:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                if (DISPLAY_WAIT_TIME!=-1) {
                    int sx=server_hg_csata[eofTurnBattle]%server_koord_map_width;
                    int sy=server_hg_csata[eofTurnBattle]/server_koord_map_width;
                    x=this.ServerCSToClientCSX(sx, sy);
                    y=this.ServerCSToClientCSY(sy);
                    int xp=(x-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx-1;
                    int yp=(y-kamera_eposy)*TILE_MERET_36+(x&1)*TILE_MERET_PER_2_18-kamera_rposy-1;
                    yp+=GUI_HEIGHT;
                    g2.setClip(xp, yp, TILE_MERET_36+2, TILE_MERET_36+2);
                    g2.drawImage(img[9], xp, yp, null);
                }
                break;
                
            case DISPLAY_END_OF_TURN_INFO_1:
            case DISPLAY_END_OF_TURN_INFO_2:
            case DISPLAY_END_OF_TURN_INFO_3:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                
                g2.setClip(0,0,SW,SH);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-70, 140, 120);
                g2.setColor(Color.white);
                if (server_hg_vedo_jatekos[eofTurnBattle]==server_hg_sajatsorszam) {
                    // megtamadtak a jatekost
                    paint_shaded(lng.attack[2], Color.white, SW_p2-45, SH_p2-48-5, true);
                    paint_shaded(lng.attack[3], Color.white, SW_p2-45, SH_p2-37-5, true);
                } else { // a jatekos tamadott
                    paint_shaded(lng.attack[0], Color.white, SW_p2-45, SH_p2-48-5, true);
                    paint_shaded(lng.attack[1], Color.white, SW_p2-45, SH_p2-37-5, true);
                }
                
                x=SW_p2-50-45;
                y=SH_p2-25-5;
                g2.setClip(x, y, SQUAD_ANIM_X, SQUAD_ANIM_Y);
                g2.drawImage(img[19], x-anim_phase_had[frames%4], y-SQUAD_ANIM_Y*players[server_hg_vedo_jatekos[eofTurnBattle]], null);
                g2.setClip(0,0,SW,SH);
                paint_shaded(""+a1sereg[0]+"/"+a1sereg[1]+"/"+a1sereg[2], Color.white, x+33+10, y+19, false); // vedo
                
                x=SW_p2-50-45;
                y=SH_p2-5-5;
                g2.setClip(x, y, SQUAD_ANIM_X, SQUAD_ANIM_Y);
                g2.drawImage(img[19], x-anim_phase_had[frames%4], y-SQUAD_ANIM_Y*players[server_hg_tamado_jatekos[eofTurnBattle]], null);
                g2.setClip(0,0,SW,SH);
                paint_shaded(""+a1sereg[3]+"/"+a1sereg[4]+"/"+a1sereg[5], Color.white, x+33+10, y+19, false); // tamado
                
                if (scene!=DISPLAY_END_OF_TURN_INFO_3) {
                    rsk.setText(lng.eofTurnInfo[2]);
                    break;
                }
                paint_shaded(lng.attack[4+(server_hg_winner[eofTurnBattle]==server_hg_sajatsorszam?1:0)], Color.white, SW_p2-45, SH_p2+30, true);
                paint_shaded(lng.attack[6], Color.white, SW_p2-45, SH_p2+45, true);
                rsk.setText(lng.eofTurnInfo[eofTurnNextBattle!=-1?0:1]);
                break;
            case WAITING_FOR_END_OF_TURN_SCENE:
            case WAITING_FOR_RESPONSE_SCENE:
                paint_game_scene(false, false, true);
                    /*g.setClip(0,0,SW,SH);
                    for (y=0; y<SH; y+=img[16].getWidth()) {
                        for (x=0; x<SW; x+=img[16].getHeight()) {
                            g.drawImage(img[16], x, y, Graphics.TOP|Graphics.LEFT);
                        }
                    }
                    g.drawImage(img[17], 0, 0, Graphics.TOP|Graphics.LEFT);
                    for (i=0; i<12; ++i) {
                        x=csataAnimPosX[i];
                        y=csataAnimPosY[i];
                        g.setClip(x, y, 16, 16);
                        x-=anim_phase_ember[(frame+i)%4];
                        g.drawImage(img[3+csataAnimTipus[i]], x, y-16*(i<6?csataAnimSzin1:csataAnimSzin2), Graphics.TOP|Graphics.LEFT);
                    }*/
                
                g2.setFont(fontpsmall);
                g2.setClip(0,0,SW,SH);
                if (scene==WAITING_FOR_RESPONSE_SCENE) {
                    paint_connection_panel(lng.other_txt[6], SW_p2-45);
                } else {
                    
                    g2.setClip(2+10, SH_p2-4, 9, 9);
                    g2.drawImage(img[2], 2+10-3*9, SH_p2-4, null);
                    g2.setClip(SW-2-9-90, SH_p2-4, 9, 9);
                    g2.drawImage(img[2], SW-2-9-2*9-90, SH_p2-4, null);
                    
                    g2.setClip(SW_p2-4-45, 2+10, 9, 9);
                    g2.drawImage(img[2], SW_p2-4-9-45, 2+10, null);
                    g2.setClip(SW_p2-4-45, SH-20-2-9, 9, 9);
                    g2.drawImage(img[2], SW_p2-4-45, SH-20-2-9, null);
                    
                    g2.setClip(0,0,SW,SH);
                    //	paint_shaded(g, "End of turn after "+((System.currentTimeMillis()-ETIME-WAIT_TIME)/1000)+"secs", 0xFFFFFF, 5, 5, false);
                    g2.setColor(new Color(0x35, 0x50, 0x46));
                    g2.fillRect(0, SH-20, SW, 20);
                    g2.setColor(Color.black);
                    g2.drawLine(0, SH-20, SW, SH-20);
                    paint_shaded(lng.MISSING_RESPONSES_STRING_1+server_missingresponses+lng.MISSING_RESPONSES_STRING_2, Color.white, SW_p2, SH-5, true);
                }
                break;
            case SEND_ACTION_SCENE:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                paint_connection_panel(lng.other_txt[5], SW_p2-45);
                move_kamera(true);
                break;
            case RUNNING_GAME_SCENE:
                paint_game_scene(true, true, true);
                
                if (!keypressed[MY_KEY_POUND]) {
                    paint_gui(false, false);
                    rsk.setText(lng.other_txt[4]);
                } else {
                    if (!gamemenubtn[0].isVisible()) {
                        gamemenubtn[0].setVisible(true);
                    }
                }
/*                if (CLKX!=Integer.MAX_VALUE) {
                    g2.setClip(0,0,SW,SH);
                    g2.setColor(Color.red);
                    g2.drawRect(CLKX, CLKY, 36, 48);
                }*/
                break;
            case PRE_RUNNING_GAME_SCENE_WITH_CLIENTS:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-60, 140, 120);
                paint_shaded(lng.startTxt[3]+server_actround, Color.white, SW_p2-45, SH_p2-45, true);
                y=SH_p2-15;
                for (i=0; i<last_client_state.length; ++i) {
                    if (server_client_state[i]!=last_client_state[i]) {
                        switch (server_client_state[i]) {
                            case 0:
                                paint_shaded(server_jatekosnev[i], Color.white, SW_p2-45, y, true);
                                y+=15;
                                paint_shaded(lng.CLS[0], Color.white, SW_p2-45, y, true);
                                y+=15;
                                break;
                            case 1:
                                break;
                            case 2:
                                paint_shaded(server_jatekosnev[i], Color.white, SW_p2-45, y, true);
                                y+=15;
                                paint_shaded(lng.CLS[2], Color.white, SW_p2-45, y, true);
                                y+=15;
                                break;
                            case 3:
                                paint_shaded(lng.CLS[3], Color.white, SW_p2-45, y, true);
                                y+=15;
                                paint_shaded(server_jatekosnev[i], Color.white, SW_p2-45, y, true);
                                y+=15;
                                break;
                        }
                    }
                }
                rsk.setText(lng.pause_txt[0]);
                break;
                
            case PRE_RUNNING_GAME_SCENE_WITH_NUMBERS:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-70, 140, 140);
                paint_shaded(lng.startTxt[3]+server_actround, Color.white, SW_p2-45, SH_p2-55, true);
                y=SH_p2-25;
                paint_numbers(SW_p2-60-45, y, server_hg_sajatsorszam);
                y+=25;
/*                    for (i=0; i<jatekosok_szama; ++i) {
                        if (i==server_hg_sajatsorszam) {
                            continue;
                        }
                        paint_numbers(SW_p2-60, y, i, SQUAD_ANIM_X, SQUAD_ANIM_Y);
                        y+=25;
                    }*/
                
                for (int ii=0, tempi=pre_running_cnt; ii<Math.min(2, jatekosok_szama-1); ++tempi) {
                    tempi%=jatekosok_szama;
                    if (tempi==server_hg_sajatsorszam) {
                        continue;
                    }
                    paint_numbers(SW_p2-60-45, y, tempi);
                    y+=25;
                    ++ii;
                }
                rsk.setText(lng.pause_txt[0]);
                if (jatekosok_szama>3) {
                    g2.setClip(SW_p2-9-45, SH-10, 18, 9);
                    g2.drawImage(img[2], SW_p2-9-45, SH-10, null);
                }
                break;
                
            case PRE_RUNNING_GAME_SCENE_WITH_NAMES:
                paint_game_scene(false, false, true);
                paint_gui(false, false);
                
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-70-45, SH_p2-70, 140, 140);
                paint_shaded(lng.startTxt[0], Color.white, SW_p2-45, SH_p2-55, true);
                paint_shaded(lng.startTxt[1], Color.white, SW_p2-45, SH_p2-40, true);
                paint_shaded(lng.startTxt[2]+server_lastround, Color.white, SW_p2-45, SH_p2-25, true);
                y=SH_p2-15;
                paint_name(SW_p2-65-45, y, server_hg_sajatsorszam);
                y+=25;
/*                    for (i=0; i<jatekosok_szama; ++i) {
                        if (i==server_hg_sajatsorszam) {
                            continue;
                        }
                        paint_name(SW_p2-65, y, i, SQUAD_ANIM_X, SQUAD_ANIM_Y);
                        y+=25;
                    }//*/
                for (int ii=0, tempi=pre_running_cnt; ii<Math.min(2, jatekosok_szama-1); ++tempi) {
                    tempi%=jatekosok_szama;
                    if (tempi==server_hg_sajatsorszam) {
                        continue;
                    }
                    paint_name(SW_p2-65-45, y, tempi);
                    ++ii;
                    y+=25;
                }
                rsk.setText(lng.pause_txt[0]);
                if (jatekosok_szama>3) {
                    g2.setClip(SW_p2-9-45, SH-10, 18, 9);
                    g2.drawImage(img[2], SW_p2-9-45, SH-10, null);
                }
                move_kamera(true);
                break;
            case JUMP_TO_MAIN_MENU_SCENE:
                background(true);
                break;
            case WAIT_AND_TRY_AGAIN:
                background(true);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-100, SH-80, 200, 45);
                y=SH-65;
                for (i=0; i<lng.error_txt[4].length; ++i, y+=15) {
                    paint_shaded(lng.error_txt[4][i], Color.white, SW_p2, y, true);
                }
                lsk.setText(lng.other_txt[2]);
                break;
                
            case WAIT_FOR_GAME_SCENE:
                background(true);
                paint_connection_panel(lng.other_txt[3], SW_p2);
                break;
            case DISPLAY_INFO_SCENE:
                background(false);
                for (i=0, y=25; server_info_array!=null && i<server_info_array.length; ++i, y+=15) {
                    paint_shaded(server_info_array[i], Color.white, SW_p2, y, true);
                }
                rsk.setText(lng.pause_txt[0]);
                break;
            case LOADING_SCENE:
/*                if (img[0]!=null && img[1]!=null) {
                    background(true);
                    g2.setColor(Color.BLACK);
                    g2.fillRect(SW_p2-51, SH_p2+4, 102, 8);
                    g2.setColor(Color.RED);
                    g2.fillRect(SW_p2-50, SH_p2+5, (loadingCnt*100)/img.length, 6);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0,0,SW,SH);
                    g2.setColor(Color.BLACK);
                }
                paint_shaded(STR_LOADING, g2.getColor(), SW_p2, SH_p2, true);//*/
                if (title!=null) {
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0,0,SW,SH);
                    g2.drawImage(title, SW_p2-(title.getWidth(null)>>1), SH_p2-(title.getHeight(null)>>1), null);
                }
                break;
            case MAIN_MENU_SCENE:
                background(true);
                g2.setColor(Color.white);
                paint_shaded(lng.STR_WELCOME, Color.white, SW_p2, SH_p2-15, true);
                x=g2.getFontMetrics().stringWidth(lng.STR_NAME);
                y=g2.getFontMetrics().stringWidth(lng.STR_PASS);
                paint_shaded(lng.STR_NAME, Color.white, SW_p2-Math.max(x, y)-65, SH_p2+13, false);
                paint_shaded(lng.STR_PASS, Color.white, SW_p2-Math.max(x, y)-65, SH_p2+33, false);
                paint_shaded(lng.CLICK_HERE_MORE, Color.white, 100, SH-5, false);
                lsk.setText(lng.CLICK_HERE);
                rsk.setText(lng.HELP);
                loginBtn.setText(lng.STR_LOGIN);
                
                loginName.setVisible(true);
                pass[0].setVisible(true);
                pass[1].setVisible(false);
                mail.setVisible(false);
                loginBtn.setVisible(true);
                break;
            case REG_SCENE:
                background(true);
                g2.setColor(Color.white);
                x=g2.getFontMetrics().stringWidth(lng.STR_NAME);
                y=g2.getFontMetrics().stringWidth(lng.STR_PASS);
                v=Math.max( Math.max(x, y), Math.max(g2.getFontMetrics().stringWidth(lng.STR_MAIL), g2.getFontMetrics().stringWidth(lng.STR_PASS2)) );                
                paint_shaded(lng.STR_NAME, Color.white, SW_p2-v-65, SH_p2+13, false);
                paint_shaded(lng.STR_PASS, Color.white, SW_p2-v-65, SH_p2+33, false);
                paint_shaded(lng.STR_PASS2, Color.white, SW_p2-v-65, SH_p2+53, false);
                paint_shaded(lng.STR_MAIL, Color.white, SW_p2-v-65, SH_p2+73, false);
                
                paint_shaded(lastErrorStr, Color.white, SW_p2, SH_p2-15, true);
                
                lsk.setText(lng.divide[3]);
                loginBtn.setText(lng.STR_REGISTER);
                
                loginName.setVisible(true);
                pass[0].setVisible(true);
                pass[1].setVisible(true);
                mail.setVisible(true);
                loginBtn.setVisible(true);
                break;
            case LOGIN_SCENE:
                background(true);
                paint_connection_panel(lng.other_txt[0], SW_p2);
                break;
                
            case ERROR_SCENE:
                background(true);
                fill_shaded_rect(new Color(0x35, 0x50, 0x46), SW_p2-100, SH-85, 200, 50);
                y=SH-70;
                for (i=0; i<lng.error_txt[lastError].length; ++i, y+=15) {
                    paint_shaded(lng.error_txt[lastError][i], Color.white, SW_p2, y, true);
                }
                lsk.setText(lng.other_txt[2]);
                break;
            case SHOW_TYPE_SCENE:
                background(true);
                if (!typeReaded) {
                    paint_connection_panel(lng.other_txt[1], SW_p2);
                } else {
                    lsk.setText(lng.other_txt[2]);
                }
                break;
                
            default:
                System.out.println("unpainted scene: "+scene);
        }
        lsk.setVisible(!lsk.getText().equals(""));
        rsk.setVisible(!rsk.getText().equals(""));
    }
    
    public HexaWarApplet() {
    }
    
    public void start() {
        if (thread==null) {
            thread=new Thread(this);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.start();
        }
    }
    
    public void stop() {
        isRunning=false;
    }
    
    public int ServerCSToClientCSX(int x, int y) {
        return (int)( (y&1)==0?x*2+2: (x*2)-1 +2);
    }
    public int ServerCSToClientCSY(int y) {
        return (int)(y>>1);
    }
    public int ClientCSToServerCSY(int x, int y) {
        y<<=1;
        return (int)((x&1)==0?y:y+1);
    }
    public int ClientCSToServerCSX(int x) {
        if ((x&1)==1) {
            x+=1;
        }
        x-=2;
        return x>>=1;
    }
    
    final static byte bbaacckk[]={1, 2, 3, 4, 5, 6};
    public void terjedBack(int ofs) {
        if ( (ofs%map_meret_x)+1<map_meret_x && map[ofs+1]==0 && rand.nextInt(16)<4) {
            map[ofs+1]=map[ofs];
            terjedBack(ofs+1);
        }
        if ((ofs%map_meret_x)-1>=0 && map[ofs-1]==0 && rand.nextInt(16)<4) {
            map[ofs-1]=map[ofs];
            terjedBack(ofs-1);
        }
        if ( (ofs/map_meret_x)+1<map_meret_y && map[ofs+map_meret_x]==0 && rand.nextInt(16)<4) {
            map[ofs+map_meret_x]=map[ofs];
            terjedBack(ofs+map_meret_x);
        }
        if ((ofs/map_meret_x)-1>=0 && map[ofs-map_meret_x]==0 && rand.nextInt(16)<4) {
            map[ofs-map_meret_x]=map[ofs];
            terjedBack(ofs-map_meret_x);
        }
    }
    
    public void init_map() {
        cx=server_hg_cx;
        cy=server_hg_cy;
        int[] jatekos=server_hg_jatekos;
        int[] sereg0=server_hg_sereg[0];
        int[] sereg1=server_hg_sereg[1];
        int[] sereg2=server_hg_sereg[2];
        int[] back=server_hg_hatter;
        int[] kastely=server_hg_epulet;
        int[] ut0=server_hg_e[0];
        int[] ut1=server_hg_e[1];
        int[] ut2=server_hg_e[2];
        
        map_meret_x=-1;
        map_meret_y=-1;
//#ifdef DEBUG
//#             if (cx.length!=cy.length || back.length!=cx.length || kastely.length!=cx.length) {
//#                 System.out.println(" ### ERROR: x.length!=y.length");
//#             }
//#endif
        int maxx=-1;
        for (int i=0; i<cx.length; ++i) {
            if (cx[i]>maxx) {
                maxx=cx[i];
            }
            cx[i]=ServerCSToClientCSX(cx[i], cy[i]);
            cy[i]=ServerCSToClientCSY(cy[i]);
        }
        server_koord_map_width=maxx+1;
        for (int i=0; i<server_hg_nodenum; ++i) {
            if (cx[i]>map_meret_x) {
                map_meret_x=cx[i];
            }
            if (cy[i]>map_meret_y) {
                map_meret_y=cy[i];
            }
        }
        ++map_meret_x;
        ++map_meret_y;
        
//#ifdef DEBUG
//#             System.out.println(">>> map size is "+(map_meret_x-1)+"x"+map_meret_y);
//#endif
        map=new int[map_meret_x*map_meret_y];
        int squad_num=1; // darabolashoz az utolso
        for (int i=0; i<sereg0.length; ++i) {
            if (sereg0[i]+sereg1[i]+sereg2[i]>0) {
                ++squad_num;
            }
        }
//#ifdef DEBUG
//#             System.out.println(">>> squad_num+1="+squad_num);
//#endif
        squads=new int[squad_num*SQUAD_H];
        for (int i=0; i<squads.length; ++i) {
            squads[i]=-1;
        }
        for (int i=0; i<map.length; ++i) {
            map[i]=0;
        }
//#ifdef DEBUG
//#             System.out.println(">>> cx.length="+cx.length);
//#             System.out.println(">>> cy.length="+cy.length);
//#             System.out.println(">>> background.length="+back.length);
//#             System.out.println(">>> kastely.length="+kastely.length);
//#             System.out.println(">>> jatekos.length="+jatekos.length);
//#             System.out.println(">>> sereg0.length="+sereg0.length);
//#             System.out.println(">>> sereg1.length="+sereg1.length);
//#             System.out.println(">>> sereg2.length="+sereg2.length);
//#endif
        int max=-1;
        for (int i=0; i<jatekos.length; ++i) {
            if (max<jatekos[i]) {
                max=jatekos[i];
            }
        }
        jatekosok_szama=max+1;
        server_missingresponses=jatekosok_szama-1;
        server_client_state=null;
        last_client_state=new int[jatekosok_szama];
        for (int i=0; i<last_client_state.length; ++i) {
            last_client_state[i]=0;
        }
        max=-1;
//#ifdef DEBUG
//#             System.out.println(">>> jatekosok szama "+jatekosok_szama);
//#endif
        for (int i=0; i<jatekosok_szama; ++i) {
            if (max<g2.getFontMetrics().stringWidth(server_jatekosnev[i])) {
                max=g2.getFontMetrics().stringWidth(server_jatekosnev[i]);
                leghosszabb_nevu_jatekos=i;
            }
        }
        server_sereg_vegeredmeny=new int[jatekosok_szama];
        server_varak_vegeredmeny=new int[jatekosok_szama];
        for (int i=0; i<jatekosok_szama; ++i) {
            server_sereg_vegeredmeny[i]=0;
            server_varak_vegeredmeny[i]=0;
        }
        int o;
        currentSelectionX=-1;
        currentSelectionY=-1;
        
        for (int i=0, seregOfs=0; i<server_hg_nodenum; ++i) {
            int ofs=cy[i]*map_meret_x+cx[i];
            if (ofs<map.length) {
                if (back[i]==0) {
                    if (map[ofs]==0 && rand.nextInt(16)<7) {
                        map[ofs]=bbaacckk[rand.nextInt(bbaacckk.length)];
                        terjedBack(ofs);
                    }
                } else {
                    map[ofs]=back[i];                    
                }
                if (ut0!=null && ut0[i]==1) {
                    map[ofs]|=0x10;
                }
                if (ut1!=null && ut1[i]==1) {
                    map[ofs]|=0x20;
                }
                if (ut2!=null && ut2[i]==1) {
                    map[ofs]|=0x40;
                }
            } else {
//#ifdef DEBUG
//#                     System.out.println("@@@"+ofs+"?"+map.length);
//#endif
            }
            if (kastely[i]==1) {
                placeCastle(ofs, jatekos[i], 1);
                if (jatekos[i]>=0 && jatekos[i]<server_varak_vegeredmeny.length) {
                    ++server_varak_vegeredmeny[jatekos[i]];
                }
            }
            if (sereg0[i]+sereg1[i]+sereg2[i]!=0) {
                o=jatekos[i];
                if (o==server_hg_sajatsorszam) {
                    currentSelectionX=cx[i];
                    currentSelectionY=cy[i];
                    cycleUnit=seregOfs;
                }
                squads[seregOfs+JATEKOS]=o;
//#ifdef DEBUG
//#                     System.out.println("jatekos["+i+"]="+o);
//#endif
                squads[seregOfs+BALTASOK_SZAMA_0]=sereg0[i];
                squads[seregOfs+KARDOSOK_SZAMA_1]=sereg1[i];
                squads[seregOfs+LANDZSASOK_SZAMA_2]=sereg2[i];
                squads[seregOfs+POS_X]=cx[i];
                squads[seregOfs+POS_Y]=cy[i];
                squads[seregOfs+MOVE_DIR]=0;
                squads[seregOfs+MOVE_CNT]=0;
                seregOfs+=SQUAD_H;
                
                server_sereg_vegeredmeny[o]+=sereg0[i];
                server_sereg_vegeredmeny[o]+=sereg1[i];
                server_sereg_vegeredmeny[o]+=sereg2[i];
            }
        } //*/
        for (int i=0; i<map_meret_y; ++i) {
            map[i*map_meret_x]=0;
        }
        kamera_posx=0;
        kamera_posy=0;
        calc_kamera_dest(currentSelectionX, currentSelectionY);
        jatekos=null;
        sereg0=null;
        sereg1=null;
        sereg2=null;
        back=null;
        kastely=null;
        server_hg_mozgasirany=null;
        server_hg_mozgasiranyhely=null;
        server_hg_mozgasiranyhely_szin=null;
        System.gc();
    }
    
    public int getNextPosX(int x, int direction) {
        switch (direction) {
            case 6: // key 9
            case 1: // key 3
                return (int)(x+1);
            case 2: // key 2
            case 5: // key 8
                return x;
            case 3: // key 1
            case 4: // key 7
                return (int)(x-1);
        }
        return -1;
    }
    
    public int getNextPosY(int x, int y, int direction) {
        switch (direction) {
            case 6: // key 9
                return (int)((x&1)==1?y+1:y);
            case 1: // key 3
                return (int)((x&1)==0?y-1:y);
            case 2: // key 2
                return (int)(y-1);
            case 3: // key 1
                return (int)((x&1)==0?y-1:y);
            case 4: // key 7
                return (int)((x&1)==1?y+1:y);
            case 5: // key 8
                return (int)(y+1);
        }
        return -1;
        
    }
    
    public void update_unit(int ofs) {
        if (squads[ofs+MOVE_CNT]>0) {
            --squads[ofs+MOVE_CNT];
            if (squads[ofs+MOVE_CNT]==0) {
                int newx=getNextPosX(squads[ofs+POS_X], squads[ofs+MOVE_DIR]);
                int newy=getNextPosY(squads[ofs+POS_X], squads[ofs+POS_Y], squads[ofs+MOVE_DIR]);
                squads[ofs+POS_X]=newx;
                squads[ofs+POS_Y]=newy;
                squads[ofs+MOVE_DIR]=0;
                calc_kamera_dest(squads[ofs+POS_X], squads[ofs+POS_Y]);
            }
        }
    }
    
    public int sgn(int x) {
        return x==0?0:(x<0?-1:1);
    }
    
    final static byte kamera_move_speed = 4;
    public boolean move_kamera(boolean doClip) {
        if (kamera_posx==kamera_destx && kamera_posy==kamera_desty) {
            return true;
        }
        if (((kamera_destx-kamera_posx)>>kamera_move_speed)==0) {
            kamera_posx+=sgn(kamera_destx-kamera_posx);
        } else {
            kamera_posx+=(kamera_destx-kamera_posx)>>kamera_move_speed;
        }
        if (((kamera_desty-kamera_posy)>>kamera_move_speed)==0) {
            kamera_posy+=sgn(kamera_desty-kamera_posy);
        } else {
            kamera_posy+=(kamera_desty-kamera_posy)>>kamera_move_speed;
        }
            /*
            if (kamera_destx>kamera_posx) {
                if (Math.abs(kamera_destx-kamera_posx)>SW_p2) {
                    kamera_posx+=3;
                }
                ++kamera_posx;
            }
            if (kamera_destx<kamera_posx) {
                if (Math.abs(kamera_destx-kamera_posx)>SW_p2) {
                    kamera_posx-=3;
                }
                --kamera_posx;
            }
            if (kamera_desty>kamera_posy) {
                if (Math.abs(kamera_desty-kamera_posy)>SH_p2) {
                    kamera_posy+=3;
                }
                ++kamera_posy;
            }
            if (kamera_desty<kamera_posy) {
                if (Math.abs(kamera_desty-kamera_posy)>SH_p2) {
                    kamera_posy-=3;
                }
                --kamera_posy;
            }//*/
        if (doClip) {
            clipKamera(true);
        }
        kamera_eposx = kamera_posx/TILE_MERET_36_SZER3PER4;
        kamera_eposy = kamera_posy/TILE_MERET_36;
        kamera_rposx = kamera_posx%TILE_MERET_36_SZER3PER4;
        kamera_rposy = kamera_posy%TILE_MERET_36;
        return false;
    }
    
    public boolean clipKamera(boolean postoo) {
        boolean retval=false;
        int XMIN = TILE_MERET_36_SZER3PER4-10-10;
        int YMIN = -14-10;
        //XMIN=Integer.MIN_VALUE;
        //YMIN=Integer.MIN_VALUE;
        
        int XMAX=(TILE_MERET_36>>2)+(map_meret_x)*TILE_MERET_36_SZER3PER4-300+5;
        int YMAX=(map_meret_y)*TILE_MERET_36+(TILE_MERET_36>>1)-360;
        
        if (kamera_destx<XMIN) {
            kamera_destx=XMIN;
            retval=true;
        }
        if (postoo && kamera_posx<XMIN) {
            kamera_posx=XMIN;
            retval=true;
        }
        if (XMAX<kamera_destx) {
            kamera_destx=XMAX;
            retval=true;
        }
        if (postoo && kamera_posx>XMAX) {
            kamera_posx=XMAX;
            retval=true;
        }
        if (kamera_desty<YMIN) {
            kamera_desty=YMIN;
            retval=true;
        }
        if (postoo && kamera_posy<YMIN) {
            kamera_posy=YMIN;
            retval=true;
        }
        
        if (YMAX<kamera_desty) {
            kamera_desty=YMAX;
            retval=true;
        }
        if (postoo && kamera_posy>YMAX) {
            kamera_posy=YMAX;
            retval=true;
        }
        return retval;
    }
    
    public void calc_kamera_dest(int x, int y) {
        kamera_destx=x*TILE_MERET_36_SZER3PER4+TILE_MERET_PER_2_18-SW_p2+50;
        kamera_desty=y*TILE_MERET_36+(x&1)*TILE_MERET_PER_2_18+TILE_MERET_PER_2_18-SH_p2;
    }
    
    public void update_selection() {
        if (keypressed[MY_KEY_UP]) {
            if (currentSelectionY!=0) {
                --currentSelectionY;
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_UP]=false;
        }
        if (keypressed[MY_KEY_DOWN]) {
            if (currentSelectionY!=map_meret_y-1) {
                ++currentSelectionY;
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_DOWN]=false;
        }
        if (keypressed[MY_KEY_NUM3] || (keypressed[MY_KEY_NUM6]&&(currentSelectionX&1)==1)) {
            if (currentSelectionX!=map_meret_x-1) {
                ++currentSelectionX;
                if ((currentSelectionX&1)==1 && currentSelectionY>0) {
                    --currentSelectionY;
                }
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_NUM3]=false;
            keypressed[MY_KEY_NUM6]=false;
        }
        if (keypressed[MY_KEY_NUM9] || (keypressed[MY_KEY_NUM6]&&(currentSelectionX&1)==0)) {
            if (currentSelectionX!=map_meret_x-1) {
                ++currentSelectionX;
                if ((currentSelectionX&1)==0 && currentSelectionY<map_meret_y-1) {
                    ++currentSelectionY;
                }
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_NUM9]=false;
            keypressed[MY_KEY_NUM6]=false;
        }
        if (keypressed[MY_KEY_NUM1] || (keypressed[MY_KEY_NUM4]&&(currentSelectionX&1)==1)) {
            if (currentSelectionX!=1) {
                --currentSelectionX;
                if ((currentSelectionX&1)==1 && currentSelectionY>0) {
                    --currentSelectionY;
                }
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_NUM1]=false;
            keypressed[MY_KEY_NUM4]=false;
        }
        
        if (keypressed[MY_KEY_NUM7] || (keypressed[MY_KEY_NUM4]&&(currentSelectionX&1)==0)) {
            if (currentSelectionX!=1) {
                --currentSelectionX;
                if ((currentSelectionX&1)==0 && currentSelectionY<map_meret_y-1) {
                    ++currentSelectionY;
                }
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_NUM7]=false;
            keypressed[MY_KEY_NUM4]=false;
        }
        clipKamera(true);
    }
    
    public void move_selected_unit() {
        int ofs=selectedUnit*SQUAD_H;
        int px=squads[ofs+POS_X], py=squads[ofs+POS_Y];
        boolean mehet=false;
        short direction=0;
        if (keypressed[MY_KEY_UP]) {
            if (py!=0) {
                mehet=true;
                direction=2;
            }
            keypressed[MY_KEY_UP]=false;
        }
        if (keypressed[MY_KEY_DOWN]) {
            if (py!=map_meret_y-1) {
                mehet=true;
                direction=5;
            }
            keypressed[MY_KEY_DOWN]=false;
        }
        if (keypressed[MY_KEY_NUM3] || (keypressed[MY_KEY_NUM6]&&(px&1)==1)) {
            if (px!=map_meret_x-1 && ((px&1)==1||py>0)) {
                mehet=true;
                direction=1;
            }
            keypressed[MY_KEY_NUM3]=false;
            keypressed[MY_KEY_NUM6]=false;
        }
        if (keypressed[MY_KEY_NUM9] || (keypressed[MY_KEY_NUM6]&&(px&1)==0)) {
            if (px!=map_meret_x-1 && ((px&1)==0||py<map_meret_y-1)) {
                mehet=true;
                direction=6;
            }
            keypressed[MY_KEY_NUM9]=false;
            keypressed[MY_KEY_NUM6]=false;
        }
        if (keypressed[MY_KEY_NUM1] || (keypressed[MY_KEY_NUM4]&&(px&1)==1)) {
            if (px!=1 && ((px&1)==1||py>0)) {
                mehet=true;
                direction=3;
            }
            keypressed[MY_KEY_NUM1]=false;
            keypressed[MY_KEY_NUM4]=false;
        }
        if (keypressed[MY_KEY_NUM7] || (keypressed[MY_KEY_NUM4]&&(px&1)==0)) {
            if (px!=1 && ((px&1)==0||py<map_meret_y-1)) {
                mehet=true;
                direction=4;
            }
            keypressed[MY_KEY_NUM7]=false;
            keypressed[MY_KEY_NUM4]=false;
        }
        if (mehet && squads[ofs+MOVE_DIR]==0) {
            int newx=getNextPosX(px, direction);
            int newy=getNextPosY(px, py, direction);
            if (aktLepes!=0 &&
                    path[(aktLepes-1)*PATH_H+PATH_POS_X]==newx &&
                    path[(aktLepes-1)*PATH_H+PATH_POS_Y]==newy) {
                --aktLepes;
                path[aktLepes*PATH_H+PATH_POS_X]=-1;
                path[aktLepes*PATH_H+PATH_POS_Y]=-1;
                path[aktLepes*PATH_H+PATH_DIRECTION]=-1;
                squads[ofs+MOVE_DIR]=direction;
                squads[ofs+MOVE_CNT]=MOVE_ANIM_PHASE;
            } else if (aktLepes<LEPES_SZAM-1) {
                path[aktLepes*PATH_H+PATH_POS_X]=px;
                path[aktLepes*PATH_H+PATH_POS_Y]=py;
                path[aktLepes*PATH_H+PATH_DIRECTION]=direction;
                ++aktLepes;
                squads[ofs+MOVE_DIR]=direction;
                squads[ofs+MOVE_CNT]=MOVE_ANIM_PHASE;
            }
        }
    }
    
    public void skipTurn() {
        ETIME=System.currentTimeMillis()/1000;
        WAIT_TIME=6;
        clrkey();
        scene=WAITING_FOR_END_OF_TURN_SCENE;
    }
    
    public void placeCastle(int ofs, int player, int type) {
        type=1;
//#ifdef DEBUG
//#             System.out.println("placeCastle for player "+player);
//#endif
        if (player==-1) {
            player=7;
        }
        map[ofs]=(map[ofs]&0xF)+((type&3)<<7)+((player&7)<<9);
    }
    
    public void setCastleOwner(int ofs, int player) {
        map[ofs]&=0xFFFFF1FF;
        map[ofs]|=(player&7)<<9;
    }
    
    public long StringToLong(String s) {
        return Long.parseLong(s.substring(20));
    }
    
    public void resetTime() {
        for (int i=0; i<LEPES_SZAM*PATH_H; ++i) {
            path[i]=-1;
        }
        aktLepes=0;
        roundStart=System.currentTimeMillis()/1000;
        roundEnd=roundStart+StringToLong(server_roundendtime)-StringToLong(server_servertime)-5;
        selectedUnitOrigPosX=-1;
        selectedUnitOrigPosY=-1;
        selectedUnit=-1;
        calc_kamera_dest(currentSelectionX, currentSelectionY);
    }
    
    public void extractResultAtScene(String s, int scene) {
        if (server_result==999) {
            return;
        }
        boolean readKey=true;
        server_result=-100;
        String tmp1="", tmp2="";
        for (int i=0; i<s.length()+1; ++i) {
            if (i==s.length() || (i<s.length() && (s.charAt(i)==0x0D || s.charAt(i)==0x0A)) ) {
                if (i!=s.length() && s.charAt(i)==0x0D && i+1<s.length() && s.charAt(i+1)==0x0A) {
                    continue;
                }
                if (readKey) {
                    readKey=false;
                    continue;
                }
//#ifdef DEBUG
//#                     System.out.println(tmp1+"="+tmp2);
//#endif
                if (tmp1.equals("")&&tmp2.equals("")) {
                    continue;
                }
                switch (scene) {
                    case SEND_ACTION_SCENE:
                        if (tmp1.equals("result")) {
                            server_result=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("error")) {
                            server_error=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("servertime")) {
                            server_servertime=tmp2;
                        } else if (tmp1.equals("roundendtime")) {
                            server_roundendtime=tmp2;
                        } else if (tmp1.equals("actround")) {
                            server_actround=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("missing responses")) {
                            server_missingresponses=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("hg_jatekos")) {
                            server_hg_jatekos=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg0")) {
                            server_hg_sereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg1")) {
                            server_hg_sereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg2")) {
                            server_hg_sereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_csata")) {
                            server_hg_csata=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_vedo_jatekos")) {
                            server_hg_vedo_jatekos=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_tamado_jatekos")) {
                            server_hg_tamado_jatekos=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_deffendsereg0")) {
                            server_hg_deffendsereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_deffendsereg1")) {
                            server_hg_deffendsereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_deffendsereg2")) {
                            server_hg_deffendsereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_offendsereg0")) {
                            server_hg_offendsereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_offendsereg1")) {
                            server_hg_offendsereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_offendsereg2")) {
                            server_hg_offendsereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_winner")) {
                            server_hg_winner=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_aftsereg0")) {
                            server_hg_aftsereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_aftsereg1")) {
                            server_hg_aftsereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_aftsereg2")) {
                            server_hg_aftsereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_regen")) {
                            server_hg_regen=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_regensereg0")) {
                            server_hg_regensereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_regensereg1")) {
                            server_hg_regensereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_regensereg2")) {
                            server_hg_regensereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("id")) {
                            server_id_vegeredmeny=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("points")) {
                            server_pontok_vegeredmeny=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("varak")) {
                            server_varak_vegeredmeny=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("sereg")) {
                            server_sereg_vegeredmeny=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_mozgasirany")) {
                            server_hg_mozgasirany=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_mozgasiranyhely")) {
                            server_hg_mozgasiranyhely=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("client_state")) {
                            server_client_state=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("info")) {
                            server_info=tmp2;
//#ifdef DEBUG
//#                             } else {
//#                                 System.out.println(" @SEND_ACTION_SCENE:unknown variable: "+tmp1+"="+tmp2);
//#endif
                        }
                        break;//*/
                    case LOGIN_SCENE:
                        if (tmp1.equals("result")) {
                            server_result=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("error")) {
                            server_error=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("id")) {
                            server_id=tmp2;
                        } else if (tmp1.equals("skey")) {
                            server_skey=tmp2;
                        } else if (tmp1.equals("servertime")) {
                            server_servertime=tmp2;
                        } else if (tmp1.equals("redirect")) {
                            server_redirect="http://"+tmp2+"/";
                        } else if (tmp1.equals("info")) {
                            server_info=tmp2;
                        } else if (tmp1.equals("game")) {
                            login_game=tmp2;
                        } else if (tmp1.equals("gtype")) {
                            login_gtype=tmp2;
//#ifdef DEBUG
//#                             } else {
//#                                 System.out.println(" @LOGIN_SCENE:unknown variable: "+tmp1+"="+tmp2);
//#endif
                        }
                        break;
                        
                    case SHOW_TYPE_SCENE:
                        if (tmp1.equals("result")) {
                            server_result=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("error")) {
                            server_error=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("gtypenum")) {
                            server_gtypenum=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("gtpid_0")) {
                            server_gtpid[0]=tmp2;
                        } else if (tmp1.equals("gtpid_1")) {
                            server_gtpid[1]=tmp2;
                        } else if (tmp1.equals("gtpid_2")) {
                            server_gtpid[2]=tmp2;
                        } else if (tmp1.equals("gtpid_3")) {
                            server_gtpid[3]=tmp2;
                        } else if (tmp1.equals("gtpname_0")) {
                            server_gtpname[0]=tmp2;
                        } else if (tmp1.equals("gtpname_1")) {
                            server_gtpname[1]=tmp2;
                        } else if (tmp1.equals("gtpname_2")) {
                            server_gtpname[2]=tmp2;
                        } else if (tmp1.equals("gtpname_3")) {
                            server_gtpname[3]=tmp2;
                        } else if (tmp1.equals("servertime")) {
                            server_servertime=tmp2;
                        } else if (tmp1.equals("info")) {
                            server_info=tmp2;
//#ifdef DEBUG
//#                             } else {
//#                                 System.out.println(" @SHOW_TYPE_SCENE:unknown variable: "+tmp1+"="+tmp2);
//#endif
                        }
                        break;
                        
                    case GET_REG_INFO_SCENE:
                        if (tmp1.equals("result")) {
                            server_result=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("error")) {
                            server_error=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("info")) {
                            server_info=tmp2;
//#ifdef DEBUG
//#                             } else {
//#                                 System.out.println(" @GET_REG_INFO_SCENE:unknown variable: "+tmp1+"="+tmp2);
//#endif
                        }
                        break;

                    case WAIT_FOR_GAME_SCENE:
                        if (tmp1.equals("result")) {
                            server_result=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("error")) {
                            server_error=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("servertime")) {
                            server_servertime=tmp2;
                        } else if (tmp1.equals("actgamenum")) {
                            server_actgamenum=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("hg_nodenum")) {
                            server_hg_nodenum=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("hg_cx")) {
                            server_hg_cx=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_cy")) {
                            server_hg_cy=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_hatter")) {
                            server_hg_hatter=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_epulet")) {
                            server_hg_epulet=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_e0")) {
                            server_hg_e[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_e1")) {
                            server_hg_e[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_e2")) {
                            server_hg_e[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_jatekos")) {
                            server_hg_jatekos=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg0")) {
                            server_hg_sereg[0]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg1")) {
                            server_hg_sereg[1]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sereg2")) {
                            server_hg_sereg[2]=CSVIntToArray(tmp2);
                        } else if (tmp1.equals("hg_sajatsorszam")) {
                            server_hg_sajatsorszam=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("hg_jnev0")) {
                            server_jatekosnev[0]=tmp2;
                        } else if (tmp1.equals("hg_jnev1")) {
                            server_jatekosnev[1]=tmp2;
                        } else if (tmp1.equals("hg_jnev2")) {
                            server_jatekosnev[2]=tmp2;
                        } else if (tmp1.equals("hg_jnev3")) {
                            server_jatekosnev[3]=tmp2;
                        } else if (tmp1.equals("hg_jnev4")) {
                            server_jatekosnev[4]=tmp2;
                        } else if (tmp1.equals("hg_jnev5")) {
                            server_jatekosnev[5]=tmp2;
                        } else if (tmp1.equals("hg_jnev6")) {
                            server_jatekosnev[6]=tmp2;
                        } else if (tmp1.equals("hg_jnev7")) {
                            server_jatekosnev[7]=tmp2;
                        } else if (tmp1.equals("hg_jnev8")) {
                            server_jatekosnev[8]=tmp2;
                        } else if (tmp1.equals("roundendtime")) {
                            server_roundendtime=tmp2;
                        } else if (tmp1.equals("actround")) {
                            server_actround=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("lastround")) {
                            server_lastround=Integer.parseInt(tmp2);
                        } else if (tmp1.equals("info")) {
                            server_info=tmp2;
//#ifdef DEBUG
//#                             } else {
//#                                 System.out.println(" @WAIT_FOR_GAME_SCENE:unknown variable: "+tmp1+"="+tmp2);
//#endif
                        }
                        break;
                    default:
                }
                tmp1="";
                tmp2="";
                readKey=true;
            } else {
                if (readKey) {
                    tmp1+=s.charAt(i);
                } else {
                    tmp2+=s.charAt(i);
                }
            }
        }
//#ifdef DEBUG
//#             System.out.println("extractResult finished");
//#endif
    }
    
    public void doLogout() {
        sc.sendMessage("game=hexawar&action=logout&id="+server_id+"&skey="+server_skey);
    }
    
    public void decCnt() {
        if (jatekosok_szama<=3) {
            return;
        }
        if (pre_running_cnt>0) {
            --pre_running_cnt;
        } else {
            pre_running_cnt=jatekosok_szama-1;
        }
        if (pre_running_cnt==server_hg_sajatsorszam) {
            if (pre_running_cnt>0) {
                --pre_running_cnt;
            } else {
                pre_running_cnt=jatekosok_szama-1;
            }
        }
    }
    
    public void incCnt() {
        if (jatekosok_szama<=3) {
            return;
        }
        if (pre_running_cnt!=jatekosok_szama-1) {
            ++pre_running_cnt;
        } else {
            pre_running_cnt=0;
        }
        if (pre_running_cnt==server_hg_sajatsorszam) {
            if (pre_running_cnt!=jatekosok_szama-1) {
                ++pre_running_cnt;
            } else {
                pre_running_cnt=0;
            }
        }
    }
    
    public void sendMove() {
        int sofs=selectedUnit*SQUAD_H;
        int x, y;
        String s="game=hexawar&action=move&id="+
                server_id+"&skey="+server_skey+"&gtype="+
                selectedGType+"&actgamenum="+server_actgamenum+
                "&actround="+server_actround;
        String pathx="&hg_pathx=", pathy="&hg_pathy=";
        x=selectedUnitOrigPosX;
        y=selectedUnitOrigPosY;
        s+="&hg_startx="+ClientCSToServerCSX(x);
        s+="&hg_starty="+ClientCSToServerCSY(x, y);
        x=squads[sofs+POS_X];
        y=squads[sofs+POS_Y];
        s+="&hg_celx="+ClientCSToServerCSX(x);
        s+="&hg_cely="+ClientCSToServerCSY(x, y);
        s+="&hg_katona0="+squads[sofs+BALTASOK_SZAMA_0];
        s+="&hg_katona1="+squads[sofs+KARDOSOK_SZAMA_1];
        s+="&hg_katona2="+squads[sofs+LANDZSASOK_SZAMA_2];
        if (path[PATH_POS_X]==-1) {
            x=selectedUnitOrigPosX;
            y=selectedUnitOrigPosY;
            pathx+=ClientCSToServerCSX(x);
            pathy+=ClientCSToServerCSY(x, y);
        } else {
            for (int i=0; i<LEPES_SZAM*PATH_H; i+=PATH_H) {
                if (path[i+PATH_POS_X]==-1) {
                    break;
                }
                if (i!=0) {
                    pathx+=",";
                    pathy+=",";
                }
                x=path[i+PATH_POS_X];
                y=path[i+PATH_POS_Y];
                pathx+=ClientCSToServerCSX(x);
                pathy+=ClientCSToServerCSY(x, y);
            }
            x=squads[sofs+POS_X];
            y=squads[sofs+POS_Y];
            pathx+=","+ClientCSToServerCSX(x);
            pathy+=","+ClientCSToServerCSY(x, y);
        }
        s+=pathx+pathy;
        sc.sendMessage(s);
        scene=SEND_ACTION_SCENE;
        
        currentSelectionX=x;
        currentSelectionY=y;
        calc_kamera_dest(currentSelectionX, currentSelectionY);
        selectedUnit=-1;
        canSelectUnit=-1;
    }
    
    public void startMoveSelectedUnit() {
        darabol=false;
        selectedUnit=canSelectUnit;
        selectedUnitOrigPosX=squads[selectedUnit*SQUAD_H+POS_X];
        selectedUnitOrigPosY=squads[selectedUnit*SQUAD_H+POS_Y];
    }
    
    public void calcCanSelectUnit() {
        canSelectUnit=-1;
        for (int i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
            if (squads[ofs+POS_X]==currentSelectionX
                    && squads[ofs+POS_Y]==currentSelectionY) {
                canSelectUnit=i;
                return;
            }
        }
    }
            
    public void handleUnitSelection() {
        if (selectedUnit==-1) {
            if (canSelectUnit!=-1) {
                if (squads[canSelectUnit*SQUAD_H+JATEKOS]==server_hg_sajatsorszam) {
                    startMoveSelectedUnit();
                }
            }
            keypressed[MY_KEY_FIRE]=false;
        } else {
            if (aktLepes!=0) {
                sendMove();
            } else {
                selectedUnit=-1;
            }
            keypressed[MY_KEY_FIRE]=false;
        }
    }
    
    public void game_update() {
        if (gamemenubtn==null) {
            gamemenubtn=new JButton[3+9+1];
            for (int i=0; i<3; ++i) {
                gamemenubtn[i]=new JButton();
                gamemenubtn[i].setFocusPainted(false);
                gamemenubtn[i].setFont(fontpsmall);
                gamemenubtn[i].setBounds(SW-1-90, SH-40-i*20, 90, 18);
                gamemenubtn[i].setVisible(false);
                getLayeredPane().add(gamemenubtn[i], 0);
            }
            gamemenubtn[1].setText(lng.divide[4]);
            gamemenubtn[1].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    keypressed[MY_KEY_FIRE]=true;
                }
            });
            gamemenubtn[1].addKeyListener(this);
            
            gamemenubtn[2].setText(lng.divide[5]);
            gamemenubtn[2].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    keypressed[MY_KEY_NUM0]=true;
                }
            });
            gamemenubtn[2].addKeyListener(this);
            
            gamemenubtn[0].setText(lng.MAP);
            gamemenubtn[0].addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleMiniMap();
                }
            });
            gamemenubtn[0].addKeyListener(this);
            
            for (int i=0; i<gamemenubtn.length-4; ++i) {
                gamemenubtn[i+3]=new JButton();
                gamemenubtn[i+3].setName("game_"+i );
                gamemenubtn[i+3].setFocusPainted(false);
                gamemenubtn[i+3].setIcon(new ImageIcon(img[27+i]));
                gamemenubtn[i+3].setFont(fontpsmall);
                gamemenubtn[i+3].setBounds(SW-1-30*(3-(i%3)), SH-100+(i/3)*19, 30, 19);
                gamemenubtn[i+3].addKeyListener(this);
                gamemenubtn[i+3].setVisible(false);
                gamemenubtn[i+3].addActionListener(new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        String name=((JButton)e.getSource()).getName();
                        int ofs=Integer.parseInt(name.substring(name.length()-1));
                        switch (ofs) {
                            case 1:
                                keypressed[MY_KEY_UP]=true;
                                break;
                            case 7:
                                keypressed[MY_KEY_DOWN]=true;
                                break;
                            case 4:
                                keypressed[MY_KEY_FIRE]=true;
                                break;
                            default:
                                keypressed[MY_KEY_NUM1+ofs]=true;
                                break;
                        }
                    }
                });
                getLayeredPane().add(gamemenubtn[i+3], 0);
            }
            gamemenubtn[12]=new JButton(lng.HELP);
            gamemenubtn[12].setFocusPainted(false);
            gamemenubtn[12].setFont(fontpsmall);
            gamemenubtn[12].addKeyListener(this);
            gamemenubtn[12].setBounds(SW-1-90, 70, 90, 18);
            gamemenubtn[12].addActionListener(new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if (hfrm==null) {
                            hfrm=new helpfrm(THIS);
                            hfrm.show();
                        } else {
                            hfrm.show();
                        }
                    }
                });
            gamemenubtn[12].setVisible(true);
            getLayeredPane().add(gamemenubtn[12], 0);
        }
        if ((roundEnd-System.currentTimeMillis()/1000)<0) {
            if (selectedUnit!=-1 && aktLepes!=0) {
                sendMove();
            } else {
                skipTurn();
            }
            return;
        }
        if (keypressed[MY_KEY_POUND]) {
            return;
        }
        if (selectedUnit==-1) {
            update_selection();
            calcCanSelectUnit();
        } else {
            move_selected_unit();
        }
        
        for (int ofs=0; ofs<squads.length; ofs+=SQUAD_H) {
            if (squads[ofs+JATEKOS]!=-1) {
                update_unit(ofs);
            }
        }
        if (keypressed[MY_KEY_LSOFT]) {
            if (selectedUnit!=-1) {
                if (!darabol) {
                    squads[selectedUnit*SQUAD_H+POS_X]=selectedUnitOrigPosX;
                    squads[selectedUnit*SQUAD_H+POS_Y]=selectedUnitOrigPosY;
                } else {
                    for (int i=squads.length-SQUAD_H; i<squads.length; ++i) {
                        squads[i]=-1;
                    }
                }
                currentSelectionX=selectedUnitOrigPosX;
                currentSelectionY=selectedUnitOrigPosY;
                calc_kamera_dest(currentSelectionX, currentSelectionY);
                selectedUnit=-1;
                canSelectUnit=-1;
                for (int i=0; i<PATH_H*LEPES_SZAM; ++i) {
                    path[i]=-1;
                }
                aktLepes=0;
            }
            keypressed[MY_KEY_LSOFT]=false;
        }
        if (keypressed[MY_KEY_RSOFT] && scene==RUNNING_GAME_SCENE) {
            show(gamemenubtn, false);
            show(pausebtn, true);
            scene=PAUSE_SCENE;
            keypressed[MY_KEY_RSOFT]=false;
        }
        if (keypressed[MY_KEY_NUM0]) {
            if (selectedUnit==-1 && canSelectUnit!=-1) {
                if (squads[canSelectUnit*SQUAD_H+JATEKOS]==server_hg_sajatsorszam) {
                    if (darabolEdit==null) {
                        darabolEdit=new num[3];
                        for (int i=0; i<darabolEdit.length; ++i) {
                            darabolEdit[i]=new num("", 3);
                            darabolEdit[i].setBounds(SW_p2-25-2-45, SH_p2-12+i*20-1, 30, 18);
                            darabolEdit[i].setFont(fontpsmall);
                            darabolEdit[i].setName("darabolEdit_"+i);
                            darabolEdit[i].addKeyListener(new KeyListener() {
                                public void keyPressed(KeyEvent e) {
                                    check(e);
                                }
                                public void keyReleased(KeyEvent e) {
                                    check(e);
                                }
                                public void keyTyped(KeyEvent e) {
                                    check(e);
                                }
                            });
                            getLayeredPane().add(darabolEdit[i], 0);
                        }
                    }
                    for (int i=0; i<3; ++i) {
                        darabolEdit[i].setText(String.valueOf(squads[canSelectUnit*SQUAD_H+i]>>1));
                    }
                    darabolOfs=0;
                    darabolFirst=true;
                    show(gamemenubtn, false);
                    show(darabolEdit, true);
                    scene=DIVIDE_SCENE;
                }
            }
            keypressed[MY_KEY_NUM0]=false;
        }
        if (keypressed[MY_KEY_FIRE]) {
            handleUnitSelection();
        }
        if (keypressed[MY_KEY_STAR]) {
            if (selectedUnit==-1) {
                int iter=0;
                while (true) {
                    cycleUnit-=SQUAD_H;
                    if (cycleUnit<0) {
                        cycleUnit=squads.length-SQUAD_H;
                    }
                    if (squads[cycleUnit+JATEKOS]==server_hg_sajatsorszam) {
                        currentSelectionX=squads[cycleUnit+POS_X];
                        currentSelectionY=squads[cycleUnit+POS_Y];
                        break;
                    }
                    ++iter;
                    if (iter>(squads.length/SQUAD_H)+3) {
                        break;
                    }
                }
                calc_kamera_dest(currentSelectionX, currentSelectionY);
            }
            keypressed[MY_KEY_STAR]=false;
        }
        move_kamera(true);
    }
    
    public void check(KeyEvent e) {
        String name=((JTextField)e.getSource()).getName();
        byte ofs=Byte.parseByte(name.substring(name.length()-1));
        try {
            if (Integer.parseInt(darabolEdit[ofs].getText())>squads[canSelectUnit*SQUAD_H+ofs]) {
                darabolEdit[ofs].setText(String.valueOf(squads[canSelectUnit*SQUAD_H+ofs]));
            }
        } catch (Exception ex) {
        }
    }
    
    public int setNextBattleInfo(int ofs) {
        if (server_hg_csata==null) {
//#ifdef DEBUG
//#                 System.out.println(" >>> server_hg_csata NULL");
//#endif
            return -1;
        }
        if (server_hg_vedo_jatekos==null) {
//#ifdef DEBUG
//#                 System.out.println(" >>> server_hg_vedo_jatekos NULL");
//#endif
            return -1;
        }
        if (server_hg_tamado_jatekos==null) {
//#ifdef DEBUG
//#                 System.out.println(" >>> server_hg_tamado_jatekos NULL");
//#endif
            return -1;
        }
        for (int start=ofs+1; start<server_hg_csata.length; ++start) {
            if (server_hg_vedo_jatekos[start]==server_hg_sajatsorszam
                    || server_hg_tamado_jatekos[start]==server_hg_sajatsorszam) {
                return start;
            }
        }
        return -1;
    }
    
    public void updateA1Sereg() {
        if (server_hg_winner[eofTurnBattle]==server_hg_vedo_jatekos[eofTurnBattle]) {
            limit[0]=server_hg_aftsereg[0][eofTurnBattle];
            limit[1]=server_hg_aftsereg[1][eofTurnBattle];
            limit[2]=server_hg_aftsereg[2][eofTurnBattle];
            limit[3]=0;
            limit[4]=0;
            limit[5]=0;
        } else {
            limit[0]=0;
            limit[1]=0;
            limit[2]=0;
            limit[3]=server_hg_aftsereg[0][eofTurnBattle];
            limit[4]=server_hg_aftsereg[1][eofTurnBattle];
            limit[5]=server_hg_aftsereg[2][eofTurnBattle];
        }
        for (int i=0; i<3; ++i) {
            a1sereg[i]=server_hg_deffendsereg[i][eofTurnBattle];
            a1sereg[i+3]=server_hg_offendsereg[i][eofTurnBattle];
            a1sereg[i+6]=(a1sereg[i]-limit[i])/10;
            a1sereg[i+9]=(a1sereg[i+3]-limit[i+3])/10;
            if (a1sereg[i+6]==0) {
                a1sereg[i+6]=1;
            }
            if (a1sereg[i+9]==0) {
                a1sereg[i+9]=1;
            }
        }
        String a1=""+a1sereg[0]+"/"+a1sereg[1]+"/"+a1sereg[2];
        WAIT_TIME=System.currentTimeMillis()/1000;
    }
    
    public void scrollKamera() {
        if (keypressed[MY_KEY_UP]||keypressed[MY_KEY_NUM1]||keypressed[MY_KEY_NUM3]) {
            kamera_posy-=3;
        }
        if (keypressed[MY_KEY_DOWN]||keypressed[MY_KEY_NUM7]||keypressed[MY_KEY_NUM9]) {
            kamera_posy+=3;
        }
        if (keypressed[MY_KEY_LEFT]||keypressed[MY_KEY_NUM1]||keypressed[MY_KEY_NUM7]) {
            kamera_posx-=3;
        }
        if (keypressed[MY_KEY_RIGHT]||keypressed[MY_KEY_NUM3]||keypressed[MY_KEY_NUM9]) {
            kamera_posx+=3;
        }
        clipKamera(true);
        kamera_eposx = kamera_posx/TILE_MERET_36_SZER3PER4;
        kamera_eposy = kamera_posy/TILE_MERET_36;
        kamera_rposx = kamera_posx%TILE_MERET_36_SZER3PER4;
        kamera_rposy = kamera_posy%TILE_MERET_36;
    }
    
    public void initnull() {
        DISPLAY_WAIT_TIME=-1;
        int sx=server_hg_csata[eofTurnBattle]%server_koord_map_width;
        int sy=server_hg_csata[eofTurnBattle]/server_koord_map_width;
        calc_kamera_dest(ServerCSToClientCSX(sx, sy), ServerCSToClientCSY(sy));
        clipKamera(false);
    }
    
    public void updateSquads() {
        int[] jatekos=server_hg_jatekos;
        int[] sereg0=server_hg_sereg[0];
        int[] sereg1=server_hg_sereg[1];
        int[] sereg2=server_hg_sereg[2];
        
        int squad_num=1;
        for (int i=0; i<jatekos.length; ++i) {
            if (sereg0[i]+sereg1[i]+sereg2[i]>0) {
                ++squad_num;
            }
        }
        squads=new int[squad_num*SQUAD_H];
        for (int i=0; i<squads.length; ++i) {
            squads[i]=-1;
        }
        server_sereg_vegeredmeny=new int[jatekosok_szama];
        server_varak_vegeredmeny=new int[jatekosok_szama];
        for (int i=0; i<jatekosok_szama; ++i) {
            server_sereg_vegeredmeny[i]=0;
            server_varak_vegeredmeny[i]=0;
        }
        int o;
        
        for (int i=0, seregOfs=0; i<jatekos.length; ++i) {
            if (jatekos[i]!=-1 && sereg0[i]+sereg1[i]+sereg2[i]!=0) {
                o=jatekos[i];
                squads[seregOfs+JATEKOS]=o;
                squads[seregOfs+BALTASOK_SZAMA_0]=sereg0[i];
                squads[seregOfs+KARDOSOK_SZAMA_1]=sereg1[i];
                squads[seregOfs+LANDZSASOK_SZAMA_2]=sereg2[i];
                squads[seregOfs+POS_X]=cx[i];
                squads[seregOfs+POS_Y]=cy[i];
                int ofs=cy[i]*map_meret_x+cx[i];
                if (((map[ofs]&0x180)>>7)!=0)  {
                    this.setCastleOwner(ofs, o);
                    ++server_varak_vegeredmeny[o];
                }
                squads[seregOfs+MOVE_DIR]=0;
                squads[seregOfs+MOVE_CNT]=0;
                seregOfs+=SQUAD_H;
                server_sereg_vegeredmeny[o]+=sereg0[i];
                server_sereg_vegeredmeny[o]+=sereg1[i];
                server_sereg_vegeredmeny[o]+=sereg2[i];
            }
        }
        if (server_hg_mozgasirany!=null && server_hg_mozgasiranyhely!=null) {
            server_hg_mozgasiranyhely_szin=new int[Math.min(server_hg_mozgasiranyhely.length, server_hg_mozgasirany.length)];
            for (int i=0; i<server_hg_mozgasiranyhely_szin.length; ++i) {
                if (server_hg_mozgasirany[i]==-1) { // just for sure...
                    continue;
                }
                int sx=server_hg_mozgasiranyhely[i]%server_koord_map_width;
                int sy=server_hg_mozgasiranyhely[i]/server_koord_map_width;
                int cx=this.ServerCSToClientCSX(sx, sy);
                int cy=this.ServerCSToClientCSY(sy);
                int color=0;
                for (int ofs=0; ofs<squads.length; ofs+=SQUAD_H) {
                    if (squads[ofs+JATEKOS]==-1) {
                        continue;
                    }
                    if (squads[ofs+POS_X]==cx && squads[ofs+POS_Y]==cy) {
                        color=players[squads[ofs+JATEKOS]];
                        break;
                    }
                }
                server_hg_mozgasiranyhely_szin[i]=color;
            }
        }
    }
    
    public void keyReleased(KeyEvent e) {
        switch (scene) {
            case WAITING_FOR_END_OF_TURN_SCENE:
            case RUNNING_GAME_SCENE:
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_HOME:
                    case KeyEvent.VK_NUMPAD7:
                        keypressed[MY_KEY_NUM1]=false;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                    case KeyEvent.VK_NUMPAD9:
                        keypressed[MY_KEY_NUM3]=false;
                        break;
                    case KeyEvent.VK_END:
                    case KeyEvent.VK_NUMPAD1:
                        keypressed[MY_KEY_NUM7]=false;
                        break;
                    case KeyEvent.VK_NUMPAD3:
                    case KeyEvent.VK_PAGE_DOWN:
                        keypressed[MY_KEY_NUM9]=false;
                        break;
                    case 155:
                    case KeyEvent.VK_NUMPAD0:
                        keypressed[MY_KEY_NUM0]=false;
                        break;
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                        keypressed[MY_KEY_UP]=false;
                        keypressed[MY_KEY_NUM8]=false;
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                        keypressed[MY_KEY_DOWN]=false;
                        keypressed[MY_KEY_NUM2]=false;
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                        keypressed[MY_KEY_LEFT]=false;
                        keypressed[MY_KEY_NUM4]=false;
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                        keypressed[MY_KEY_RIGHT]=false;
                        keypressed[MY_KEY_NUM6]=false;
                        break;
                    case KeyEvent.VK_NUMPAD5:
                    case 12:
                        keypressed[MY_KEY_FIRE]=false;
                        keypressed[MY_KEY_NUM5]=false;
                        break;
                }
                break;
        }
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
    public void handleMiniMap() {
        keypressed[MY_KEY_POUND]=!keypressed[MY_KEY_POUND];
        for (int i=0; i<gamemenubtn.length-1; ++i) {
            gamemenubtn[i].setVisible(!keypressed[MY_KEY_POUND]);
        }
    }
    
    boolean backgr=false;
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==67) {
            backgr=!backgr;
        }
        if (e.getKeyCode()==10||(scene>=DISPLAY_END_OF_TURN_INFO_0&&scene<=DISPLAY_END_OF_TURN_INFO_3)
        ||scene==PRE_RUNNING_GAME_SCENE_WITH_NAMES||scene==PRE_RUNNING_GAME_SCENE_WITH_NUMBERS
                || scene==PRE_RUNNING_GAME_SCENE_WITH_CLIENTS) {
            keypressed[MY_KEY_RSOFT]=true;
            return;
        }
        switch (scene) {
            case WAITING_FOR_END_OF_TURN_SCENE:
            case RUNNING_GAME_SCENE:
                switch (e.getKeyCode()) {
                    case 107:
                        handleMiniMap();
                        break;
                    case KeyEvent.VK_HOME:
                    case KeyEvent.VK_NUMPAD7:
                        keypressed[MY_KEY_NUM1]=true;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                    case KeyEvent.VK_NUMPAD9:
                        keypressed[MY_KEY_NUM3]=true;
                        break;
                    case KeyEvent.VK_END:
                    case KeyEvent.VK_NUMPAD1:
                        keypressed[MY_KEY_NUM7]=true;
                        break;
                    case KeyEvent.VK_NUMPAD3:
                    case KeyEvent.VK_PAGE_DOWN:
                        keypressed[MY_KEY_NUM9]=true;
                        break;
                    case 155:
                    case KeyEvent.VK_NUMPAD0:
                        keypressed[MY_KEY_NUM0]=true;
                        break;
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_UP:
                        keypressed[MY_KEY_UP]=true;
                        keypressed[MY_KEY_NUM8]=true;
                        break;
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_DOWN:
                        keypressed[MY_KEY_DOWN]=true;
                        keypressed[MY_KEY_NUM2]=true;
                        break;
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_LEFT:
                        keypressed[MY_KEY_LEFT]=true;
                        keypressed[MY_KEY_NUM4]=true;
                        break;
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_RIGHT:
                        keypressed[MY_KEY_RIGHT]=true;
                        keypressed[MY_KEY_NUM6]=true;
                        break;
                    case KeyEvent.VK_NUMPAD5:
                    case 12:
                        keypressed[MY_KEY_FIRE]=true;
                        keypressed[MY_KEY_NUM5]=true;
                        break;
                }
                break;
        }
    }
    
    public void mouseExited(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseClicked(MouseEvent e) {
        if (scene!=RUNNING_GAME_SCENE || keypressed[MY_KEY_POUND]) {
            return;
        }
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                if (selectedUnit==-1) {
                    mouseSelectionUpdate(e);
                    calc_kamera_dest(currentSelectionX, currentSelectionY);
                } else {
                    int xp=(squads[selectedUnit*SQUAD_H+POS_X])*TILE_MERET_36_SZER3PER4-kamera_posx+TILE_MERET_PER_2_18;
                    int yp=(squads[selectedUnit*SQUAD_H+POS_Y])*TILE_MERET_36+((squads[selectedUnit*SQUAD_H+POS_X])&1)*TILE_MERET_PER_2_18-kamera_posy+TILE_MERET_PER_2_18;
                    yp+=GUI_HEIGHT;
                    int ang=180+(int)((180.0*Math.atan2(e.getY()-yp, e.getX()-xp))/Math.PI);
                    if (ang<65) {
                        keypressed[MY_KEY_NUM1]=true;
                    } else if (ang<115) {
                        keypressed[MY_KEY_UP]=true;
                    } else if (ang<180) {
                        keypressed[MY_KEY_NUM3]=true;
                    } else if (ang<245) {
                        keypressed[MY_KEY_NUM9]=true;
                    } else if (ang<295) {
                        keypressed[MY_KEY_DOWN]=true;
                    } else {
                        keypressed[MY_KEY_NUM7]=true;
                    }
                    //System.out.println("ang="+ang+" selected Unit's pos="+xp+","+yp);
                }
                break;
            case MouseEvent.BUTTON3:
                if (selectedUnit==-1) {
                    mouseSelectionUpdate(e);
                    calc_kamera_dest(currentSelectionX, currentSelectionY);
                }
                handleUnitSelection();
                break;
        }
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    
    public void mouseSelectionUpdate(MouseEvent e) {
        int cx=((e.getX()+kamera_posx)/TILE_MERET_36_SZER3PER4);
        int cy=((e.getY()+kamera_posy-GUI_HEIGHT-((cx)&1)*TILE_MERET_PER_2_18)/TILE_MERET_36);
        cx=Math.max(1, cx);
        cy=Math.max(0, cy);
        currentSelectionX=Math.min(cx, map_meret_x-1);
        currentSelectionY=Math.min(cy, map_meret_y-1);
        CLKX=currentSelectionX*TILE_MERET_36_SZER3PER4-kamera_posx;
        CLKY=currentSelectionY*TILE_MERET_36+(currentSelectionX&1)*TILE_MERET_PER_2_18-kamera_posy+GUI_HEIGHT;
        int ofs=(e.getY()-CLKY)*36+e.getX()-CLKX;
        if (ofs>=0 && ofs<mousemap.length) {
            switch (mousemap[ofs]) {
                case 0:
                    break;
                case 1:
                    --currentSelectionX;
                    ++currentSelectionY;
                    break;
                case 2:
                    --currentSelectionX;
                    break;
            }
        }
        clipKamera(true);
        calcCanSelectUnit();
    }
    
/*int mappy[]= {
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,
  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,255,  0,  0,
255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,
255,  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,  0,  0,255,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,255,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,  0,255,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,255,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,
255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,
255,255,255,255,255,255,255,255,255,255,255,255,255,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,
  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,
255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,
  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,
  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,  0,  0,  0,255,255
};//*/
    final byte mousemap[] = {
2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,        
    };
    
    public void show(JComponent[] btns, boolean flag) {
        for (int i=0; btns!=null && i<btns.length; ++i) {
            btns[i].setVisible(flag);
        }
    }
    
    public void removeMenuBtn() {
        for (int i=0; i<menubtn.length; ++i) {
            getLayeredPane().remove(menubtn[i]);
            menubtn[i]=null;
        }
        menubtn=null;
    }
    
    public void clrkey() {
        for (int i=0; i<keypressed.length; ++i) {
            keypressed[i]=false;
        }
        
    }
    
    public void handle_key() {
        int i;
        boolean volt;
        switch (scene) {
            case REG_OK_SCENE:
                if (keypressed[MY_KEY_LSOFT]) {
                    clearLogin();
                    scene=MAIN_MENU_SCENE;
                    keypressed[MY_KEY_LSOFT]=false;
                }
                if (keypressed[MY_KEY_RSOFT]) {
                    sc.sendMessage("action=login&lang="+lng.CLIENT_LANG+"&login="
                            +loginName.getText()+"&passwd="+String.valueOf(pass[0].getPassword()));
                    scene=LOGIN_SCENE;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                break;            
            case GET_REG_INFO_SCENE:
                if (isWorking) {
                } else {
                    extractResultAtScene(serverResult, GET_REG_INFO_SCENE);
                    lastErrorStr=lng.regInfo[(server_error>=2&&server_error<=4?server_error:0)];
                    switch (server_result) {
                        case 0:
                            clearLogin();
                            scene=REG_SCENE;
                            break;
                        case 1:
                            scene=REG_OK_SCENE;
                            break;
                        case 999:
                            lastError=8;
                            scene=ERROR_SCENE;
                            break;
                        default: // todo
                            clearLogin();
                            scene=REG_SCENE;
                    }
                    checkForInfo();
                }
                break;
            
            case DIVIDE_SCENE:
                if (keypressed[MY_KEY_LSOFT]) {
                    if (squads[canSelectUnit*SQUAD_H+BALTASOK_SZAMA_0]==Integer.parseInt(darabolEdit[0].getText()) &&
                            squads[canSelectUnit*SQUAD_H+KARDOSOK_SZAMA_1]==Integer.parseInt(darabolEdit[1].getText()) &&
                            squads[canSelectUnit*SQUAD_H+LANDZSASOK_SZAMA_2]==Integer.parseInt(darabolEdit[2].getText())) {
                        startMoveSelectedUnit();
                    } else {
                        darabol=true;
                        selectedUnitOrigPosX=squads[canSelectUnit*SQUAD_H+POS_X];
                        selectedUnitOrigPosY=squads[canSelectUnit*SQUAD_H+POS_Y];
                        selectedUnit=(squads.length/SQUAD_H)-1;
                        for (i=3; i<SQUAD_H; ++i) {
                            squads[selectedUnit*SQUAD_H+i]=squads[canSelectUnit*SQUAD_H+i];
                        }
                        squads[selectedUnit*SQUAD_H+BALTASOK_SZAMA_0]=Integer.parseInt(darabolEdit[0].getText());
                        squads[selectedUnit*SQUAD_H+KARDOSOK_SZAMA_1]=Integer.parseInt(darabolEdit[1].getText());
                        squads[selectedUnit*SQUAD_H+LANDZSASOK_SZAMA_2]=Integer.parseInt(darabolEdit[2].getText());
                    }
                    keypressed[MY_KEY_LSOFT]=false;
                    show(gamemenubtn, true);
                    show(darabolEdit, false);
                    scene=RUNNING_GAME_SCENE;
                }
                if (keypressed[MY_KEY_RSOFT]) {
                    keypressed[MY_KEY_RSOFT]=false;
                    show(gamemenubtn, true);
                    show(darabolEdit, false);
                    scene=RUNNING_GAME_SCENE;
                }
/*                    if (keypressed[MY_KEY_UP]) {
                        if (darabolOfs==0) {
                            darabolOfs=2;
                        } else {
                            --darabolOfs;
                        }
                        darabolFirst=true;
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (darabolOfs==2) {
                            darabolOfs=0;
                        } else {
                            ++darabolOfs;
                        }
                        darabolFirst=true;
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    volt=false;
                    for (i=MY_KEY_NUM0; i<MY_KEY_NUM9+1; ++i) {
                        if (keypressed[i]) {
                            volt=true;
                            break;
                        }
                    }
                    if (volt) {
                        if (darabolFirst) {
                            darabolEgyseg[darabolOfs]=(short)(i-MY_KEY_NUM0);
                            darabolFirst=false;
                        } else {
                            darabolEgyseg[darabolOfs]*=10;
                            darabolEgyseg[darabolOfs]+=i-MY_KEY_NUM0;
                        }
                        if (squads[canSelectUnit*SQUAD_H+darabolOfs]<darabolEgyseg[darabolOfs]) {
                            darabolEgyseg[darabolOfs]=squads[canSelectUnit*SQUAD_H+darabolOfs];
                            darabolFirst=true;
                        }
                        if (darabolEgyseg[darabolOfs]>999) {
                            darabolFirst=true;
                        }
                        for (i=MY_KEY_NUM0; i<MY_KEY_NUM9+1; ++i) {
                            keypressed[i]=false;
                        }
                    }//*/
                break;
                
            case PAUSE_SCENE:
                if (pausebtn==null) {
                    pausebtn=new JButton[lng.pause_txt.length];
                    for (i=0; i<pausebtn.length; ++i) {
                        pausebtn[i]=new JButton();
                        pausebtn[i].setName("menubtn_"+i);
                        pausebtn[i].setText(lng.pause_txt[i]);
                        pausebtn[i].setBounds(SW_p2-55-45, SH_p2-20+i*20, 110, 18);
                        pausebtn[i].setFont(fontpsmall);
                        pausebtn[i].setFocusPainted(false);
                        pausebtn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                String name=((JButton)e.getSource()).getName();
                                menu_selected=Byte.parseByte(name.substring(name.length()-1));
                                keypressed[MY_KEY_FIRE]=true;
                            }
                        });
                        this.getLayeredPane().add(pausebtn[i], 0);
                    }
                }
                if (keypressed[MY_KEY_FIRE]) {
                    switch (menu_selected) {
                        case 0:
                            scene=RUNNING_GAME_SCENE;
                            show(gamemenubtn, true);
                            break;
                        case 1:
                            skipTurn();
                            show(gamemenubtn, false);
                            break;
                        case 2:
                            doLogout();
                            scene=JUMP_TO_MAIN_MENU_SCENE;
                            show(gamemenubtn, false);
                            break;
                    }
                    show(pausebtn, false);
                    keypressed[MY_KEY_FIRE]=false;
                }
                move_kamera(true);
                break;
            case END_OF_GAME_SCENE_1:
                if (keypressed[MY_KEY_RSOFT]) {
                    scene=END_OF_GAME_SCENE_2;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                break;
            case END_OF_GAME_SCENE_2:
                if (keypressed[MY_KEY_RSOFT]) {
                    keypressed[MY_KEY_RSOFT]=false;
                    menubtn=null;
                    typeReaded=false;
                    sc.sendMessage("game=hexawar&action=getgametypes&id="+server_id+"&skey="+server_skey);
                    scene=SHOW_TYPE_SCENE;
                }
                break;
            case DISPLAY_END_OF_TURN_INFO_0:
                if (DISPLAY_WAIT_TIME==-1) {
                    if (move_kamera(false)) {
                        DISPLAY_WAIT_TIME=System.currentTimeMillis();
                    }
                } else {
                    if (System.currentTimeMillis()-DISPLAY_WAIT_TIME>2000) {
                        WAIT_TIME=System.currentTimeMillis()/1000;
                        scene=DISPLAY_END_OF_TURN_INFO_1;
                    }
                }
                break;
                
            case DISPLAY_END_OF_TURN_INFO_1:
                if (keypressed[MY_KEY_RSOFT]) {
                    scene=DISPLAY_END_OF_TURN_INFO_3;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                if (System.currentTimeMillis()/1000-WAIT_TIME>=2) {
                    DISPLAY_WAIT_TIME=System.currentTimeMillis();
                    scene=DISPLAY_END_OF_TURN_INFO_2;
                }
                move_kamera(true);
                break;
                
            case DISPLAY_END_OF_TURN_INFO_2:
                if (keypressed[MY_KEY_RSOFT]) {
                    scene=DISPLAY_END_OF_TURN_INFO_3;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                if (System.currentTimeMillis()-DISPLAY_WAIT_TIME<50) {
                    return;
                }
                volt=false;
                for (i=0; i<6; ++i) {
                    if (a1sereg[i]>limit[i]) {
                        if (a1sereg[i]-a1sereg[i+6]>limit[i]) {
                            a1sereg[i]-=a1sereg[i+6];
                        } else {
                            a1sereg[i]=limit[i];
                        }
                        volt=true;
                    }
                }
                if (volt) {
                    DISPLAY_WAIT_TIME=System.currentTimeMillis();
                } else {
                    scene=DISPLAY_END_OF_TURN_INFO_3;
                }
                break;
                
            case DISPLAY_END_OF_TURN_INFO_3:
                System.arraycopy(limit, 0, a1sereg, 0, a1sereg.length/2);
                if (keypressed[MY_KEY_RSOFT]) {
                    if (eofTurnNextBattle!=-1) {
                        eofTurnBattle=eofTurnNextBattle;
                        eofTurnNextBattle=setNextBattleInfo(eofTurnBattle);
                        updateA1Sereg();
                        initnull();
                        scene=DISPLAY_END_OF_TURN_INFO_0;
                    } else {
                        if (gameStarted) {
                            calc_kamera_dest(currentSelectionX, currentSelectionY);
                            scene=PRE_RUNNING_GAME_SCENE_WITH_NUMBERS;
                        } else {
                            scene=END_OF_GAME_SCENE_1;
                        }
                    }
                    keypressed[MY_KEY_RSOFT]=false;
                }
                break;
            case WAITING_FOR_END_OF_TURN_SCENE:
                if (System.currentTimeMillis()/1000-ETIME>WAIT_TIME) {
                    sc.sendMessage("game=hexawar&action=askround&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType+"&actgamenum="+server_actgamenum+"&actround="+server_actround);
                    clrkey();
                    scene=WAITING_FOR_RESPONSE_SCENE;
                }
                scrollKamera();
                
/*                    for (i=0; i<csataAnimMoveOfs.length; ++i) {
                        csataAnimPosX[i]+=csataAnimMoveOfs[i];
                        if (csataAnimMoveOfs[i]>0 && csataAnimPosX[i]>csataAnimTargetDistance[i]) {
                            csataAnimTargetDistance[i]=(short)((i<6?30:90)+(rnd.nextInt()%10));
                            csataAnimMoveOfs[i]=-1;
                        }
                        if (csataAnimMoveOfs[i]<0 && csataAnimPosX[i]<csataAnimTargetDistance[i]) {
                            csataAnimMoveOfs[i]=1;
                            csataAnimTargetDistance[i]+=(short)(10+Math.abs(rnd.nextInt()%20));
                        }
                    }*/
                break;
                
            case SEND_ACTION_SCENE:
            case WAITING_FOR_RESPONSE_SCENE:
                if (isWorking) { // todo: connection timeout
                    scrollKamera();
                } else {
                    int[] oldcs=server_hg_csata;
                    int[] oldmh=server_hg_mozgasiranyhely;
                    int[] oldmi=server_hg_mozgasirany;
                    int[] oldms=server_hg_mozgasiranyhely_szin;
                    
                    server_hg_csata=null;
                    server_hg_mozgasiranyhely=null;
                    server_hg_mozgasirany=null;
                    server_hg_mozgasiranyhely_szin=null;
                    
                    extractResultAtScene(serverResult, SEND_ACTION_SCENE);
                    switch (server_result) {
                        case 0: // hiba volt
                            switch (server_error) {
                                case 0:
                                    lastError=3;
                                    break;
                                case 3:
                                    lastError=5;
                                    break;
                                case 4:
                                    lastError=6;
                                    break;
                                case 5:
                                    lastError=7;
                                    break;
                            }
                            scene=ERROR_SCENE;
                            break;
                            
                        case 1: // a kor meg nem ert veget
                            server_hg_csata=oldcs;
                            server_hg_mozgasiranyhely=oldmh;
                            server_hg_mozgasirany=oldmi;
                            server_hg_mozgasiranyhely_szin=oldms;
                            
                            ETIME=System.currentTimeMillis()/1000;
                            WAIT_TIME=Math.min(5, StringToLong(server_roundendtime)-StringToLong(server_servertime));
/*                                if (scene==SEND_ACTION_SCENE) {
                                    initCsataAnim();
                                }//*/
                            clrkey();
                            scene=WAITING_FOR_END_OF_TURN_SCENE;
                            break;
                            
                        case 2: // a kor veget ert
                            updateSquads();
                            resetTime();
                            eofTurnBattle=setNextBattleInfo(-1);
                            if (eofTurnBattle!=-1) {
                                eofTurnNextBattle=setNextBattleInfo(eofTurnBattle);
                                updateA1Sereg();
                                initnull();
                                scene=DISPLAY_END_OF_TURN_INFO_0;
                            } else {
                                scene=PRE_RUNNING_GAME_SCENE_WITH_NUMBERS;
                            }
//#ifdef DEBUG
//#                                 System.out.println(" ahahaha");
//#endif
                            break;
                            
                        case 3: // vege a jateknak
                            gameStarted=false;
                            eofTurnBattle=setNextBattleInfo(-1);
                            if (eofTurnBattle!=-1) {
                                eofTurnNextBattle=setNextBattleInfo(eofTurnBattle);
                                updateA1Sereg();
                                initnull();
                                scene=DISPLAY_END_OF_TURN_INFO_0;
                            } else {
                                scene=END_OF_GAME_SCENE_1;
                            }
                            break;
                            
                        case 999:
                            lastError=8;
                            scene=ERROR_SCENE;
                            break;
                            
                        default:
                            lastError=1;
                            scene=ERROR_SCENE;
                    }
                    checkForInfo();
                }
                break;
            case RUNNING_GAME_SCENE:
                game_update();
                break;
            case PRE_RUNNING_GAME_SCENE_WITH_NAMES:
                if (keypressed[MY_KEY_RSOFT]) {
                    scene=PRE_RUNNING_GAME_SCENE_WITH_NUMBERS;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                if (keypressed[MY_KEY_UP]) {
                    decCnt();
                    keypressed[MY_KEY_UP]=false;
                }
                if (keypressed[MY_KEY_DOWN]) {
                    incCnt();
                    keypressed[MY_KEY_DOWN]=false;
                }
                break;
                
            case PRE_RUNNING_GAME_SCENE_WITH_CLIENTS:
                if (keypressed[MY_KEY_RSOFT]) {
                    if (server_client_state!=null) {
                        for (int iii=0; iii<last_client_state.length; ++iii) {
                            last_client_state[iii]=server_client_state[iii];
                        }
                    }
                    show(gamemenubtn, true);
                    scene=RUNNING_GAME_SCENE;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                if (keypressed[MY_KEY_UP]) {
                    decCnt();
                    keypressed[MY_KEY_UP]=false;
                }
                if (keypressed[MY_KEY_DOWN]) {
                    incCnt();
                    keypressed[MY_KEY_DOWN]=false;
                }
                move_kamera(true);
                break;
                
            case PRE_RUNNING_GAME_SCENE_WITH_NUMBERS:
                if (keypressed[MY_KEY_RSOFT]) {
                    boolean voltvolt=false;
                    if (server_client_state!=null) {
                        for (int iii=0; iii<last_client_state.length; ++iii) {
                            voltvolt|=(server_client_state[iii]!=last_client_state[iii]);
                        }
                    }
                    scene=voltvolt?PRE_RUNNING_GAME_SCENE_WITH_CLIENTS:RUNNING_GAME_SCENE;
                    if (scene==RUNNING_GAME_SCENE) {
                        show(gamemenubtn, true);
                    }
                    keypressed[MY_KEY_RSOFT]=false;
                }
                move_kamera(true);
                break;
            case JUMP_TO_MAIN_MENU_SCENE:
                if (isWorking) {
                } else {
                    scene=MAIN_MENU_SCENE;
                }
                break;
            case WAIT_AND_TRY_AGAIN:
                if (System.currentTimeMillis()/1000-ETIME>5) {
                    sc.sendMessage("game=hexawar&action=waitforgametype&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType);
                    scene=WAIT_FOR_GAME_SCENE;
                } else {
                    if (keypressed[MY_KEY_LSOFT]) {
                        doLogout();
                        scene=JUMP_TO_MAIN_MENU_SCENE;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                }
                break;
                
            case WAIT_FOR_GAME_SCENE:
                if (isWorking) {
                } else {
                    extractResultAtScene(serverResult, WAIT_FOR_GAME_SCENE);
                    switch (server_result) {
                        case 0:
                            lastError=3;
                            scene=ERROR_SCENE;
                            break;
                        case 1:
                            ETIME=System.currentTimeMillis()/1000;
                            scene=WAIT_AND_TRY_AGAIN;
                            break;
                        case 2:
                            init_map();
                            resetTime();
                            selectedUnit=-1;
                            pre_running_cnt=0;
                            scene=PRE_RUNNING_GAME_SCENE_WITH_NAMES;
                            gameStarted=true;
                            break;
                        case 999:
                            lastError=8;
                            scene=ERROR_SCENE;
                            break;
                        default: // todo
                            lastError=3;
                            scene=ERROR_SCENE;
                    }
                    checkForInfo();
                }
                break;
                
            case ERROR_SCENE:
                if (keypressed[MY_KEY_LSOFT]) {
                    doLogout();
                    scene=JUMP_TO_MAIN_MENU_SCENE;
                    keypressed[MY_KEY_LSOFT]=false;
                }
                break;
            case SHOW_TYPE_SCENE:
                if (isWorking) {
                } else {
                    if (!typeReaded) {
                        extractResultAtScene(serverResult, SHOW_TYPE_SCENE);
                        switch (server_result) {
                            case 0:
                                lastError=2;
                                scene=ERROR_SCENE;
                                break;
                            case 1:
                                typeReaded=true;
                                break;
                            case 999:
                                lastError=8;
                                scene=ERROR_SCENE;
                                break;
                            default: // todo
                                lastError=3;
                                scene=ERROR_SCENE;
                        }
                        checkForInfo();
                    } else {
                        if (menubtn==null) {
                            menubtn=new JButton[server_gtypenum];
                            for (i=0; i<menubtn.length; ++i) {
                                menubtn[i]=new JButton();
                                menubtn[i].setName("menubtn_"+i);
                                menubtn[i].setText(server_gtpname[i]);
                                menubtn[i].setBounds(SW-162, SH-(menubtn.length-i+1)*20, 160, 18);
                                menubtn[i].setFont(fontpsmall);
                                menubtn[i].setFocusPainted(false);
                                menubtn[i].addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        String name=((JButton)e.getSource()).getName();
                                        menu_selected=Byte.parseByte(name.substring(name.length()-1));
                                        keypressed[MY_KEY_FIRE]=true;
                                    }
                                });
                                this.getLayeredPane().add(menubtn[i], 0);
                            }
                        }
                        if (keypressed[MY_KEY_LSOFT]) {
                            doLogout();
                            scene=JUMP_TO_MAIN_MENU_SCENE;
                            keypressed[MY_KEY_LSOFT]=false;
                            removeMenuBtn();
                        }
                        if (keypressed[MY_KEY_FIRE]) {
                            selectedGType=server_gtpid[menu_selected];
                            sc.sendMessage("game=hexawar&action=waitforgametype&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType);
                            scene=WAIT_FOR_GAME_SCENE;
                            keypressed[MY_KEY_FIRE]=false;
                            removeMenuBtn();
                        }
                    }
                }
                break;
                
            case DISPLAY_INFO_SCENE:
                if (keypressed[MY_KEY_RSOFT]) {
                    server_info_array=null;
                    server_info=null;
                    scene=oldSceneBeforeInfo;
                    keypressed[MY_KEY_RSOFT]=false;
                }
                break;
                
            case LOADING_SCENE:
                if (title==null) {
                    MediaTracker mTracker = new MediaTracker(this);
                    try {
                        title=Toolkit.getDefaultToolkit().createImage(getClass().getResource("hexatitle.png"));
                        mTracker.addImage(title, 0);
                        mTracker.checkID(0, true);
                        mTracker.waitForID(0);                        
                    } catch (Exception e) {
                        title=Idummy;
                    }
                    loadingStart=System.currentTimeMillis();
                    break;
                }
                if (loadingCnt<img.length) {
                    MediaTracker mTracker = new MediaTracker(this);
                    if (loadingCnt==10||loadingCnt==20) {
                        img[loadingCnt]=Idummy;
                        ++loadingCnt;
                    }
                    try {
                        img[loadingCnt]=Toolkit.getDefaultToolkit().createImage(getClass().getResource((loadingCnt<10?"0":"")+loadingCnt+".png"));
                        mTracker.addImage(img[loadingCnt], 0);
                        mTracker.checkID(0, true);
                        mTracker.waitForID(0);
                    } catch (Exception e) {
                        img[loadingCnt]=Idummy;
                        e.printStackTrace();
                    }
                    ++loadingCnt;
                } else {
                    if (System.currentTimeMillis()-loadingStart>5000) {
                        SQUAD_ANIM_X=img[19].getWidth(null)>>2;
                        SQUAD_ANIM_Y=img[19].getHeight(null)>>3;
                        ++scene;
                    }
                }
                break;
                
            case MAIN_MENU_SCENE:
                loginBtn.setBounds(SW_p2-60, SH_p2+40, 120, 18);
                if (keypressed[MY_KEY_LSOFT]) {
                    clearLogin();
                    lastErrorStr=lng.CREATE_ACCOUNT;
                    
                    scene=REG_SCENE;
                    keypressed[MY_KEY_LSOFT]=false;
                }
                if (keypressed[MY_KEY_RSOFT]) {
                    if (hfrm==null) {
                        hfrm=new helpfrm(THIS);
                        hfrm.show();
                    } else {
                        hfrm.show();
                    }
                    keypressed[MY_KEY_RSOFT]=false;
                }
                break;
            case REG_SCENE:
                loginBtn.setBounds(SW_p2-60, SH_p2+80, 120, 18);
                if (keypressed[MY_KEY_LSOFT]) {
                    scene=MAIN_MENU_SCENE;
                    keypressed[MY_KEY_LSOFT]=false;
                }
                break;
                
            case LOGIN_SCENE: // todo: connection timeout
                if (isWorking) {
                } else {
                    loginName.setText("");
                    pass[0].setText("");
                    extractResultAtScene(serverResult, LOGIN_SCENE);
                    switch (server_result) {
                        case 0:
                            lastError=0;
                            scene=ERROR_SCENE;
                            break;
                        case 1:
                            typeReaded=false;
                            sc.sendMessage("game=hexawar&action=getgametypes&id="+server_id+"&skey="+server_skey);
                            scene=SHOW_TYPE_SCENE;
                            break;
                        case 2: // csatlakozas a korabbi jatekhoz
                            selectedGType=login_gtype;
                            sc.sendMessage("game="+login_game+"&action=askstate&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType);
                            System.out.println(" connecting to prevoius game");
                            scene=WAIT_FOR_GAME_SCENE;
                            break;
                        case 42: // full
                            lastError=9;
                            scene=ERROR_SCENE;
                            break;
                        case 43: // redirect
                            sc.setServerURL(server_redirect);
                            sc.sendMessage("action=login&lang="+lng.CLIENT_LANG+"&login="
                                    +loginName.getText()+"&passwd="+String.valueOf(pass[0].getPassword()));
                            break;
                            
                        case 999:
                            lastError=8;
                            scene=ERROR_SCENE;
                            break;
                            
                        default: // todo
                            lastError=1;
                            scene=ERROR_SCENE;
                    }
                    checkForInfo();
                }
                break;
                
            default:
                System.out.println("unhandled scene: "+scene);       
        }
        keypressed[MY_KEY_RSOFT]=false;
        keypressed[MY_KEY_LSOFT]=false;
    }
    
    public void clearLogin() {
        loginName.setText("");
        pass[0].setText("");
        pass[1].setText("");
        mail.setText("");
    }
    
    public void checkForInfo() {
        if (server_info!=null) {
            server_info_array=tordel(server_info);
            oldSceneBeforeInfo=scene;
            infoOfs=0;
            scene=DISPLAY_INFO_SCENE;
        }
    }
    public int cntLines(String s) {
        int width=0, space=0, cnt=0, index=0;
        while ((space=s.indexOf(" ", index))!=-1) {
            int nlcnt=0;
            while (s.charAt(space-nlcnt-1)=='\n') {
                ++nlcnt;
            }
            if (nlcnt!=0) {
                int adder=g2.getFontMetrics().stringWidth(s.substring(index, space));
                if (width+adder>BRIEFING_WIDTH_IN_PIXEL) {
                    ++cnt;
                }
                cnt+=nlcnt;
                width=0;
                index=space+1;
            } else {
                int adder=g2.getFontMetrics().stringWidth(s.substring(index, space));
                if (width+adder>BRIEFING_WIDTH_IN_PIXEL) {
                    if (width==0) {
                        index=space+1;
                    }
                    ++cnt;
                    width=0;
                } else {
                    width+=adder;
                    index=space+1;
                }
            }
        }
        if (width+g2.getFontMetrics().stringWidth(s.substring(index))>BRIEFING_WIDTH_IN_PIXEL) {
            if (width==0) {
                
            } else {
                ++cnt;
            }
        } else {
        }
        ++cnt;
//#ifdef DEBUG
//#             System.out.println("@@@ cntLines returns "+cnt);
//#endif
        return cnt;
    }
    public String[] tordel(String s) {
        if (s==null) {
            return null;
        }
        String[] retval=new String[cntLines(s)];
        int width=0;
        int space=0;
        int cnt=0;
        String tordelt="";
        while ((space=s.indexOf(" "))!=-1) {
            int nlcnt=0;
            while (s.charAt(space-nlcnt-1)=='\n') {
                ++nlcnt;
            }
            if (nlcnt!=0) {
                int adder=g2.getFontMetrics().stringWidth(s.substring(0, space-nlcnt));
                if (width+adder>BRIEFING_WIDTH_IN_PIXEL) {
                    retval[cnt]=tordelt;
                    tordelt="";
                    ++cnt;
                }
                retval[cnt]=tordelt+s.substring(0, space-nlcnt);
                s=s.substring(space+1);
                ++cnt;
                --nlcnt;
                while (nlcnt!=0) {
                    retval[cnt]="";
                    ++cnt;
                    --nlcnt;
                }
                width=0;
                tordelt="";
            } else {
                int adder=g2.getFontMetrics().stringWidth(s.substring(0, space));
                if (width+adder>BRIEFING_WIDTH_IN_PIXEL) {
                    if (width==0) {
                        retval[cnt]=s.substring(0, space);
                        s=s.substring(space+1);
                    } else {
                        retval[cnt]=tordelt;
                    }
                    ++cnt;
                    width=0;
                    tordelt="";
                } else {
                    width+=adder;
                    tordelt+=s.substring(0, space+1);
                    s=s.substring(space+1);
                }
            }
        }
        if (width+g2.getFontMetrics().stringWidth(s)>BRIEFING_WIDTH_IN_PIXEL) {
            if (width==0) {
                retval[cnt]=s;
            } else {
                retval[cnt]=tordelt;
                ++cnt;
                retval[cnt]=s;
            }
        } else {
            retval[cnt]=tordelt+s;
        }
        if (cnt+1!=retval.length) {
            System.out.println(" fakk:   cnt="+cnt+"+1!=retval.length="+retval.length);
            for (int i=cnt+1; i<retval.length; ++i) {
                retval[i]="";
            }
        }
        return retval;
    }
    
    public void run() {
/*        int cnt=0;
        for (int i=0; i<mappy.length-4; i+=4, ++cnt) {
            int val=mappy[i]+(mappy[i+1]<<8)+(mappy[i+2]<<16);
            switch (val) {
                case 0:
                    System.out.print("0,");
                    break;
                case 0xFFFFFF:
                    System.out.print("1,");
                    break;
                default:
                    System.out.print("2,");
                    
            }
            if (cnt%36==35) {
                System.out.println("");
            }
        }//*/
        
        THIS=this;
        lng=new lng_eng();
        this.getContentPane().setLayout(null);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.requestFocus();
        try {
            SW=Integer.parseInt(this.getParameter("SW"));
            SH=Integer.parseInt(this.getParameter("SH"));
        } catch (Exception e) {
            SW=400;
            SH=200;
        }
        SW_p2=SW>>1;
        SH_p2=SH>>1;
        TILE_VISIBLE_X=(byte)(SW/TILE_MERET_36_SZER3PER4+1);
        TILE_VISIBLE_Y=(byte)(SH/TILE_MERET_36_SZER3PER4+1);
        BRIEFING_WIDTH_IN_PIXEL=SW-30;
        keypressed=new boolean[MY_KEY_SZAM];
        sc=new ServerConnection(this.getParameter("SERVER_URL"));
        offscr=createImage(SW, SH);
        g2=offscr.getGraphics();
        
        fontpsmall=g2.getFont().deriveFont(Font.PLAIN);
        loginName=new cup();
        loginName.setBounds(SW_p2-60, SH_p2, 120, 18);
        loginName.setText("");
        loginName.setFont(fontpsmall);
        loginName.setVisible(false);
        getLayeredPane().add(loginName, 0);
        pass=new pwd[2];
        for (int i=0; i<pass.length; ++i) {
            pass[i]=new pwd();
            pass[i].setEchoChar('*');
            pass[i].setBounds(SW_p2-60, SH_p2+20+i*20, 120, 18);
            pass[i].setText("");
            pass[i].setVisible(false);
            pass[i].setFont(fontpsmall);
            getLayeredPane().add(pass[i], 0);
        }
        mail=new JTextField();
        mail.setVisible(false);
        mail.setFont(fontpsmall);
        mail.setBounds(SW_p2-60, SH_p2+60, 120, 18);
        mail.setText("");
        mail.setVisible(false);
        getLayeredPane().add(mail, 0);
        
        loginBtn=new javax.swing.JButton();
        loginBtn.setBounds(SW_p2-60, SH_p2+80, 120, 18);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(fontpsmall);
        loginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (scene==MAIN_MENU_SCENE) {
                    if (!loginName.getText().equals("") && !String.valueOf(pass[0].getPassword()).equals("")) {
                        sc.sendMessage("action=login&lang="+lng.CLIENT_LANG+"&login="
                                +loginName.getText()+"&passwd="+String.valueOf(pass[0].getPassword()));
                        scene=LOGIN_SCENE;
                    }
                } else if (scene==REG_SCENE) {
                    if (loginName.getText().equals("")) {
                        lastErrorStr=lng.UN_NULL;
                        loginName.requestFocus();
                    } else if (String.valueOf(pass[0].getPassword()).equals("")) {
                        lastErrorStr=lng.PASS_NULL;
                        pass[0].requestFocus();
                    } else if ( !String.valueOf(pass[0].getPassword()).equals(String.valueOf(pass[1].getPassword())) ) {
                        lastErrorStr=lng.PASSWORDS_DONT_MATCH;
                        pass[0].setText("");                        
                        pass[1].setText("");
                        pass[0].requestFocus();
                    } else if (mail.getText().equals("")) {
                        lastErrorStr=lng.MAIL_NULL;
                        mail.requestFocus();
                    } else {
                        sc.sendMessage("action=register&login="+loginName.getText()
                          +"&passwd="+String.valueOf(pass[0].getPassword())+"&email="+mail.getText());
                        scene=GET_REG_INFO_SCENE;
                    }
                }
            }
        });
        loginBtn.setVisible(false);
        getLayeredPane().add(loginBtn, Integer.MAX_VALUE);
        lsk=new JButton();
        lsk.setVisible(false);
        lsk.setBounds(1, SH-20, 90, 18);
        lsk.setFocusPainted(false);
        lsk.setFont(fontpsmall);
        lsk.addKeyListener(this);
        lsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                keypressed[MY_KEY_LSOFT]=true;
            }
        });
        getLayeredPane().add(lsk, Integer.MAX_VALUE);
        rsk=new JButton();
        rsk.setBounds(SW-1-90, SH-20, 90, 18);
        rsk.setFocusPainted(false);
        rsk.setVisible(false);
        rsk.setFont(fontpsmall);
        rsk.addKeyListener(this);
        rsk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                keypressed[MY_KEY_RSOFT]=true;
            }
        });
        getLayeredPane().add(rsk, Integer.MAX_VALUE);
        
        bg=new BGPainter();
        bg.setBounds(0,0,SW,SH);        
        getLayeredPane().add(bg, Integer.MIN_VALUE);
        requestFocus();
        while (isRunning) {    
            startTime=System.currentTimeMillis();
            handle_key();
            repaint();
            ++frames;
            endTime=System.currentTimeMillis();
            if (scene!=0 && endTime-startTime<1000/20) {
                try {
                    Thread.sleep(1000/20-endTime+startTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }   
    
    Image[] img=new Image[36];
    Image offscr=null, title=null;
    long loadingStart;
    Graphics g2;
    int SQUAD_ANIM_X, SQUAD_ANIM_Y;
    Image Idummy=createImage(1,1);
    int frames=0, loadingCnt=0, BRIEFING_WIDTH_IN_PIXEL;
    int SW, SH, SW_p2, SH_p2, CLKX=Integer.MAX_VALUE, CLKY=Integer.MAX_VALUE;
    byte TILE_VISIBLE_X, TILE_VISIBLE_Y;
    byte oldSceneBeforeInfo, infoOfs, scene=0;
    boolean isRunning=true, typeReaded=false, areyousure=false;
    boolean gameStarted=false;
    int eofTurnBattle=-1, eofTurnNextBattle=-1;
    
    final static int MY_KEY_SZAM = 19;
    final static int MY_KEY_UP = 0;
    final static int MY_KEY_DOWN = 1;
    final static int MY_KEY_LEFT = 2;
    final static int MY_KEY_RIGHT = 3;
    final static int MY_KEY_FIRE = 4;
    final static int MY_KEY_LSOFT = 5;
    final static int MY_KEY_RSOFT = 6;
    final static int MY_KEY_STAR = 7;
    final static int MY_KEY_POUND = 8;
    final static int MY_KEY_NUM0 = 9;
    final static int MY_KEY_NUM1 = 10;
    final static int MY_KEY_NUM2 = 11;
    final static int MY_KEY_NUM3 = 12;
    final static int MY_KEY_NUM4 = 13;
    final static int MY_KEY_NUM5 = 14;
    final static int MY_KEY_NUM6 = 15;
    final static int MY_KEY_NUM7 = 16;
    final static int MY_KEY_NUM8 = 17;
    final static int MY_KEY_NUM9 = 18;
    boolean keypressed[];
    
    JTextField mail;
    cup loginName;
    pwd pass[];
    num darabolEdit[];
    JButton loginBtn, pausebtn[]=null, gamemenubtn[]=null, menubtn[]=null, lsk, rsk;
    BGPainter bg;
    Thread thread=null;
    long startTime, endTime;
    boolean isWorking=false;
    ServerConnection sc;
    public Font fontpsmall;
    
    final static byte LOADING_SCENE = 0;
    final static byte MAIN_MENU_SCENE = 1;
    final static byte LOGIN_SCENE = 2;
    final static byte ERROR_SCENE = 3;
    final static byte SHOW_TYPE_SCENE = 4;
    final static byte WAIT_FOR_GAME_SCENE = 5;
    final static byte DISPLAY_INFO_SCENE = 6;
    final static byte WAIT_AND_TRY_AGAIN = 7;
    final static byte JUMP_TO_MAIN_MENU_SCENE=8;
    final static byte PRE_RUNNING_GAME_SCENE_WITH_CLIENTS=9;
    final static byte RUNNING_GAME_SCENE=10;
    final static byte PRE_RUNNING_GAME_SCENE_WITH_NUMBERS=11;
    final static byte PRE_RUNNING_GAME_SCENE_WITH_NAMES=12;
    final static byte SEND_ACTION_SCENE=13;
    final static byte WAITING_FOR_END_OF_TURN_SCENE=14;
    final static byte WAITING_FOR_RESPONSE_SCENE=15;
    final static byte DISPLAY_END_OF_TURN_INFO_0=16; // \
    final static byte DISPLAY_END_OF_TURN_INFO_1=17; // |
    final static byte DISPLAY_END_OF_TURN_INFO_2=18; // | legyenek egymas utan
    final static byte DISPLAY_END_OF_TURN_INFO_3=19; // / 
    final static byte END_OF_GAME_SCENE_1=20;
    final static byte END_OF_GAME_SCENE_2=21;
    final static byte PAUSE_SCENE=22;
    final static byte DIVIDE_SCENE=23;
    final static byte REG_SCENE=24;
    final static byte GET_REG_INFO_SCENE=25;
    final static byte REG_OK_SCENE=26;
   
    long ETIME, WAIT_TIME, DISPLAY_WAIT_TIME;
    int pre_running_cnt=0;
    byte lastError, menu_selected;
    int server_result, server_error, server_gtypenum, server_missingresponses;
    String selectedGType;
    int server_hg_csata[];
    int server_hg_regen[];
    int server_hg_vedo_jatekos[];
    int server_hg_tamado_jatekos[];
    int server_hg_winner[];
    int server_actgamenum, server_actround, server_hg_nodenum, server_lastround;
    String server_url, server_id, server_skey, server_servertime;
    String server_redirect;
    String serverResult, server_roundendtime;
    String server_gtpid[] = new String[4];
    String server_gtpname[] = new String[4];
    String server_jatekosnev[]= new String[9];
    String server_info=null, server_info_array[]=null;
    String login_game="";
    String login_gtype="";
    int server_hg_cx[];
    int server_hg_cy[];
    int server_hg_hatter[];
    int server_hg_epulet[];
    int server_hg_jatekos[];
    int jatekosok_szama, leghosszabb_nevu_jatekos;
    int server_hg_sajatsorszam;
    int server_hg_e[][] = new int[3][];
    int server_hg_sereg[][]=new int[3][];
    int server_hg_deffendsereg[][] = new int[3][];
    int server_hg_offendsereg[][] = new int[3][];
    int server_hg_aftsereg[][] = new int[3][];
    int server_hg_regensereg[][] = new int[3][];
    int server_id_vegeredmeny[];
    int server_varak_vegeredmeny[];
    int server_pontok_vegeredmeny[];
    int server_sereg_vegeredmeny[];
    int server_hg_mozgasirany[]=null;
    int server_hg_mozgasiranyhely[]=null;
    int server_hg_mozgasiranyhely_szin[]=null;
    int server_koord_map_width=-1; // szerver szerint a palya merete: w=max(x)+1
    int server_client_state[], last_client_state[];
    
    int selectedUnitOrigPosX=-1, selectedUnitOrigPosY=-1;
    long roundStart, roundEnd;
    boolean darabol=false; // osztotta-e az egyseget
    short origSquadOfs=-1; // ha darabol, akkor mi volt az eredeti egyseg
//    int darabolEgyseg[] = {0, 0, 0}; // ennyi egyseg lesz az uj seregben
    short darabolOfs=0; // melyik sorban all
    boolean darabolFirst=true; // elso szamjegyet irja be a sorba
    
    final static int LEPES_SZAM = 10;
    final byte PATH_H = 3;
    final byte PATH_POS_X = 0;
    final byte PATH_POS_Y = 1;
    final byte PATH_DIRECTION = 2;
    int path[] = new int[LEPES_SZAM*PATH_H];
    //short csataAnimPosX[], csataAnimPosY[]; // csataanimaciokor a manusok pozicioja
    //byte csataAnimTipus[]; // csataanimaciokor a manusok tipusa (baltas, stb.)
    //byte csataAnimSzin1, csataAnimSzin2; // csataanimaciokor a ket ellenfel szine
    //byte csataAnimMoveOfs[]; // milyen iranyban mozogjanak
    //short csataAnimTargetDistance[]; // milyen messze tavolodjanak el a kiindulo poziciojuktol
    int aktLepes;
    int cx[], cy[];
    int map_meret_x=16, map_meret_y=16, kamera_posx=0, kamera_posy=0;
    int kamera_eposx=0, kamera_eposy=0, kamera_rposx=0, kamera_rposy=0;
    int kamera_destx, kamera_desty;
    int map[];
    int squads[];
    int a1sereg[]=new int[12];
    int limit[]=new int[6];
    Random rand=new Random(System.currentTimeMillis());
    helpfrm hfrm=null;
    
    int currentSelectionX=0, currentSelectionY=0;
    int selectedUnit=-1, canSelectUnit=-1;
    int cycleUnit=-1;
    String[] h2pArray=null, rulesArray=null;
    HexaWarApplet THIS;
    lng_eng lng=null;
    String lastErrorStr="";
    
    private class ServerConnection implements Runnable {
        String server_url;
        String query;
        java.util.Hashtable ht;
        
        public void setServerURL(String url) {
            server_url=url;
        }
        
        public ServerConnection(String url) {
            server_url = url;
            ht=new java.util.Hashtable();
        }
        
        public void sendMessage(String s) {
            if (isWorking) {
                return;
            }
            isWorking=true;
            ht.clear();
            int startOfs1=0, startOfs2=0, endOfs1=0;
            boolean elso=true;
            for (int i=0; i<s.length(); ++i) {
                if (elso && s.charAt(i)=='=') {
                    endOfs1=i;
                    startOfs2=i+1;
                    elso=false;
                } else if (!elso && s.charAt(i)=='&') {
//#ifdef DEBUG
//#                         System.out.println(" kulcs="+s.substring(startOfs1, endOfs1));
//#                         System.out.println(" ertek="+s.substring(startOfs2, i));
//#endif
                    ht.put(s.substring(startOfs1, endOfs1), s.substring(startOfs2, i));
                    elso=true;
                    startOfs1=i+1;
                }
            }
//#ifdef DEBUG
//#                 System.out.println(" kulcs="+s.substring(startOfs1, endOfs1));
//#                 System.out.println(" ertek="+s.substring(startOfs2, s.length()));
//#endif
            ht.put(s.substring(startOfs1, endOfs1), s.substring(startOfs2, s.length()));
            query=TransferCode.textmap2msgstr(ht).toString();
//#ifdef DEBUG
//#                 System.out.println(">>> "+server_url+"?"+s);
//#                 System.out.println(">>> "+server_url+"?"+query);
//#endif
            (new Thread(this)).start();
        }
        
        public void run() {
            StringBuffer respBuff = new StringBuffer();
            server_url = "http://92.43.201.42/mobil";
            String url = server_url+"?P="+query;
//#ifdef DEBUG
//#                 System.out.println(" final req is "+url);
//#endif
            
            InputStream instr=null;
            InputStreamReader inReader=null;
            
            boolean vege=false;
            int reconnect=5;
            while (!vege) {
                --reconnect;
                try {
                    java.net.URL conn=new java.net.URL(url);
                    instr=conn.openStream();
                    inReader = new InputStreamReader(instr);
                    int ch;
                    while ((ch=inReader.read())!=-1) {
                        respBuff.append((char)ch);
                    }
                    vege=true;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (reconnect==0) {
                        server_result=999;
                        vege=true;
                    } else {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception ee) { }
                    }
                }
            }
            try {
                if (instr!=null) {
                    instr.close();
                }
            } catch (IOException e) { }
            System.out.println("RESPONSE: ["+ respBuff.toString()+"]");
            ht.clear();
            if (server_result==999) {
                isWorking=false;
                return;
            } //*/
            int res=TransferCode.msgstr2textmap(respBuff.deleteCharAt(respBuff.length()-1).toString(), ht);
            if (res!=0) {
                ht.clear();
                server_result=999;
                isWorking=false;
                return;
            }
//#ifdef DEBUG
//#                     System.out.println("ht.size="+ht.size()+" res="+res);
//#endif
            respBuff=new StringBuffer();
            for (java.util.Enumeration e1=ht.keys(), e2=ht.elements(); e1.hasMoreElements(); ) {
                respBuff.append(e1.nextElement());
                respBuff.append("\n");
                respBuff.append(e2.nextElement());
                if (e1.hasMoreElements()) {
                    respBuff.append("\n");
                }
            }
            serverResult=respBuff.toString();
//#ifdef DEBUG
//                     System.out.println(serverResult);
//                     System.out.println("------------------");
//#endif
            isWorking=false;
            
        }
    }
    
    public int[] CSVIntToArray(String s) {
        if (s==null || s.equals("")) {
            return null;
        }
        int num=0, v;
        for (int i=0; i<s.length(); ++i) {
            if (s.charAt(i)==','){
                ++num;
            }
        }
        int[] retval=new int[num+1];
        num=0;
        while ((v=s.indexOf(","))!=-1) {
            retval[num]=Integer.parseInt(s.substring(0, v));
            s=s.substring(v+1);
            ++num;
        }
        retval[num]=Integer.parseInt(s);
        return retval;
    }
}
