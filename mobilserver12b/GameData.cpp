// GameData.cpp: implementation of the GameData class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

GameData::GameData(): references(0), playernames(true), playerids(true)
{ 
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("NEW GameData");
#endif
}

GameData::~GameData()
{
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("DELETE GameData");
#endif
}

void GameData::link(GameData*& pGameData)
{
	scopewrite sw(rwLock);
	references++;
	pGameData = this;
}

void GameData::unlink(GameData*& pGameData)
{
	pGameData->rwLock.wrlock();
	pGameData->references--; // csökkentem a referenciaszámot
	if (pGameData->references<1) // ha már senki nem hivatkozik rá akkor törlöm
	{
		pGameData->rwLock.unlock();
		delete pGameData;
	}
	else
	{
		pGameData->rwLock.unlock();
	}
	pGameData = NULL;
}
