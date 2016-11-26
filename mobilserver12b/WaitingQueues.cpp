// WaitingQueues.cpp: implementation of the WaitingQueues class.
//
//////////////////////////////////////////////////////////////////////

#include <stdio.h>

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

WaitingQueues* WaitingQueues::pWaitingQueues = NULL;

WaitingQueues* WaitingQueues::getWaitingQueues()
{
  if (pWaitingQueues==NULL) pWaitingQueues = new WaitingQueues;
  return pWaitingQueues;
}

WaitingQueues::WaitingQueues(): gamequeuelist(SL_OWNOBJECTS|SL_SORTED)
{
  //LocalConnection lconn;
  //variant vars, quiz_resp, hexawar_resp;
  //int quiz_ri, hexawar_ri;
  //quiz_ri = lconn.query("SELECT id,nev,jatekosszam,temak FROM kviz_jatektipus WHERE ervenyesseg_kezdet<=now() AND ervenyesseg_veg>=now()", vars, quiz_resp);
  //hexawar_ri = lconn.query("SELECT id,nev,jatekosszam FROM hexawar_jatektipus WHERE ervenyesseg_kezdet<=now() AND ervenyesseg_veg>=now()", vars, hexawar_resp);
  
	//GameWaitingQueues* pQuizGameQueues = new GameWaitingQueues;
	GameWaitingQueues* pHexawarGameQueues = new GameWaitingQueues;
	//pQuizGameQueues->name = "kviz";
	pHexawarGameQueues->name = "hexawar";
	
	// �sszes kviz j�t�kt�pus adatai
  //variant row;
  //for (int i=0; anext(quiz_resp, i, row); )
  //{
  //  GameTypeWaitingQueue* pQuizGameTypeQueue = new GameTypeWaitingQueue;
  //  pQuizGameTypeQueue->id = (string)aget(row,0);
  //  pQuizGameTypeQueue->nev = (string)aget(row,1);
  //  pQuizGameTypeQueue->playernum = (int)aget(row,2);
  //  pQuizGameTypeQueue->data.put("temak", (string)aget(row,3));
  //  pQuizGameQueues->gtwqlist.add(pQuizGameTypeQueue->id,pQuizGameTypeQueue);
  //}
	//gamequeuelist.add("kviz",pQuizGameQueues);
	
	// �sszes hexawar j�t�kt�pus adatai
  //for (int i=0; anext(hexawar_resp, i, row); )
  //{
  //  GameTypeWaitingQueue* pHexawarGameTypeQueue = new GameTypeWaitingQueue;
  //  pHexawarGameTypeQueue->id = (string)aget(row,0);
  //  pHexawarGameTypeQueue->nev = (string)aget(row,1);
  //  pHexawarGameTypeQueue->playernum = (int)aget(row,2);
  //  pHexawarGameQueues->gtwqlist.add(pHexawarGameTypeQueue->id,pHexawarGameTypeQueue);
  //}

  GameTypeWaitingQueue* pHexawarGameTypeQueue;

  // 2 jatekos - kezdo szint
  pHexawarGameTypeQueue = new GameTypeWaitingQueue;
  pHexawarGameTypeQueue->id = "0";
  pHexawarGameTypeQueue->nev = "2 players beginner";
  pHexawarGameTypeQueue->playernum = 2;
  pHexawarGameQueues->gtwqlist.add(pHexawarGameTypeQueue->id,pHexawarGameTypeQueue);
  
  // 2 jatekos - normal szint
  pHexawarGameTypeQueue = new GameTypeWaitingQueue;
  pHexawarGameTypeQueue->id = "1";
  pHexawarGameTypeQueue->nev = "2 players";
  pHexawarGameTypeQueue->playernum = 2;
  pHexawarGameQueues->gtwqlist.add(pHexawarGameTypeQueue->id,pHexawarGameTypeQueue);
  
  // 3 jatekos - kezdo szint
  pHexawarGameTypeQueue = new GameTypeWaitingQueue;
  pHexawarGameTypeQueue->id = "2";
  pHexawarGameTypeQueue->nev = "3 players beginner";
  pHexawarGameTypeQueue->playernum = 3;
  pHexawarGameQueues->gtwqlist.add(pHexawarGameTypeQueue->id,pHexawarGameTypeQueue);

  // 3 jatekos - normal
  pHexawarGameTypeQueue = new GameTypeWaitingQueue;
  pHexawarGameTypeQueue->id = "3";
  pHexawarGameTypeQueue->nev = "3 players";
  pHexawarGameTypeQueue->playernum = 3;
  pHexawarGameQueues->gtwqlist.add(pHexawarGameTypeQueue->id,pHexawarGameTypeQueue);

	gamequeuelist.add("hexawar",pHexawarGameQueues);
}


