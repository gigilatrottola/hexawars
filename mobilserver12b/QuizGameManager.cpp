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

// a v�lasz stringbe berakja a GameData-ban t�rolt teljes j�t�k �llapotot
// FONTOS: a f�ggv�ny a GameData adatokat olvassa ki ez�rt a h�v�sa el�tt
// lock-olni kell azt!
void QuizGameManager::sendGameData(textmap* ptmParams, textmap& tmResponse)
{
	// todo...
}
