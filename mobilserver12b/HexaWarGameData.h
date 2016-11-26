#if !defined(_HEXAWARGAMEDATA_H__INCLUDED_)
#define _HEXAWARGAMEDATA_H__INCLUDED_

#define MAX_SEREG_LETSZAM   1000000

// client states
#define CLS_PLAYING     0
#define CLS_FINISHED    1
#define CLS_LOST        2
#define CLS_DROPPED     3


// jatekos aktualis allasa
struct player_standing
{
  int varak;
  int sereg;
  int hossz; // a kor amikor vesztett/kiesett vagy (last_round+1) ami a kezdeti beallitas, minel nagyobb annal jobb az eredmenye
};

class MovementSequence
{
public:
  int sereg[3];
  tpodlist<large> movementX;
  tpodlist<large> movementY;
  int act_move; // megindexeli a movement listakat
  MovementSequence(textmap* ptmParams);
  bool validate(HexaWarGraph* pHexaWarGraph, string& err_msg);
  inline int sereg_sum() { return sereg[0]+sereg[1]+sereg[2]; }
  void maximalize(int* max_sereg); // a max_sereg-re redukal ahol nagyobb a sereg
  inline void wipe_sereg() { sereg[2] = sereg[1] = sereg[0] = 0; } // sereg torlese
};

typedef tobjlist<MovementSequence> MovementSequenceList;

// az utoljara lezart kor csatainak adatai ebben
class LastRoundBattles
{
public:
  tpodlist<int> csata;

  tpodlist<int> vedo_jatekos;
  tpodlist<int> deffendsereg0;
  tpodlist<int> deffendsereg1;
  tpodlist<int> deffendsereg2;

  tpodlist<int> tamado_jatekos;
  tpodlist<int> offendsereg0;
  tpodlist<int> offendsereg1;
  tpodlist<int> offendsereg2;
  
  tpodlist<int> winner;
  tpodlist<int> aftsereg0;
  tpodlist<int> aftsereg1;
  tpodlist<int> aftsereg2;

  void add(int cs, int vj, int ds0, int ds1, int ds2,
           int tj, int os0, int os1, int os2,
           int w,  int as0, int as1, int as2);
  void clear();
  void send(textmap& tmResponse);
};

class LastRoundRegen
{
public:
  tpodlist<int> regen;
  tpodlist<int> regensereg0;
  tpodlist<int> regensereg1;
  tpodlist<int> regensereg2;
  void add(int r, int rs0, int rs1, int rs2);
  void clear();
  void send(textmap& tmResponse);
};

class HexaWarGameData : public GameData  
{
public:
  int playernum;
	HexaWarGraph* pHexaWarGraph;
	
	datetime start_time;      // az idopont amikor a jatek elkezdodott
  datetime round_timespan;  // fordulo ideje

  int last_round;           // melyik az utolso fordulo, amikor vege a jateknak

  int last_closed_round;      // melyik fordulot szamolta ki szerver utoljara
  datetime last_closed_time;  // utoljara kiszamolt fordulo idopontja: az ez UTAN erkezo uzenetek szamitanak a kovetkezo forduloba

  tpodlist<datetime> last_step_time;  // jatekosok mikor leptek utoljara
  tpodlist<int> client_closed_round; // melyik fordulo zarasa lett utoljara elkuldve a kliensnek
  tpodlist<int> client_state;

  inline int is_client_synchronized(int client_index) { return client_closed_round[client_index]>=last_closed_round; }

  inline bool is_client_gameover(int player_index) { return client_state[player_index]!=CLS_PLAYING; }

  //bool is_others_gameover(int my_index);
  
  void sendGameOver(textmap& tmResponse); // a kliensnek kuldott utolso uzenet plussz tartalma

  tobjlist<MovementSequenceList> movement_sequences; // mindegyik jatekos osszes feljegyzett mozgasprogramja

  LastRoundBattles last_round_battles;
  LastRoundRegen last_round_regen;

  int missing_responses();

	HexaWarGameData(int iplayernum, textmap& data);
	virtual ~HexaWarGameData();

  void sendSeregMovementDirections(textmap& tmResponse);
  void sendStateAndBattlesAndRegen(textmap& tmResponse); // kliensnek kuldi utolso kor esemenyeit es az aktualis allapotot

  void sendClientState(textmap& tmResponse);
  
  void calculateActualRound(); // egy kor kiszamolasa az elozoleg elmentett mozgasprogramok alapjan
  void calculateRegeneration(Random& rnd_gen); // calculateActualRound() hivja meg
  void calculateOneMove(int act_player_index, Random& rnd_gen); // calculateActualRound() hivja meg
  void calculatePlayerStandingsAndSetStates(); // kor vegen hogy kiszamoljuk a jatekosok allasat es uj allapotat
  
  tpodlist<player_standing> player_standings;

  tpodlist<int> scores_id_list;
  tpodlist<int> scores_points_list;
  tpodlist<int> scores_varak_list;
  tpodlist<int> scores_sereg_list;
  void calculateAndSendPlayerPoints(textmap& tmResponse);
};


#endif // !defined(_HEXAWARGAMEDATA_H__INCLUDED_)
