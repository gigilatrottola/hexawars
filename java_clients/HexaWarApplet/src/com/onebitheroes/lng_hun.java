package com.onebitheroes;

public class lng_hun extends lng_body {
    public String MAP="+: Térkép";
    public String HELP="Segítség";
    public String CLOSE_HELP="Bezár";
    public String STR_LOGIN="Belépés";
    public String CLICK_HERE="Itt";
    public String STR_REGISTER="Regisztráció";
    public String CLICK_HERE_MORE="regisztrálhatsz.";
    public String STR_WELCOME="Üdvözöllek! A folytatáshoz be kell jelentkezned!";
    public String STR_NAME="Felhasználónév:";
    public String STR_PASS="Jelszó:";
    public String STR_PASS2="Jelszó újra:";
    public String STR_MAIL="E-Mail:";
    public String CLIENT_LANG = "HU";
    public String UNDO_LABEL = "Visszavon";
    public String MISSING_RESPONSES_STRING_1 = "Várakozás ";
    public String MISSING_RESPONSES_STRING_2 = " játékosra.";
    public String CREATE_ACCOUNT="Fiók regisztrálása";
    public String UN_NULL="Hiba: A 'Felhasználónév' mez\u0151 üres.";
    public String PASS_NULL="Hiba: A 'Jelszó' mez\u0151 üres.";
    public String MAIL_NULL="Hiba: Az 'E-Mail' mez\u0151 üres.";
    public String PASSWORDS_DONT_MATCH="Hiba: A jelszavak nem egyeznek!";
    
    
    public String endofgame[]={"Vége a játéknak!", "A gy\u0151ztes", "Helyezésed: ", // 0 1 2
        "els\u0151!", "második!", "harmadik!", "negyedik!", "ötödik!", "hatodik!", "hetedik!", "nyolcadik!"}; // 3 4 5 6 7 8 9 10
    public String menu_txt_1[] ={"Fiók beállítások", "Belépés", "Szabályok", "Hogyan játsz", "Kilépés"};
    public String other_txt[] = {"Csatlakozás", "Típusok lekérdezése", // 01
        "Vissza",	"Csatlakozás", // 23
        "Menü", "Lépés küldése", // 45
        "Ellenfelek lépései", "Következ\u0151", // 67
        "A kiválasztott fiókot", "el fogom távolítani.", //89
        "Biztos vagy benne?", "Regisztrálás" }; // 10 11
    public String startTxt[] = {"A játék", "elkezd\u0151dött!",
        "Körök száma: ", "Aktuális kör: "};
    public String eofTurnInfo[] = { "Következ\u0151", "Tovább", "Kihagy"};
    public String attack[]= { "Megtámadtad az", "ellenséget!",
        "Az ellenség megtámadta", "a seregedet",
        "Elvesztetted ", "Megnyerted ",
        "a csatát!",};
    public String divide[] = {"Sereg darabolása", " / ", "Darabol", "Mégse",
        "5:Mozgat", "0:Darabol"};
    public String account[] = {"Új fiók", "Név: ", "Jelszó: ", "E-Mail: ",
        "Kiválasztott fiók: "};
    public String accountMod[] = {"Kiválaszt", "Módosít", "Töröl", "Regisztrál"};
    public String accountChanged[] = {"A fiókot", "eltároltam.", "Regisztrálni is",
        "szeretnéd?"};
    public String regInfo[] = { "A regisztráció sikertelen volt.",
        "A regisztráció sikerült! Szeretnél ezzel a fiókkal belépni?",
        "A regisztráció sikertelen volt. Hibás a jelszó.",
        "A regisztráció sikertelen volt. Hibás a felhasználónév.",
        "A regisztráció sikertelen volt. A felhasználónév már foglalt.",
    };
    
