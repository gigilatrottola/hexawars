// GameManager.cpp: implementation of the GameManager class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

GameManager::GameManager(SessionUser* pSUser): pUser(pSUser)
{ 
  pGameData=NULL;
  my_index=0;
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("NEW GameManager");
#endif
}

GameManager::~GameManager()
{
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("DELETE GameManager");
#endif
	// ha van GameData adat akkor takarítani kell
	if (pGameData!=NULL) 
		GameData::unlink(pGameData);
}
