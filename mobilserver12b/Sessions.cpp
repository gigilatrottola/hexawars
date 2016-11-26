// Sessions.cpp: implementation of the Sessions class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

Sessions* Sessions::pSessions = NULL;

Sessions* Sessions::getSessions()
{
  if (pSessions==NULL) pSessions = new Sessions;
  return pSessions;
}

Sessions::Sessions(): user_data_hash(SL_SORTED|SL_CASESENS)
{
	user_data_array = new SessionUserData[MAX_SESSION_COUNT];
  last_check = now();
}

Sessions::~Sessions()
{
	delete[] user_data_array;
}

int Sessions::get_count()
{
  scopelock sl(hash_mutex); //  lockol�s f�ggv�ny v�g�ig
  int hash_count = user_data_hash.get_count();
  return hash_count;
}

void Sessions::deleteAllSessions() // minden session torlese, fuggetlenul az allastol
{
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
        mlog("Sessions::deleteAllSessions() called.");
#endif
  scopelock sl(hash_mutex); //  lockol�s f�ggv�ny v�g�ig
  int cnt = user_data_hash.get_count();
	for (int i=0; i<cnt; i++) // torlok minden SessionUser objektumot
	{
    scopelock sl_user(user_data_hash[i]->user_mutex);
    // session torlese
    delete user_data_hash[i]->p_session_user;
    user_data_hash[i]->p_session_user = NULL;
	}
  user_data_hash.clear(); // hash teljes tartalmat is torlom
}

void Sessions::deleteExpiredSessions() // elotte hash_mutex lockolasa kell!
{
  // ennyi id�nk�nt t�rt�nik meg az ellen�rz�s
	const datetime tocheck_timespan = encodetime(0, 10, 0, 0);
  // a session lej�r ha ennyi ideje nem haszn�lt�k:
	const datetime timeout_timespan = encodetime(0, 10, 0, 0);
  // aktualis ido
  const datetime act_time = now();
  
  if ( act_time >= (last_check+tocheck_timespan) )
  {
    int cnt = user_data_hash.get_count();
	  for (int i=0; i<cnt; i++)
	  {
      // ellenorzendo adat lockolasa
      scopelock sl_user(user_data_hash[i]->user_mutex);
	    // ha megadott ideje nem �rkezett �zenet a klienst�l akkor timeout van
	    if ( act_time >= (user_data_hash[i]->p_session_user->last_access_time+timeout_timespan) )
	    {
        // lejart session torlese
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
        mlog("Sessions::deleteExpiredSessions: session timeout, deleted user_id="+user_data_hash[i]->p_session_user->id);
#endif
        delete user_data_hash[i]->p_session_user;
        user_data_hash[i]->p_session_user = NULL;
		    user_data_hash.del(i);
		    cnt--;
			  i--;
	    }
    }
	  last_check = act_time;
  }
}

/* 
 * session-k�d kisz�m�t�sa, ez a visszat�r�si �rt�k
 * user-session objektum l�trehoz�sa, ha m�r van az adott id-j� usernek sessionje
 * akkor csak annak a session-kulcs�t adja vissza
 * egyben megvizsg�lja hogy van-e olyan session amit le lehet z�rni
 * ha a felhaszn�l� adott ideje nem kattintott akkor kil�pteti a sessionb�l
 */
void Sessions::createNewSessionUser(string id, textmap* ptmParams, textmap& tmResponse)
{
  scopelock sl(hash_mutex); //  lockol�s f�ggv�ny v�g�ig

  // el�sz�r a lej�rt session�k t�rl�se
  deleteExpiredSessions();

  // megnezem hogy sessionben van-e mar
  // ha igen akkor a meglevo session kulcs visszaadasa
  int idx;
	if (user_data_hash.search(id,idx))
	{
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
    mlog("Sessions::createNewSessionUser: login to existing session. user_id="+id);
#endif
    scopelock sl_user(user_data_hash[idx]->user_mutex);
    SessionUser* pUser = user_data_hash[idx]->p_session_user;
    tmResponse.put("id", id);
    tmResponse.put("skey", pUser->skey);
    if (pUser->pGameMng==NULL)
    {
      tmResponse.put("result", "1");
    }
    else // valami futo jatekban van mar
    {
      tmResponse.put("result", "2");
      // a jatek alapadatait is visszaadom
      GameData* pData = pUser->pGameMng->pGameData;
      scoperead gamedata_sr(pData->rwLock);
      tmResponse.put("game", pData->gamename);
	    tmResponse.put("gtype", pData->gametypeid);
    	tmResponse.put("gtypename", pData->gametypename);
    }
		return;
	}

	// ha az adott id-vel rendelkez� felhaszn�l�nak nincs session-je akkor l�trehozok neki egyet
  SessionUserData* p_free_slot = NULL;
  int slot_idx = 0;
  for (slot_idx=0; slot_idx<MAX_SESSION_COUNT; slot_idx++)
  {
    if (user_data_array[slot_idx].p_session_user==NULL)
    {
      p_free_slot = user_data_array + slot_idx;
      break;
    }
  }
  if (p_free_slot==NULL) // NINCS szabad slot!
  {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::createNewSessionUser: no slots available for new session!");
#endif 
    setErrorResponse(tmResponse, "42");
    return;
  }

  // letrehozom az uj SessionUser objektumot es berakom slotba, slotot bejegyzem hash-be
  Random rnd;
	string skey = itostring(slot_idx) + '-' + itostring(rnd.getInt(100000000,999999999)) + itostring(rnd.getInt(100000000,999999999));

  scopelock sl_user(user_data_array[slot_idx].user_mutex); // elvileg nem kell, csak biztonsag kedveert :)

  user_data_array[slot_idx].p_session_user = new SessionUser(id,(*ptmParams)["login"],skey,(*ptmParams)["lang"],(*ptmParams)["mid"]); // uj user a slotba

  user_data_hash.add(id, p_free_slot); // hash-be bejegyzem

#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
    mlog(string("Sessions::createNewSessionUser: new session created. user_id=")+id+string(" slot_idx=")+itostring(slot_idx));
#endif

  tmResponse.put("result", "1"); // sikeres session l�trehoz�s
  tmResponse.put("id", id);
  tmResponse.put("skey", skey);
}