WaitingQueues::~WaitingQueues()
{

}


/*
 * az action=getgametypes k�r�sre ad v�laszt:
 * gtypenum=j�t�kt�pusok sz�ma
 * gtpid_i=i-edik j�t�kt�pus id (kviz_jatektipus.id)
 * gtpname_i=i-edik j�t�kt�pus neve
 */
void WaitingQueues::getGameTypes(textmap* ptmParams, textmap& tmResponse)
{
	scoperead sr(listRwLock);
	
	GameWaitingQueues* pGameWaitingQueues = gamequeuelist[ (*ptmParams)["game"] ];
	if (pGameWaitingQueues==NULL) // ha nincs ilyen nev� j�t�k
	{
		// hiba�zenet k�ld�se
    setErrorResponse(tmResponse, "0"); // �rv�nytelen game
	}
	else
	{
		scoperead sr2(pGameWaitingQueues->listRwLock);
		
		int gtwq_cnt = pGameWaitingQueues->gtwqlist.get_count();
		tmResponse.put("gtypenum", itostring(gtwq_cnt));
		int idx;
		for (idx=0; idx<gtwq_cnt; idx++)
		{
			tmResponse.put("gtpid_"+itostring(idx), pGameWaitingQueues->gtwqlist[idx]->id);
			tmResponse.put("gtpname_"+itostring(idx), pGameWaitingQueues->gtwqlist[idx]->nev);			
		}
		tmResponse.put("result", "1"); // siker
	}
}


/*
 * az action=waitforgametype k�r�sre ad v�laszt
 * bemeneti adatok: game �s gtype = az adott j�t�kt�pus ID-ja
 
 */
