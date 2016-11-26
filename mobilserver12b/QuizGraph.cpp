// QuizGraph.cpp: implementation of the QuizGraph class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

#include <math.h>

#include "templates.hpp"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

QuizGraph::QuizGraph(QuizGameData* pQGD) // a GraphNode objektumok automatikus t�rl�se
{
	pQuizGameData = pQGD;
	// itt j�n a gr�f gener�l�s, a j�t�kosokra vonatkoz� adatok a pQuizGameData-b�l nyerhet�k
	
	//el�sz�r a gr�f m�retei a r�sztvev�k sz�ma alapj�n, minden r�sztvev�re 4 csom�pont jusson
	jatekosszam = pQuizGameData->playernames.get_count();
	width = height = (int)( 2.0 * sqrt(jatekosszam) + 0.5 );

	// a node objektumok l�trehoz�sa
	nodes_cnt = width*height;
	nodes = new GraphNode[nodes_cnt];
	
	// csom�pontok felt�lt�se adatokkal
  Random rnd;
	int x,y;
	for (y=0; y<height; y++)
		for (x=0; x<width; x++)
		{
			int idx = y*width + x;
			// 2
			if ( (x<(width-1)) && (y>0) ) nodes[idx].myedgeindexes[0] = idx-width+1; 
			else nodes[idx].myedgeindexes[0] = -1;
			// 4
			if (x<(width-1)) nodes[idx].myedgeindexes[1] = idx+1; 
			else nodes[idx].myedgeindexes[1] = -1;
			// 6
			if (y<(height-1)) nodes[idx].myedgeindexes[2] = idx+width;
			else nodes[idx].myedgeindexes[2] = -1;
			// 7
			if ( (x<(width-1)) && (y<(height-1)) ) nodes[idx].myedgeindexes[3] = idx+width+1;
			else nodes[idx].myedgeindexes[3] = -1;
		}

	// �lek t�mak�rei vagy -1 ha nincs �l
	int idx,k;
	for (idx=0; idx<nodes_cnt; idx++)
	{
		// inicializ�l�s: nincs �l
		for (k=0; k<4; k++) nodes[idx].edges[k] = -1;
		// �sszes l�tez� szomsz�dcsom�pont �sszegy�jt�se egy list�ba, ebb�l sorsolok majd
		tpodlist<int> vec;
		for (k=0; k<4; k++) if (nodes[idx].myedgeindexes[k]>=0) vec.add(k);
		if (vec.get_count()>=1)
		{
			shuffle<int>(vec,rnd);
			int rnd_edgenum = rnd.getInt(1,vec.get_count());
			for (k=0; k<rnd_edgenum; k++)
			{
				nodes[idx].edges[vec[k]] = rnd.getInt(0,pQuizGameData->topics.get_count()-1);
			}
		}

	}

	// minden j�t�kosnak v�lasztani kell egy kezd�pontot ahonnan indul
	tpodlist<int> vec;
	for (k=0; k<nodes_cnt; k++) vec.add(k);
	shuffle<int>(vec,rnd);
	for (k=0; k<jatekosszam; k++) nodes[vec[k]].owner = k;
}

QuizGraph::~QuizGraph()
{
	delete[] nodes;
}

/*
 * A gr�f le�r�s�nak hozz�ad�sa a szerver v�lasz�hoz.
 * width, height - gr�f m�retei
 * edges - az �lek topic_id-j�t tartalmaz� hexa karakterek sztringje
 * playernum - j�t�kosok sz�ma
 * pname_i - az i-edik j�t�kos neve, 0-t�l playernum-1-ig meg kell adni mindet
 * nobp_i  - az i-edik j�t�kos �ltal birtokolt mez�k indexei vessz�vel elv�lasztva
 * a kliens �ltal v�rt whichami mez� nincs benne, ezt a gr�f objektum nem tudja,
 * az aktu�lis j�t�kos manager oszt�lynak kell tudnia ezt mag�r�l �s hozz�tenni!
 */
void QuizGraph::addGraphToResponse(textmap& tmResponse)
{
	tmResponse.put("width", itostring(width));
	tmResponse.put("height", itostring(height));

	string edges;
	// felt�lt�m az edges sztringet az �leknek megfelel� hexa �rt�kekkel
	// egy csom�pont 4 �le az 8 hexa karakter, �l: 00h-FEh, FFh=nincs �l
	// teh�t 254 k�l�nb�z� t�mak�r lehet �sszesen az adott j�t�kt�pusban
	int x,y;
	for (y=0; y<height; y++)
		for (x=0; x<width; x++)
		{
			int idx = y*width+x;
			// �lek t�mak�rei vagy -1 ha nincs �l
			int k;
			for (k=0; k<4; k++)
			{
				int topic = nodes[idx].edges[k];
				if (topic<0) edges += "FF";
				else edges += itostring(topic, 16, 2, '0');
			}
		}
	tmResponse.put("edges", edges);

	tmResponse.put("playernum", itostring(jatekosszam));
	// j�t�kosonk�nti adatok megad�sa: pname_i �s nobp_i
	int jatekos;
	for (jatekos=0; jatekos<jatekosszam; jatekos++)
	{
		// j�t�kos neve
		tmResponse.put("pname_"+itostring(jatekos), *(pQuizGameData->playernames[jatekos]) );

		// j�t�kos �ltal birtokolt csom�pontok indexe vessz�vel elv�lasztva
		string owned;
		for (y=0; y<height; y++)
			for (x=0; x<width; x++)
			{
				int idx = y*width+x;
				if (nodes[idx].owner==jatekos)
				{
					if (length(owned)>0) owned += ",";
					owned += itostring(idx);
				}
			}		
		tmResponse.put("nobp_"+itostring(jatekos), owned);
	}
}
