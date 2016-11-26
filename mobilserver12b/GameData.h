// GameData.h: interface for the GameData class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_)
#define AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_

// nem rakok bele hivatkoz�st a GameManager-ekre mert azok id�k�zben t�rl�dhetnek!
// ink�bb egy kis redund�ns adatt�rol�s (nevek �s id-k)

class GameData  
{
private:
	int references;

public:
	rwlock rwLock;

	tobjlist<string> playernames; // j�t�kosok nevei
	tobjlist<string> playerids;   // j�t�kos id-k (sessionuser el�r�s�hez ha kell
	
	string gamename; // j�t�k neve (pl. kviz)
	string gametypeid; // j�t�kt�pus id
	string gametypename; // j�t�kt�pus neve

	GameData();
	virtual ~GameData();
    
	// a megadott pointer r�mutat erre az objektumra, a referenciasz�m n�vel�se
	void link(GameData*& pGameData);

	// a megadott pointer �ltal muatatott objektumra val� hivatkoz�s t�rl�se:
	// a referenciasz�m cs�kkent�se �s a mutat� NULL-ba �ll�t�sa,
	// ha sz�ks�ges akkor az objektum t�rl�se (ha referenciasz�m 0 lett)
	static void unlink(GameData*& pGameData);

};

#endif // !defined(AFX_GAMEDATA_H__65CA32E5_5A3F_43EC_9B6C_F410421B4B9C__INCLUDED_)
