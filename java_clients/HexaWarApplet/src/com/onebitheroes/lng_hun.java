package com.onebitheroes;

public class lng_hun extends lng_body {
    public String MAP="+: T�rk�p";
    public String HELP="Seg�ts�g";
    public String CLOSE_HELP="Bez�r";
    public String STR_LOGIN="Bel�p�s";
    public String CLICK_HERE="Itt";
    public String STR_REGISTER="Regisztr�ci�";
    public String CLICK_HERE_MORE="regisztr�lhatsz.";
    public String STR_WELCOME="�dv�z�llek! A folytat�shoz be kell jelentkezned!";
    public String STR_NAME="Felhaszn�l�n�v:";
    public String STR_PASS="Jelsz�:";
    public String STR_PASS2="Jelsz� �jra:";
    public String STR_MAIL="E-Mail:";
    public String CLIENT_LANG = "HU";
    public String UNDO_LABEL = "Visszavon";
    public String MISSING_RESPONSES_STRING_1 = "V�rakoz�s ";
    public String MISSING_RESPONSES_STRING_2 = " j�t�kosra.";
    public String CREATE_ACCOUNT="Fi�k regisztr�l�sa";
    public String UN_NULL="Hiba: A 'Felhaszn�l�n�v' mez\u0151 �res.";
    public String PASS_NULL="Hiba: A 'Jelsz�' mez\u0151 �res.";
    public String MAIL_NULL="Hiba: Az 'E-Mail' mez\u0151 �res.";
    public String PASSWORDS_DONT_MATCH="Hiba: A jelszavak nem egyeznek!";
    
    
    public String endofgame[]={"V�ge a j�t�knak!", "A gy\u0151ztes", "Helyez�sed: ", // 0 1 2
        "els\u0151!", "m�sodik!", "harmadik!", "negyedik!", "�t�dik!", "hatodik!", "hetedik!", "nyolcadik!"}; // 3 4 5 6 7 8 9 10
    public String menu_txt_1[] ={"Fi�k be�ll�t�sok", "Bel�p�s", "Szab�lyok", "Hogyan j�tsz", "Kil�p�s"};
    public String other_txt[] = {"Csatlakoz�s", "T�pusok lek�rdez�se", // 01
        "Vissza",	"Csatlakoz�s", // 23
        "Men�", "L�p�s k�ld�se", // 45
        "Ellenfelek l�p�sei", "K�vetkez\u0151", // 67
        "A kiv�lasztott fi�kot", "el fogom t�vol�tani.", //89
        "Biztos vagy benne?", "Regisztr�l�s" }; // 10 11
    public String startTxt[] = {"A j�t�k", "elkezd\u0151d�tt!",
        "K�r�k sz�ma: ", "Aktu�lis k�r: "};
    public String eofTurnInfo[] = { "K�vetkez\u0151", "Tov�bb", "Kihagy"};
    public String attack[]= { "Megt�madtad az", "ellens�get!",
        "Az ellens�g megt�madta", "a seregedet",
        "Elvesztetted ", "Megnyerted ",
        "a csat�t!",};
    public String divide[] = {"Sereg darabol�sa", " / ", "Darabol", "M�gse",
        "5:Mozgat", "0:Darabol"};
    public String account[] = {"�j fi�k", "N�v: ", "Jelsz�: ", "E-Mail: ",
        "Kiv�lasztott fi�k: "};
    public String accountMod[] = {"Kiv�laszt", "M�dos�t", "T�r�l", "Regisztr�l"};
    public String accountChanged[] = {"A fi�kot", "elt�roltam.", "Regisztr�lni is",
        "szeretn�d?"};
    public String regInfo[] = { "A regisztr�ci� sikertelen volt.",
        "A regisztr�ci� siker�lt! Szeretn�l ezzel a fi�kkal bel�pni?",
        "A regisztr�ci� sikertelen volt. Hib�s a jelsz�.",
        "A regisztr�ci� sikertelen volt. Hib�s a felhaszn�l�n�v.",
        "A regisztr�ci� sikertelen volt. A felhaszn�l�n�v m�r foglalt.",
    };
    
