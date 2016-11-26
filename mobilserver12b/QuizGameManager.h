// QuizGameManager.h: interface for the QuizGameManager class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_QUIZGAMEMANAGER_H__41B3BC89_DF2C_4136_8705_5ABB6B17AE48__INCLUDED_)
#define AFX_QUIZGAMEMANAGER_H__41B3BC89_DF2C_4136_8705_5ABB6B17AE48__INCLUDED_

#include "GameManager.h"

class QuizGameManager : public GameManager  
{
public:

	QuizGameManager(SessionUser* pSUser): GameManager(pSUser) { }

	virtual int handleMessage(textmap* ptmParams, textmap& tmResponse);

	virtual string getName() { return "kviz"; }

	virtual void sendGameData(textmap* ptmParams, textmap& tmResponse);
};

#endif // !defined(AFX_QUIZGAMEMANAGER_H__41B3BC89_DF2C_4136_8705_5ABB6B17AE48__INCLUDED_)
