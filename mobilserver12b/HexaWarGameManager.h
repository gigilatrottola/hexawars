#if !defined(_HEXAWARGAMEMANAGER_H__INCLUDED_)
#define _HEXAWARGAMEMANAGER_H__INCLUDED_

class HexaWarGameManager : public GameManager  
{
public:
  virtual ~HexaWarGameManager();

	HexaWarGameManager(SessionUser* pSUser): GameManager(pSUser) { }

  int handle_move(MovementSequence* p_mov_seq, textmap& tmResponse);

	virtual int handleMessage(textmap* ptmParams, textmap& tmResponse);

	virtual string getName() { return "hexawar"; }

	virtual void sendGameData(textmap* ptmParams, textmap& tmResponse);
};

#endif // !defined(_HEXAWARGAMEMANAGER_H__INCLUDED_)
