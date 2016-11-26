#include <math.h>
#include <stdlib.h>
#include "include.h"
#include "templates.hpp"


////////////////////////////////////////////////////////////////////////////////
// Laci harcfuggveny BEGIN

void reduceOne(int kivonando, 
               int& x, //adott tipusu katona szama
               int& maradek)
{
  if(x>=kivonando)
	{
	  x = x - kivonando;
	}
	else
	{
	  maradek += kivonando - x;
	  x = 0;
	}
}

void reduceRemaining(int kivonando, 
               int& x, //adott tipusu katona szama
               int& maradek)
{
  if(x>=kivonando)
	{
	  x = x - kivonando;
	  maradek -= kivonando;
	}
	else
	{
	  maradek = kivonando - x;
	  x = 0;
	}
}

void reduceArmy(int remainingAmmount, int &aX, int &bX, int &cX) // sereg meretet csokkento fuggveny
{
	int osszEredeti = aX + bX + cX;
	int toDie = osszEredeti - remainingAmmount;
	int kivonando = toDie/3;
	int maradek = toDie - (kivonando*3);
	reduceOne(kivonando,aX,maradek);
	reduceOne(kivonando,bX,maradek);
	reduceOne(kivonando,cX,maradek);
	if(maradek>0)
	{
	  reduceRemaining(maradek,aX,maradek);
	}
	if(maradek>0)
	{
	  reduceRemaining(maradek,bX,maradek);
	}
	if(maradek>0)
	{
	  reduceRemaining(maradek,cX,maradek);
	}
}

//megcsereli a sorrendet, hogy veletlennek tunjon az eredmeny
void switchArmy(int gyoztesSereg,int &aX, int &bX, int &cX, Random& rnd_gen)
{
  int sorrend = rnd_gen.getInt(1,6);
  switch (sorrend)
  {
    case 1:
      reduceArmy(gyoztesSereg, aX, bX, cX);
      break;
    case 2:
      reduceArmy(gyoztesSereg, aX, cX, bX);
      break;
    case 3:
      reduceArmy(gyoztesSereg, bX, aX, cX);
      break;
    case 4:
      reduceArmy(gyoztesSereg, bX, cX, aX);
      break;
    case 5:
      reduceArmy(gyoztesSereg, cX, aX, bX);
      break;
    case 6:
      reduceArmy(gyoztesSereg, cX, bX, aX);
      break;
  }
}


void increaseArmy(int &aX, int &bX, int &cX, bool varbanVan)
{
  int novekmenyszazalek = 10;
  if(varbanVan)
  {
    aX+=0;
    bX+=0;
    cX+=0;
  }
  else
  {
    aX+=(aX*novekmenyszazalek/100);
    bX+=(aX*novekmenyszazalek/100);
    cX+=(aX*novekmenyszazalek/100);
  }
  return;
}

//ha az ertek kisebb mint a megadott szint, akkor erteket a szintre allitja
void maxBackInt(int level, int &value)
{
  if(level>value)
  {
    value = level;
  }
  return;
}

