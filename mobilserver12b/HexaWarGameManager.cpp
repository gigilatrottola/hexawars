#include "include.h"

// 0: jatek vege, 1: nincs vege
// p_mov_seq: "askround" eseten NULL, a "move" eseten kapott objektumot itt kell majd torolni
int HexaWarGameManager::handle_move(MovementSequence* p_mov_seq, textmap& tmResponse)
{
  HexaWarGameData* pGame = (HexaWarGameData*)pGameData;
  datetime act_time = now();

  // tobbi kliensre megnezni timeoutoltak-e, ha igen akkor CLS_DROPPED allapotba allitas
  for (int i=0; i<pGame->playernum; i++)
  {
    if ( (i!=my_index) && (pGame->client_state[i]==CLS_PLAYING))
    {
      if (pGame->last_step_time[i]+3*pGame->round_timespan<act_time)
        pGame->client_state[i] = CLS_DROPPED;
        pGame->player_standings[i].hossz = pGame->last_closed_round; // az utolso eddig lezart korig birta
    }
  }

  // ha mar gameover volt nekem akkor csak elkuldom valaszkent a meglevo allapotot
  if (pGame->is_client_gameover(my_index))
  {
    pGame->sendStateAndBattlesAndRegen(tmResponse); // elkuldom az utoljara lezart kor utani allapotot es annak esemenyeit
    pGame->sendGameOver(tmResponse); // GAME OVER
    tmResponse.put("roundendtime", getTimeString(pGame->last_closed_time+pGame->round_timespan));
    tmResponse.put("actround", itostring(pGame->last_closed_round+1));
    pGame->sendClientState(tmResponse);
    return 0;
  }

  // ha idaig jutottunk akkor PLAYING allapotban vagyok
  int i_am_playing = 1; // alapban nincs vege a jateknak
  
  if (pGame->is_client_synchronized(my_index))
  {  
    if (p_mov_seq!=NULL) // move: mozgasprogram eltarolasa ha ervenyes a lepes
    {
      string err_str;
      if (p_mov_seq->validate(pGame->pHexaWarGraph,err_str))
      {
        // a beerkezett mozgasprogramot eltarolom akkor ha az elozo zaras ota meg nem lepett
        if (pGame->last_step_time[my_index]<=pGame->last_closed_time) // csak elozo korben lepett
        {
          pGame->movement_sequences[my_index]->add(p_mov_seq);
        }
        else // mar lepett az aktualis korben, a mozgasprogramot eldobom
        {
          delete p_mov_seq; // ures lepes
        }
      }
      else
      {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
        mlog("HexaWarGameManager::handle_move: invalid movement sequence: "+err_str);
#endif
        delete p_mov_seq; // torlom a hibas mozgasszekvenciat
      }
    }
    pGame->last_step_time[my_index] = act_time;
  }

  // megprobalom lezarni a kort, ha eleg adat erkezett hozza eddig
  // kor vegeter ha a kovetkezo 2 kozul valamelyik teljesul:
  // 1, timeout van: lejart a korido az elozo zaras ota
  // 2, mindenki elkuldte mar a lepeset az elozo lezaras ota
  int missing_responses = pGame->missing_responses();
  if ( (act_time>(pGame->last_closed_time+pGame->round_timespan)) || (missing_responses<=0) )
  {
    // a kornek vege: kiszamoljuk az esemenyeket a mozgasprogramok alapjan es lezarjuk az aktualis kort
#if (defined LOG_DEBUG) && (LOG_LEVEL>=1)
    mlog("Ending round. before: last_closed_round="+itostring(pGame->last_closed_round)+"  act_time="+getTimeString(act_time)+
         "  last_closed_time="+getTimeString(pGame->last_closed_time)+"  round_timespan="+itostring(pGame->round_timespan));
#endif
    pGame->calculateActualRound();
    pGame->last_closed_round++;
    pGame->last_closed_time = act_time;
#if (defined LOG_DEBUG) && (LOG_LEVEL>=1)
    mlog("Ending round. after: last_closed_round="+itostring(pGame->last_closed_round)+"  last_closed_time="+getTimeString(pGame->last_closed_time));
#endif
    // lezart kor eredmenyet elkuldom
    tmResponse.put("result", "2");
    pGame->sendStateAndBattlesAndRegen(tmResponse); // elkuldom az utoljara lezart kor utani allapotot es annak esemenyeit
    pGame->client_closed_round[my_index] = pGame->last_closed_round; // szinkronizalas megtortent "2"-es uzenettel

    // jatekosok allasanak es uj allapotanak kiszamitasa
    pGame->calculatePlayerStandingsAndSetStates(); // ez valtja at CLS_PLAYING-bol CLS_FINISHED es CLS_LOST allapotokra
    
    if (pGame->is_client_gameover(my_index)) // ha befejezodott a jatek szamomra
    {
      pGame->sendGameOver(tmResponse);
      i_am_playing = 0;
    }
  }
  else // aktualis kornek nincs meg vege
  {
    if (pGame->is_client_synchronized(my_index))
    {
      tmResponse.put("result", "1");
      tmResponse.put("missing responses", itostring(missing_responses));
    }
    else 
    {
      // lezart kor eredmenyet elkuldom
      tmResponse.put("result", "2");
      pGame->sendStateAndBattlesAndRegen(tmResponse); // elkuldom az utoljara lezart kor utani allapotot es annak esemenyeit
      pGame->client_closed_round[my_index] = pGame->last_closed_round; // szinkronizalas megtortent "2"-es uzenettel
      
      if (pGame->is_client_gameover(my_index)) // ha befejezodott a jatek szamomra
      {
        pGame->sendGameOver(tmResponse);
        i_am_playing = 0;
      }
    }
  }
  tmResponse.put("roundendtime", getTimeString(pGame->last_closed_time+pGame->round_timespan));
  tmResponse.put("actround", itostring(pGame->last_closed_round+1));
  pGame->sendClientState(tmResponse);
  return i_am_playing;
}

