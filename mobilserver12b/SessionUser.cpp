// SessionUser.cpp: implementation of the SessionUser class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

class WaitingQueues;

SessionUser::SessionUser(string id, string name, string skey, string lang, string mid)
{
	this->id = id;
	this->name = name;
	this->skey = skey;
  this->lang = lang;
  this->mid = mid;
	login_time = now();
	last_access_time = now();
	pGameMng = NULL;
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("NEW SessionUser: id="+id+"  name="+name+"  skey="+skey);
#endif
}

SessionUser::~SessionUser()
{
	if (pGameMng!=NULL) delete pGameMng;
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("DELETE SessionUser: id="+id);
#endif
}

/*
 * az adott felhaszn�l�nak k�ld�tt �zenet feldolgoz�sa
 */
void SessionUser::handleMessage(textmap* ptmParams, textmap& tmResponse)
{
	last_access_time = now(); // feljegyzem hogy a felhaszn�l� k�r�st k�ld�tt �ppen most

	if (pGameMng==NULL) // ha nincs j�t�kban
	{
		if ( (*ptmParams)["action"] == "getgametypes" )
		{
			WaitingQueues::getWaitingQueues()->getGameTypes(ptmParams,tmResponse);
		}
		else if ( (*ptmParams)["action"] == "waitforgametype" )
		{
			WaitingQueues::getWaitingQueues()->waitForGameType(ptmParams,tmResponse, this);
		}
	}
	else // ha m�r l�trej�tt valami j�t�k
	{
		if ( pGameMng->getName() == (*ptmParams)["game"] )
		{
		  // ha a game kliens az amihez van game manager l�trehozva akkor 
		  // �tadjuk az �zenetet annak
      int retval = pGameMng->handleMessage(ptmParams, tmResponse);
			if (retval==0)
			{
			  delete pGameMng;
			  pGameMng = NULL;
			}
		}
		else // ha egy m�sik j�t�k �zenet�t kapta
		{
			// t�rl�m a j�t�k�t
			delete pGameMng;
			pGameMng = NULL;
			// v�lasz:
      setErrorResponse(tmResponse, "0");
		}
	}

}