/*
 * l�tez� session-h�z �rkezett k�r�s, ellen�rzi hogy van-e felhaszn�l� a bej�v�
 * id �s skey adatokhoz, ha igen akkor �tadja a k�r�s kezel�s�t annak a felhaszn�l�nak
 * ha nincs akkor hiba�zenet visszaad�sa
 */
void Sessions::handleUserRequest(textmap* ptmParams, textmap& tmResponse)
{
	stringlist skey_lst(true);
  split('-', (*ptmParams)["skey"], skey_lst);  // "array_index-skey_random_number"

  if (skey_lst.get_count()!=2)
  {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::handleUserRequest: invalid session key format!");
#endif
		setErrorResponse(tmResponse, "2");
    return;
  }

  int array_index = (int)str2num(*(skey_lst[0]), -1);

  if ( (array_index<0) || (array_index>=MAX_SESSION_COUNT) )
  {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::handleUserRequest: invalid session user array index!");
#endif
		setErrorResponse(tmResponse, "2");
    return;
  }

  SessionUserData& user_data = user_data_array[array_index];
  
  user_data.user_mutex.lock(); // LOCK!!!

  try
  {

  if (user_data.p_session_user==NULL)
  {
    user_data.user_mutex.unlock();
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::handleUserRequest: NULL session!");
#endif
		setErrorResponse(tmResponse, "2");
    return;
  }

  if (user_data.p_session_user->skey != (*ptmParams)["skey"])
  {
    user_data.user_mutex.unlock();
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::handleUserRequest: invalid session key!");
#endif
		setErrorResponse(tmResponse, "2");
    return;
  }

  if (user_data.p_session_user->id != (*ptmParams)["id"])
  {
    user_data.user_mutex.unlock();
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Sessions::handleUserRequest: invalid user id!");
#endif
		setErrorResponse(tmResponse, "2");
    return;
  }

  if ( (*ptmParams)["action"] == "logout" )
  {
    // eloszor a hash-t kell lockolni hogy ne legyen deadlock!
    user_data.user_mutex.unlock();
    // torles
    scopelock sl(hash_mutex);
    user_data.user_mutex.lock();
    if (user_data.p_session_user!=NULL) // az elozo unlock miatt megint elleorizni kell!
    {
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
      mlog("Sessions::handleUserRequest: logout from session. user_id="+user_data.p_session_user->id);
#endif
      user_data_hash.del(user_data.p_session_user->id);
      delete user_data.p_session_user;
      user_data.p_session_user = NULL;
      tmResponse.put("result", "1");
    }
  }
	else
  {
    user_data.p_session_user->handleMessage(ptmParams, tmResponse);
  }

  }//try
  catch(exception* pErr)
  {
    user_data.user_mutex.unlock();
    throw;
  }
  user_data.user_mutex.unlock();
}

void Sessions::getDebugData(textmap* ptmParams, textmap& tmResponse)
{
  scopelock sl(hash_mutex); //  lockol�s f�ggv�ny v�g�ig

  int user_count = user_data_hash.get_count();
  tmResponse.put("user_data_hash.get_count()=", itostring(user_count)); 

	for (int i=0; i<user_count; i++)
	{
    // adat lockolasa, ezutan a blokkban biztonsagosan elerheto a user_data_hash[i]->p_session_user osszes adata
    scopelock sl_user(user_data_hash[i]->user_mutex);
    SessionUser* p_user  = user_data_hash[i]->p_session_user;
    int user_array_index = user_data_hash[i] - user_data_array;
    string user_str = "player["+itostring(user_array_index)+"]";

    tmResponse.put(user_str+".id", p_user->id);
    tmResponse.put(user_str+".name", p_user->name);
    tmResponse.put(user_str+".skey", p_user->skey);
    tmResponse.put(user_str+".login_time", getTimeString(p_user->login_time));
    tmResponse.put(user_str+".last_access_time", getTimeString(p_user->last_access_time));
    string game_str;
    if (p_user->pGameMng==NULL) game_str = "NULL";
    else
    {
      game_str = p_user->pGameMng->getName();
      // ...
    }
    tmResponse.put(user_str+".pGameMng", game_str);  
  }
}