// 0   : game over van, lehet torolni a game manager objektumot
// 1.. : mehet tovabb a jatek
int HexaWarGameManager::handleMessage(textmap* ptmParams, textmap& tmResponse)
{
  // ez az eset elvileg nem fordulhat elo, de ellenorzom:
  if (pGameData==NULL)
  {
    setErrorResponse(tmResponse, "0"); // érvénytelen game
    return 1;
  }
  HexaWarGameData* pData = (HexaWarGameData*)pGameData;

  // a jatek adatait egy idoben csak egy jatekos szal irhatja!!!
  scopewrite gamedata_sw(pData->rwLock);

  if ( (*ptmParams)["gtype"] != pData->gametypeid )
  {
    setErrorResponse(tmResponse, "0"); // érvénytelen gtype
    return 1;
  }

  // akciotol fuggo elagazas
	if ( (*ptmParams)["action"] == "askround" )
  {
    return handle_move(NULL, tmResponse);
  }
  else if ( (*ptmParams)["action"] == "move" )
  {
    MovementSequence* p_mov_seq = new MovementSequence(ptmParams);
    return handle_move(p_mov_seq, tmResponse);
  }
  else if ( (*ptmParams)["action"] == "askstate" )
  {
    sendGameData(ptmParams, tmResponse);
    tmResponse.put("result","2");
    // ha mar gameover volt nekem akkor csak elkuldom valaszkent a meglevo allapotot
    if (pData->is_client_gameover(my_index))
    {
      pData->sendGameOver(tmResponse); // GAME OVER
      return 0;
    }
    return 1;
  }
  else // ervenytelen action
  {
    setErrorResponse(tmResponse, "1");
    return 1;
  }
}

// a válasz stringbe berakja a GameData-ban tárolt teljes játék állapotot
// FONTOS: a függvény a GameData adatokat olvassa ki ezért a hívása elõtt
// lock-olni kell a GameData-t olvasasra ha szukseges
void HexaWarGameManager::sendGameData(textmap* ptmParams, textmap& tmResponse)
{
  HexaWarGameData* pData = (HexaWarGameData*)pGameData;
  pData->pHexaWarGraph->getGraphData(tmResponse);
  pData->pHexaWarGraph->getPlayerState(tmResponse);

  tmResponse.put("hg_sajatsorszam", itostring(my_index));
  for (int i=0; i<pData->playernames.get_count(); i++) 
    tmResponse.put(string("hg_jnev")+itostring(i), *(pData->playernames[i]));

  tmResponse.put("roundendtime", getTimeString(pData->last_closed_time+pData->round_timespan));
  tmResponse.put("actround", itostring(pData->last_closed_round+1));
  tmResponse.put("lastround", itostring(pData->last_round));	

  pData->sendClientState(tmResponse);
}

HexaWarGameManager::~HexaWarGameManager()
{
  HexaWarGameData* pData = (HexaWarGameData*)pGameData;
  if (pData!=NULL)
  {
    int my_place=0;
    for (my_place=0; my_place<pData->playernum; my_place++)
    {
      if (pData->scores_id_list[my_place]==my_index)
        break;
    }
    int my_points = pData->scores_points_list[my_place];
    LocalConnection lconn;
    variant vars, resp;
	  put(vars, 0, variant(my_points));
    put(vars, 1, variant(pUser->id));
	  lconn.query("UPDATE account SET points=points+?, played=played+1 WHERE id=?", vars, resp);
  }
}
