// GameManager.h: interface for the GameManager class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GAMEMANAGER_H__896B7BF6_2B34_42BA_B2BD_5EB096FAA67E__INCLUDED_)
#define AFX_GAMEMANAGER_H__896B7BF6_2B34_42BA_B2BD_5EB096FAA67E__INCLUDED_


class GameData;

class GameManager  
{
public:
	SessionUser* pUser;

	GameData* pGameData;
	
	int my_index;

	GameManager(SessionUser* pSUser);

	virtual int handleMessage(textmap* ptmParams, textmap& tmResponse) = 0;

	virtual string getName() = 0;

	virtual ~GameManager();

	virtual void sendGameData(textmap* ptmParams, textmap& tmResponse) = 0;
};

#endif // !defined(AFX_GAMEMANAGER_H__896B7BF6_2B34_42BA_B2BD_5EB096FAA67E__INCLUDED_)