void WaitingQueues::waitForGameType(textmap* ptmParams, textmap& tmResponse, SessionUser* pSessionUser)
{
  int act_time = now();

	scoperead sr(listRwLock);
	
	GameWaitingQueues* pGameWaitingQueues = gamequeuelist[ (*ptmParams)["game"] ];
	if (pGameWaitingQueues==NULL) // ha nincs ilyen nev� j�t�k
	{
		// hiba�zenet k�ld�se
    setErrorResponse(tmResponse, "0"); // �rv�nytelen game
	}
	else
	{
		scoperead sr2(pGameWaitingQueues->listRwLock);
		
		GameTypeWaitingQueue* pGameTypeWaitingQueue = pGameWaitingQueues->gtwqlist[ (*ptmParams)["gtype"] ];
		if (pGameTypeWaitingQueue==NULL) // ha nincs ilyen t�pus� j�t�k akkor hiba�zenet
		{
      setErrorResponse(tmResponse, "3");
		}
		else
		{
			// a v�rakoz�si sort v�ltoztatom meg, ez�rt �r�s lock kell!
			scopewrite sw(pGameTypeWaitingQueue->listRwLock);

			// t�rl�m azokat akik jelentkeztek ebbe a v�rakoz�si sorba de m�r
			// r�gen k�ldtek �zenetet hogy v�rnak m�g
			const datetime timeout_timespan = encodetime(0, 1, 0, 0); // 1 percet engedek
			// ennyi id�nk�nt t�rt�nik meg az ellen�rz�s
			const datetime tocheck_timespan = encodetime(0, 1, 0, 0);
			if ( act_time >= (pGameTypeWaitingQueue->last_check+tocheck_timespan) )
			{
				int cnt = pGameTypeWaitingQueue->waitinguserlist.get_count(); int i;
				for (i=0; i<cnt; i++)
				{
					// ha megadott ideje nem �rkezett �zenet a klienst�l akkor timeout van
					if ( act_time >= (pGameTypeWaitingQueue->waitinguserlist[i]->wait_time+timeout_timespan) )
					{
						pGameTypeWaitingQueue->waitinguserlist.del(i);
						cnt--;
						i--;
					}
				}
				pGameTypeWaitingQueue->last_check = act_time;
			}

			// megn�zem hogy szerepel-e m�r a v�rakoz�si sorban
			int idx;
			if (pGameTypeWaitingQueue->waitinguserlist.search(pSessionUser->id, idx))
			{
				// ha benne van akkor update-elem
				pGameTypeWaitingQueue->waitinguserlist[idx]->wait_time = act_time;
				// ha valaki m�r ind�tott egy j�tszm�t �s engem belesorsolt
				// akkor be�ll�tom magamnak a j�tszma v�ltoz�kat azt�n t�rl�m magam
				// a v�rakoz�si sorb�l
				if (pGameTypeWaitingQueue->waitinguserlist[idx]->pGameData!=NULL)
				{
					if (pGameTypeWaitingQueue->waitinguserlist[idx]->pGameData->gamename == "kviz")
					{
						pSessionUser->pGameMng = new QuizGameManager(pSessionUser);
					}
					else if (pGameTypeWaitingQueue->waitinguserlist[idx]->pGameData->gamename == "hexawar")
					{
					  pSessionUser->pGameMng = new HexaWarGameManager(pSessionUser);
					}
					else
					{
            setErrorResponse(tmResponse, "3"); // �rv�nytelen game t�pus
						return;
					}
					pGameTypeWaitingQueue->waitinguserlist[idx]->pGameData->link(pSessionUser->pGameMng->pGameData);
					GameData::unlink(pGameTypeWaitingQueue->waitinguserlist[idx]->pGameData);
					// beallitom a sorszamomat:
					pSessionUser->pGameMng->my_index = pGameTypeWaitingQueue->waitinguserlist[idx]->my_index;
					// torlom magam a varakozasi sorbol:
					pGameTypeWaitingQueue->waitinguserlist.del(idx);
					// lockolnom kell a game data-t mielott hozzaferek
					scoperead sgd_sr(pSessionUser->pGameMng->pGameData->rwLock);
					pSessionUser->pGameMng->sendGameData(ptmParams, tmResponse);
					tmResponse.put("result", "2"); // J�T�K INDUL!!!
					return;
				}
			}
			else
			{
				// ha nincs benne akkor addolom
				WaitingUserData* pWUD = new WaitingUserData;
				pWUD->name = pSessionUser->name;
        pWUD->mid = pSessionUser->mid;
				pWUD->apply_time = act_time;
				pWUD->wait_time = act_time;
				pWUD->pGameData = NULL;
				idx = pGameTypeWaitingQueue->waitinguserlist.add(pSessionUser->id, pWUD);
			}

			// j�tszma ind�t�sa ha lehet
			// a mostani verzi�ban el�g ha megfelel� sz�m� v�rakoz� van m�r
			// a felt�tel hogy csak a pGameData==NULL �rt�k mellett sz�m�t szabad v�rakoz�nak!
			tpodlist<int> recruitedPlayers; // kisorsolt j�t�kosok
			int wu_cnt = pGameTypeWaitingQueue->waitinguserlist.get_count(); // v�rakoz�k sz�ma
			int players_needed = pGameTypeWaitingQueue->playernum - 1; // mennyit kell sorsolni
																	   // -1 ami saj�t magam
			int i;

#ifndef BOTBOTPLAY
      // ha bot vagyok akkor nem akarok jatszani
      if (pSessionUser->mid != "BOT")
      {
#endif
        for (i=0; i<wu_cnt; i++)
        {
          // olyat keresek magamon k�v�l aki m�g nincs j�t�kban
          if ( (i!=idx) && (pGameTypeWaitingQueue->waitinguserlist[i]->pGameData==NULL) )
          {
#ifndef BOTBOTPLAY
            // ha a kivalasztott jatekos BOT akkor csak abban az esetben fogadom el 
            // ha mar legalabb 20 masodperce varakozom
            if ( (pGameTypeWaitingQueue->waitinguserlist[i]->mid != "BOT") ||
                 ((act_time - pGameTypeWaitingQueue->waitinguserlist[idx]->apply_time)>=20000) )
            {
#endif
              recruitedPlayers.add(i);
              if (recruitedPlayers.get_count() >= players_needed) break;
#ifndef BOTBOTPLAY
            }
#endif
          }
        }
#ifndef BOTBOTPLAY
      }
#endif

			// ha siker�lt �sszegy�jteni a sz�ks�ges sz�m� v�rakoz�t akkor azokat
			// berakom a j�t�kba amit most l�trehozok, �s magamat is berakom �s t�rl�m
			// a v�rakoz�si sorb�l
			if (recruitedPlayers.get_count() >= players_needed)
			{
				// j�t�k l�trehoz�sa
				GameData* pGameData;
				if ( (*ptmParams)["game"] == "kviz" )
				{	
					// l�trehozom a j�tszm�t �tadva a t�pus spec. adatait:
					pGameData = new QuizGameData(pGameTypeWaitingQueue->data);
					// l�trehozom a j�t�k manager objektumomat:
					pSessionUser->pGameMng = new QuizGameManager(pSessionUser);
				}
				else if ( (*ptmParams)["game"] == "hexawar" )
				{
				  pGameData = new HexaWarGameData(pGameTypeWaitingQueue->playernum, pGameTypeWaitingQueue->data);
				  pSessionUser->pGameMng = new HexaWarGameManager(pSessionUser);
				}
				else
				{
          setErrorResponse(tmResponse, "3"); // �rv�nytelen game t�pus
					return;
				}
				// referencia a j�t�k managerb�l a j�tszm�ra:
				pGameData->link(pSessionUser->pGameMng->pGameData);
				pGameData->gametypeid = pGameTypeWaitingQueue->id;
				pGameData->gametypename = pGameTypeWaitingQueue->nev;
				// hozz�adom magam a j�tszm�hoz:
				pGameData->playerids.add(new string(pSessionUser->id));
				pGameData->playernames.add(new string(pSessionUser->name));
				// �n vagyok a 0-ik sorszamu, ezt beirom a managerbe:
				pSessionUser->pGameMng->my_index = 0;
				// j�t�kosok hozz�ad�sa
				for (i=0; i<players_needed; i++)
				{
					int pi = recruitedPlayers[i];
					pGameData->link(pGameTypeWaitingQueue->waitinguserlist[pi]->pGameData);
					pGameData->playerids.add(new string(pGameTypeWaitingQueue->waitinguserlist.getkey(pi)));
					pGameData->playernames.add(new string(pGameTypeWaitingQueue->waitinguserlist[pi]->name));
					// 1-tol indul mivel magamat mar addoltam
					pGameTypeWaitingQueue->waitinguserlist[pi]->my_index = i+1;
				}
				// t�rl�m magam a v�rakoz�si sorb�l
				pGameTypeWaitingQueue->waitinguserlist.del(idx);
				pSessionUser->pGameMng->sendGameData(ptmParams, tmResponse);
				tmResponse.put("result", "2"); // J�T�K INDUL!!!
				return;
			}

			// fel lett v�ve a v�rakoz�si sorba de m�g v�rnia kell a j�t�k indul�s�ra
			tmResponse.put("result", "1"); 
		}
	}
}
