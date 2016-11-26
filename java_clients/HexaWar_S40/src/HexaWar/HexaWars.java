/*
 MobileConquest project S40
 Code: Peter Osze
 */
package HexaWars;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.Connector;
import com.nokia.mid.ui.*;
import javax.microedition.io.ContentConnection;

////#define DEBUG

public class HexaWars extends MIDlet {
    Display disp;
    boolean isWorking;
    
    public HexaWars() {
        disp=Display.getDisplay(this);
        GameCanvas canv=new GameCanvas();
        Thread thr=new Thread(canv);
        disp.setCurrent(canv);
        thr.start();
    }
    
    protected void startApp() {
    }
    
    protected void pauseApp() {
    }
    
    protected void destroyApp(boolean unconditional) {
        disp.setCurrent(null);
        notifyDestroyed();
    }
    public static byte[] loadResource(String name) {
        try {
            if (name == null)
                return null;
            InputStream res = Runtime.getRuntime().getClass().getResourceAsStream(name);
            if (res == null)
                return null;
            int flen = (int) res.skip(32768);
            res.close();
            if (flen < 1)
                return null;
            res = Runtime.getRuntime().getClass().getResourceAsStream(name);
            byte buffer[] = new byte[flen];
            int bytes = 0;
            int offset = 0;
            while (true) {
                bytes = res.read(buffer, offset, buffer.length - offset);
                offset += bytes;
                if (bytes == -1 || offset >= buffer.length)
                    break;
            }
            res.close();
            return buffer;
        } catch (Exception e) {
            return null;
        }
    }
    
    class GameCanvas extends FullCanvas implements Runnable {
        final int SW=128;
        final int SH=128;
        final int SW_p2=SW>>1;
        final int SH_p2=SH>>1;
        final static int GUI_HEIGHT = 12;
        int SLEEP = 10;
        int UPDATE_DIV = 1;
        int update=0;
        
        final static int KEY_UP=-1;
        final static int KEY_DOWN=-2;
        final static int KEY_LEFT=-3;
        final static int KEY_RIGHT=-4;
        final static int KEY_FIRE=-5;
        final static int KEY_LSOFT = -6;
        final static int KEY_RSOFT = -7;
        final static int KEY_STAR = 42;
        
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
        
        final static byte OBH_SCENE = 0;
        final static byte LOGIN_SCENE = 1;
        final static byte ERROR_SCENE = 2;
        final static byte SHOW_TYPE_SCENE = 3;
        final static byte WAIT_FOR_GAME_SCENE = 4;
        final static byte WAIT_AND_TRY_AGAIN = 5;
        final static byte PAUSE_SCENE = 6;
        final static byte LOGO_SCENE = 10;
        final static byte MAIN_MENU_SCENE = 11;
        final static byte RUNNING_GAME_SCENE=13;
        final static byte SEND_ACTION_SCENE=14;
        final static byte WAITING_FOR_END_OF_TURN_SCENE=15;
        final static byte WAITING_FOR_RESPONSE_SCENE=16;
        final static byte DIVIDE_SCENE=17;
        final static byte ACCOUNT_SCENE=18;
        final static byte INPUT_ACCOUNT_NAME=19;
        final static byte INPUT_ACCOUNT_PASS=20;
        final static byte INPUT_ACCOUNT_MAIL=21;
        final static byte ACCOUNT_CHANGED=22;
        final static byte ACCOUNT_SUBMENU_SCENE=23;
        final static byte GET_REG_INFO_SCENE=24;
        final static byte REG_ERROR_SCENE=25;
        final static byte REG_OK_SCENE=26;
        final static byte DISPLAY_END_OF_TURN_INFO_1=27;
        final static byte DISPLAY_END_OF_TURN_INFO_2=28;
        final static byte DISPLAY_END_OF_TURN_INFO_3=29;
        final static byte END_OF_GAME_SCENE_1=30;
        final static byte PRE_RUNNING_GAME_SCENE_WITH_NAMES=31;
        final static byte PRE_RUNNING_GAME_SCENE_WITH_NUMBERS=32;
        final static byte SPEED_CALIB_SCENE=33;
        final static byte DISPLAY_INFO_SCENE=34;
        final static byte LOGOUT_SCENE=35;
        final static byte JUMP_TO_MAIN_MENU_SCENE=36;
        final static byte DISPLAY_END_OF_TURN_INFO_0=37;
        final static byte END_OF_GAME_SCENE_2=38;
        final static byte PRE_RUNNING_GAME_SCENE_WITH_CLIENTS=39;
        final static byte RULES_SCENE=40;
        final static byte HOWTOPLAY_SCENE=41;
        
        final static String RSNAME="hexawar_save";
        
        final static byte IMG_NUM = 21;
        final static byte TILE_VISIBLE_X = 6;
        final static byte TILE_VISIBLE_Y = 5;
        
        final static byte BALTAS = 0;
        final static byte KARDOS = 1;
        final static byte LANDZSAS = 2;
        
        final Font fontpsmall = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        final Font fontpmedium = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        
        int map[];
        int squads[];
        int a1sereg[]=new int[12];
        int limit[]=new int[6];
        
        byte SQUAD_H = 8;
        byte BALTASOK_SZAMA_0 = 0;
        byte KARDOSOK_SZAMA_1 = 1;
        byte LANDZSASOK_SZAMA_2 = 2;
        byte POS_X = 3;
        byte POS_Y = 4;
        byte JATEKOS = 5;
        byte MOVE_DIR = 6;
        byte MOVE_CNT = 7;
        
        Image Idummy = Image.createImage(1,1), img[];
        Image obh;
        long ETIME, WAIT_TIME, DISPLAY_WAIT_TIME;
        int updates=0, frame, pre_running_cnt=0;
        byte scene=OBH_SCENE, menu_selected, oldSceneBeforeInfo, infoOfs;
        
        final static byte INFO_LINES=7; // ennyi sort ir ki a tordelt infoszovegbol egy kepernyore
        
        int map_meret_x=16, map_meret_y=16, kamera_posx=0, kamera_posy=0;
        int kamera_eposx=0, kamera_eposy=0, kamera_rposx=0, kamera_rposy=0;
        int kamera_destx, kamera_desty;
        Random rnd;
        Graphics g2;
        
        final static byte EZUST = 0;
        final static byte KEK = 1;
        final static byte LILA = 2;
        final static byte NARANCS = 3;
        final static byte PIROS = 4;
        final static byte SARGA = 5;
        final static byte SZURKE = 6;
        final static byte ZOLD = 7;
        byte players[] = {EZUST, SZURKE, PIROS, LILA, NARANCS, ZOLD, KEK, SARGA };
        final byte castleSizeX[] = {21, 20, 21};
        final byte castleSizeY[] = {21, 18, 22};
        final byte castleSizeX_p2[] = {10, 10, 10};
        final byte castleSizeY_p2[] = {9, 10, 11};
        int currentSelectionX=0, currentSelectionY=0;
        int selectedUnit=-1, canSelectUnit=-1;
        int cycleUnit=-1;
//        final int[] anim_phase_ember = {0*16, 1*16, 2*16, 1*16};
        final int[] anim_phase_had = {0*14, 1*14, 2*14, 3*14};
        final byte[] move_offset_x = { 27,   0, -27,-27,  0, 27};
        final byte[] move_offset_y = {-18, -38, -18, 18, 38, 18};
        final static byte MOVE_ANIM_PHASE = 4;
        final static byte TILE_MERET_36 = 36;
        final static byte TILE_MERET_36_SZER3PER4 = (TILE_MERET_36*3/4);
        final static byte TILE_MERET_PER_2_18 = 18;
        boolean isRunning=true, typeReaded=false, areyousure=false;
        final static byte DISPLAY_HELP_LINES = 8;
        String[] h2pArray=null, rulesArray=null;
        
        ServerConnection sc;

///#define ENG
//#define HUN
        
//#ifdef ENG
//#         final static String UNDO_LABEL = "Undo";
//#         final static String MISSING_RESPONSES_STRING_1 = "Waiting for ";
//#         final static String MISSING_RESPONSES_STRING_2 = " Player(s)";
//#         final static String CLIENT_LANG = "EN";
//#         String endofgame[]={"Game Over", "The winner is", "You placed ", // 0 1 2
//#         "1st!", "2nd!", "3rd!", "4th!", "5th!", "6th!", "7th!", "8th!"}; // 3 4 5 6 7 8 9 10
//#         String menu_txt_1[] ={"Account settings", "Login", "Rules", "How to play", "Exit"};
//#         String other_txt[] = {"Connecting", "Loading types", // 01
//#         "Back",	"Connecting", // 23
//#         "Game Menu", "Sending your move", // 45
//#         "Querying opponents move", "Next", // 67
//#         "The selected account", "will be removed.", //89
//#         "Are you sure?", "Registering" }; // 10 11
//#         String startTxt[] = {"The game", "has been started!",
//#         "Number of turns: ", "Turn nr. "};
//#         String eofTurnInfo[] = { "Next", "Continue", "Skip"};
//#         String attack[]= { "You have attacked", "the enemy!",
//#         "The enemy has attacked", "your army",
//#         "You have lost ", "You have won ",
//#         "the battle!",};
//#         String divide[] = {"Divide army", " of ", "Divide", "Cancel",
//#         "5: Move   0: Divide"};
//#         String account[] = {"New Account", "Name: ", "Password: ", "E-Mail: ",
//#         "Selected account: "};
//#         String accountMod[] = {"Select", "Modify", "Delete", "Register"};
//#         String accountChanged[] = {"Your account", "has been stored.", "Do you want",
//#         "to register it?"};
//#         String regInfo[][] = { {"Registration", "failed."},
//#         { "Registration", "completed!", "Do you want", "to log in?"},
//#         { "Registration", "failed.", "Invalid password."},
//#         { "Registration", "failed.", "Invalid username."},
//#         { "Registration", "failed.", "Username is", "already in use."},
//#         };
//#         String yesno[] = {"Yes", "No"};
//#         String pause_txt[] = { "Continue", "Skip Turn", "End Game", "Exit" };
//#         String error_txt[][] = { {"Login failed!", "Bad username", "or password"}, // 0
//#         {"Unknown error"}, // 1
//#         {"Reading gametypes","failed!"}, // 2
//#         {"Invalid game!", ""}, // 3
//#         {"Login successful,", "waiting for opponents."}, // 4
//#         {"Invalid gametype!", ""}, // 5
//#         {"Invalid round.", "(Connection", "timeout?)"}, // 6
//#         {"", "Invalid move", "(This is a bug)"}, // 7
//#         {"Unable to connect", "to the network."}, // 8
//#         {"Server is full.", "Please try again", "later..."}, // 9
//#         };
//#         
//#         String calib[]={"< Sleep >", "< AnimUpdate >"};
//#         
//#         String howtoplayTxt = "How to play\n\n " +
//#                 "A GPRS connection is needed to play the game. "+
//#                 "Before the beginning of a game, a user must "+
//#                 "be registered with a login name, password and "+
//#                 "email address. Then you can login with the user. "+
//#                 "If the login was successful, you get the "+
//#                 "list of the types of games you can choose from.\n\n "+
//#                 "Movement: You can move on the map with the "+
//#                 "cursor buttons or the 1 2 3 4 6 7 8 9 buttons. "+
//#                 "You can select an army with the 5 button. After "+
//#                 "selecting the army, you can move it with the "+
//#                 "cursor buttons or the 1 2 3 4 6 7 8 9 buttons "+
//#                 "to the destination, ending the movement "+
//#                 "with the 5 button.\n\n You get the result of "+
//#                 "your movement at the end of the turn. You may "+
//#                 "move part of your army. You can divide your army "+
//#                 "with the 0 button. You can divide a moving army "+
//#                 "too, in this case the original army continues "+
//#                 "its movement, the detached army goes towards the new "+
//#                 "destination. Only a part of the map can be seen during the "+
//#                 "game, you can check a mini-map showing the whole area by "+
//#                 "pressing \"#\".";
//#         
//#         String rulesTxt = "Rules\n\n Hexawars is a turn-based "+
//#                 "multiplayer strategy game.\n\n The goal of the game is to "+
//#                 "occupy the forts with your armies. At the beginning every "+
//#                 "player has the same number of forts, with an army in each. "+
//#                 "There are empty forts on the rest of the game area.\n\n "+
//#                 "Every turn lasts a limited time. The players make their "+
//#                 "move simultaneously. You can determine the movement of your armies "+
//#                 "for more turns in advance.\n\n You can occupy a fort with your"+
//#                 "army if you stop on it or pass trough it. If the fort was empty, "+
//#                 "it will be yours without a battle.\n\n If an army meets an enemy "+
//#                 "army, the battle takes place automatically.\n\n The players get "+
//#                 "reinforcements every turn automatically in the occupied forts.\n\n "+
//#                 "The game ends if one of the players occupies all of the forts, or the last "+
//#                 "turn is over. In this case, the player with the most occupied forts wins, if "+
//#                 "the number of occupied forts is equal, the player with more soldiers wins.";
//#         final String CLS[]={"joined", "", "vanquished", "Connection lost with"};
//#endif
        
        
//#ifdef HUN
        final static String UNDO_LABEL = "Visszavon";
        final static String MISSING_RESPONSES_STRING_1 = "Várakozás ";
        final static String MISSING_RESPONSES_STRING_2 = " játékosra.";
        final static String CLIENT_LANG = "HU";
        String endofgame[]={"Vége a játéknak!", "A gyõztes", "Helyezésed: ", // 0 1 2
        "elsõ!", "második!", "harmadik!", "negyedik!", "ötödik!", "hatodik!", "hetedik!", "nyolcadik!"}; // 3 4 5 6 7 8 9 10
        String menu_txt_1[] ={"Fiók beállítások", "Belépés", "Szabályok", "Hogyan játsz", "Kilépés"};
        String other_txt[] = {"Csatlakozás", "Típusok lekérdezése", // 01
        "Vissza",	"Csatlakozás", // 23
        "Menü", "Lépés küldése", // 45
        "Ellenfelek lépései", "Következõ", // 67
        "A kiválasztott fiókot", "el fogom távolítani.", //89
        "Biztos vagy benne?", "Regisztrálás" }; // 10 11
        String startTxt[] = {"A játék", "elkezdõdött!",
        "Körök száma: ", "Aktuális kör: "};
        String eofTurnInfo[] = { "Következõ", "Tovább", "Kihagy"};
        String attack[]= { "Megtámadtad az", "ellenséget!",
        "Az ellenség megtámadta", "a seregedet",
        "Elvesztetted ", "Megnyerted ",
        "a csatát!",};
        String divide[] = {"Sereg darabolása", " / ", "Darabol", "Mégse",
        "5: Mozgat   0: Darabol"};
        String account[] = {"Új fiók", "Név: ", "Jelszó: ", "E-Mail: ",
        "Kiválasztott fiók: "};
        String accountMod[] = {"Kiválaszt", "Módosít", "Töröl", "Regisztrál"};
        String accountChanged[] = {"A fiókot", "eltároltam.", "Regisztrálni is",
        "szeretnéd?"};
        String regInfo[][] = { {"A regisztráció", "sikertelen volt."},
        { "A regisztráció", "sikerült!", "Szeretnél ezzel a", "fiókkal belépni?"},
        { "A regisztráció", "sikertelen volt.", "Hibás a jelszó."},
        { "A regisztráció", "sikertelen volt.", "Hibás a felhasználónév."},
        { "A regisztráció", "sikertelen volt.", "A felhasználónév", "már foglalt."},
        };
        String yesno[] = {"Igen", "Nem"};
        String pause_txt[] = { "Tovább", "Kör kihagyása", "Játék vége", "Kilépés" };
        String error_txt[][] = { {"A belépés sikertelen!", "Hibás a jelszó vagy", "a felhasználónév."}, // 0
        {"Ismeretlen hiba."}, // 1
        {"A játéktípusok","lekérése sikertelen!"}, // 2
        {"Érvénytelen játék!", ""}, // 3                                       
        {"A belépés sikerült,", "várok az ellenfelekre."}, // 4
        {"Hibás játéktípus!", ""}, // 5
        {"Hibás kör.", "(Connection", "timeout?)"}, // 6
        {"", "Hibás lépés", "(Ez asszem bug)"}, // 7
        {"Nem tudok csatlakozni", "a hálózathoz."}, // 8
        {"A szerver foglalt.", "Kérlek próbálkozz", " egy kicsit késõbb."}, // 9
        };
         
