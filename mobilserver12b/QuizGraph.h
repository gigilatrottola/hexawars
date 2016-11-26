// QuizGraph.h: interface for the QuizGraph class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_)
#define AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_


class GraphNode // a terv szerint ez az osztály csak adatot tárol, a mûveletek a QuizGraph-ban vannak
{
public:
	/* A szomszéd csomópontok jelölése: 
	 *  ___ ___ ___
	 * | 0 | 1 | 2 |
	 * |___|___|___|
	 * | 3 |   | 4 |
	 * |___|___|___|
	 * | 5 | 6 | 7 |
	 * |___|___|___|
	 * 
	 */
	int myedgeindexes[4]; // a 2,4,6,7 helyen levõ négy él a tulajdona a csomópontnak,
						  // ezeknek a QuizGraph.nodes listában levõ indexe vagy -1 ha 
						  // nincs abban az irányban szomszéd
	int edges[4]; // témakör sorszáma vagy -1 ha nincs él abban az irányban

	int owner;  // -1 ha senki se foglalta el, vagy a tulajdonos sorszáma,
				// a tulajdonost az a QuizGameData.playerlist lista indexelésével kapjuk meg
};


class QuizGameData;


class QuizGraph  
{
public:
	void addGraphToResponse(textmap& tmResponse);
	
	QuizGameData* pQuizGameData; // a gráfot létrehozó objektumra mutat, abban vannak a player adatok

	int jatekosszam;
	int nodes_cnt;
	int width;
	int height;
	GraphNode* nodes;

	QuizGraph(QuizGameData* pQGD);
	virtual ~QuizGraph();

};

#endif // !defined(AFX_QUIZGRAPH_H__83AF1794_75F4_4838_BB23_4AEADA119F32__INCLUDED_)
