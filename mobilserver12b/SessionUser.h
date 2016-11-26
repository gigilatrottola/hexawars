// SessionUser.h: interface for the SessionUser class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_SESSIONUSER_H__02B339FA_49AF_4BDB_BB91_3B22E9BA1351__INCLUDED_)
#define AFX_SESSIONUSER_H__02B339FA_49AF_4BDB_BB91_3B22E9BA1351__INCLUDED_

class GameManager;

class SessionUser
{
public:
	
	string id; // account.id
	string name;  // account.login
	string skey; // generált rnd session kulcs
  string lang; // a kliens nyelve ( EN, ... )
  string mid; // mobile/midlet ID
	datetime login_time; // amikor létrejött a session
	datetime last_access_time; // utolsó kliens kiszolgálás ideje
	GameManager* pGameMng;

	SessionUser(string id, string name, string skey, string lang, string mid);
	~SessionUser();
	
	void handleMessage(textmap* ptmParams, textmap& tmResponse);

};

#endif // !defined(AFX_SESSIONUSER_H__02B339FA_49AF_4BDB_BB91_3B22E9BA1351__INCLUDED_)
