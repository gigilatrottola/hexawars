// QuizGraph.h: interface for the QuizGraph class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_)
#define AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_


class GraphNode // a terv szerint ez az oszt�ly csak adatot t�rol, a m�veletek a QuizGraph-ban vannak
{
public:
	/* A szomsz�d csom�pontok jel�l�se: 
	 *  ___ ___ ___
	 * | 0 | 1 | 2 |
	 * |___|___|___|
	 * | 3 |   | 4 |
	 * |___|___|___|
	 * | 5 | 6 | 7 |
	 * |___|___|___|
	 * 
	 */
	int myedgeindexes[4]; // a 2,4,6,7 helyen lev� n�gy �l a tulajdona a csom�pontnak,
						  // ezeknek a QuizGraph.nodes list�ban lev� indexe vagy -1 ha 
						  // nincs abban az ir�nyban szomsz�d
	int edges[4]; // t�mak�r sorsz�ma vagy -1 ha nincs �l abban az ir�nyban

	int owner;  // -1 ha senki se foglalta el, vagy a tulajdonos sorsz�ma,
				// a tulajdonost az a QuizGameData.playerlist lista indexel�s�vel kapjuk meg
};


class QuizGameData;


class QuizGraph  
{
public:
	void addGraphToResponse(textmap& tmResponse);
	
	QuizGameData* pQuizGameData; // a gr�fot l�trehoz� objektumra mutat, abban vannak a player adatok

	int jatekosszam;
	int nodes_cnt;
	int width;
	int height;
	GraphNode* nodes;

	QuizGraph(QuizGameData* pQGD);
	virtual ~QuizGraph();

};

#endif // !defined(AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_)