        String calib[]={"< Sleep >", "< AnimUpdate >"};
         
        String howtoplayTxt = "Hogyan játsz\n\n "+
        "A játékhoz GPRS kapcsolat szükséges. A játék megkezdése "+
        "elõtt regisztrálni kell egy felhasználót a név, jelszó és "+
        "egy e-mail cím megadásával.\n\n Ezután a regisztrált felhasználóval be kell "+
        "jelentkezni a játékba. Sikeres bejelentkezés esetén a játékos megkapja "+
        "az épp játszható játéktípusok listáját, amibõl választhat.\n\n Mozgás: a térképen "+
        "mozogni az akció billentyûkkel illetve az 1 2 3 4 6 7 8 9 gombokkal lehet. "+
        "Sereget kiválasztani az 5-ös gombbal lehet. Kiválasztás után a sereget a mozgás "+
        "billentyûkkel lehet irányítani a kívánt célhoz. A mozgás befejezését az 5-ös "+
        "gomb lenyomásával lehet jelezni.\n A játékos a lépés eredményérõl a kör végén"+
        "üzenetben értesül.\n\n Lehetõség van a sereg egy részének mozgatására is. "+
        "A sereget a \"0\"-ás gombbal lehet feldarabolni. Mozgó seregrõl is leválasztható "+
        "csapat, ekkor a leválasztott katonák az új cél felé mozognak, míg a maradék az "+
        "eredeti cél felé folytatja útját.\n\n A játék során a térképnek csak egy kis része "+
        "látszik. A \"#\" lenyomásával elõjön egy mini térkép, ami az egész területet mutatja.";
         
        String rulesTxt = "Szabályok\n\n A HexaWars egy körökre osztott multiplayer "+
            "stratégiai játék.\n\n A játék célja erõdök elfoglalása a seregeiddel. "+
            "Kezdetben mindenkinek azonos számú erõdje van, az erõdökben egy-egy "+
            "sereggel.\n A játéktér többi részén üres erõdök találhatók.\n\n "+
            "Minden kör meghatározott ideig tart. A játékosok minden körben egyszerre "+
            "mozoghatnak. Egy paranccsal egy seregnek több környi mozgását is meg lehet "+
            "elõre adni.\n\n Erõdöt elfoglalni az erõdön saját sereggel való megállással, "+
            "illetve áthaladással lehet. Ha az erõd üres, akkor csata nélkül az új "+
            "tulajdonos birtokába kerül.\n\n Ha egy sereg ellenfél seregével találkozik, "+
            "akkor automatikusan lezajlik a csata.\n\n Az utánpótlást az elfoglalt "+
            "erõdökben körönként toborzott új katonák jelentik.\n\n "+
            "A játéknak vége, ha az egyik játékos az összes erõdöt elfoglalta vagy "+
            "ha lejárt az utolsó kör. Ha lejárt az utolsó kör, akkor az gyõz, akinek több "+
            "elfoglalt erõdje van, ha az erõdök száma azonos, akkor a több katonával "+
            "rendelkezõ nyer.";
        final String CLS[]={"csatlakozott", "", "legyõzték", "Nem válaszolt"};
//#endif
        
        byte lastError;
        byte speedCalibCnt=0, speedCalibOfs=0;
        boolean speedCalibDir, setSleep=true;
        
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
        int darabolEgyseg[] = {0, 0, 0}; // ennyi egyseg lesz az uj seregben
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
        int aktLepes, a1seregStartOfs;
        int cx[], cy[];
        
        short accountNum=0; // hany account van jelenleg
        StringBuffer newLogin[]=new StringBuffer[3]; // new account eseten ide kerulnek a regisztralando cuccok
        String loginName[]=new String[3]; // 3 account adatai
        String pwd[]=new String[3]; // 3 account adatai
        String email[]=new String[3]; // 3 account adatai
        final static String karakterek="ABCDEFGHIJKLMNOPQRSTUVWXYZ_.@0123456789<";
        final static byte CHAR_PER_LINE = 7;
        short cursorPos=0; // piros negyzet pozicioja
        int accountOfs=0; // account modositasanal hanyadikat valasztotta
        boolean newAccount=false; // uj accountot hoz-e letre
        int defaultAccount=-1; // melyik a kivalasztott account amit a loginnal hasznal
        boolean gameStarted=false;
        
        int eofTurnBattle=-1, eofTurnNextBattle=-1;
        
        public long StringToLong(String s) {
        /*Calendar c=Calendar.getInstance();
                c.set(Calendar.YEAR, getYear(s));
                c.set(Calendar.MONTH, getMonth(s)-1);
                c.set(Calendar.DAY_OF_MONTH, getDay(s));
                c.set(Calendar.HOUR, getHour(s));
                c.set(Calendar.MINUTE, getMin(s));
        c.set(Calendar.SECOND, getSec(s));
        return c.getTime().getTime();*/
            return Long.parseLong(s.substring(20));
        }
        
/*    public int getYear(String s) {
        return Integer.parseInt(s.substring(0, 4));
    }
    public int getMonth(String s) {
        return Integer.parseInt(s.substring(5, 7));
    }
    public int getDay(String s) {
        return Integer.parseInt(s.substring(8, 10));
    }
    public int getHour(String s) {
        return Integer.parseInt(s.substring(11, 13));
    }
    public int getMin(String s) {
        return Integer.parseInt(s.substring(14, 16));
    }
    public int getSec(String s) {
        return Integer.parseInt(s.substring(17, 19));
    }//*/
        
