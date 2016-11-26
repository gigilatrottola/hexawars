// QuizGameManager.cpp: implementation of the QuizGameManager class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////


int QuizGameManager::handleMessage(textmap* ptmParams, textmap& tmResponse)
{
	return 1;
}

// a válasz stringbe berakja a GameData-ban tárolt teljes játék állapotot
// FONTOS: a függvény a GameData adatokat olvassa ki ezért a hívása elõtt
// lock-olni kell azt!
void QuizGameManager::sendGameData(textmap* ptmParams, textmap& tmResponse)
{
	// todo...
}