void executeFight(int &aA, int &bA, int &cA, //in-out A katonainak szama tipusonkent, fuggveny valtoztatja 
                  int &aB, int &bB, int &cB, //in-out B katonainak szama tipusonkent, fuggveny valtoztatja 
				          int &winner, //out param 0: mind meghalt; 1: A nyert; 2: B nyert 
				          int varbanVan, //in param 0: mezon vannak; -1: A varban van; 1: B varban van
                  Random& rnd_gen)
{
	int elonyAB; // negativ: elony A-nal, pozitiv: elony B-nel
	int elonyA[3]; // a kulonbozo katonakbol A-nak mennyi elonye van
	int elonyB[3]; // a kulonbozo katonakbol B-nek mennyi elonye van
	
	// ero viszonyok kiszamitasa:
	/*
	* katona tipusok ko-papir-ollo rendszerben erosebbek egymasnal
	* az ero aranya 1:0.7-hez.
	* az alabbi szamitas nem pontos, csak kozelites
	*/
	if(aA > 0 && bB > 0)
	{
	  if(bB<aA)
	  {
	    elonyA[0] = bB - (int)(0.7 * bB);
	  }
	  else
	  {
	    elonyA[0] = aA - (int)(0.7 * aA);
	  }
	  maxBackInt(0,elonyA[0]);
	}
	else
	{
	  elonyA[0] = 0;
	}
	
	if(bA > 0 && cB > 0)
	{
	  if(cB<bA)
	  {
	    elonyA[1] = cB - (int)(0.7 * cB);
	  }
	  else
	  {
	    elonyA[1] = bA - (int)(0.7 * bA);
	  }
	  maxBackInt(0,elonyA[1]);
	}
	else
	{
	  elonyA[1] = 0;
	}
	
	if(cA > 0 && aB > 0)
	{
	  if(aB<cA)
	  {
  	  elonyA[2] = aB - (int)(0.7 * aB);
  	}
  	else
  	{
  	  elonyA[2] = cA - (int)(0.7 * cA);
  	}
  	maxBackInt(0,elonyA[2]);
	}
	else
	{
	  elonyA[2] = 0;
	}
	

	int osszElonyA = elonyA[0] + elonyA[1] + elonyA[2];

  
	if(aB > 0 && bA > 0)
	{
	  if(bA<aB)
	  {
	    elonyB[0] = bA - (int)(0.7 * bA);
	  }
	  else
	  {
	    elonyB[0] = aB - (int)(0.7 * aB);
	  }
	  maxBackInt(0,elonyB[0]);
	}
	else
	{
	  elonyB[0] = 0;
	}
	
	if(bB > 0 && cA > 0)
	{
	  if(cA<bB)
	  {
	    elonyB[1] = cA - (int)(0.7 * cA);
	  }
	  else
	  {
	    elonyB[1] = bB - (int)(0.7 * bB);
	  }
	  maxBackInt(0,elonyB[1]);
	}
	else
	{
	  elonyB[1] = 0;
	}
	
	if(cB > 0 && aA > 0)
	{
	  if(aA<cB)
	  {
	    elonyB[2] = aA - (int)(0.7 * aA);
	  }
	  else
	  {
	    elonyB[2] = cB - (int)(0.7 * cB);
	  }
	  maxBackInt(0,elonyB[2]);
	}
	else
	{
	  elonyB[2] = 0;
	}

	int osszElonyB = elonyB[0] + elonyB[1] + elonyB[2];
	


	elonyAB = osszElonyB - osszElonyA;
	


	int varhErtek = aB + bB + cB;
	

	
	//varhErtek eltolasa a ko-papir-ollo cucc miatt:
	varhErtek += elonyAB;
	

	
	int mi = 0; // varhato ertektol negativ iranyban eltero szoras
	maxBackInt((int)(varhErtek *0.7), mi);
	maxBackInt((int)(varhErtek - (aA + bA + cA)),mi);
	
	int ma = mi + (varhErtek - mi)*2; //varhato ertektol pozitiv iranyban eltero szoras
	
	int eredmeny = rnd_gen.getInt(mi, ma);
	
	if(eredmeny < (aA + bA + cA)) //A nyerte a csatat!
	{
	  winner = 1;
	  int gyoztesSereg = (aA + bA + cA) - eredmeny; // mert A inverz modon szerepelt az x tengelyen
	  
	  // ellenorzes: eredmeny sereg nem lehet nagyobb, mint a kiindulo
		if(gyoztesSereg > (aA + bA + cA))
		{
			gyoztesSereg = (aA + bA + cA);
		}
	  
	  //reduceArmy(gyoztesSereg, aA, bA, cA);
		switchArmy(gyoztesSereg, aA, bA, cA, rnd_gen);
		
		// B katonainak nullazasa:
		aB = 0;
		bB = 0;
		cB = 0;
	}
	else // B nyerte a csatat
	{
	  winner = 2;
	  int gyoztesSereg = eredmeny - (aA + bA + cA);
	  
	  // ellenorzes: eredmeny sereg nem lehet nagyobb, mint a kiindulo
		if(gyoztesSereg > (aB + bB + cB))
		{
			gyoztesSereg = (aB + bB + cB);
		}
		
		//reduceArmy(gyoztesSereg, aB, bB, cB);
		switchArmy(gyoztesSereg, aB, bB, cB, rnd_gen);

		// A katonainak nullazasa:
		aA = 0;
		bA = 0;
		cA = 0;
		
	}
}

void increaseInCastle(int &aX, int &bX, int &cX, Random& rnd_gen)
{
  int novekmenydb = 50;
  int novekszik = rnd_gen.getInt(1,3);
  switch (novekszik)
  {
  case 1:
    aX+=novekmenydb;
    break;
  case 2:
    bX+=novekmenydb;
    break;
  case 3:
    cX+=novekmenydb;
    break;
  }
}

// Laci harcfuggveny END.
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
// jatek eredmeny szamitas

// jatekos adatok sorbarendezeshez
struct player_points
{
  int id;     // a jatekos sorszama a jatekban
  int varak;  // megszerzett varak szama
  int sereg;  // osszes katonajanak szama (mindharom fajta)
  int hossz;  // mennyi korig jatszott
  int points; // mennyi pontot kapott
};

int compare_players(const void* pp1, const void* pp2)
{
  if (((player_points*)pp1)->varak < ((player_points*)pp2)->varak) return 1;
  if (((player_points*)pp1)->varak > ((player_points*)pp2)->varak) return -1;
  // varak szama azonos ha idaig jutottunk
  if (((player_points*)pp1)->sereg < ((player_points*)pp2)->sereg) return 1;
  if (((player_points*)pp1)->sereg > ((player_points*)pp2)->sereg) return -1;
  // seregek is azonosak, ekkor a nagyobb korszam szamit
  if (((player_points*)pp1)->hossz < ((player_points*)pp2)->hossz) return 1;
  if (((player_points*)pp1)->hossz > ((player_points*)pp2)->hossz) return -1;
  // minden azonos :)
  return 0;
}

