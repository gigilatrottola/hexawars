// QuizGraph.cpp: implementation of the QuizGraph class.
//
//////////////////////////////////////////////////////////////////////

#include "include.h"

#include <math.h>

#include "templates.hpp"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

QuizGraph::QuizGraph(QuizGameData* pQGD) // a GraphNode objektumok automatikus törlése
{
	pQuizGameData = pQGD;
	// itt jön a gráf generálás, a játékosokra vonatkozó adatok a pQuizGameData-ból nyerhetõk
	
	//elõször a gráf méretei a résztvevõk száma alapján, minden résztvevõre 4 csomópont jusson
	jatekosszam = pQuizGameData->playernames.get_count();
	width = height = (int)( 2.0 * sqrt(jatekosszam) + 0.5 );

	// a node objektumok létrehozása
	nodes_cnt = width*height;
	nodes = new GraphNode[nodes_cnt];
	
	// csomópontok feltöltése adatokkal
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

	// élek témakörei vagy -1 ha nincs él
	int idx,k;
	for (idx=0; idx<nodes_cnt; idx++)
	{
		// inicializálás: nincs él
		for (k=0; k<4; k++) nodes[idx].edges[k] = -1;
		// összes létezõ szomszédcsomópont összegyûjtése egy listába, ebbõl sorsolok majd
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

	// minden játékosnak választani kell egy kezdõpontot ahonnan indul
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
 * A gráf leírásának hozzáadása a szerver válaszához.
 * width, height - gráf méretei
 * edges - az élek topic_id-ját tartalmazó hexa karakterek sztringje
 * playernum - játékosok száma
 * pname_i - az i-edik játékos neve, 0-tól playernum-1-ig meg kell adni mindet
 * nobp_i  - az i-edik játékos által birtokolt mezõk indexei vesszõvel elválasztva
 * a kliens által várt whichami mezõ nincs benne, ezt a gráf objektum nem tudja,
 * az aktuális játékos manager osztálynak kell tudnia ezt magáról és hozzátenni!
 */
void QuizGraph::addGraphToResponse(textmap& tmResponse)
{
	tmResponse.put("width", itostring(width));
	tmResponse.put("height", itostring(height));

	string edges;
	// feltöltöm az edges sztringet az éleknek megfelelõ hexa értékekkel
	// egy csomópont 4 éle az 8 hexa karakter, él: 00h-FEh, FFh=nincs él
	// tehát 254 különbözõ témakör lehet összesen az adott játéktípusban
	int x,y;
	for (y=0; y<height; y++)
		for (x=0; x<width; x++)
		{
			int idx = y*width+x;
			// élek témakörei vagy -1 ha nincs él
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
	// játékosonkénti adatok megadása: pname_i és nobp_i
	int jatekos;
	for (jatekos=0; jatekos<jatekosszam; jatekos++)
	{
		// játékos neve
		tmResponse.put("pname_"+itostring(jatekos), *(pQuizGameData->playernames[jatekos]) );

		// játékos által birtokolt csomópontok indexe vesszõvel elválasztva
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