    public String yesno[] = {"Igen", "Nem"};
    public String pause_txt[] = { "Tovább", "Kör kihagyása", "Kilépés" };
    public String error_txt[][] = { {"A belépés sikertelen!", "Hibás a jelszó vagy", "a felhasználónév."}, // 0
        {"Ismeretlen hiba."}, // 1
        {"A játéktípusok","lekérése sikertelen!"}, // 2
        {"Érvénytelen játék!", ""}, // 3
        {"A belépés sikerült,", "várok az ellenfelekre."}, // 4
        {"Hibás játéktípus!", ""}, // 5
        {"Hibás kör.", "(Connection", "timeout?)"}, // 6
        {"", "Hibás lépés", "(Ez asszem bug)"}, // 7
        {"Nem tudok csatlakozni", "a hálózathoz."}, // 8
        {"A szerver foglalt.", "Kérlek próbálkozz", " egy kicsit kés\u0151bb."}, // 9
    };
    
    public String howtoplayTxt = "Hogyan játsz\n\n"+
            "Mozgás: a térképen "+
            "mozogni az akció billenty\u0171kkel illetve az 1 2 3 4 6 7 8 9 gombokkal lehet. "+
            "Sereget kiválasztani az 5-ös gombbal lehet. Kiválasztás után a sereget a mozgás "+
            "billenty\u0171kkel lehet irányítani a kívánt célhoz. A mozgás befejezését az 5-ös "+
            "gomb lenyomásával lehet jelezni.\nA játékos a lépés eredményér\u0151l a kör végén"+
            "üzenetben értesül.\n\nLehet\u0151ség van a sereg egy részének mozgatására is. "+
            "A sereget a \"0\"-ás gombbal lehet feldarabolni. Mozgó seregr\u0151l is leválasztható "+
            "csapat, ekkor a leválasztott katonák az új cél felé mozognak, míg a maradék az "+
            "eredeti cél felé folytatja útját.\n\nAz egységeket az egér segítségével is lehet navigálni. "+
            "Egy sereg mozgatását a jobb egérgombbal lehet megkezdeni, az útvonalat a bal egérgombbal "+
            "lehet kijelölni, az egység mozgásának pedig a jobb gombbal fejezhet\u0151 be.\nA különböz\u0151 információs "+
            "panelek a numerikus billenyt\u0171zet Enter gombjával is bezárhatóak.";
    
    public String rulesTxt = "Szabályok\n\nA HexaWars egy körökre osztott multiplayer "+
            "stratégiai játék.\n\nA játék célja er\u0151dök elfoglalása a seregeiddel. "+
            "Kezdetben mindenkinek azonos számú er\u0151dje van, az er\u0151dökben egy-egy "+
            "sereggel.\nA játéktér többi részén üres er\u0151dök találhatók.\n\n"+
            "Minden kör meghatározott ideig tart. A játékosok minden körben egyszerre "+
            "mozoghatnak. Egy paranccsal egy seregnek több környi mozgását is meg lehet "+
            "el\u0151re adni.\n\nEr\u0151döt elfoglalni az er\u0151dön saját sereggel való megállással, "+
            "illetve áthaladással lehet. Ha az er\u0151d üres, akkor csata nélkül az új "+
            "tulajdonos birtokába kerül.\n\nHa egy sereg ellenfél seregével találkozik, "+
            "akkor automatikusan lezajlik a csata.\n\nAz utánpótlást az elfoglalt "+
            "er\u0151dökben körönként toborzott új katonák jelentik.\n\n"+
            "A játéknak vége, ha az egyik játékos az összes er\u0151döt elfoglalta vagy "+
            "ha lejárt az utolsó kör. Ha lejárt az utolsó kör, akkor az gy\u0151z, akinek több "+
            "elfoglalt er\u0151dje van, ha az er\u0151dök száma azonos, akkor a több katonával "+
            "rendelkez\u0151 nyer.";
    
    final String CLS[]={"csatlakozott", "", "legy\u0151zték", "Nem válaszolt"};
}
