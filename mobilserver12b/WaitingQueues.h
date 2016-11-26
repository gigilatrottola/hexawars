// WaitingQueues.h: interface for the WaitingQueues class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_WAITINGQUEUES_H__11C3C098_ED6B_4CED_BE2F_BE41E44F7AFF__INCLUDED_)
#define AFX_WAITINGQUEUES_H__11C3C098_ED6B_4CED_BE2F_BE41E44F7AFF__INCLUDED_


struct WaitingUserData // a sorsol�shoz �s t�rl�shez sz�ks�ges adatok
{
	string name;
  string mid;
	datetime apply_time; // mikor jelentkezett
	datetime wait_time; // utols� wait �zenet ideje
	int my_index;       // a pGameData beallitoja tolti fel ezt is
	GameData* pGameData;

	~WaitingUserData() { if (pGameData!=NULL) GameData::unlink(pGameData); }
};

struct GameTypeWaitingQueue
{
	tstrlist<WaitingUserData> waitinguserlist; // v�rakoz� j�t�kosok adatai
	datetime last_check; // utols� waitinguserlist-b�l t�rl�s ideje
	rwlock listRwLock;
	string id; // game type id
	string nev;
	int playernum; // r�sztvev� j�t�kosok sz�ma
	textmap data; // egy�b j�t�kt�pusf�gg� adatok ide az adatb�zisb�l ebbe ker�lnek

	GameTypeWaitingQueue(): waitinguserlist(SL_OWNOBJECTS|SL_SORTED) { }
};

struct GameWaitingQueues // egy j�t�khoz (pl. kviz) tartoz� �sszes v�rakoz�si sor
{
	string name; // j�t�k neve, pl. kviz
	tstrlist<GameTypeWaitingQueue> gtwqlist; // a kulcs a j�t�kt�pus id
	rwlock listRwLock; // az eg�sz lista lockja
	GameWaitingQueues(): gtwqlist(SL_OWNOBJECTS|SL_SORTED) { }
};

class WaitingQueues  
{
private:
	rwlock listRwLock;
	tstrlist<GameWaitingQueues> gamequeuelist;

  static WaitingQueues* pWaitingQueues;
  
  WaitingQueues(); // private: nem peldanyosithatja mas osztaly, csak sajat maga

public:

  // singleton, ha nincs letrehozza, ha van akkor csak visszaadja a mutatot
  static WaitingQueues* getWaitingQueues();

	virtual ~WaitingQueues();

	void getGameTypes(textmap* ptmParams, textmap& tmResponse);
	void waitForGameType(textmap* ptmParams, textmap& tmResponse, SessionUser* pSessionUser);
};

#endif // !defined(AFX_WAITINGQUEUES_H__11C3C098_ED6B_4CED_BE2F_BE41E44F7AFF__INCLUDED_)