void HexaWarGameData::calculateAndSendPlayerPoints(textmap& tmResponse)
{
  player_points* player_point_array = new player_points[playernum];
  // az utolso kor utan szamitott adatok atmasolasa a sorbarendezendo tombbe
  for (int i=0; i<playernum; i++)
  {
    player_point_array[i].id = i;
    player_point_array[i].varak = player_standings[i].varak;
    player_point_array[i].sereg = player_standings[i].sereg;
    player_point_array[i].hossz = player_standings[i].hossz;
    player_point_array[i].points = 0;
  }
  // gyorsrendezes
  qsort(player_point_array, playernum, sizeof(player_points), compare_players);
  
  // ez az altalanos kod lenne:
  //player_point_array[playernum-1].points = 0; // utolso helyezett nem kap pontot
  //for (int i=playernum-2; i>=0; i--) // utolso elottitol az elsoig ciklus
  //{
  //  // ha azonos az erteke az alatta levovel akkor azonos pontszamot kap vele
  //  if (compare_players((void*)(player_point_array+i),(void*)(player_point_array+i+1))==0)
  //    player_point_array[i].points = player_point_array[i+1].points;
  //  else // ha nem azonos akkor a helyezes szerinti pontszamot kapja
  //    player_point_array[i].points = playernum-i;
  //}
  // helyette ez van:
  // kapott pontok kiszamitasa jatektipustol fuggoen
  int gtypeid = (int)str2num(gametypeid);
  // 0 : 2 player beginner -> nincsenek pontok
  // 1 : 2 player -> 2,0 pontok
  // 2 : 3 player beginner -> nincs pont
  // 3 : 3 player -> 3,1,0
  if (playernum==2)
  {
    if (gtypeid==1)
    {
      player_point_array[0].points = 2;
    }
  }
  else if (playernum==3)
  {
    if (gtypeid==3)
    {
      player_point_array[0].points = 3;
      player_point_array[1].points = 1;
    }
  }

  // kimenet feltoltese adattal
  for (int i=0; i<playernum; i++) 
  {
    scores_id_list[i]     = player_point_array[i].id;
    scores_points_list[i] = player_point_array[i].points;
    scores_varak_list[i]  = player_point_array[i].varak;
    scores_sereg_list[i]  = player_point_array[i].sereg;
  }
  delete[] player_point_array;
  tmResponse.put("id", IntListtoCSIS<int>(scores_id_list));
  tmResponse.put("points", IntListtoCSIS<int>(scores_points_list));
  tmResponse.put("varak", IntListtoCSIS<int>(scores_varak_list));
  tmResponse.put("sereg", IntListtoCSIS<int>(scores_sereg_list));
}

/*
bool HexaWarGameData::has_player_lost(int player_index)
{
  int node_count = pHexaWarGraph->getNodeCount();
  for (int i=0; i<node_count; i++)
  {
    HexagonNode<HexaWarNodeData>* pNode = pHexaWarGraph->getNode(i);
    if (pNode->jatekos==player_index) // ha elfoglalta a teruletet
    {
      if (pNode->node_data.epulet!=0) return false; // ha ez egy var
      if (pNode->node_data.sereg_sum() > 0)
        return false; // ha van a mezon serege
    }
  }
  return true;
}*/

////////////////////////////////////////////////////////////////////////////////

void LastRoundBattles::clear()
{
  csata.clear();
  vedo_jatekos.clear();
  deffendsereg0.clear();
  deffendsereg1.clear();
  deffendsereg2.clear();
  tamado_jatekos.clear();
  offendsereg0.clear();
  offendsereg1.clear();
  offendsereg2.clear();
  winner.clear();
  aftsereg0.clear();
  aftsereg1.clear();
  aftsereg2.clear();
}

void LastRoundBattles::add(int cs, int vj, int ds0, int ds1, int ds2, 
                                   int tj, int os0, int os1, int os2,
                                   int w,  int as0, int as1, int as2)
{
  csata.add(cs);
  vedo_jatekos.add(vj);
  deffendsereg0.add(ds0);
  deffendsereg1.add(ds1);
  deffendsereg2.add(ds2);
  tamado_jatekos.add(tj);
  offendsereg0.add(os0);
  offendsereg1.add(os1);
  offendsereg2.add(os2);
  winner.add(w);
  aftsereg0.add(as0);
  aftsereg1.add(as1);
  aftsereg2.add(as2);
}

void LastRoundBattles::send(textmap& tmResponse)
{
  tmResponse.put("hg_csata", IntListtoCSIS<int>(csata));
  tmResponse.put("hg_vedo_jatekos", IntListtoCSIS<int>(vedo_jatekos));
  tmResponse.put("hg_deffendsereg0", IntListtoCSIS<int>(deffendsereg0));
  tmResponse.put("hg_deffendsereg1", IntListtoCSIS<int>(deffendsereg1));
  tmResponse.put("hg_deffendsereg2", IntListtoCSIS<int>(deffendsereg2));
  tmResponse.put("hg_tamado_jatekos", IntListtoCSIS<int>(tamado_jatekos));
  tmResponse.put("hg_offendsereg0", IntListtoCSIS<int>(offendsereg0));
  tmResponse.put("hg_offendsereg1", IntListtoCSIS<int>(offendsereg1));
  tmResponse.put("hg_offendsereg2", IntListtoCSIS<int>(offendsereg2));
  tmResponse.put("hg_winner", IntListtoCSIS<int>(winner));
  tmResponse.put("hg_aftsereg0", IntListtoCSIS<int>(aftsereg0));
  tmResponse.put("hg_aftsereg1", IntListtoCSIS<int>(aftsereg1));
  tmResponse.put("hg_aftsereg2", IntListtoCSIS<int>(aftsereg2));
}