        public void hideNotify() {
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
        
        public void doLogout() {
            sc.sendMessage("game=hexawar&action=logout&id="+server_id+"&skey="+server_skey);
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
                if (max<fontpsmall.stringWidth(server_jatekosnev[i])) {
                    max=fontpsmall.stringWidth(server_jatekosnev[i]);
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
                    map[ofs]=back[i];
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
        
        public void load_scene(byte newscene, boolean unload) {
            if (unload) {
                switch (scene) {
                    case OBH_SCENE:
                        obh=Idummy;
                        break;
                    case LOGO_SCENE:
                        
                        break;
                    case RUNNING_GAME_SCENE:
                    case PRE_RUNNING_GAME_SCENE_WITH_NAMES:
                    case PAUSE_SCENE:
                    case SEND_ACTION_SCENE:
                    case WAITING_FOR_RESPONSE_SCENE:
                        for (int i=4; i<20; ++i) {
                            img[i]=Idummy;
                        }
                        break;
                    case WAITING_FOR_END_OF_TURN_SCENE:
                        break;
                        
                    case WAIT_FOR_GAME_SCENE:
                        img[11]=Idummy;
                        img[20]=Idummy;
                        break;
                        
                    case END_OF_GAME_SCENE_1:
                    case END_OF_GAME_SCENE_2:
                        for (int i=4; i<img.length; ++i) {
                            img[i]=Idummy;
                        }
                        break;
                    default:
//#ifdef DEBUG
//#                         System.out.println("@@@ unload_scene: "+scene);
//#endif
                }
                System.gc();
            }
            try {
                switch (newscene) {
                    case OBH_SCENE:
                        obh=Image.createImage("/obh.png");
                        break;
                    case LOGO_SCENE:
                    case ERROR_SCENE:
                    case MAIN_MENU_SCENE:
                        img[0]=Image.createImage("/00.png");
                        img[1]=Image.createImage("/01.png");
                        img[11]=Image.createImage("/11.png");
                        img[20]=Image.createImage("/20.png");
                        break;
                    case RUNNING_GAME_SCENE:
                    case PRE_RUNNING_GAME_SCENE_WITH_NAMES:
                        for (int i=4; i<20; ++i) { // itt 11 nem kellene, 7, 8 nincs is
                            try {
                                img[i]=Image.createImage("/"+(i<10?"0":"")+i+".png");
                            } catch (Exception e) {
                                img[i]=Idummy;
                            }
                        }
                        break;
                    case WAITING_FOR_END_OF_TURN_SCENE:
                        break;
                    case END_OF_GAME_SCENE_1:
                    case END_OF_GAME_SCENE_2:
                        img[6]=Image.createImage("/06.png");
                        break;
                    default:
//#ifdef DEBUG
//#                         System.out.println("@@@ load_scene: "+newscene);
//#endif
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        GameCanvas() {
//#ifdef DEBUG
//#             System.out.println(" haho SW="+SW+" SH="+SH);
//#endif
            for (int i=0; i<server_jatekosnev.length; ++i) {
                server_jatekosnev[i]="";
            }
            for (int i=0; i<3; ++i) {
                loginName[i]="";
                pwd[i]="";
                email[i]="";
            }
            loadScore();
//            csataAnimPosX=new short[12];
//            csataAnimPosY=new short[12];
//            csataAnimTipus=new byte[12];
//            csataAnimMoveOfs=new byte[12];
//            csataAnimTargetDistance=new short[12];
            byte surl[]=loadResource("/serverurl");
            String s="";
            for (int i=0; i<surl.length; ++i) {
                s+=String.valueOf((char)surl[i]);
            }
//#ifdef DEBUG
//#             System.out.println(">>> server_url="+s);
//#endif
            server_url=s;
            sc=new ServerConnection(server_url);
            keypressed = new boolean[MY_KEY_SZAM];
            for (int i=0; i<MY_KEY_SZAM; ++i) {
                keypressed[i]=false;
            }
            rnd=new Random(System.currentTimeMillis());
            img=new Image[IMG_NUM];
            for (int i=0; i<IMG_NUM; ++i) {
                img[i]=Idummy;
            }
            load_scene(OBH_SCENE, true);
            scene=OBH_SCENE;
            WAIT_TIME=System.currentTimeMillis();
        }
        
        final int BRIEFING_WIDTH_IN_PIXEL=SW-30;
        
        public int cntLines(String s) {
            int width=0, space=0, cnt=0, index=0;
            while ((space=s.indexOf(" ", index))!=-1) {
                int nlcnt=0;
                while (s.charAt(space-nlcnt-1)=='\n') {
                    ++nlcnt;
                }
                if (nlcnt!=0) {
                    int adder=g2.getFont().stringWidth(s.substring(index, space));
                    if (width+adder>BRIEFING_WIDTH_IN_PIXEL) {
                        ++cnt;
                    }
                    cnt+=nlcnt;
                    width=0;
                    index=space+1;
                } else {
                    int adder=g2.getFont().stringWidth(s.substring(index, space));
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
            if (width+g2.getFont().stringWidth(s.substring(index))>BRIEFING_WIDTH_IN_PIXEL) {
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
                    int adder=g2.getFont().stringWidth(s.substring(0, space-nlcnt));
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
                    int adder=g2.getFont().stringWidth(s.substring(0, space));
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
            if (width+g2.getFont().stringWidth(s)>BRIEFING_WIDTH_IN_PIXEL) {
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
        
        public void checkForInfo() {
            if (server_info!=null) {
                server_info_array=tordel(server_info);
                oldSceneBeforeInfo=scene;
                infoOfs=0;
                scene=DISPLAY_INFO_SCENE;
            }
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
                            break;
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
        
/*        public void initCsataAnim() {
            for (int i=0; i<12; ++i) {
                csataAnimPosX[i]=(short)((i<6?30:90)+(rnd.nextInt()%10));
                csataAnimPosY[i]=(short)(6+(i<6?i*18:(i-6)*18));
                csataAnimTipus[i]=(byte)Math.abs(rnd.nextInt()%3);
                csataAnimMoveOfs[i]=(byte)(i<6?1:-1);
                csataAnimTargetDistance[i]=(short)(csataAnimPosX[i]+(10+Math.abs(rnd.nextInt()%20)));
            }
            csataAnimSzin1=(byte)Math.abs(rnd.nextInt()%8);
            csataAnimSzin2=(byte)((csataAnimSzin1+Math.abs(rnd.nextInt()%6))%8);
        }//*/
        
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
            a1seregStartOfs=SW_p2-(fontpsmall.stringWidth(a1)>>1)-10/2-14/2;
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
        
        public void handle_key() {
            int i;
            boolean volt;
            switch (scene) {
                case JUMP_TO_MAIN_MENU_SCENE:
                    if (isWorking) {
                    } else {
                        menu_selected=0;
                        scene=MAIN_MENU_SCENE;
                    }
                    break;
                case LOGOUT_SCENE:
                    if (isWorking) {
                    } else {
                        isRunning=false;
                    }
                    break;
                case DISPLAY_INFO_SCENE:
                    if (keypressed[MY_KEY_RSOFT]) {
                        server_info_array=null;
                        server_info=null;
                        scene=oldSceneBeforeInfo;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    if (keypressed[MY_KEY_UP]) {
                        if (infoOfs>0) {
                            --infoOfs;
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (infoOfs<server_info_array.length-INFO_LINES) {
                            ++infoOfs;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    break;
                case END_OF_GAME_SCENE_1:
                    if (keypressed[MY_KEY_RSOFT]) {
                        scene=END_OF_GAME_SCENE_2;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    break;
            case END_OF_GAME_SCENE_2:
                if (keypressed[MY_KEY_RSOFT]) {
                        load_scene(MAIN_MENU_SCENE, true);
                    keypressed[MY_KEY_RSOFT]=false;
                    typeReaded=false;
                    menu_selected=0;
                    sc.sendMessage("game=hexawar&action=getgametypes&id="+server_id+"&skey="+server_skey);
                    scene=SHOW_TYPE_SCENE;
                }
                break;

                    
                case WAITING_FOR_END_OF_TURN_SCENE:
                    if (System.currentTimeMillis()/1000-ETIME>WAIT_TIME) {
                        sc.sendMessage("game=hexawar&action=askround&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType+"&actgamenum="+server_actgamenum+"&actround="+server_actround);
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
                    
                case PAUSE_SCENE:
                    if (keypressed[MY_KEY_UP]) {
                        if (menu_selected>0) {
                            --menu_selected;
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (menu_selected<pause_txt.length-1) {
                            ++menu_selected;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_FIRE]) {
                        switch (menu_selected) {
                            case 0:
                                scene=RUNNING_GAME_SCENE;
                                break;
                            case 1:
                                skipTurn();
                                break;
                            case 2:
                                doLogout();
                                load_scene(MAIN_MENU_SCENE, true);
                                scene=JUMP_TO_MAIN_MENU_SCENE;
                                break;
                            case 3:
                                doLogout();
                                load_scene(MAIN_MENU_SCENE, true);
                                scene=LOGOUT_SCENE;
                                break;
                        }
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    move_kamera(true);
                    break;
                case ERROR_SCENE:
                    for (i=0; i<keypressed.length; ++i) {
                        if (keypressed[i]) {
                            menu_selected=0;
                            scene=MAIN_MENU_SCENE;
                            keypressed[i]=false;
                        }
                    }
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
                        extractResultAtScene(serverResult, SEND_ACTION_SCENE);
                        switch (server_result) {
                            case 0: // hiba volt
                                load_scene(ERROR_SCENE, true);
                                switch (server_error) {
                                    case 0:
                                        lastError=3;
                                        scene=ERROR_SCENE;
                                        break;
                                    case 3:
                                        lastError=5;
                                        scene=ERROR_SCENE;
                                        break;
                                    case 4:
                                        lastError=6;
                                        scene=ERROR_SCENE;
                                        break;
                                    case 5:
                                        lastError=7;
                                        scene=ERROR_SCENE;
                                        break;
                                }
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
                                load_scene(WAITING_FOR_END_OF_TURN_SCENE, false);
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
                                    load_scene(END_OF_GAME_SCENE_1, true);
                                    scene=END_OF_GAME_SCENE_1;
                                }
                                break;
                                
                            case 999:
                                lastError=8;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                                
                            default:
                                lastError=1;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                        }
                        checkForInfo();
                    }
                    break;
                    
                case LOGIN_SCENE: // todo: connection timeout
                    if (isWorking) {
                    } else {
                        extractResultAtScene(serverResult, LOGIN_SCENE);
                        switch (server_result) {
                            case 0:
                                lastError=0;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                            case 1:
                                typeReaded=false;
                                sc.sendMessage("game=hexawar&action=getgametypes&id="+server_id+"&skey="+server_skey);
                                scene=SHOW_TYPE_SCENE;
                                menu_selected=0;
                                break;
                            case 2: // csatlakozas a korabbi jatekhoz
                                selectedGType=login_gtype;
                                sc.sendMessage("game="+login_game+"&action=askstate&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType);
                                scene=WAIT_FOR_GAME_SCENE;
                                break;
                            case 42: // full
                                lastError=9;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                            case 43: // redirect
                                sc.setServerURL(server_redirect);
                                sc.sendMessage("action=login&lang="+CLIENT_LANG+"&login="
                                        +loginName[defaultAccount]+"&passwd="+pwd[defaultAccount]);
                                break;
                                
                            case 999:
                                lastError=8;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                                
                            default: // todo
                                lastError=1;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                        }
                        checkForInfo();
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
                                load_scene(ERROR_SCENE, true);
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
                                load_scene(RUNNING_GAME_SCENE, true);
                                pre_running_cnt=0;
                                scene=PRE_RUNNING_GAME_SCENE_WITH_NAMES;
                                gameStarted=true;
                                break;
                            case 999:
                                lastError=8;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                            default: // todo
                                lastError=3;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                        }
                        checkForInfo();
                    }
                    break;
                    
                case GET_REG_INFO_SCENE:
                    if (isWorking) {
                    } else {
                        extractResultAtScene(serverResult, GET_REG_INFO_SCENE);
                        switch (server_result) {
                            case 0:
                                scene=REG_ERROR_SCENE;
                                break;
                            case 1:
                                scene=REG_OK_SCENE;
                                break;
                            case 999:
                                lastError=8;
                                load_scene(ERROR_SCENE, true);
                                scene=ERROR_SCENE;
                                break;
                            default: // todo
                                scene=REG_ERROR_SCENE;
                        }
                        checkForInfo();
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
                                    load_scene(ERROR_SCENE, true);
                                    scene=ERROR_SCENE;
                                    break;
                                case 1:
                                    menu_selected=0;
                                    typeReaded=true;
                                    break;
                                case 999:
                                    lastError=8;
                                    load_scene(ERROR_SCENE, true);
                                    scene=ERROR_SCENE;
                                    break;
                                default: // todo
                                    lastError=3;
                                    load_scene(ERROR_SCENE, true);
                                    scene=ERROR_SCENE;
                            }
                            checkForInfo();
                        } else {
                            if (keypressed[MY_KEY_UP]) {
                                if (menu_selected>0) {
                                    --menu_selected;
                                }
                                keypressed[MY_KEY_UP]=false;
                            }
                            if (keypressed[MY_KEY_DOWN]) {
                                if (menu_selected<server_gtypenum-1) {
                                    ++menu_selected;
                                }
                                keypressed[MY_KEY_DOWN]=false;
                            }
                            if (keypressed[MY_KEY_LSOFT]) {
                                menu_selected=0;
                                doLogout();
                                scene=MAIN_MENU_SCENE;
                                keypressed[MY_KEY_LSOFT]=false;
                            }
                            if (keypressed[MY_KEY_FIRE]) {
                                selectedGType=server_gtpid[menu_selected];
                                sc.sendMessage("game=hexawar&action=waitforgametype&id="+server_id+"&skey="+server_skey+"&gtype="+selectedGType);
                                scene=WAIT_FOR_GAME_SCENE;
                                keypressed[MY_KEY_FIRE]=false;
                            }
                        }
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
                                load_scene(END_OF_GAME_SCENE_1, true);
                                scene=END_OF_GAME_SCENE_1;
                            }
                        }
                        keypressed[MY_KEY_RSOFT]=false;
                    }
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
                    
                case RUNNING_GAME_SCENE:
                    game_update();
                    break;
                    
                case REG_ERROR_SCENE:
                    if (keypressed[MY_KEY_RSOFT]) {
                        menu_selected=0;
                        scene=ACCOUNT_SCENE;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    break;
                case REG_OK_SCENE:
                    if (keypressed[MY_KEY_LSOFT]) {
                        menu_selected=0;
                        scene=ACCOUNT_SCENE;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    if (keypressed[MY_KEY_RSOFT]) {
                        defaultAccount=accountOfs;
                        sc.sendMessage("action=login&lang="+CLIENT_LANG+"&login="
                                +loginName[defaultAccount]+"&passwd="+pwd[defaultAccount]);
                        scene=LOGIN_SCENE;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    break;
                case ACCOUNT_SUBMENU_SCENE:
                    if (keypressed[MY_KEY_DOWN] && !areyousure) {
                        if (menu_selected<accountMod.length-1) {
                            ++menu_selected;
                        } else {
                            menu_selected=0;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_UP] && !areyousure) {
                        if (menu_selected>0) {
                            --menu_selected;
                        } else {
                            menu_selected=(byte)(accountMod.length-1);
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_FIRE]) {
                        switch (menu_selected) {
                            case 0: // select
                                defaultAccount=accountOfs;
                                break;
                            case 1: // modify
                                newLogin[0]=new StringBuffer(loginName[accountOfs]);
                                newLogin[1]=new StringBuffer(pwd[accountOfs]);
                                newLogin[2]=new StringBuffer(email[accountOfs]);
                                newAccount=false;
                                System.out.println("fakk");
                                cursorPos=0;
                                scene=INPUT_ACCOUNT_NAME;
                                break;
                            case 2: // delete
                                if (areyousure) {
                                } else {
                                    areyousure=true;
                                }
                                break;
                            case 3: // register
                                sc.sendMessage("action=register&login="+loginName[accountOfs]
                                        +"&passwd="+pwd[accountOfs]+"&email="+email[accountOfs]);
                                scene=GET_REG_INFO_SCENE;
                                break;
                        }
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    if (keypressed[MY_KEY_RSOFT]) {
                        if (areyousure) {
                            deleteAccount();
                            saveScore();
                            menu_selected=0;
                            scene=ACCOUNT_SCENE;
                            areyousure=false;
                        }
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    if (keypressed[MY_KEY_LSOFT]) {
                        if (areyousure) {
                            areyousure=false;
                        } else {
                            menu_selected=0;
                            scene=ACCOUNT_SCENE;
                        }
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    break;
                    
                case ACCOUNT_CHANGED:
                    if (keypressed[MY_KEY_LSOFT]) {
                        scene=ACCOUNT_SCENE;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    if (keypressed[MY_KEY_RSOFT]) {
                        sc.sendMessage("action=register&login="+loginName[accountOfs]
                                +"&passwd="+pwd[accountOfs]+"&email="+email[accountOfs]);
                        scene=GET_REG_INFO_SCENE;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    break;
                    
                case INPUT_ACCOUNT_NAME:
                case INPUT_ACCOUNT_PASS:
                case INPUT_ACCOUNT_MAIL:
                    if (keypressed[MY_KEY_UP]) {
                        if (cursorPos>=CHAR_PER_LINE) {
                            cursorPos-=CHAR_PER_LINE;
                        } else {
                            cursorPos+=(karakterek.length()/CHAR_PER_LINE)*CHAR_PER_LINE;
                            if (cursorPos>=karakterek.length()) {
                                cursorPos-=CHAR_PER_LINE;
                            }
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (cursorPos+CHAR_PER_LINE<karakterek.length()) {
                            cursorPos+=CHAR_PER_LINE;
                        } else {
                            cursorPos%=CHAR_PER_LINE;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_LEFT]) {
                        if (cursorPos>0) {
                            --cursorPos;
                        } else {
                            cursorPos=(short)(karakterek.length()-1);
                        }
                        keypressed[MY_KEY_LEFT]=false;
                    }
                    if (keypressed[MY_KEY_RIGHT]) {
                        if (cursorPos<karakterek.length()-1) {
                            ++cursorPos;
                        } else {
                            cursorPos=0;
                        }
                        keypressed[MY_KEY_RIGHT]=false;
                    }
                    if (keypressed[MY_KEY_LSOFT]) {
                        --scene;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    if (keypressed[MY_KEY_FIRE]) {
                        if (cursorPos==karakterek.length()-1) {
                            if (newLogin[scene-INPUT_ACCOUNT_NAME].length()!=0) {
                                newLogin[scene-INPUT_ACCOUNT_NAME].deleteCharAt(newLogin[scene-INPUT_ACCOUNT_NAME].length()-1);
                            }
                        } else {
                            if (newLogin[scene-INPUT_ACCOUNT_NAME].length()<STRLENGTH) {
                                newLogin[scene-INPUT_ACCOUNT_NAME].append(karakterek.charAt(cursorPos));
                            }
                        }
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    if (keypressed[MY_KEY_RSOFT]) {
                        if (newLogin[scene-INPUT_ACCOUNT_NAME].length()!=0) {
                            if (scene==INPUT_ACCOUNT_MAIL) {
                                if (newAccount) {
                                    for (i=0; i<3; ++i) {
                                        if (loginName[i].equals("")) {
                                            loginName[i]=newLogin[0].toString();
                                            pwd[i]=newLogin[1].toString();
                                            email[i]=newLogin[2].toString();
                                            if (accountNum==0) {
                                                defaultAccount=0;
                                            }
                                            ++accountNum;
                                            accountOfs=i;
                                            saveScore();
                                            ++scene;
                                            break;
                                        }
                                    }
                                } else {
                                    loginName[accountOfs]=newLogin[0].toString();
                                    pwd[accountOfs]=newLogin[1].toString();
                                    email[accountOfs]=newLogin[2].toString();
                                    saveScore();
                                    scene=ACCOUNT_SUBMENU_SCENE;
                                }
                            } else {
                                ++scene;
                            }
                        }
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    break;
                    
                case ACCOUNT_SCENE:
                    if (keypressed[MY_KEY_DOWN]) {
                        if (accountNum==3) {
                            if (menu_selected<2) {
                                ++menu_selected;
                            } else {
                                menu_selected=0;
                            }
                        } else {
                            if (menu_selected<1+accountNum-1) {
                                ++menu_selected;
                            } else {
                                menu_selected=0;
                            }
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_UP]) {
                        if (menu_selected>0) {
                            --menu_selected;
                        } else {
                            menu_selected=(byte)(accountNum==3?2:accountNum);
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_FIRE]) {
                        if (accountNum==3) {
                            switch (menu_selected) {
                                case 0:
                                case 1:
                                case 2:
                                    accountOfs=menu_selected;
                                    menu_selected=0;
                                    scene=ACCOUNT_SUBMENU_SCENE;
                                    break;
                            }
                        } else {
                            if (menu_selected==0) {
                                newLogin[0]=new StringBuffer("");
                                newLogin[1]=new StringBuffer("");
                                newLogin[2]=new StringBuffer("");
                                cursorPos=0;
                                newAccount=true;
                                scene=INPUT_ACCOUNT_NAME;
                            } else {
                                accountOfs=(short)(menu_selected-1);
                                menu_selected=0;
                                scene=ACCOUNT_SUBMENU_SCENE;
                            }
                        }
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    if (keypressed[MY_KEY_LSOFT]) {
                        saveScore();
                        scene=MAIN_MENU_SCENE;
                        menu_selected=0;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    break;
                case DIVIDE_SCENE:
                    if (keypressed[MY_KEY_LSOFT]) {
                        if (squads[canSelectUnit*SQUAD_H+BALTASOK_SZAMA_0]==darabolEgyseg[0] &&
                                squads[canSelectUnit*SQUAD_H+KARDOSOK_SZAMA_1]==darabolEgyseg[1] &&
                                squads[canSelectUnit*SQUAD_H+LANDZSASOK_SZAMA_2]==darabolEgyseg[2]) {
                            startMoveSelectedUnit();
                        } else {
                            darabol=true;
                            selectedUnitOrigPosX=squads[canSelectUnit*SQUAD_H+POS_X];
                            selectedUnitOrigPosY=squads[canSelectUnit*SQUAD_H+POS_Y];
                            selectedUnit=(squads.length/SQUAD_H)-1;
                            for (i=3; i<SQUAD_H; ++i) {
                                squads[selectedUnit*SQUAD_H+i]=squads[canSelectUnit*SQUAD_H+i];
                            }
                            squads[selectedUnit*SQUAD_H+BALTASOK_SZAMA_0]=darabolEgyseg[0];
                            squads[selectedUnit*SQUAD_H+KARDOSOK_SZAMA_1]=darabolEgyseg[1];
                            squads[selectedUnit*SQUAD_H+LANDZSASOK_SZAMA_2]=darabolEgyseg[2];
                        }
                        
                        keypressed[MY_KEY_LSOFT]=false;
                        scene=RUNNING_GAME_SCENE;
                    }
                    if (keypressed[MY_KEY_RSOFT]) {
                        keypressed[MY_KEY_RSOFT]=false;
                        scene=RUNNING_GAME_SCENE;
                    }
                    if (keypressed[MY_KEY_UP]) {
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
                    }
                    break;
                    
                case SPEED_CALIB_SCENE:
                    if (keypressed[MY_KEY_RSOFT]) {
                        img[19]=Idummy;
                        System.gc();
                        menu_selected=0;
                        scene=MAIN_MENU_SCENE;
                        keypressed[MY_KEY_RSOFT]=false;
                    }
                    if (keypressed[MY_KEY_DOWN] ||
                            keypressed[MY_KEY_UP]) {
                        setSleep=!setSleep;
                        keypressed[MY_KEY_UP]=false;
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_LEFT]) {
                        if (setSleep) {
                            if (SLEEP>10) {
                                SLEEP-=5;
                            }
                        } else {
                            if (UPDATE_DIV>1) {
                                --UPDATE_DIV;
                            }
                        }
                        keypressed[MY_KEY_LEFT]=false;
                    }
                    if (keypressed[MY_KEY_RIGHT]) {
                        if (setSleep) {
                            SLEEP+=5;
                        } else {
                            ++UPDATE_DIV;
                        }
                        keypressed[MY_KEY_RIGHT]=false;
                    }
                    break;
                    
                case RULES_SCENE:
                    if (keypressed[MY_KEY_UP]) {
                        if (menu_selected>0) {
                            --menu_selected;
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (menu_selected+DISPLAY_HELP_LINES<rulesArray.length) {
                            ++menu_selected;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    if (keypressed[MY_KEY_LSOFT]) {
                        menu_selected=2;
                        scene=MAIN_MENU_SCENE;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    break;
                    
                case HOWTOPLAY_SCENE:
                    if (keypressed[MY_KEY_LSOFT]) {
                        menu_selected=3;
                        scene=MAIN_MENU_SCENE;
                        keypressed[MY_KEY_LSOFT]=false;
                    }
                    if (keypressed[MY_KEY_UP]) {
                        if (menu_selected>0) {
                            --menu_selected;
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (menu_selected+DISPLAY_HELP_LINES<h2pArray.length) {
                            ++menu_selected;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    break;
                    
                case MAIN_MENU_SCENE:
                    if (keypressed[MY_KEY_STAR]) {
                        ++speedCalibCnt;
                        if (speedCalibCnt==3) {
                            speedCalibCnt=0;
                            try {
                                img[19]=Image.createImage("/19.png");
                            } catch (Exception e) {
                            }
                            scene=SPEED_CALIB_SCENE;
                        }
                        keypressed[MY_KEY_STAR]=false;
                        return;
                    }
                    if (keypressed[MY_KEY_FIRE]) {
                        switch (menu_selected) {
                            case 0:
                                scene=ACCOUNT_SCENE;
                                break;
                            case 1:
                                if (defaultAccount!=-1) {
                                    sc.sendMessage("action=login&lang="+CLIENT_LANG+"&login="
                                            +loginName[defaultAccount]+"&passwd="+pwd[defaultAccount]);
                                    scene=LOGIN_SCENE;
                                } else {
                                    menu_selected=0;
                                    scene=ACCOUNT_SCENE;
                                }
                                break;
                            case 2:
                                menu_selected=0;
                                scene=RULES_SCENE;
                                break;
                            case 3:
                                menu_selected=0;
                                scene=HOWTOPLAY_SCENE;
                                break;
                            case 4:
                                isRunning=false;
                                break;
                        }
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    if (keypressed[MY_KEY_UP]) {
                        if (menu_selected>0) {
                            --menu_selected;
                        }
                        keypressed[MY_KEY_UP]=false;
                    }
                    if (keypressed[MY_KEY_DOWN]) {
                        if (menu_selected!=menu_txt_1.length-1) {
                            ++menu_selected;
                        }
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    break;
                case OBH_SCENE:
                    if (h2pArray==null && g2!=null) {
                        h2pArray=tordel(howtoplayTxt);
                        rulesArray=tordel(rulesTxt);
                    } else {
                        for (i=0; i<MY_KEY_SZAM; ++i) {
                            if (keypressed[i]) {
                                load_scene(LOGO_SCENE, true);
                                scene=LOGO_SCENE;
                                keypressed[i]=false;
                                break;
                            }
                        }
                        if (System.currentTimeMillis()-WAIT_TIME>3000) {
                            load_scene(LOGO_SCENE, true);
                            scene=LOGO_SCENE;
                        }
                    }
                    break;
                case LOGO_SCENE:
                    menu_selected=0;
                    scene=MAIN_MENU_SCENE;
                    for (i=0; i<MY_KEY_SZAM; ++i) {
                        if (keypressed[i]) {
                            scene=MAIN_MENU_SCENE;
                            keypressed[i]=false;
                            break;
                        }
                    }
                    break;
                default:
            }
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
        
        public void moveUp(int ofs) {
            loginName[ofs-1]=loginName[ofs];
            pwd[ofs-1]=pwd[ofs];
            email[ofs-1]=email[ofs];
        }
        
        public void deleteAccount() {
            switch (accountOfs) {
                case 0:
                    moveUp(1);
                    moveUp(2);
                    break;
                case 1:
                    moveUp(2);
                    break;
                case 2:
                    break;
            }
            loginName[2]="";
            pwd[2]="";
            email[2]="";
            --accountNum;
            if (defaultAccount==accountOfs) {
                --defaultAccount;
            }
            if (defaultAccount==-1 && accountNum>0) {
                defaultAccount=0;
            }
//#ifdef DEBUG
//#             System.out.println(">>> defaultAccount:"+defaultAccount);
//#endif
        }
        
        public void run() {
            while (isRunning) {
                ++update;
                frame=update/UPDATE_DIV;
                handle_key();
                try {
                    repaint();
                    Thread.sleep(SLEEP);
                    serviceRepaints();
                }  catch(Exception e) {};
            }
            saveScore();
            notifyDestroyed();
        }
        
        public void paint_footer(String s1, String s2, int color) {
            g2.setClip(0,0,SW,SH);
            paint_shaded(s1, color, 2, SH-12, false);
            paint_shaded(s2, color, SW-2-g2.getFont().stringWidth(s2), SH-12, false);
        }
        
        final int relx[]={ 6, 13,  6,  7, 12,  4};
        final int rely[]={11,  4, 11, 11,  4, 11};
        final int sx[]={24, 12, 24, 24, 12, 24};
        final int sy[]={15, 28, 15, 15, 28, 15};
        final int startOfs[]={0, 24, 24+12, 24+12+24, 24+12+24+24, 24+12+24+24+12};
        
        public void paint_arrow(int posx, int posy, int direction, int color) {
            int xp=(posx-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx;
            int yp=(posy-kamera_eposy)*TILE_MERET_36+(posx&1)*TILE_MERET_PER_2_18-kamera_rposy;
            yp+=GUI_HEIGHT;
            --direction;
            switch (direction) {
                case 0: // jobbra fel
                    yp-=TILE_MERET_PER_2_18-8;
                    xp+=TILE_MERET_PER_2_18-2;
                    break;
                case 1: // fel
                    yp-=TILE_MERET_PER_2_18;
                    break;
                case 2: // balra fel
                    yp-=TILE_MERET_PER_2_18-8;
                    xp-=TILE_MERET_PER_2_18-2;
                    break;
                case 3: // balra le
                    xp-=TILE_MERET_PER_2_18-2;
                    yp+=TILE_MERET_PER_2_18-8;
                    break;
                case 4: // le
                    yp+=TILE_MERET_PER_2_18;
                    break;
                case 5: // jobbra le
                    xp+=TILE_MERET_PER_2_18-2;
                    yp+=TILE_MERET_PER_2_18-8;
                    break;
            }
            xp+=relx[direction];
            yp+=rely[direction];
            g2.setClip(xp, yp, sx[direction], sy[direction]);
            g2.drawImage(img[15], xp-startOfs[direction], yp-color*28, Graphics.TOP|Graphics.LEFT);
        }
        
        final static int POTYI_MERET = 12;
        final static int POTYI_MERET_p2 = POTYI_MERET>>1;
        final static int POTYI_MERET_szer_3_per_4 = (POTYI_MERET*3)/4;
        public void paint_game_scene(boolean paintSelection, boolean paintFooter) {
            g2.setClip(0,0,SW,SH);
            
            int xp=0, yp=0, val=0, ofs=0, tmp=0, i=0;
            
            if (scene==RUNNING_GAME_SCENE && keypressed[MY_KEY_POUND]) {
                g2.drawImage(img[0], 0, 0, Graphics.TOP|Graphics.LEFT);
                ofs=0;
                int startx=SW_p2-(POTYI_MERET>>2)-(map_meret_x*POTYI_MERET_szer_3_per_4)/2;
                int starty=SH_p2-(map_meret_y*POTYI_MERET)/2;
                
                for (int y=0; y<map_meret_y; ++y) {
                    ++ofs;
                    for (int x=1; x<map_meret_x; ++x, ++ofs) {
                        tmp=map[ofs];
                        val=(tmp&0x180)>>7;
                        xp=startx+x*POTYI_MERET_szer_3_per_4;
                        yp=starty+y*POTYI_MERET+(x&1)*POTYI_MERET_p2;
                        g2.setClip(xp, yp, POTYI_MERET, POTYI_MERET);
                        g2.drawImage(img[16], xp-(val!=0?players[(tmp&0xE00)>>9]:8)*POTYI_MERET, yp, Graphics.TOP|Graphics.LEFT);
                    }
                }
                for (ofs=0; ofs<squads.length; ofs+=SQUAD_H) {
                    if (squads[ofs+JATEKOS]==-1) {
                        continue;
                    }
                    xp=startx+squads[ofs+POS_X]*9+4;
                    yp=starty+squads[ofs+POS_Y]*12+(squads[ofs+POS_X]&1)*6+3;
                    g2.setClip(xp, yp, 5, 5);
                    g2.drawImage(img[17], xp-players[squads[ofs+JATEKOS]]*5, yp, Graphics.TOP|Graphics.LEFT);
                }
                paint_gui(true);
                return;
            }
            
            g2.setColor(0);
            g2.fillRect(0,0,SW,SH);
            
            for (int y=-1; y<TILE_VISIBLE_Y; ++y) {
                for (int x=-1; x<TILE_VISIBLE_X; ++x) {
                    if (y+kamera_eposy<0 || y+kamera_eposy>=map_meret_y
                            || x+kamera_eposx<0 || x+kamera_eposx>=map_meret_x) {
                        ofs=-1;
                    } else {
                        ofs=(y+kamera_eposy)*map_meret_x+x+kamera_eposx;
                        val=map[ofs];
                    }
                    xp=x*TILE_MERET_36_SZER3PER4-kamera_rposx;
                    yp=y*TILE_MERET_36+((x+kamera_eposx)&1)*TILE_MERET_PER_2_18-kamera_rposy;
                    
                    yp+=GUI_HEIGHT;
                    g2.setClip(xp, yp, TILE_MERET_36, TILE_MERET_36);
                    g2.drawImage(img[4], xp-(ofs==-1?0:(val&0xF)*TILE_MERET_36), yp,
                            Graphics.TOP|Graphics.LEFT);
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
                    /*int color=0;
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
            
/* UTAK
            for (int y=-1; y<TILE_VISIBLE_Y; ++y) {
                for (int x=-1; x<TILE_VISIBLE_X; ++x) {
                    if (y+kamera_eposy<0 || y+kamera_eposy>=map_meret_y
                            || x+kamera_eposx<0 || x+kamera_eposx>=map_meret_x) {
                        continue;
                    } else {
                        val=map[(y+kamera_eposy)*map_meret_x+x+kamera_eposx];
                    }
                    xp=x*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18;
                    yp=y*TILE_MERET_36+((x+kamera_eposx)&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18;
 
                    yp+=GUI_HEIGHT;
                    if ((val&0x10)==0x10) { // felfel van ut
                        g2.setClip(xp-2, yp-34, 5, 34);
                        g2.drawImage(img[5], xp-2-25, yp-34, Graphics.TOP|Graphics.LEFT);
                    }
                    if ((val&0x20)==0x20) { // balra fel van ut
                        g2.setClip(xp-25, yp-19, 25, 19);
                        g2.drawImage(img[5], xp-25, yp-19-19, Graphics.LEFT|Graphics.TOP);
                    }
                    if ((val&0x40)==0x40) { // jobbra fel van ut
                        g2.setClip(xp, yp-19, 25, 19);
                        g2.drawImage(img[5], xp, yp-19, Graphics.LEFT|Graphics.TOP);
                    }
                }
            } // */
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
                        g2.setClip(xp, yp, castleSizeX[val-1], castleSizeY[val-1]);
                        g2.drawImage(img[5+val], xp-players[(tmp&0xE00)>>9]*castleSizeX[val-1], yp, Graphics.TOP|Graphics.LEFT);
                    }
                }
            }
            
 /*       g2.setClip(0,0,SW,SH);
        for (int y=-1; y<TILE_VISIBLE_Y; ++y) {
            for (int x=-1; x<TILE_VISIBLE_X; ++x) {
                if (y+kamera_eposy<0 || y+kamera_eposy>=map_meret_y
                    || x+kamera_eposx<0 || x+kamera_eposx>=map_meret_x) {
                    continue;
                } else {
                    ofs=(y+kamera_eposy)*map_meret_x+x+kamera_eposx;
                }
  
                                xp=x*TILE_MERET_36_SZER3PER4-kamera_rposx;
                                yp=y*TILE_MERET_36+((x+kamera_eposx)&1)*TILE_MERET_PER_2_18-kamera_rposy;
                                yp+=GUI_HEIGHT;
  
                g2.setColor(0xFFFFFF);
                g2.drawString("("+(x+kamera_eposx)+","+(y+kamera_eposy)+")", xp, yp, Graphics.TOP|Graphics.LEFT);
                }
            }//*/
            
            for (i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
                if (squads[ofs+JATEKOS]==-1 || squads[ofs+JATEKOS]==server_hg_sajatsorszam) {
                    continue;
                }
        /*    int tipus=0;
            if (squads[ofs+BALTASOK_SZAMA_0]<squads[ofs+LANDZSASOK_SZAMA_2]) {
                if (squads[ofs+LANDZSASOK_SZAMA_2]<squads[ofs+KARDOSOK_SZAMA_1]) {
                    tipus=KARDOS;
                } else {
                    tipus=LANDZSAS;
                }
            } else {
                if (squads[ofs+BALTASOK_SZAMA_0]<squads[ofs+KARDOSOK_SZAMA_1]) {
                    tipus=KARDOS;
                } else {
                    tipus=BALTAS;
                }
            }*/
                xp=(squads[ofs+POS_X]-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18-8;
                yp=(squads[ofs+POS_Y]-kamera_eposy)*TILE_MERET_36+((squads[ofs+POS_X])&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18-8;
                if (squads[ofs+MOVE_CNT]>0) {
                    xp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_x[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                    yp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_y[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                } else {
                }
                yp+=GUI_HEIGHT;
                //g2.setClip(xp, yp, 16, 16);
                g2.setClip(xp, yp, 14, 15);
                if (squads[ofs+MOVE_CNT]!=0 || selectedUnit==i) {
                    xp-=anim_phase_had[frame%4];
                    
                }
                //g2.drawImage(img[3+tipus], xp, yp-16*players[squads[ofs+JATEKOS]], Graphics.TOP|Graphics.LEFT);
                g2.drawImage(img[19], xp, yp-15*players[squads[ofs+JATEKOS]], Graphics.TOP|Graphics.LEFT);
            }
            
            for (i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
                if (squads[ofs+JATEKOS]==-1 || squads[ofs+JATEKOS]!=server_hg_sajatsorszam) {
                    continue;
                }
        /*    int tipus=0;
            if (squads[ofs+BALTASOK_SZAMA_0]<squads[ofs+LANDZSASOK_SZAMA_2]) {
                if (squads[ofs+LANDZSASOK_SZAMA_2]<squads[ofs+KARDOSOK_SZAMA_1]) {
                    tipus=KARDOS;
                } else {
                    tipus=LANDZSAS;
                }
            } else {
                if (squads[ofs+BALTASOK_SZAMA_0]<squads[ofs+KARDOSOK_SZAMA_1]) {
                    tipus=KARDOS;
                } else {
                    tipus=BALTAS;
                }
            }*/
                xp=(squads[ofs+POS_X]-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx+TILE_MERET_PER_2_18-8;
                yp=(squads[ofs+POS_Y]-kamera_eposy)*TILE_MERET_36+((squads[ofs+POS_X])&1)*TILE_MERET_PER_2_18-kamera_rposy+TILE_MERET_PER_2_18-8;
                if (squads[ofs+MOVE_CNT]>0) {
                    xp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_x[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                    yp+=((MOVE_ANIM_PHASE-squads[ofs+MOVE_CNT])*move_offset_y[squads[ofs+MOVE_DIR]-1])/MOVE_ANIM_PHASE;
                } else {
                }
                yp+=GUI_HEIGHT;
                //g2.setClip(xp, yp, 16, 16);
                g2.setClip(xp, yp, 14, 15);
                if (squads[ofs+MOVE_CNT]!=0 || selectedUnit==i) {
                    xp-=anim_phase_had[frame%4];
                    
                }
                //g2.drawImage(img[3+tipus], xp, yp-16*players[squads[ofs+JATEKOS]], Graphics.TOP|Graphics.LEFT);
                g2.drawImage(img[19], xp, yp-15*players[squads[ofs+JATEKOS]], Graphics.TOP|Graphics.LEFT);
            }
            
            if (selectedUnit==-1 && paintSelection) {
                xp=(currentSelectionX-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx-1;
                yp=(currentSelectionY-kamera_eposy)*TILE_MERET_36+((currentSelectionX)&1)*TILE_MERET_PER_2_18-kamera_rposy-1;
                yp+=GUI_HEIGHT;
                g2.setClip(xp, yp, TILE_MERET_36+2, TILE_MERET_36+2);
                if (canSelectUnit!=-1 && squads[canSelectUnit*SQUAD_H+JATEKOS]!=server_hg_sajatsorszam) {
                    xp-=38;
                }
                g2.drawImage(img[9], xp, yp, Graphics.TOP|Graphics.LEFT);
            }
            
/*      g2.setClip(0,0,SW,SH);
                g2.setColor(0xFFFFFF);
                g2.drawString(""+kamera_eposx+","+kamera_eposy, 5, 5, Graphics.TOP|Graphics.LEFT);
                g2.drawString(""+kamera_posx+","+kamera_posy, 5, 25, Graphics.TOP|Graphics.LEFT);
                g2.drawString(""+kamera_rposx+","+kamera_rposy, 5, 45, Graphics.TOP|Graphics.LEFT);//*/
            
            if (paintFooter) {
                g2.setClip(0,0,SW,SH);
                if (selectedUnit!=-1) {
                    paint_footer(UNDO_LABEL, "", 0xFFFFFF);
                } else if (canSelectUnit!=-1 && squads[canSelectUnit*SQUAD_H+JATEKOS]==
                        server_hg_sajatsorszam) {
                    paint_footer(divide[4], "", 0xFFFFFF);
                } else {
                    paint_footer("#: MiniMap", "", 0xFFFFFF);
                }
            }
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
        
        final static int XMIN = TILE_MERET_36*3/4;
        public boolean clipKamera(boolean postoo) {
            boolean retval=false;
            if (kamera_destx<XMIN) {
                kamera_destx=XMIN;
                retval=true;
            }
            if (postoo && kamera_posx<XMIN) {
                kamera_posx=XMIN;
                retval=true;
            }
            if (kamera_desty<1) {
                kamera_desty=1;
                retval=true;
            }
            if (postoo && kamera_posy<1) {
                kamera_posy=1;
                retval=true;
            }
            int XMAX = TILE_MERET_36+(map_meret_x-1)*TILE_MERET_36_SZER3PER4-SW;
            if (XMAX<kamera_destx) {
                kamera_destx=XMAX;
                retval=true;
            }
            if (postoo && kamera_posx>XMAX) {
                kamera_posx=XMAX;
                retval=true;
            }
            int YMAX = (map_meret_y+1)*TILE_MERET_36-SH-8 +20;
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
            kamera_destx=x*TILE_MERET_36_SZER3PER4+TILE_MERET_PER_2_18-SW_p2;
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
//          initCsataAnim();
            load_scene(WAITING_FOR_END_OF_TURN_SCENE, false);
            scene=WAITING_FOR_END_OF_TURN_SCENE;
        }
        
        public void game_update() {
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
                canSelectUnit=-1;
                for (int i=0, ofs=0; ofs<squads.length; ofs+=SQUAD_H, ++i) {
                    if (squads[ofs+POS_X]==currentSelectionX
                            && squads[ofs+POS_Y]==currentSelectionY) {
                        canSelectUnit=i;
                        break;
                    }
                }
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
                menu_selected=0;
                scene=PAUSE_SCENE;
                keypressed[MY_KEY_RSOFT]=false;
            }
            if (keypressed[MY_KEY_NUM0]) {
                if (selectedUnit==-1 && canSelectUnit!=-1) {
                    if (squads[canSelectUnit*SQUAD_H+JATEKOS]==server_hg_sajatsorszam) {
                        for (int i=0; i<3; ++i) {
                            darabolEgyseg[i]=(short)(squads[canSelectUnit*SQUAD_H+i]>>1);
                        }
                        darabolOfs=0;
                        darabolFirst=true;
                        scene=DIVIDE_SCENE;
                    }
                }
                keypressed[MY_KEY_NUM0]=false;
            }
            if (keypressed[MY_KEY_FIRE]) {
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
        
        public void startMoveSelectedUnit() {
            darabol=false;
            selectedUnit=canSelectUnit;
            selectedUnitOrigPosX=squads[selectedUnit*SQUAD_H+POS_X];
            selectedUnitOrigPosY=squads[selectedUnit*SQUAD_H+POS_Y];
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
        
        public void background(boolean logo) {
            g2.drawImage(img[0], 0, 0, Graphics.TOP|Graphics.LEFT);
            if (logo) {
                g2.drawImage(img[20], 0, 0, Graphics.TOP|Graphics.LEFT);
            }
        }
        
        public void paint_menu_txt_center(String[] txt, int x, int y, int step,
                boolean center) {
            for (int i=0; i<txt.length; ++i, y+=step) {
                paint_shaded(txt[i], menu_selected==i?0xFFFFFF:0x696969, x, y, center);
            }
        }
        
        public void fill_shaded_rect(int color, int x, int y, int w, int h) {
            g2.setColor(0);
            g2.fillRect(x+w, y+2, 2, h);
            g2.fillRect(x+2, y+h, w, 2);
            g2.setColor(color);
            g2.fillRect(x, y, w, h);
        }
        
        public void paint_menu_txt(String txt[], int x, int y, int width,
                int step, boolean center, int start, boolean whole) {
            int nx;
            g2.setClip(0,0,SW,SH);
            for (int i=start; i<txt.length && (i<start+3 || whole); ++i, y+=step) {
                nx=x+(center?(width>>1)-(g2.getFont().stringWidth(txt[i])>>1):0);
                if (menu_selected==i) {
                    fill_shaded_rect(0x355046, x, y, width, step-3);
                    paint_shaded(txt[i], 0xFFFFFF, nx, y+1, false);
                } else {
                    g2.setClip(x, y, width, step-2);
                    g2.drawImage(img[11], x, y, Graphics.TOP|Graphics.LEFT);
                    g2.drawImage(img[11], x+img[11].getWidth(), y, Graphics.TOP|Graphics.LEFT);
                    g2.setClip(0,0,SW,SH);
                    paint_shaded(txt[i], 0x696969, nx, y, false);
                }
            }
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
                g2.drawImage(img[14], x-tmp, y, Graphics.TOP|Graphics.LEFT);
                x+=5;
            }
            g2.setClip(0,0,SW,SH);
        }
        
        public void paint_shaded(String s, int color, int x, int y, boolean center) {
            int _x=x;
            if (center) {
                _x=SW_p2-((g2.getFont().stringWidth(s))>>1);
            }
            g2.setColor(0);
            g2.drawString(s, _x+1, y+1, Graphics.TOP|Graphics.LEFT);
            g2.setColor(color);
            g2.drawString(s, _x, y, Graphics.TOP|Graphics.LEFT);
        }
        
        public void paint_connection_panel(String s, int size) {
            fill_shaded_rect(0x355046, (SW-size)>>1, SH-40, size, 20);
            int pos=SW_p2-(g2.getFont().stringWidth(s)>>1);
            for (int i=0; i<(frame/5)%4; ++i) {
                s+=".";
            }
            paint_shaded(s, 0xFFFFFF, pos, SH-40+5, false);
        }
        
        final int guipos[] = {20, 50, 80};
        
        public void paint_gui(boolean onlyTime) {
            g2.setClip(0,0,SW,SH);
            g2.drawImage(img[12], 0, 0, Graphics.TOP|Graphics.LEFT);
            String s=""+(roundEnd-System.currentTimeMillis()/1000);
            paint_num(s, SW-5*(s.length())-4, 2);
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
                    g2.setClip(guipos[i]-6-1, 1, 6, 8);
                    g2.drawImage(img[18], guipos[i]-6*i-6-1, 1, Graphics.TOP|Graphics.LEFT);
                    
                    g2.setClip(0,0,SW,SH);
                    g2.drawImage(img[13], guipos[i], 1, Graphics.TOP|Graphics.LEFT);
                    
                    int t=squads[val*SQUAD_H+i];
                    s=""+(t>999?999:t);
                    while (s.length()<3) {
                        s="-"+s;
                    }
                    paint_num(s, guipos[i]+3, 2);
                }
            }
        }
        
        public void paint_divide_panel() {
            g2.setClip(0,0,SW,SH);
            fill_shaded_rect(0x355046, SW_p2-50, SH_p2-40, 100, 80);
            
            for (int i=0; i<3; ++i) {
                g2.setClip(SW_p2-40, SH_p2-10+i*14, 6, 8);
                g2.drawImage(img[18], SW_p2-40-6*i, SH_p2-10+i*14, Graphics.TOP|Graphics.LEFT);
                g2.setClip(0,0,SW,SH);
                g2.setColor(0);
                g2.fillRect(SW_p2-25-2, SH_p2-10+i*14-1, 30, 11);
                if (darabolOfs==i) {
                    g2.setColor(0xFF0000);
                    g2.drawRect(SW_p2-25-2, SH_p2-10+i*14-1, 30, 11);
                }
                paint_shaded(""+darabolEgyseg[i], 0xFFFFFF, SW_p2-25, SH_p2-10+i*14, false);
                paint_shaded(divide[1], 0xFFFFFF, SW_p2+5, SH_p2-10+i*14, false);
                paint_shaded(""+squads[canSelectUnit*SQUAD_H+i], 0xFFFFFF, SW_p2+20, SH_p2-10+i*14, false);
            }
            paint_shaded(divide[0], 0xFFFFFF, 0, SH_p2-35, true);
        }
        
        final static int betukoz = 15;
        public void paint_table(int startx, int starty, int accountTxt, int newLoginOfs) {
            int y=starty;
            int x=startx;
            int w=CHAR_PER_LINE*betukoz+5;
            int h=(1+karakterek.length()/CHAR_PER_LINE)*betukoz+5;
            fill_shaded_rect(0x355046, x-7, y-5, w, h);
            fill_shaded_rect(0x355046, 55, SH-30, SW-60, 13);
            
            g2.setColor(0xFFFFFF);
            for (int i=0; i<karakterek.length(); ++i) {
                g2.drawString(karakterek.substring(i, i+1), x, y, Graphics.TOP|Graphics.LEFT);
                if (i%CHAR_PER_LINE==CHAR_PER_LINE-1) {
                    y+=betukoz;
                    x=startx;
                } else {
                    x+=betukoz;
                }
            }
            g2.setColor(0xFF0000);
            g2.drawRect(startx+(cursorPos%CHAR_PER_LINE)*betukoz-5, starty+(cursorPos/CHAR_PER_LINE)*betukoz-3, betukoz, betukoz);
            paint_shaded(account[accountTxt], 0xFFFFFF, 55-g2.getFont().stringWidth(account[accountTxt]), SH-28, false);
            g2.setClip(55, SH-30, SW-60, 13);
            g2.setColor(0xFFFFFF);
            g2.drawString(newLogin[newLoginOfs].toString(), SW-5, SH-28, Graphics.TOP|Graphics.RIGHT);
        }
        
        public int getStart(String[] menu) {
            int start=menu_selected-1;
            if (start<0) {
                start=0;
            }
            while (start+2>=menu.length) {
                --start;
            }
            return start;
        }
        
        public void paint_down_arrow(int x, int y) {
            g2.setClip(x, y, 9, 9);
            g2.drawImage(img[1], x, y, Graphics.TOP|Graphics.LEFT);
        }
        public void paint_up_arrow(int x, int y) {
            g2.setClip(x, y, 9, 9);
            g2.drawImage(img[1], x-9, y, Graphics.TOP|Graphics.LEFT);
        }
        
        public void paint_name(int x, int y, int i) {
            g2.setClip(x, y, 14, 15);
            g2.drawImage(img[19], x-anim_phase_had[frame%4], y-15*players[i], Graphics.TOP|Graphics.LEFT);
            g2.setClip(0,0,SW,SH);
            paint_shaded(server_jatekosnev[i], 0xFFFFFF, x+14+10, y+3, false);
        }
        
        public void paint_number(int x, int y, int i) {
            g2.setClip(x, y, 14, 15);
            g2.drawImage(img[19], x-anim_phase_had[frame%4], y-15*players[i], Graphics.TOP|Graphics.LEFT);
            g2.setClip(0,0,SW,SH);
            x+=20;
            paint_shaded(""+server_sereg_vegeredmeny[i], 0xFFFFFF, x, y+4, false);
            x+=45;
            g2.setClip(x, y, castleSizeX[0], castleSizeY[0]);
            g2.drawImage(img[6], x-players[i]*castleSizeX[0], y, Graphics.TOP|Graphics.LEFT);
            x+=25;
            g2.setClip(0,0,SW,SH);
            paint_shaded(""+server_varak_vegeredmeny[i], 0xFFFFFF, x, y+4, false);            
        }
        
        public void paint(Graphics g) {
            int x=0, y=0, i;
            if (g2!=g) {
                g2=g;
            }
            g.setClip(0,0,SW,SH);
            g.setFont(fontpsmall);
            String a1,a2;
            switch (scene) {
                case RULES_SCENE:
                    g.drawImage(img[0], 0, 0, Graphics.TOP|Graphics.LEFT);
                    for (y=0, i=menu_selected; y<DISPLAY_HELP_LINES; ++i, ++y) {
                        paint_shaded(rulesArray[i], 0xFFFFFF, 0, y*13+5, true);
                    }
                    if (menu_selected+DISPLAY_HELP_LINES<rulesArray.length) {
                        paint_down_arrow(SW_p2-10, SH-10);
                    }
                    if (menu_selected!=0) {
                        paint_up_arrow(SW_p2+1, SH-10);
                    }
                    paint_footer(other_txt[2], "", 0xFFFFFF);
                    break;
                case HOWTOPLAY_SCENE:
                    g.drawImage(img[0], 0, 0, Graphics.TOP|Graphics.LEFT);
                    for (y=0, i=menu_selected; y<DISPLAY_HELP_LINES; ++i, ++y) {
                        paint_shaded(h2pArray[i], 0xFFFFFF, 0, y*13+5, true);
                    }
                    if (menu_selected+DISPLAY_HELP_LINES<h2pArray.length) {
                        paint_down_arrow(SW_p2-10, SH-10);
                    }
                    if (menu_selected!=0) {
                        paint_up_arrow(SW_p2+1, SH-10);
                    }
                    paint_footer(other_txt[2], "", 0xFFFFFF);
                    break;
                case DISPLAY_INFO_SCENE:
                    g.drawImage(img[0], 0, 0, Graphics.TOP|Graphics.LEFT);
                    for (i=infoOfs, y=5; server_info_array!=null && i<infoOfs+INFO_LINES; ++i, y+=15) {
                        if (i==server_info_array.length) {
                            break;
                        }
                        paint_shaded(server_info_array[i], 0xFFFFFF, 0, y, true);
                    }
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    g.setClip(SW_p2-9, SH-10, 18, 9);
                    g.drawImage(img[1], SW_p2-9, SH-10, Graphics.TOP|Graphics.LEFT);
                    break;
                case SPEED_CALIB_SCENE:
                    g2.setColor(0);
                    g2.fillRect(0,0,SW,SH);
                    paint_footer("", other_txt[2], 0xFFFFFF);
                    x=SW_p2-7;
                    y=30;
                    g2.setClip(x+speedCalibOfs, y, 14, 15);
                    x-=anim_phase_had[frame%4];
                    g2.drawImage(img[19], x+speedCalibOfs, y, Graphics.TOP|Graphics.LEFT);
                    if (Math.abs(speedCalibOfs)==40) {
                        speedCalibDir=!speedCalibDir;
                    }
                    speedCalibOfs+=speedCalibDir?1:-1;
                    
                    g2.setClip(0,0,SW,SH);
                    paint_shaded(calib[0], setSleep?0xFFFFFF:0x696969, 0, SH_p2, true);
                    paint_shaded(calib[1], !setSleep?0xFFFFFF:0x696969, 0, SH_p2+15, true);
                    paint_shaded("Sleep="+SLEEP, 0xFFFFFF, 0, SH_p2+35, true);
                    paint_shaded("UpdateSkip="+UPDATE_DIV, 0xFFFFFF, 0, SH_p2+50, true);
                    break;
                case WAIT_FOR_GAME_SCENE:
                    background(true);
                    paint_connection_panel(other_txt[3], 120);
                    break;
                    
                case SHOW_TYPE_SCENE:
                    background(true);
                    if (!typeReaded) {
                        paint_connection_panel(other_txt[1], 120);
                    } else {
                        String[] tmp=new String[server_gtypenum];
                        for (i=0; i<tmp.length; ++i) {
                            tmp[i]=server_gtpname[i];
                        }
                        
                        paint_menu_txt(tmp, SW-95, SH-3*15, 93, 15, true, getStart(tmp), false );
                        if (menu_selected+1<tmp.length) {
                            paint_down_arrow(20, SH-23);
                        }
                        if (menu_selected!=0) {
                            paint_up_arrow(20, SH-33);
                        }
                        
                        //paint_menu_txt(tmp, SW-95, SH-server_gtypenum*15, 93, 15, true, 0);
                        paint_footer(other_txt[2], "", 0xFFFFFF);
                    }
                    break;
                    
                case WAIT_AND_TRY_AGAIN:
                    background(true);
                    fill_shaded_rect(0x355046, 15, 80, SW-30, 35);
                    y=85;
                    for (i=0; i<error_txt[4].length; ++i, y+=15) {
                        paint_shaded(error_txt[4][i], 0xFFFFFF, 0, y, true);
                    }
                    paint_footer(other_txt[2], "", 0xFFFFFF);
                    break;
                case GET_REG_INFO_SCENE:
                    background(false);
                    paint_connection_panel(other_txt[11], 120);
                    break;
                case REG_ERROR_SCENE:
                    background(false);
                    fill_shaded_rect(0x355046, 15, 30, SW-30, SH-50);
                    int ofs=(server_error>=2&&server_error<=4?server_error:0);
                    for (i=0; i<regInfo[ofs].length; ++i) {
                        paint_shaded(regInfo[ofs][i], 0xFFFFFF, 0, 40+i*15, true);
                    }
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    break;
                case REG_OK_SCENE:
                    background(false);
                    fill_shaded_rect(0x355046, 15, 30, SW-30, SH-50);
                    for (i=0; i<regInfo[1].length; ++i) {
                        paint_shaded(regInfo[1][i], 0xFFFFFF, 0, 40+i*15, true);
                    }
                    paint_footer(yesno[1], yesno[0], 0xFFFFFF);
                    break;
                    
                case OBH_SCENE:
                    g.setColor(0x515151);
                    g.fillRect(0, 0, SW, SH);
                    g.drawImage(obh, (SW-obh.getWidth())>>1,
                            (SH-obh.getHeight())>>1, Graphics.TOP|Graphics.LEFT);
                    break;
                case LOGO_SCENE:
                case LOGOUT_SCENE:
                case JUMP_TO_MAIN_MENU_SCENE:
                    background(true);
                    break;
                    
                case MAIN_MENU_SCENE:
                    background(true);
                    paint_menu_txt(menu_txt_1, SW-95, SH-3*15, 93, 15, true, getStart(menu_txt_1), false );
                    if (menu_selected+1<menu_txt_1.length) {
                        paint_down_arrow(20, SH-23);
                    }
                    if (menu_selected!=0) {
                        paint_up_arrow(20, SH-33);
                    }
                    break;
                    
                case ACCOUNT_SUBMENU_SCENE:
                    background(false);
                    //g.setColor(0);
                    g.setClip(2, 2, SW-4, 50);
                    g.drawImage(img[11], 2, 2, Graphics.TOP|Graphics.LEFT);
                    g.drawImage(img[11], 2+img[11].getWidth(), 2, Graphics.TOP|Graphics.LEFT);
                    g.setClip(0,0,SW,SH);
                    g.setColor(0);
                    g.drawRect(2, 2, SW-4, 50);
                    g.setClip(2,2,SW-4,50);
                    paint_shaded(account[1]+loginName[accountOfs], 0xFFFFFF, 0, 7, true);
                    paint_shaded(account[2]+pwd[accountOfs], 0xFFFFFF, 0, 22, true);
                    paint_shaded(account[3]+email[accountOfs], 0xFFFFFF, 0, 37, true);
                    
                    g.setClip(0,0,SW,SH);
                    
                    paint_menu_txt(accountMod, SW-95, SH-4*15, 93, 15, true, 0, true);
                    paint_footer(other_txt[2], "", 0xFFFFFF);
                    if (areyousure) {
                        g.setColor(0x355046);
                        g.fillRect(0, SH-50, SW, 50);
                        paint_shaded(other_txt[8], 0xFFFFFF, 0, SH-45, true);
                        paint_shaded(other_txt[9], 0xFFFFFF, 0, SH-35, true);
                        paint_shaded(other_txt[10], 0xFFFFFF, 0, SH-25, true);
                        paint_footer(yesno[1], yesno[0], 0xFFFFFF);
                    }
                    break;
                    
                case ACCOUNT_CHANGED:
                    background(false);
                    fill_shaded_rect(0x355046, 15, 30, SW-30, SH-50);
                    for (i=0; i<accountChanged.length; ++i) {
                        paint_shaded(accountChanged[i], 0xFFFFFF, 0, 40+i*15, true);
                    }
                    paint_footer(yesno[1], yesno[0], 0xFFFFFF);
                    break;
                    
                case INPUT_ACCOUNT_NAME:
                case INPUT_ACCOUNT_PASS:
                case INPUT_ACCOUNT_MAIL:
                    background(false);
                    paint_table(15, 5, scene-INPUT_ACCOUNT_NAME+1, scene-INPUT_ACCOUNT_NAME);
                    paint_footer(other_txt[2], newLogin[scene-INPUT_ACCOUNT_NAME].length()!=0?other_txt[7]:"", 0xFFFFFF);
                    break;
                    
                case ACCOUNT_SCENE:
                    background(false);
                    String mntmp[]=null;
                    if (accountNum<3) {
                        mntmp=new String[1+accountNum];
                        mntmp[0]=account[0];
                        x=1;
                    } else {
                        mntmp=new String[3];
                        x=0;
                    }
                    for (i=0; i<accountNum; ++i) {
                        mntmp[x+i]=loginName[i];
                    }
                    paint_menu_txt(mntmp, SW-95, SH-3*15, 93, 15, true, 0, false);
                    
                    g.setClip(5, 5, SW-10, 40);
                    g.drawImage(img[11], 5, 5, Graphics.TOP|Graphics.LEFT);
                    g.drawImage(img[11], 5+img[11].getWidth(), 5, Graphics.TOP|Graphics.LEFT);
                    g.setClip(0,0,SW,SH);
                    g.setColor(0);
                    g.drawRect(5, 5, SW-10, 40);
                    if (defaultAccount!=-1 && accountNum!=0) {
                        paint_shaded(account[4], 0xFFFFFF, 0, 15, true);
                        paint_shaded(loginName[defaultAccount], 0xFFFFFF, 0, 30, true);
                    }
                    
                    paint_footer(other_txt[2], "", 0xFFFFFF);
                    break;
                    
                case LOGIN_SCENE:
                    background(true);
                    paint_connection_panel(other_txt[0], 120);
                    break;
                    
                case ERROR_SCENE:
                    background(true);
                    fill_shaded_rect(0x355046, 15, 80, SW-30, 45);
                    y=85;
                    for (i=0; i<error_txt[lastError].length; ++i, y+=15) {
                        paint_shaded(error_txt[lastError][i], 0xFFFFFF, 0, y, true);
                    }
                    
                    break;
                    
                case SEND_ACTION_SCENE:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    paint_connection_panel(other_txt[5], 120);
                    move_kamera(true);
                    break;
                case END_OF_GAME_SCENE_2:
                    background(false);
                    paint_shaded(endofgame[0], 0xFFFFFF, 0, 5, true);
                    y=35;
                    paint_shaded(endofgame[1], 0xFFFFFF, 0, y, true); y+=15;
                    paint_shaded(server_jatekosnev[server_id_vegeredmeny[0]], 0xFFFFFF, 0, y, true); y+=15;
                    y+=15;
                    paint_shaded(endofgame[2], 0xFFFFFF, 0, y, true); y+=15;
                    int v=0;
                    while (server_id_vegeredmeny[v]!=server_hg_sajatsorszam) {
                        ++v;
                    }
                    paint_shaded(endofgame[3+v], 0xFFFFFF, 0, y, true); y+=15;
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    break;
                case END_OF_GAME_SCENE_1:
                    background(false);
                    paint_shaded(endofgame[0], 0xFFFFFF, 0, 5, true);
                    y=33;
                    for (i=0; i<Math.min(server_sereg_vegeredmeny.length, server_varak_vegeredmeny.length); ++i) {
                        x=10;
                        g.setClip(x, y, 14, 15);
                        g.drawImage(img[19], x-anim_phase_had[frame%4], y-15*players[server_id_vegeredmeny[i]], Graphics.TOP|Graphics.LEFT);
                        g.setClip(0,0,SW,SH);
                        x+=20;
                        paint_shaded(""+server_sereg_vegeredmeny[i], 0xFFFFFF, x, y+4, false);
                        x+=45;
                        g.setClip(x, y, castleSizeX[0], castleSizeY[0]);
                        g.drawImage(img[6], x-players[server_id_vegeredmeny[i]]*castleSizeX[0], y, Graphics.TOP|Graphics.LEFT);
                        x+=25;
                        g.setClip(0,0,SW,SH);
                        paint_shaded(""+server_varak_vegeredmeny[i], 0xFFFFFF, x, y+4, false);
                        y+=25;
                    }
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    break;
                    
                case DISPLAY_END_OF_TURN_INFO_0:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    if (DISPLAY_WAIT_TIME!=-1) {
                        int sx=server_hg_csata[eofTurnBattle]%server_koord_map_width;
                        int sy=server_hg_csata[eofTurnBattle]/server_koord_map_width;
                        x=this.ServerCSToClientCSX(sx, sy);
                        y=this.ServerCSToClientCSY(sy);
                        int xp=(x-kamera_eposx)*TILE_MERET_36_SZER3PER4-kamera_rposx-1;
                        int yp=(y-kamera_eposy)*TILE_MERET_36+(x&1)*TILE_MERET_PER_2_18-kamera_rposy-1;
                        yp+=GUI_HEIGHT;
                        g2.setClip(xp, yp, 38, 38);
                        g2.drawImage(img[9], xp, yp, Graphics.TOP|Graphics.LEFT);
                    }
                    break;
                    
                case DISPLAY_END_OF_TURN_INFO_1:
                case DISPLAY_END_OF_TURN_INFO_2:
                case DISPLAY_END_OF_TURN_INFO_3:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    
                    g.setClip(0,0,SW,SH);
                    fill_shaded_rect(0x355046, SW_p2-60, SH_p2-45, 120, 90);
                    g.setColor(0xFFFFFF);
                    if (server_hg_vedo_jatekos[eofTurnBattle]==server_hg_sajatsorszam) {
                        // megtamadtak a jatekost
                        paint_shaded(attack[2], 0xFFFFFF, 0, SH_p2-43, true);
                        paint_shaded(attack[3], 0xFFFFFF, 0, SH_p2-33, true);
                    } else { // a jatekos tamadott
                        paint_shaded(attack[0], 0xFFFFFF, 0, SH_p2-43, true);
                        paint_shaded(attack[1], 0xFFFFFF, 0, SH_p2-33, true);
                    }
                    
                    a1=""+(a1sereg[0]+"/"+a1sereg[1]+"/"+a1sereg[2]);
                    a2=""+(a1sereg[3]+"/"+a1sereg[4]+"/"+a1sereg[5]);
                    
                    x=a1seregStartOfs;
                    y=SH_p2-18;
                    g.setClip(x, y, 14, 15);
                    g.drawImage(img[19], x-anim_phase_had[frame%4], y-15*players[server_hg_vedo_jatekos[eofTurnBattle]], Graphics.TOP|Graphics.LEFT);
                    g.setClip(0,0,SW,SH);
                    paint_shaded(a1, 0xFFFFFF, x+14+10, y+4, false); // vedo
                    
                    x=a1seregStartOfs;
                    y=SH_p2;
                    g.setClip(x, y, 14, 15);
                    g.drawImage(img[19], x-anim_phase_had[frame%4], y-15*players[server_hg_tamado_jatekos[eofTurnBattle]], Graphics.TOP|Graphics.LEFT);
                    g.setClip(0,0,SW,SH);
                    paint_shaded(a2, 0xFFFFFF, x+14+10, y+4, false); // tamado
                    
                    if (scene!=DISPLAY_END_OF_TURN_INFO_3) {
                        paint_footer("", eofTurnInfo[2], 0xFFFFFF);
                        break;
                    }
                    boolean won=server_hg_winner[eofTurnBattle]==server_hg_sajatsorszam;
                    paint_shaded(attack[4+(won?1:0)], 0xFFFFFF, 0, SH_p2+22, true);
                    paint_shaded(attack[6], 0xFFFFFF, 0, SH_p2+32, true);
                    
                    paint_footer("", eofTurnInfo[eofTurnNextBattle!=-1?0:1], 0xFFFFFF);
                    break;
                case PRE_RUNNING_GAME_SCENE_WITH_CLIENTS:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    fill_shaded_rect(0x355046, SW_p2-60, SH_p2-50, 120, 95);
                    paint_shaded(startTxt[3]+server_actround, 0xFFFFFF, 0, SH_p2-45, true);
                    y=35;
                    for (i=0; i<last_client_state.length; ++i) {
                        if (server_client_state[i]!=last_client_state[i]) {
                            switch (server_client_state[i]) {
                                case 0:
                                    paint_shaded(server_jatekosnev[i], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    paint_shaded(CLS[0], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    paint_shaded(server_jatekosnev[i], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    paint_shaded(CLS[2], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    break;
                                case 3:
                                    paint_shaded(CLS[3], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    paint_shaded(server_jatekosnev[i], 0xFFFFFF, 0, y, true);
                                    y+=15;
                                    break;
                            }
                        }
                    }
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    break;
                    
                case PRE_RUNNING_GAME_SCENE_WITH_NUMBERS:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    fill_shaded_rect(0x355046, SW_p2-60, SH_p2-50, 120, 95);
                    paint_shaded(startTxt[3]+server_actround, 0xFFFFFF, 0, SH_p2-45, true);
                    y=33;
                    paint_number(10, y, server_hg_sajatsorszam);
                    y+=25;
/*                    for (i=0; i<jatekosok_szama; ++i) {
                        if (i==server_hg_sajatsorszam) {
                            continue;
                        }
                        paint_number(10, y, i);
                        y+=25;
                    }*/
                    for (int ii=0, tempi=pre_running_cnt; ii<Math.min(2, jatekosok_szama-1); ++tempi) {
                        tempi%=jatekosok_szama;
                        if (tempi==server_hg_sajatsorszam) {
                            continue;
                        }
                        paint_number(10, y, tempi);
                        y+=25;
                        ++ii;
                    }
                    
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    if (jatekosok_szama>3) {
                        g2.setClip(SW_p2-9, SH-10, 18, 9);
                        g2.drawImage(img[1], SW_p2-9, SH-10, Graphics.TOP|Graphics.LEFT);
                    }
                    break;
                    
                case PRE_RUNNING_GAME_SCENE_WITH_NAMES:
                    paint_game_scene(false, false);
                    paint_gui(false);
                    x=SW_p2-5-7-(fontpsmall.stringWidth(server_jatekosnev[leghosszabb_nevu_jatekos])>>1);
                    fill_shaded_rect(0x355046, SW_p2-60, SH_p2-50, 120, 95);
                    paint_shaded(startTxt[0], 0xFFFFFF, 0, SH_p2-50+2, true);
                    paint_shaded(startTxt[1], 0xFFFFFF, 0, SH_p2-40+2, true);
                    paint_shaded(startTxt[2]+server_lastround, 0xFFFFFF, 0, SH_p2-30+4, true);
                    y=SH_p2-10;
                    paint_name(x, y, server_hg_sajatsorszam);
                    y+=18;
                    for (int ii=0, tempi=pre_running_cnt; ii<Math.min(2, jatekosok_szama-1); ++tempi) {
                        tempi%=jatekosok_szama;
                        if (tempi==server_hg_sajatsorszam) {
                            continue;
                        }
                        paint_name(x, y, tempi);
                        ++ii;
                        y+=18;
                    }
                    paint_footer("", pause_txt[0], 0xFFFFFF);
                    if (jatekosok_szama>3) {
                        g2.setClip(SW_p2-9, SH-10, 18, 9);
                        g2.drawImage(img[1], SW_p2-9, SH-10, Graphics.TOP|Graphics.LEFT);
                    }                    
                    move_kamera(true);
                    break;
                    
                case RUNNING_GAME_SCENE:
                    paint_game_scene(true, true);
                    if (!keypressed[MY_KEY_POUND]) {
                        paint_gui(false);
                        g2.setClip(SW-10, SH-10, 9, 9);
                        g2.drawImage(img[1], SW-10, SH-10, Graphics.TOP|Graphics.LEFT);
                    }
                    break;
                    
                case DIVIDE_SCENE:
                    paint_game_scene(true, false);
                    g.setClip(0,0,SW,SH);
/*				for (i=0; i<4; ++i) {
                                        g.drawImage(img[11], (i&1)*img[11].getWidth(), (i>>1)*img[11].getHeight(), Graphics.TOP|Graphics.LEFT);
                                }*/
                    paint_gui(false);
                    paint_divide_panel();
                    paint_footer(divide[2], divide[3], 0xFFFFFF);
                    move_kamera(true);
                    break;
                    
                case WAITING_FOR_END_OF_TURN_SCENE:
                case WAITING_FOR_RESPONSE_SCENE:
                    paint_game_scene(false, false);
                    
                    
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
                    g.setClip(2, SH_p2-4, 9, 9);
                    g.drawImage(img[1], 2-3*9, SH_p2-4, Graphics.TOP|Graphics.LEFT);
                    g.setClip(SW-2-9, SH_p2-4, 9, 9);
                    g.drawImage(img[1], SW-2-9-2*9, SH_p2-4, Graphics.TOP|Graphics.LEFT);
                    
                    g.setClip(SW_p2-4, 2, 9, 9);
                    g.drawImage(img[1], SW_p2-4-9, 2, Graphics.TOP|Graphics.LEFT);
                    g.setClip(SW_p2-4, SH-12-2-9, 9, 9);
                    g.drawImage(img[1], SW_p2-4, SH-12-2-9, Graphics.TOP|Graphics.LEFT);
                    
                    g.setFont(fontpsmall);
                    g.setClip(0,0,SW,SH);
                    if (scene==WAITING_FOR_RESPONSE_SCENE) {
                        paint_connection_panel(other_txt[6], 120);
                    } else {
                        //	paint_shaded(g, "End of turn after "+((System.currentTimeMillis()-ETIME-WAIT_TIME)/1000)+"secs", 0xFFFFFF, 5, 5, false);
                        g.setColor(0x355046);
                        g.fillRect(0, SH-12, SW, 12);
                        g.setColor(0);
                        g.drawLine(0, SH-12, SW, SH-12);
                        paint_shaded(MISSING_RESPONSES_STRING_1+server_missingresponses+MISSING_RESPONSES_STRING_2, 0xFFFFFF, 5, SH-11, true);
                    }
                    break;
                    
                case PAUSE_SCENE:
                    paint_game_scene(true, false);
                    paint_gui(false);
                    g.setColor(0x355046);
                    g.fillRect(20, 20, SW-40, SH-30);
                    g.setColor(0);
                    g.drawRect(20, 20, SW-41, SH-31);
                    
                    g.setFont(fontpmedium);
                    paint_shaded(other_txt[4], 0xFFFFFF, 0, 30, true);
                    g.setFont(fontpsmall);
                    g.setColor(0);
                    
                    paint_menu_txt_center(pause_txt, 0, 60, 13, true);
                    break;
            }
        }
        
        public void keyPressed(int keyCode) {
            switch (keyCode) {
                case KEY_NUM3:
                    keypressed[MY_KEY_NUM3]=true;
                    break;
                case KEY_NUM7:
                    keypressed[MY_KEY_NUM7]=true;
                    break;
                case KEY_NUM9:
                    keypressed[MY_KEY_NUM9]=true;
                    break;
                case KEY_NUM1:
                    keypressed[MY_KEY_NUM1]=true;
                    break;
                case KEY_NUM2:
                    keypressed[MY_KEY_NUM2]=true;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_UP]=true;
                    }
                    break;
                case KEY_UP:
                    keypressed[MY_KEY_UP] = true;
                    break;
                case KEY_NUM8:
                    keypressed[MY_KEY_NUM8]=true;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_DOWN]=true;
                    }
                    break;
                case KEY_DOWN:
                    keypressed[MY_KEY_DOWN] = true;
                    break;
                case KEY_NUM4:
                    keypressed[MY_KEY_NUM4]=true;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_LEFT]=true;
                    }
                    break;
                case KEY_LEFT:
                    keypressed[MY_KEY_LEFT] = true;
                    break;
                case KEY_NUM6:
                    keypressed[MY_KEY_NUM6]=true;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_RIGHT]=true;
                    }
                    break;
                case KEY_RIGHT:
                    keypressed[MY_KEY_RIGHT] = true;
                    break;
                case KEY_NUM5:
                    keypressed[MY_KEY_NUM5]=true;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_FIRE]=true;
                    }
                    break;
                case KEY_FIRE:
                    keypressed[MY_KEY_FIRE] = true;
                    break;
                case KEY_NUM0:
                    keypressed[MY_KEY_NUM0] = true;
                    break;
                case KEY_RSOFT:
                    keypressed[MY_KEY_RSOFT] = true;
                    break;
                case KEY_LSOFT:
                    keypressed[MY_KEY_LSOFT] = true;
                    break;
                case KEY_POUND:
                    keypressed[MY_KEY_POUND]=true;
                    break;
                case KEY_STAR:
                    keypressed[MY_KEY_STAR]=true;
                    break;
            }
        }
        
        public void keyReleased(int keyCode) {
            switch (keyCode) {
                case KEY_NUM3:
                    keypressed[MY_KEY_NUM3]=false;
                    break;
                case KEY_NUM1:
                    keypressed[MY_KEY_NUM1]=false;
                    break;
                case KEY_NUM2:
                    keypressed[MY_KEY_NUM2]=false;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_UP]=false;
                    }
                    break;
                case KEY_UP:
                    keypressed[MY_KEY_UP]=false;
                    break;
                case KEY_NUM9:
                    keypressed[MY_KEY_NUM9]=false;
                    break;
                case KEY_NUM7:
                    keypressed[MY_KEY_NUM7]=false;
                    break;
                case KEY_NUM8:
                    keypressed[MY_KEY_NUM8]=false;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_DOWN]=false;
                    }
                    break;
                case KEY_DOWN:
                    keypressed[MY_KEY_DOWN]=false;
                    break;
                case KEY_NUM4:
                    keypressed[MY_KEY_NUM4]=false;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_LEFT]=false;
                    }
                    break;
                case KEY_LEFT:
                    keypressed[MY_KEY_LEFT]=false;
                    break;
                case KEY_NUM6:
                    keypressed[MY_KEY_NUM6]=false;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_RIGHT]=false;
                    }
                    break;
                case KEY_RIGHT:
                    keypressed[MY_KEY_RIGHT]=false;
                    break;
                case KEY_NUM5:
                    keypressed[MY_KEY_NUM5]=false;
                    if (scene!=DIVIDE_SCENE) {
                        keypressed[MY_KEY_FIRE]=false;
                    }
                    break;
                case KEY_FIRE:
                    keypressed[MY_KEY_FIRE]=false;
                    break;
                case KEY_NUM0:
                    keypressed[MY_KEY_NUM0]=false;
                    break;
                case KEY_RSOFT:
                    keypressed[MY_KEY_RSOFT]=false;
                    break;
                case KEY_LSOFT:
                    keypressed[MY_KEY_LSOFT]=false;
                    break;
                case KEY_POUND:
                    keypressed[MY_KEY_POUND]=false;
                    break;
            }
        }
        
        private Image getImage(String url) {
            ContentConnection conn=null;
            try {
                conn=(ContentConnection)Connector.open(url);
            } catch (Exception e) {
                if (conn!=null) {
                    try {
                        conn.close();
                    } catch (Exception eee) {
                    }
                }
                return null;
            }
            DataInputStream in=null;
            try {
                in=conn.openDataInputStream();
            } catch (Exception e) {
                if (in!=null) {
                    try {
                        in.close();
                    } catch (Exception ee) {
                    }
                }
                return null;
            }
            Image ret=null;
            byte imageData[];
            try {
                int length=(int)conn.getLength();
                if (length!=-1) {
                    imageData=new byte[length];
                    in.readFully(imageData);
                } else {
                    ByteArrayOutputStream out=new ByteArrayOutputStream();
                    int ch;
                    while ((ch=in.read())!=-1) {
                        out.write(ch);
                    }
                    imageData=out.toByteArray();
                    out.close();
                }
                ret=Image.createImage(imageData, 0, imageData.length);
            } catch (Exception e) {
                ret=null;
            }
            
            if (in!=null) {
                try {
                    in.close();
                } catch (Exception eeee) {
                }
            }
            if (conn!=null) {
                try {
                    conn.close();
                } catch (Exception eeeee) {
                }
            }
            return ret;
        }
        
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
                String url = server_url+"?P="+query;
//#ifdef DEBUG
//#                 System.out.println(" final req is "+url);
//#endif
                HttpConnection conn = null;
                InputStream instr = null;
                InputStreamReader inReader=null;

                try {
                    boolean vege=false;
                    int reconnect=5;
                    while (!vege) {
                        --reconnect;
                        conn = (HttpConnection)Connector.open(url, Connector.READ);
                        conn.setRequestMethod(HttpConnection.GET);
                        conn.setRequestProperty("Connection","close");
                        int rsp_code = conn.getResponseCode();
                        if (rsp_code!=HttpConnection.HTTP_OK) {
                            if (reconnect==0) {
                                server_result=999;
                                vege=true;
                            } else {
                                Thread.sleep(5000);
                            }
                        } else {
                            instr = conn.openInputStream();
                            inReader = new InputStreamReader(instr);
                            int ch;
                            while ((ch=inReader.read())!=-1) {
                                respBuff.append((char)ch);
                            }
                            vege=true;
                        }
                    }
                } catch (Exception e) {
                    server_result=999;
                } finally {
                    try {
                        if (instr!=null) {
                            instr.close();
                        }
                    } catch (IOException e) { }
                    try {
                        if (conn!=null) {
                            conn.close();
                        }
                    } catch (IOException e) { }
                    ht.clear();
                    if (server_result==999) {
                        isWorking=false;
                        return;
                    } //*/
                    
//#ifdef DEBUG
//#                     System.out.println("answer is "+respBuff.toString());
//#endif
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
//#                     System.out.println(serverResult);
//#endif
                    isWorking=false;
                }
            }
        }
        
        
        
/*	public Sound loadSound(String s) {
                Sound sound=null;
                try {
                        byte[] data;
                        InputStream stream;
                        data = new byte[20000];
                        stream = Runtime.getRuntime().getClass().getResourceAsStream(s);
                        stream.read(data);
                        sound = new Sound(data, Sound.FORMAT_WAV);
                        //sound.setGain(63);
                        stream.close();
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                return sound;
        }
 
        void playSound(int n, int loop) {
                for (int i=0; i<sounds.length; ++i) {
                        if (sounds[i].getState()!=Sound.SOUND_STOPPED) {
                                sounds[i].stop();
                        }
                }
                if (isSound) {
                        sounds[n].play(loop);
                }
        } */
        
        public void loadScore() {
            RecordStore record=null;
            char c;
            StringBuffer s;
            try {
                record = RecordStore.openRecordStore(RSNAME, false);
                try {
                    byte[] data = record.getRecord(1);
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                    
                    accountNum=dis.readShort();
//#ifdef DEBUG
//#                     System.out.println("accountNum="+accountNum);
//#endif
                    for (int i=0; i<3; ++i) {
                        s=new StringBuffer();
                        for (int j=0; j<STRLENGTH; ++j) {
                            c=dis.readChar();
                            if (c!=UNUSEDCHAR) {
                                s.append(c);
                            }
                        }
                        loginName[i]=s.toString();
//#ifdef DEBUG
//#                         System.out.println("loginName["+i+"]="+loginName[i]);
//#endif
                        
                        s=new StringBuffer();
                        for (int j=0; j<STRLENGTH; ++j) {
                            c=dis.readChar();
                            if (c!=UNUSEDCHAR) {
                                s.append(c);
                            }
                        }
                        pwd[i]=s.toString();
//#ifdef DEBUG
//#                         System.out.println("pwd["+i+"]="+pwd[i]);
//#endif
                        s=new StringBuffer();
                        for (int j=0; j<STRLENGTH; ++j) {
                            c=dis.readChar();
                            if (c!=UNUSEDCHAR) {
                                s.append(c);
                            }
                        }
                        email[i]=s.toString();
//#ifdef DEBUG
//#                         System.out.println("email["+i+"]="+email[i]);
//#endif
                    }
                    defaultAccount=dis.readInt();
                } catch(IOException ioe) {
//#ifdef DEBUG
//#                     System.out.println("loadScore #0 "+ioe);
//#endif
                }
                record.closeRecordStore();
            } catch(RecordStoreNotFoundException rsne) {
//#ifdef DEBUG
//#                 System.out.println("loadScore #1 "+rsne);
//#endif
            } catch(RecordStoreException rse) {
//#ifdef DEBUG
//#                 System.out.println("loadScore #1 "+rse);
//#endif
            }
        }
        
        final static int STRLENGTH = 25;
        final static char UNUSEDCHAR = '*';
        public void saveScore() {
            RecordStore record=null;
            StringBuffer s;
            try {
                record = RecordStore.openRecordStore(RSNAME, true);
                int nr = record.getNumRecords();
                try	{
                    ByteArrayOutputStream baostream = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baostream);
                    
                    dos.writeShort(accountNum);
                    for (int i=0; i<3; ++i) {
                        s=new StringBuffer(loginName[i]);
                        while (s.length()<STRLENGTH) {
                            s.append(UNUSEDCHAR);
                        }
//#ifdef DEBUG
//#                         System.out.println("0:"+s.toString().length());
//#endif
                        dos.writeChars(s.toString());
                        
                        s=new StringBuffer(pwd[i]);
                        while (s.length()<STRLENGTH) {
                            s.append(UNUSEDCHAR);
                        }
//#ifdef DEBUG
//#                         System.out.println("1:"+s.toString().length());
//#endif
                        dos.writeChars(s.toString());
                        
                        s=new StringBuffer(email[i]);
                        while (s.length()<STRLENGTH) {
                            s.append(UNUSEDCHAR);
                        }
//#ifdef DEBUG
//#                         System.out.println("2:"+s.toString().length());
//#endif
                        dos.writeChars(s.toString());
                    }
                    dos.writeInt(defaultAccount);
                    
                    byte[] data = baostream.toByteArray();
                    if(nr==0) {
                        record.addRecord(data, 0, data.length);
                    } else {
                        record.setRecord(1, data, 0, data.length);
                    }
                } catch(IOException ioe) {
//#ifdef DEBUG
//#                     System.out.println("saveScore #0 "+ioe);
//#endif
                }
                record.closeRecordStore();
            } catch(RecordStoreFullException rsne) {
//#ifdef DEBUG
//#                 System.out.println("saveScore #1 "+rsne);
//#endif
            } catch(RecordStoreNotFoundException rsne) {
//#ifdef DEBUG
//#                 System.out.println("saveScore #2 "+rsne);
//#endif
            } catch(RecordStoreException rse) {
//#ifdef DEBUG
//#                 System.out.println("saveScore #3 "+rse);
//#endif
            }
        }
        
    } // GameCanvas
    
} // Midlet
