// QuizGameData.h: interface for the QuizGameData class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_QUIZGAMEDATA_H__A0AE640C_B9EB_4DB1_9E65_6C771FFE6DA3__INCLUDED_)
#define AFX_QUIZGAMEDATA_H__A0AE640C_B9EB_4DB1_9E65_6C771FFE6DA3__INCLUDED_

class QuizGameData : public GameData  
{
public:
	
	tpodlist<int> topics; // témakörök ebben a kvízjátékban

	QuizGameData(textmap& data);
	virtual ~QuizGameData();
};

#endif // !defined(AFX_QUIZGAMEDATA_H__A0AE640C_B9EB_4DB1_9E65_6C771FFE6DA3__INCLUDED_)
