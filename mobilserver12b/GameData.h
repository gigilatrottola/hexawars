// GameData.h: interface for the GameData class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_)
#define AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_

// nem rakok bele hivatkozást a GameManager-ekre mert azok idõközben törlõdhetnek!
// inkább egy kis redundáns adattárolás (nevek és id-k)

class GameData  
{
private:
	int references;

public:
	rwlock rwLock;

	tobjlist<string> playernames; // játékosok nevei
	tobjlist<string> playerids;   // játékos id-k (sessionuser eléréséhez ha kell
	
	string gamename; // játék neve (pl. kviz)
	string gametypeid; // játéktípus id
	string gametypename; // játéktípus neve

	GameData();
	virtual ~GameData();
    
	// a megadott pointer rámutat erre az objektumra, a referenciaszám növelése
	void link(GameData*& pGameData);

	// a megadott pointer által muatatott objektumra való hivatkozás törlése:
	// a referenciaszám csökkentése és a mutató NULL-ba állítása,
	// ha szükséges akkor az objektum törlése (ha referenciaszám 0 lett)
	static void unlink(GameData*& pGameData);

};

#endif // !defined(AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_)