void LastRoundRegen::add(int r, int rs0, int rs1, int rs2)
{
  regen.add(r);
  regensereg0.add(rs0);
  regensereg1.add(rs1);
  regensereg2.add(rs2);
}

void LastRoundRegen::clear()
{
  regen.clear();
  regensereg0.clear();
  regensereg1.clear();
  regensereg2.clear();
}

void LastRoundRegen::send(textmap& tmResponse)
{
  tmResponse.put("hg_regen", IntListtoCSIS<int>(regen));
  tmResponse.put("hg_regensereg0", IntListtoCSIS<int>(regensereg0));
  tmResponse.put("hg_regensereg1", IntListtoCSIS<int>(regensereg1));
  tmResponse.put("hg_regensereg2", IntListtoCSIS<int>(regensereg2));
}


////////////////////////////////////////////////////////////////////////////////
MovementSequence::MovementSequence(textmap* ptmParams)
{
  act_move = 0;
  for (int i=0; i<3; i++) sereg[i] = (int)str2num((*ptmParams)["hg_katona"+itostring(i)], 0);

  //movementX.add(str2num((*ptmParams)["hg_startx"], 0));
  CSIStoIntList((*ptmParams)["hg_pathx"], movementX);
  //movementX.add(str2num((*ptmParams)["hg_celx"], 0));

  //movementY.add(str2num((*ptmParams)["hg_starty"], 0));
  CSIStoIntList((*ptmParams)["hg_pathy"], movementY);
  //movementY.add(str2num((*ptmParams)["hg_cely"], 0));
}

// hibas mozgasszekvencia eseten false es a hiba oka err_msg-be kerul
bool MovementSequence::validate(HexaWarGraph* pHexaWarGraph, string& err_msg)
{
  // ellenorzom hogy jol adta-e meg a az adatokat
  if (movementX.get_count()!=movementY.get_count()) { err_msg="MovementSequence::validate: X and Y moves count not equal."; return false; }
  int movement_count = movementX.get_count();
  if (movement_count>17) { err_msg="MovementSequence::validate: too many moves, max. is 16."; return false; }
  // sereg letszamok ellenorzese
  int ossz_sereg = 0;
  for (int i=0; i<3; i++)
  {
    ossz_sereg += sereg[i];
    if ( (sereg[i]<0) || (sereg[i]>MAX_SEREG_LETSZAM) ) { err_msg="MovementSequence::validate: sereg[i] out of range."; return false; }
  }
  if (ossz_sereg<1) { err_msg="MovementSequence::validate: ossz_sereg<1"; return false; }
  // vegigmegyek a mozgasszekvencian megnezve hogy ervenyes-e az adott graf eseten:
  // 1, ervenyes mezok legyenek amik leteznek a grafban
  for (int i=0; i<movement_count; i++) 
    if (pHexaWarGraph->getNode(movementX[i],movementY[i])==NULL) 
      { err_msg="MovementSequence::validate: invalid node coordinates."; return false; }
  // 2, a start-cel mezoparos szomszedos legyen
  for (int i=0; i<(movement_count-1); i++)
  {
    if (pHexaWarGraph->getNeighbourDirection(movementX[i], movementY[i], movementX[i+1], movementY[i+1])<0)
    {
      err_msg="MovementSequence::validate: not neighbour points in movement path!"; 
      return false;
    }
  }
  err_msg="";
  return true;
}

void MovementSequence::maximalize(int* max_sereg)
{
  for (int i=0; i<3; i++) if (sereg[i]>max_sereg[i]) sereg[i] = max_sereg[i];
}