    public String yesno[] = {"Igen", "Nem"};
    public String pause_txt[] = { "Tov�bb", "K�r kihagy�sa", "Kil�p�s" };
    public String error_txt[][] = { {"A bel�p�s sikertelen!", "Hib�s a jelsz� vagy", "a felhaszn�l�n�v."}, // 0
        {"Ismeretlen hiba."}, // 1
        {"A j�t�kt�pusok","lek�r�se sikertelen!"}, // 2
        {"�rv�nytelen j�t�k!", ""}, // 3
        {"A bel�p�s siker�lt,", "v�rok az ellenfelekre."}, // 4
        {"Hib�s j�t�kt�pus!", ""}, // 5
        {"Hib�s k�r.", "(Connection", "timeout?)"}, // 6
        {"", "Hib�s l�p�s", "(Ez asszem bug)"}, // 7
        {"Nem tudok csatlakozni", "a h�l�zathoz."}, // 8
        {"A szerver foglalt.", "K�rlek pr�b�lkozz", " egy kicsit k�s\u0151bb."}, // 9
    };
    
    public String howtoplayTxt = "Hogyan j�tsz\n\n"+
            "Mozg�s: a t�rk�pen "+
            "mozogni az akci� billenty\u0171kkel illetve az 1 2 3 4 6 7 8 9 gombokkal lehet. "+
            "Sereget kiv�lasztani az 5-�s gombbal lehet. Kiv�laszt�s ut�n a sereget a mozg�s "+
            "billenty\u0171kkel lehet ir�ny�tani a k�v�nt c�lhoz. A mozg�s befejez�s�t az 5-�s "+
            "gomb lenyom�s�val lehet jelezni.\nA j�t�kos a l�p�s eredm�ny�r\u0151l a k�r v�g�n"+
            "�zenetben �rtes�l.\n\nLehet\u0151s�g van a sereg egy r�sz�nek mozgat�s�ra is. "+
            "A sereget a \"0\"-�s gombbal lehet feldarabolni. Mozg� seregr\u0151l is lev�laszthat� "+
            "csapat, ekkor a lev�lasztott katon�k az �j c�l fel� mozognak, m�g a marad�k az "+
            "eredeti c�l fel� folytatja �tj�t.\n\nAz egys�geket az eg�r seg�ts�g�vel is lehet navig�lni. "+
            "Egy sereg mozgat�s�t a jobb eg�rgombbal lehet megkezdeni, az �tvonalat a bal eg�rgombbal "+
            "lehet kijel�lni, az egys�g mozg�s�nak pedig a jobb gombbal fejezhet\u0151 be.\nA k�l�nb�z\u0151 inform�ci�s "+
            "panelek a numerikus billenyt\u0171zet Enter gombj�val is bez�rhat�ak.";
    
    public String rulesTxt = "Szab�lyok\n\nA HexaWars egy k�r�kre osztott multiplayer "+
            "strat�giai j�t�k.\n\nA j�t�k c�lja er\u0151d�k elfoglal�sa a seregeiddel. "+
            "Kezdetben mindenkinek azonos sz�m� er\u0151dje van, az er\u0151d�kben egy-egy "+
            "sereggel.\nA j�t�kt�r t�bbi r�sz�n �res er\u0151d�k tal�lhat�k.\n\n"+
            "Minden k�r meghat�rozott ideig tart. A j�t�kosok minden k�rben egyszerre "+
            "mozoghatnak. Egy paranccsal egy seregnek t�bb k�rnyi mozg�s�t is meg lehet "+
            "el\u0151re adni.\n\nEr\u0151d�t elfoglalni az er\u0151d�n saj�t sereggel val� meg�ll�ssal, "+
            "illetve �thalad�ssal lehet. Ha az er\u0151d �res, akkor csata n�lk�l az �j "+
            "tulajdonos birtok�ba ker�l.\n\nHa egy sereg ellenf�l sereg�vel tal�lkozik, "+
            "akkor automatikusan lezajlik a csata.\n\nAz ut�np�tl�st az elfoglalt "+
            "er\u0151d�kben k�r�nk�nt toborzott �j katon�k jelentik.\n\n"+
            "A j�t�knak v�ge, ha az egyik j�t�kos az �sszes er\u0151d�t elfoglalta vagy "+
            "ha lej�rt az utols� k�r. Ha lej�rt az utols� k�r, akkor az gy\u0151z, akinek t�bb "+
            "elfoglalt er\u0151dje van, ha az er\u0151d�k sz�ma azonos, akkor a t�bb katon�val "+
            "rendelkez\u0151 nyer.";
    
    final String CLS[]={"csatlakozott", "", "legy\u0151zt�k", "Nem v�laszolt"};
}
