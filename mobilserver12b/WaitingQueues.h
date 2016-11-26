// WaitingQueues.h: interface for the WaitingQueues class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_WAITINGQUEUES_H__11C3C098_ED6B_4CED_BE2F_BE41E44F7AFF__INCLUDED_)
#define AFX_WAITINGQUEUES_H__11C3C098_ED6B_4CED_BE2F_BE41E44F7AFF__INCLUDED_


struct WaitingUserData // a sorsoláshoz és törléshez szükséges adatok
{
	string name;
  string mid;
	datetime apply_time; // mikor jelentkezett
	datetime wait_time; // utolsó wait üzenet ideje
	int my_index;       // a pGameData beallitoja tolti fel ezt is
	GameData* pGameData;

	~WaitingUserData() { if (pGameData!=NULL) GameData::unlink(pGameData); }
};

struct GameTypeWaitingQueue
{
	tstrlist<WaitingUserData> waitinguserlist; // várakozó játékosok adatai
	datetime last_check; // utolsó waitinguserlist-bõl törlés ideje
	rwlock listRwLock;
	string id; // game type id
	string nev;
	int playernum; // résztvevõ játékosok száma
	textmap data; // egyéb játéktípusfüggõ adatok ide az adatbázisból ebbe kerülnek

	GameTypeWaitingQueue(): waitinguserlist(SL_OWNOBJECTS|SL_SORTED) { }
};

struct GameWaitingQueues // egy játékhoz (pl. kviz) tartozó összes várakozási sor
{
	string name; // játék neve, pl. kviz
	tstrlist<GameTypeWaitingQueue> gtwqlist; // a kulcs a játéktípus id
	rwlock listRwLock; // az egész lista lockja
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