////////////////////////////////////////////////////////////////////////////////
HexaWarGameData::HexaWarGameData(int iplayernum, textmap& data):
  playernum(iplayernum), pHexaWarGraph(NULL), movement_sequences(true)
{
	gamename = "hexawar";

	textmap tm;
  if (playernum==2)
  {
    tm.put("hg_nodenum", "42");
    tm.put("hg_cx", "0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2");
    tm.put("hg_cy", "0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7,8,8,8,9,9,9,10,10,10,11,11,11,12,12,12,13,13,13");
    tm.put("hg_hatter", "0,1,0,0,0,1,0,0,1,1,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,1,0,0,1,0,0,0,1,1,0,0,1,0,0,0,1,0");
    tm.put("hg_epulet", "0,1,0,0,0,1,0,0,1,1,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,1,0,0,1,0,0,0,1,1,0,0,1,0,0,0,1,0");
    tm.put("hg_jatekos", "-1,-1,-1,-1,-1,-1,-1,-1,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,-1,-1,-1,-1,-1,-1,-1,-1");
    tm.put("hg_sereg0", "0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0,0,0,0,77,77,0,0,0,0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0");
    tm.put("hg_sereg1", "0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0,0,0,0,77,77,0,0,0,0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0");
    tm.put("hg_sereg2", "0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0,0,0,0,77,77,0,0,0,0,0,0,0,0,0,0,0,77,0,0,0,0,0,0,0,0");
    tm.put("hg_e0", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e2", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    last_round = 12;
    /*tm.put("hg_nodenum", "36");
    tm.put("hg_cx", "0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2");
    tm.put("hg_cy", "0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7,8,8,8,9,9,9,10,10,10,11,11,11");
    tm.put("hg_hatter", "1,1,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
    tm.put("hg_epulet", "1,1,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
    tm.put("hg_jatekos", "0,-1,-1,-1,-1,-1,-1,-1,-1,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,-1");
    tm.put("hg_sereg0", "100,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,50,100,0");
    tm.put("hg_sereg1", "50,0,0,0,0,0,0,0,0,100,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,100,50,0");
    tm.put("hg_sereg2", "75,0,0,0,0,0,0,0,0,75,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,75,75,0");
    tm.put("hg_e0", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e2", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    last_round = 7;*/
  }
  else if (playernum==3)
  {
    Random rnd;
    int ri = rnd.getInt(1,100);
    if (ri<=50)
    {
      tm.put("hg_nodenum", "36");
      tm.put("hg_cx", "0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2");
      tm.put("hg_cy", "0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7,8,8,8,9,9,9,10,10,10,11,11,11");
      tm.put("hg_hatter", "1,1,0,0,0,0,1,0,1,1,0,0,0,1,1,0,1,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
      tm.put("hg_epulet", "1,1,0,0,0,0,1,0,1,1,0,0,0,1,1,0,1,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
      tm.put("hg_jatekos", "0,-1,-1,-1,-1,-1,-1,-1,2,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,-1");
      tm.put("hg_sereg0", "100,0,0,0,0,0,0,0,100,50,0,0,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,100,50,0");
      tm.put("hg_sereg1", "50,0,0,0,0,0,0,0,50,100,0,0,0,0,0,0,0,0,0,0,100,0,0,0,0,0,0,0,0,0,0,0,0,50,100,0");
      tm.put("hg_sereg2", "75,0,0,0,0,0,0,0,75,75,0,0,0,0,0,0,0,0,0,0,75,0,0,0,0,0,0,0,0,0,0,0,0,75,75,0");
      tm.put("hg_e0", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      tm.put("hg_e1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      tm.put("hg_e2", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      last_round = 26;
    }
    else
    {
      tm.put("hg_nodenum", "72");
      tm.put("hg_cx", "0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3");
      tm.put("hg_cy", "0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5,6,6,6,6,7,7,7,7,8,8,8,8,9,9,9,9,10,10,10,10,11,11,11,11,12,12,12,12,13,13,13,13,14,14,14,14,15,15,15,15,16,16,16,16,17,17,17,17");
      tm.put("hg_hatter", "0,0,0,1,1,0,1,0,0,0,0,0,0,0,0,1,1,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,1,0,1,1,0,0,0");
      tm.put("hg_epulet", "0,0,0,1,1,0,1,0,0,0,0,0,0,0,0,1,1,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,1,0,1,1,0,0,0");
      tm.put("hg_jatekos", "-1,-1,-1,1,0,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,-1,0,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,-1,2,1,-1,-1,-1");
      tm.put("hg_sereg0", "0,0,0,0,200,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,200,200,0,0,0");
      tm.put("hg_sereg1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,50,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0");
      tm.put("hg_sereg2", "0,0,0,50,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      tm.put("hg_e0", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      tm.put("hg_e1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      tm.put("hg_e2", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
      last_round = 32;
    }
    /*tm.put("hg_nodenum", "36");
    tm.put("hg_cx", "0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2");
    tm.put("hg_cy", "0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7,8,8,8,9,9,9,10,10,10,11,11,11");
    tm.put("hg_hatter", "1,1,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
    tm.put("hg_epulet", "1,1,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,1,0");
    tm.put("hg_jatekos", "0,-1,-1,-1,-1,-1,-1,-1,2,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,-1");
    tm.put("hg_sereg0", "100,0,0,0,0,0,0,0,50,50,0,0,0,0,0,0,0,0,0,0,100,0,0,0,0,0,0,0,0,0,0,0,0,50,100,0");
    tm.put("hg_sereg1", "50,0,0,0,0,0,0,0,100,100,0,0,0,0,0,0,0,0,0,0,50,0,0,0,0,0,0,0,0,0,0,0,0,100,50,0");
    tm.put("hg_sereg2", "75,0,0,0,0,0,0,0,75,75,0,0,0,0,0,0,0,0,0,0,75,0,0,0,0,0,0,0,0,0,0,0,0,75,75,0");
    tm.put("hg_e0", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e1", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    tm.put("hg_e2", "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
    last_round = 30;*/
  }
  else
  {
    throw new exception("HexaWarGameData::HexaWarGameData: unsupported playernum.");
  }
  
	pHexaWarGraph = new HexaWarGraph(tm);
  pHexaWarGraph->setPlayerState(tm);
	start_time = now();
	round_timespan = 120 * 1000;
  last_closed_round = 0;
  last_closed_time = start_time;
  
  // beallitom hogy meg az ido kezdeten leptem latszolag
  last_step_time.set_capacity(playernum);
  for (int i=0; i<playernum; i++) last_step_time.add(start_time);
  
  // beallitom hogy minden kliens szinkronban van kezdetben
  client_closed_round.set_capacity(playernum);
  for (int i=0; i<playernum; i++) client_closed_round.add(last_closed_round);
  
  // alapallapot beallitasa
  client_state.set_capacity(playernum);
  for (int i=0; i<playernum; i++) client_state.add(CLS_PLAYING);
  
  // letrehozom minden jatekoshoz az ures mozgasprogram listakat
  movement_sequences.set_capacity(playernum);
	for (int i=0; i<playernum; i++) movement_sequences.add(new MovementSequenceList(true));

  // a jatekosok aktualis allapotat tarolo cucc
  player_standings.set_capacity(playernum);
  for (int i=0; i<playernum; i++)
  {
    player_standing ps;
    ps.varak = 0;
    ps.sereg = 0;
    ps.hossz = last_round+1; // legjobbnal is jobbat kivanok neki :)
    player_standings.add(ps);
  }

  // eredmenyekhez letrehozom cuccot
  scores_id_list.set_capacity(playernum);
  scores_points_list.set_capacity(playernum);
  scores_varak_list.set_capacity(playernum);
  scores_sereg_list.set_capacity(playernum);
  for (int i=0; i<playernum; i++) 
  {
    scores_id_list.add(i);
    scores_points_list.add(0);
    scores_varak_list.add(0);
    scores_sereg_list.add(0);
  }
}

HexaWarGameData::~HexaWarGameData()
{
	if (pHexaWarGraph!=NULL) delete pHexaWarGraph;
}

/*
bool HexaWarGameData::is_gameover()
{
  int client_gameover_count = 0;
  for (int i=0; i<playernum; i++)
    if (is_client_gameover(i))
      client_gameover_count++;
  return client_gameover_count>=playernum;
}*/

// a tobbiek mar mind befejeztek a jatekot
/*
bool HexaWarGameData::is_others_gameover(int my_index)
{
  int others_gameover_count = 0;
  for (int i=0; i<playernum; i++)
    if ( (i!=my_index) && (is_client_gameover(i)) )
      others_gameover_count++;
  return others_gameover_count>=(playernum-1);
}*/

void HexaWarGameData::calculatePlayerStandingsAndSetStates()
{
  // inicializalas
  for (int i=0; i<playernum; i++)
  {
    player_standings[i].varak = 0;
    player_standings[i].sereg = 0;
  }
  // varak es seregek szambavetele
  int node_count = pHexaWarGraph->getNodeCount();
  for (int i=0; i<node_count; i++)
  {
    HexagonNode<HexaWarNodeData>* pNode = pHexaWarGraph->getNode(i);
    if ( (pNode->jatekos>=0) && (pNode->jatekos<playernum) )
    {
      player_standings[pNode->jatekos].sereg += pNode->node_data.sereg_sum();
      if (pNode->node_data.epulet!=0)
        player_standings[pNode->jatekos].varak++;
    }
  }
  // allapotok allitasa minden jatekos szamara aki meg jatszik
  int players_out = 0;
  for (int i=0; i<playernum; i++)
  {
    if (client_state[i]==CLS_PLAYING)
    {
      if ( (player_standings[i].varak==0) && (player_standings[i].sereg==0) )
      {
        client_state[i] = CLS_LOST;
        player_standings[i].hossz = last_closed_round; // eddig a korig maradt eletben
        players_out++;
      }
      else if (last_closed_round>=last_round)
      {
        client_state[i] = CLS_FINISHED;
        players_out++;
      }
    }
    else
    {
      players_out++;
    }
  }
  // ha mar csak 1 maradt talpon akkor szamara is CLS_FINISHED van
  if ((playernum-players_out)==1)
  {
    for (int i=0; i<playernum; i++)
      if (client_state[i]==CLS_PLAYING) { client_state[i] = CLS_FINISHED; break; }
  }
}

// mennyi nem kuldott valaszt az elozo kor lezarasa ota
int HexaWarGameData::missing_responses()
{
  int done = 0;
  for (int i=0; i<playernum; i++)
    if ( is_client_gameover(i) || (last_step_time[i]>last_closed_time) )
      done++;
  return playernum-done;
}

void HexaWarGameData::sendClientState(textmap& tmResponse)
{ 
  tmResponse.put("client_state", IntListtoCSIS<int>(client_state));
}

void HexaWarGameData::sendSeregMovementDirections(textmap& tmResponse)
{
  tpodlist<int> hg_mozgasiranyhely;
  tpodlist<int> hg_mozgasirany;
  int movement_sequences_player_count = movement_sequences.get_count();
  for (int player_idx = 0; player_idx<movement_sequences_player_count; player_idx++)
  {
    MovementSequenceList* pActPlayerMovSeqList = movement_sequences[player_idx];
    int mov_seq_list_count = pActPlayerMovSeqList->get_count();
    for (int mov_seq_idx=0; mov_seq_idx<mov_seq_list_count; mov_seq_idx++)
    {
      MovementSequence* p_mov_seq = (*pActPlayerMovSeqList)[mov_seq_idx];
      int last_index = p_mov_seq->movementX.get_count() - 1;
      if (p_mov_seq->act_move<last_index)
      {
        // a kovetkezo lepes: p_mov_seq->act_move -> p_mov_seq->act_move+1 indexeken
        int startX = p_mov_seq->movementX[p_mov_seq->act_move];
        int startY = p_mov_seq->movementY[p_mov_seq->act_move];
        int celX = p_mov_seq->movementX[p_mov_seq->act_move+1];
        int celY = p_mov_seq->movementY[p_mov_seq->act_move+1];
        int dir = pHexaWarGraph->getNeighbourDirection(startX, startY, celX, celY);
        if (dir>=0)
        {
          hg_mozgasiranyhely.add(pHexaWarGraph->getNodeIndex(startX, startY));
          hg_mozgasirany.add(dir);
        }
      }
    }
  }
  tmResponse.put("hg_mozgasiranyhely", IntListtoCSIS<int>(hg_mozgasiranyhely));
  tmResponse.put("hg_mozgasirany", IntListtoCSIS<int>(hg_mozgasirany));
}

void HexaWarGameData::sendStateAndBattlesAndRegen(textmap& tmResponse)
{
  // allapot:
  pHexaWarGraph->getPlayerState(tmResponse);
  // csatak utolso lezart korben
  last_round_battles.send(tmResponse);
  // utolso lezart korben a regeneralodasok:
  last_round_regen.send(tmResponse);
  // az egysegek mozgasiranya minden jatekosra
  sendSeregMovementDirections(tmResponse);
}

void HexaWarGameData::calculateRegeneration(Random& rnd_gen)
{
  int node_count = pHexaWarGraph->getNodeCount();
  for (int i=0; i<node_count; i++)
  {
    HexagonNode<HexaWarNodeData>* pNode = pHexaWarGraph->getNode(i);
    if ( (pNode->jatekos>=0) && (pNode->node_data.epulet>0) )  // ha van tulajdonos es van var
      increaseInCastle(pNode->node_data.sereg[0], 
                       pNode->node_data.sereg[1],
                       pNode->node_data.sereg[2], 
                       rnd_gen);
  }
}

void HexaWarGameData::calculateOneMove(int act_player_index, Random& rnd_gen)
{
  MovementSequenceList* pActPlayerMovSeqList = movement_sequences[act_player_index];
  // lepek egyet minden szekvenciaval, a vegerol indulva lefele
  for (int mov_seq_idx=pActPlayerMovSeqList->get_count()-1; mov_seq_idx>=0; mov_seq_idx--)
  {
    MovementSequence* p_mov_seq = (*pActPlayerMovSeqList)[mov_seq_idx];
    int last_index = p_mov_seq->movementX.get_count() - 1;
    if (p_mov_seq->act_move>=last_index) // ha mar a vegen vagyunk torles
    {
      pActPlayerMovSeqList->del(mov_seq_idx); // torlom a korabban mar befejezett szekvenciat
    }
    else // vegre kell hajtani a lepest
    {
      // az aktualis lepes vegrehajtasa, p_mov_seq->act_move -> p_mov_seq->act_move+1 lepes vegrehajtasa
      int startX = p_mov_seq->movementX[p_mov_seq->act_move];
      int startY = p_mov_seq->movementY[p_mov_seq->act_move];
      HexagonNode<HexaWarNodeData>* pStartNode = pHexaWarGraph->getNode(startX,startY);
      int celX = p_mov_seq->movementX[p_mov_seq->act_move+1];
      int celY = p_mov_seq->movementY[p_mov_seq->act_move+1];
      HexagonNode<HexaWarNodeData>* pCelNode = pHexaWarGraph->getNode(celX,celY);

      // ellenorzom hogy egyaltalan enyem-e az adott csomopont, ha nem akkor semmit nem kell csinalni, torlom a seregem is
      if (act_player_index!=pStartNode->jatekos) p_mov_seq->wipe_sereg();
      else
      {
        // mivel nem indithatok tobb katonat innen mint amennyi van, ezert
        // sereg letszam maximalizalasa az aktualis helyen levo katonak szamaban valo maximalizalassal
        p_mov_seq->maximalize(pStartNode->node_data.sereg);
        if (p_mov_seq->sereg_sum()>0) // amennyi seregem maradt a kezdetibol azzal atmegyek a celteruletre
        {
#if (defined LOG_DEBUG) && (LOG_LEVEL>=1)
          mlog("HexaWarGameData::calculateOneMove: act_player_index="+itostring(act_player_index)+
               "  startX="+itostring(startX)+"  startY="+itostring(startY)+"  celX="+itostring(celX)+"  celY="+itostring(celY));
#endif
          // start pontbol torlom az onnan elmenoket
          for (int i=0; i<3; i++) pStartNode->node_data.sereg[i] -= p_mov_seq->sereg[i];
          // cel pont valtoztatasa aszerint hogy kinek a tulajdona
          // ha nincs ott katona akkor olyan mintha ures lenne
          if (pCelNode->jatekos==act_player_index) // sajat teruletre lepek
          {  
            // cel ponthoz hozzaadom az oda jovoket
            for (int i=0; i<3; i++) pCelNode->node_data.sereg[i] += p_mov_seq->sereg[i];
          }
          else if ( (pCelNode->jatekos==-1) || (pCelNode->node_data.sereg_sum()<=0) ) // ures terulet vagy masik jatekos tulajdona de nincs rajta serege
          {
            // cel terulet az en tulajdonomma valik
            pCelNode->jatekos = act_player_index;
            // cel ures teruletre beirom az odajovoket
            for (int i=0; i<3; i++) pCelNode->node_data.sereg[i] = p_mov_seq->sereg[i];
          }
          else // masik jatekose a terulet --> csata!
          {
            // a csata adatait tarolo valtozok
            int cs = pHexaWarGraph->getNodeIndex(celX,celY);
            int vj = pCelNode->jatekos;
            int ds0 = pCelNode->node_data.sereg[0];
            int ds1 = pCelNode->node_data.sereg[1];
            int ds2 = pCelNode->node_data.sereg[2];
            int tj = pStartNode->jatekos;
            int os0 = p_mov_seq->sereg[0];
            int os1 = p_mov_seq->sereg[1];
            int os2 = p_mov_seq->sereg[2];
            // a harcfuggveny parameterit tarolo valtozok
            int aA = ds0;
            int bA = ds1;
            int cA = ds2;
            int aB = os0;
            int bB = os1;
            int cB = os2;
            int winner;
            int varbanVan = (pCelNode->node_data.epulet>0) ? -1 : ( (pStartNode->node_data.epulet>0) ? 1 : 0 );
            // CSATA!!!
#if (defined LOG_DEBUG) && (LOG_LEVEL>=2)
          mlog("executeFight elott: aA="+itostring(aA)+"  bA="+itostring(bA)+"  cA="+itostring(cA)+
               "  aB="+itostring(aB)+"  bB="+itostring(bB)+"  cB="+itostring(cB)+
               "  varbanVan="+itostring(varbanVan));
#endif
            executeFight(aA, bA, cA, //in-out A katonainak szama tipusonkent, fuggveny valtoztatja 
                         aB, bB, cB, //in-out B katonainak szama tipusonkent, fuggveny valtoztatja 
                         winner, //out param 0: mind meghalt; 1: A nyert; 2: B nyert 
                         varbanVan, //in param 0: mezon vannak; -1: A varban van; 1: B varban van
                         rnd_gen
                        );
#if (defined LOG_DEBUG) && (LOG_LEVEL>=2)
          mlog("executeFight utan: aA="+itostring(aA)+"  bA="+itostring(bA)+"  cA="+itostring(cA)+
               "  aB="+itostring(aB)+"  bB="+itostring(bB)+"  cB="+itostring(cB)+
               "  winner="+itostring(winner));
#endif
            // harcfuggveny kimenete alapjan:
            if (winner==1) // A nyert == vedo nyert
            {
              // node tulajdonos nem valtozik
              // vedo sereg:
              pCelNode->node_data.sereg[0] = aA;
              pCelNode->node_data.sereg[1] = bA;
              pCelNode->node_data.sereg[2] = cA;
              // tamado sereg:
              p_mov_seq->wipe_sereg(); // meghalt a sereg
            }
            else if (winner==2) // B nyert == tamado nyert
            {
              // node tulaja a tamado lett
              pCelNode->jatekos = pStartNode->jatekos;
              // vedo sereg megsemmisult
              // tamado sereg elfoglalta a varat
              pCelNode->node_data.sereg[0] = aB;
              pCelNode->node_data.sereg[1] = bB;
              pCelNode->node_data.sereg[2] = cB;
              // tamado sereg:
              p_mov_seq->sereg[0] = aB;
              p_mov_seq->sereg[1] = bB;
              p_mov_seq->sereg[2] = cB;
            }
            else // mindenki meghalt
            {
              // node marad vedo tulajdonaban
              // vedok
              pCelNode->node_data.sereg[0] = 0;
              pCelNode->node_data.sereg[1] = 0;
              pCelNode->node_data.sereg[2] = 0;
              // tamadok
              p_mov_seq->wipe_sereg();
            }
            // maradek sereg es gyoztes 
            int w = pCelNode->jatekos;
            int as0 = pCelNode->node_data.sereg[0]; 
            int as1 = pCelNode->node_data.sereg[1];
            int as2 = pCelNode->node_data.sereg[2];
            // elmentem a tobbi csata esemeny melle
            last_round_battles.add(cs, vj, ds0, ds1, ds2, tj, os0, os1, os2, w, as0, as1, as2);
          }
        }
      }
      if (p_mov_seq->sereg_sum()>0) // ha nem semmisult meg a seregem
      {
        // leptetes
        p_mov_seq->act_move++;
        // leptetes utan ha vegere ert szekvencia torles
        if (p_mov_seq->act_move>=last_index) pActPlayerMovSeqList->del(mov_seq_idx);
      }
      else // meghalt a sereg
      {
        pActPlayerMovSeqList->del(mov_seq_idx);
      }
    }  
  }
}

void HexaWarGameData::calculateActualRound()
{
  Random rnd_gen;
  // korabbi esemenyeket torlom
  last_round_battles.clear();
  last_round_regen.clear();

  // vegigmegyek a mozgasprogramokon es elvegzem a lepeseket: 1 kor = 3 lepes minden jatekossal
  // minden lepesnel random sorrend kisorsolasa
  for (int lepes=0; lepes<3; lepes++) // 3 lepes megtetele, minden lepesnel mas rnd sorrend kisorsolasa
  {
    tpodlist<int> rnd_sorrend;
    for (int i=0; i<playernum; i++) rnd_sorrend.add(i);
    shuffle<int>(rnd_sorrend, rnd_gen);
    for (int i=0; i<playernum; i++)
      calculateOneMove(rnd_sorrend[i], rnd_gen);
  }
  // sereg regeneraciok szamolasa
  calculateRegeneration(rnd_gen);
}

void HexaWarGameData::sendGameOver(textmap& tmResponse)
{
  tmResponse.put("result", "3"); // megvaltozik "2"-rol "3"-ra!
  calculateAndSendPlayerPoints(tmResponse);
}
