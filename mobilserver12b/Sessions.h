// Sessions.h: interface for the Sessions class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_SESSIONS_H__49A02C43_0595_43AC_8C99_E3E7DB9B051F__INCLUDED_)
#define AFX_SESSIONS_H__49A02C43_0595_43AC_8C99_E3E7DB9B051F__INCLUDED_


#define MAX_SESSION_COUNT   50000

class SessionUserData
{
public:
  mutex user_mutex;
  SessionUser* p_session_user;
  SessionUserData(): p_session_user(NULL) { }
  ~SessionUserData() { if (p_session_user!=NULL) delete p_session_user; }
};

class Sessions
{
private:
  
  static Sessions* pSessions;

  mutex hash_mutex;
	tstrlist<SessionUserData> user_data_hash; // login eseten ebbol ellenorzi gyorsan, hogy van-e adott id-vel session
  
  SessionUserData* user_data_array; // ebben az arrayban van MAX_SESSION_COUNT szamu SessionUserData elem

	datetime last_check; // utoljára mikor lett leellenõrizve a session lejárat

  void deleteExpiredSessions();
	
	Sessions(); // private: csak sajat maga hozhatja letre, singleton modell megvalositasa getSession()-el

public:

  static Sessions* getSessions();
	
	virtual ~Sessions();

	void createNewSessionUser(string id, textmap* ptmParams, textmap& tmResponse);

	void handleUserRequest(textmap* ptmParams, textmap& tmResponse);

	int get_count();

  void getDebugData(textmap* ptmParams, textmap& tmResponse);

  void deleteAllSessions();
};

#endif // !defined(AFX_SESSIONS_H__49A02C43_0595_43AC_8C99_E3E7DB9B051F__INCLUDED_)
