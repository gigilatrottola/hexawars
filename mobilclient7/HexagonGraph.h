// HexagonGraph.h: interface for the HexagonGraph class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_HEXAGONGRAPH_H__6AB20DFB_F808_488D_BB06_67054479362E__INCLUDED_)
#define AFX_HEXAGONGRAPH_H__6AB20DFB_F808_488D_BB06_67054479362E__INCLUDED_


// a HexaWar jatektipus adatstrukturaja
class HexaWarNodeData
{
public:
  //////////////////////////////////////////////////////////////////////////////
  // kliens AI adatok:
  large cel_hasznossag; // mennyire erdemes odamenni sereggel
  large kezdes_hasznossag; // mennyire erdemes innen indulni sereggel
  //////////////////////////////////////////////////////////////////////////////
  // A csomópont hátterének típusa ( fû, szikla, ...)
	int hatter;
  // epulet az adott csomóponton (késõbb lehet vártípus, stb...)
	// 0 - nincs semmi
	// 1-... - van
	int epulet;
  /**
	 * a 3 féle katona mennyisége
	 */
	int sereg[3];
  // alapertekek beallitasa
  HexaWarNodeData():
    cel_hasznossag(0), // kezdeti potencial semleges
    kezdes_hasznossag(0),
    hatter(0), // valami alap háttér
    epulet(0) // nincs semmi
  {
    sereg[2] = sereg[1] = sereg[0] = 0; // nincs senki
  }
  inline int sereg_sum() { return sereg[0]+sereg[1]+sereg[2]; }
};


// A hatszög alapú pálya egy elemét reprezentáló adat
template <typename T_node_data>
class HexagonNode
{	
public:
	// az adott csomópont koordinátája
	int x, y;
	
	// A 0,1,2 csomópontokba vezetõ él típusa vagy -1 ha nincs oda él
	int my_edges[3];

  /**
	 * melyik jatekose az adott csomopont
	 * -1 - senkié
	 * 0-N - valamelyik játékosé
	 */
	int jatekos;
	
  // az adott jatek fajta adatait tartalmazo tipus egy peldanya
  T_node_data node_data;
	
	// default értékek beállítása 
	HexagonNode(): 
    x(0), y(0),
    jatekos(-1) // senkié
  {
    my_edges[2] = my_edges[1] = my_edges[0] = -1; // nincsenek élek
  }
};

////////////////////////////////////////////////////////////////////////////////
// HexagonGraph

/**
 * A hatszög alapú pálya ebben az osztályban van. A pálya tetszõleges alakú lehet,
 * azaz lehetnek benne üres csomópontok is. A nodes vektor tartalmazza az összes
 * csomópont adatot, abból kerül feltöltésre a graph tömb. Ahol nincs csomópont ott
 * -1 értékkel, ahol van ott a nodes-beli csomópont indexével (0-tól nodes mérete-1)
 * 
 * A 6 szomszédos csomópont számozása:
 *    	__
 *   __/5 \__
 *  /4 \__/0 \
 *  \__/  \__/
 *	/3 \__/1 \
 *  \__/2 \__/
 *	   \__/
 * 
 * Ezek közül az elsõ három csomópontba vezetõ él lesz az adott
 * csomópont tulajdonában. 
 */
template <typename T_node_data>
class HexagonGraph
{	
protected:
	// a gráfot tartalmazó tomb, elemei a graf csomopontjai
	HexagonNode<T_node_data>* nodes;
  int node_cnt;
	// a gráf elemei egy graph_vector_width*graph_vector_height nagyságú tömbben
	// az int érték vagy a nodes tömb egy indexe vagy -1 ha nincs ott csomópont 
  int* graph;
	// a vektorban tárolt gráfhoz a szélesség adat
	// a gráf X irányban egymás melletti cellái számának fele,
	// azaz páros számú cella lehet csak X irányban nézve
	int graph_width;
	// a vektorban tárolt gráfhoz a magasság adat
	// Y irányban 2 egymás melletti oszlopban levõ cellák összegének felel meg
	int graph_height;
		
	// páros Y esetén a 6 irányban levõ szomszédok X koordinátájának
	// eltolása az adott csomópont X koordinátájához képest
	static const int neighbour_X_displacement_evenY[6];
	// páratlan Y esetén a 6 irányban levõ szomszédok X koordinátájának
	// eltolása az adott csomópont X koordinátájához képest
	static const int neighbour_X_displacement_oddY[6];
	// a 6 irányban a szomszédok Y koordinátájának eltolása az adott
	// csomópont Y koordinátájához képest
	static const int neighbour_Y_displacement[6];
  
public:
  // konstruktor:
  // feltoltes textmapbol, dobhat exceptiont ha hibas adatok vannak
  // a textmapban
  // konstruktorban dobott exception eseten a sajat destruktor torzse nem fut le
  // viszont az ososztaly es a tagvaltozok destruktora lefut
  HexagonGraph(const textmap& tm);
  ~HexagonGraph();

  // berakja a response-ba a graf statikus adatait, amit a konstruktorban kiolvas
  void getGraphData(textmap& tmResponse);
	
	void setPlayerState(const textmap& tm);
  void getPlayerState(textmap& tmResponse);
  
  // fuggvenyek a letezo csomopontok linearis eleresehez pl. for ciklushoz
  inline int getNodeIndex(int x, int y) { return x + graph_width * y; }
  inline int getNodeCount() { return node_cnt; }
  inline HexagonNode<T_node_data>* getNode(int index) { return nodes+index; }
	
	// adott [x,y] koordinatabol indulva direction iranyban hexagonban
  // megadja az [nx,ny] koordinatat, nem ellenorzi hogy ott van-e valami
  void getNeighbourCoords(int x, int y, int direction, int& nx, int& ny);

  // (x,y) -> (nx,ny) szomszedok kozti el iranyat adja meg vagy -1 ha nem is szomszedosak
  // nem ellenorzi hogy van-e ott tenylegesen csomopont
  int getNeighbourDirection(int x, int y, int nx, int ny);

  // adott koordinatan levo HexagonNode visszaadasa, vagy NULL ha nincs ott node
  // ha az abrazolt teruleten kivul esik vagy ha az adott helyen nincs node 
  // akkor NULL-t ad vissza, egyebkent pointert a graph egyik elemere
  HexagonNode<T_node_data>* getNode(int x, int y);

  // adott node adott iranyu szomszedja vagy NULL ha arrafele nincs semmi
  HexagonNode<T_node_data>* getNeighbourNode(const HexagonNode<T_node_data>* const pHN, int direction);
	
  // Adott irányú élt adja vissza az adott [x,y] koordinatarol nezve adott
  // direction (0..5) iranyban. Lehetseges hogy nem letezik csomopont [x,y]
  // mezon de van ide el (nem sajat el esete), lehetseges hogy nincs szomszedos
  // csomopont de van arrafele el (sajat el esete).
	// return: él típusa (0-N) vagy -1 ha nincs él
  int getEdge(int x, int y, int direction);
};

// static const adatok
template <typename T_node_data>
const int HexagonGraph<T_node_data>::neighbour_X_displacement_evenY[] = { 1, 1, 0, 0, 0, 0 };

template <typename T_node_data>
const int HexagonGraph<T_node_data>::neighbour_X_displacement_oddY[] = { 0, 0, 0, -1, -1, 0 }; 

template <typename T_node_data>
const int HexagonGraph<T_node_data>::neighbour_Y_displacement[] = { -1, 1, 2, 1, -1, -2 };


template <typename T_node_data>
HexagonGraph<T_node_data>::HexagonGraph(const textmap& tm)
{
  // a gráf alaprajz eltárolása a vektorban
  // a megadott csomópontok száma:
  node_cnt = (int)str2num(tm["hg_nodenum"], -1);
  if (node_cnt==-1) throw new exception("HexagonGraph: hg_nodenum is invalid");
  
  // ebben tárolom az adatokat:
  nodes = new HexagonNode<T_node_data>[node_cnt];

  try
  {
    tpodlist<large> list;
    // a koordináták, x és y
    CSIStoIntList(tm["hg_cx"], list);
    if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].x = (int)list[i];
    else throw new exception("HexagonGraph: hg_cx has invalid size");
    
    list.clear();
    CSIStoIntList(tm["hg_cy"], list);
    if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].y = (int)list[i];
    else throw new exception("HexagonGraph: hg_cy has invalid size");
    
    list.clear();
    CSIStoIntList(tm["hg_e0"], list);
    if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].my_edges[0] = (int)list[i];
    else throw new exception("HexagonGraph: hg_e0 has invalid size");
    
    list.clear();
    CSIStoIntList(tm["hg_e1"], list);
    if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].my_edges[1] = (int)list[i];
    else throw new exception("HexagonGraph: hg_e1 has invalid size");
    
    list.clear();
    CSIStoIntList(tm["hg_e2"], list);
    if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].my_edges[2] = (int)list[i];
    else throw new exception("HexagonGraph: hg_e2 has invalid size");
    
    // a gráf méretét megállapítom úgy hogy pont beleférjenek a
    // megadott koordinátájú csomópontok
    int max_x = 0;
    int max_y = 0;
    for (int i=0; i<node_cnt;i++)
    { 
      if (nodes[i].x>max_x) max_x = nodes[i].x;
      if (nodes[i].y>max_y) max_y = nodes[i].y;
    }
    graph_width  = max_x+1;
    graph_height = max_y+1;
    int graph_size = graph_width*graph_height;
    if ( (graph_width<2) || (graph_width>64) ) throw new exception("HexagonGraph: graph_vector_width invalid");
    if ( (graph_height<2) || (graph_height>64) ) throw new exception("HexagonGraph: graph_vector_height invalid");

    graph = new int[graph_size];
    // feltöltöm a gráfot a nodes indexeivel ahol van csomópont
    // és -1-el ahol nincs
    for (int i=0; i<graph_size; i++) graph[i] = -1;
    for (int i=0; i<node_cnt;i++) graph[ nodes[i].x + graph_width * nodes[i].y ] = i; 
  }
  catch (exception* pExc)
  {
    delete[] nodes;
    throw pExc;
  }
}

template <typename T_node_data>
HexagonGraph<T_node_data>::~HexagonGraph()
{
  delete[] nodes;
  delete[] graph;
}

template <typename T_node_data>
void HexagonGraph<T_node_data>::getGraphData(textmap& tmResponse)
{
  tmResponse.put("hg_nodenum", itostring(node_cnt));
  string str;

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].x); }
  tmResponse.put("hg_cx", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].y); }
  tmResponse.put("hg_cy", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].my_edges[0]); }
  tmResponse.put("hg_e0", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].my_edges[1]); }
  tmResponse.put("hg_e1", str);
  
  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].my_edges[2]); }
  tmResponse.put("hg_e2", str);
}

template <typename T_node_data>
void HexagonGraph<T_node_data>::setPlayerState(const textmap& tm)
{
  tpodlist<large> list;
  CSIStoIntList(tm["hg_jatekos"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].jatekos = (int)list[i];
  else throw new exception("HexagonGraph::setPlayerState: hg_jatekos has invalid size");
}

template <typename T_node_data>
void HexagonGraph<T_node_data>::getPlayerState(textmap& tmResponse)
{
  string hg_jatekos;
  for (int i=0; i<node_cnt; i++)
  {
    if (i>0) hg_jatekos += ",";
    hg_jatekos += itostring(nodes[i].jatekos);
  }
  tmResponse.put("hg_jatekos", hg_jatekos);
}

template <typename T_node_data>
void HexagonGraph<T_node_data>::getNeighbourCoords(int x, int y, int direction, int& nx, int& ny)
{
  nx = x + ( (y % 2 == 0) ? 
             neighbour_X_displacement_evenY[direction] :
             neighbour_X_displacement_oddY[direction] );
  ny = y + neighbour_Y_displacement[direction];
}

template <typename T_node_data>
int HexagonGraph<T_node_data>::getNeighbourDirection(int x, int y, int nx, int ny)
{
  int x2,y2;
  for (int dir=0; dir<6; dir++)
  {
    getNeighbourCoords(x, y, dir, x2, y2);
    if ( (x2==nx) && (y2==ny) ) return dir;
  }
  return -1;
}

template <typename T_node_data>
HexagonNode<T_node_data>* HexagonGraph<T_node_data>::getNode(int x, int y)
{
  // ellenõrzöm hogy nx és ny értékek a graph tömb által ábrázolt
  // világrészbe beleesnek-e, ha nem akkor null-t adok vissza
  if ( (x<0) || (x>=graph_width) ) return NULL;
  if ( (y<0) || (y>=graph_height) ) return NULL;
  // megnézem hogy az adott helyen van-e csomópont, ha nincs akkor null-t adok
  int node_idx = graph[ x + graph_width * y ];
  return ( (node_idx==-1) ? NULL : (nodes+node_idx) );
}

template <typename T_node_data>
HexagonNode<T_node_data>* HexagonGraph<T_node_data>::getNeighbourNode(const HexagonNode<T_node_data>* const pHN, int direction)
{
  int nx, ny;
  getNeighbourCoords(pHN->x, pHN->y, direction, nx, ny);
  return getNode(nx, ny);
}

template <typename T_node_data>
int HexagonGraph<T_node_data>::getEdge(int x, int y, int direction)
{
  HexagonNode<T_node_data>* pHN;
	if (direction<3) // sajat el esete
		{
      pHN = getNode(x,y);
		}
		else // az el az adott iranyu csomoponthoz tartozik
		{
      int nx,ny;
      getNeighbourCoords(x,y,direction,nx,ny);
      pHN = getNode(nx,ny);
      direction -= 3;
		}
    if (pHN==NULL) return -1;
		return pHN->my_edges[direction];
}

////////////////////////////////////////////////////////////////////////////////
// HexaWarGraph

class HexaWarGraph: public HexagonGraph<HexaWarNodeData>
{
public:
  /**
	 * A paraméterként megadott hashtáblában levõ sztringekbõl kiolvassa
	 * a gráf felépítését és kitölti belõle a tagváltozókat.
	 * A kulcsok - értékek amik kellenek:
	 * hg_nodenum = mennyi csomópont van a gráf leírásban
	 * hg_cx = X koordináta
	 * hg_cy = Y koordináta
	 * hg_hatter = háttér hatszög
	 * hg_epulet = épület
	 * hg_e0 = 0 irányban az él
	 * hg_e1 = 1 irányban az él
	 * hg_e2 = 2 irányban az él
	 */
  HexaWarGraph(const textmap& tm);

  // berakja a response-ba a graf statikus adatait, amit a konstruktorban kiolvas
  void getGraphData(textmap& tmResponse);

  /**
	 * A játékosok tevékenysége adja meg a gráf aktuális állapotát.
	 * Az állapotot leíró adatok:
	 * hg_jatekos = játékos aki elfoglalta vagy -1
	 * hg_sereg0 = 0. típusú katonák száma
	 * hg_sereg1 = 1. 
	 * hg_sereg2 = 2.
	 */	
  void setPlayerState(const textmap& tm);
  
  // az aktualis allapotot kiolvassa es az alapjan kiegesziti a szerver valasz
  // stringjet az addResponseProperty() fuggveny hasznalataval
  // hg_jatekos, hg_sereg0, hg_sereg1, hg_sereg2 ertekkel egesziti ki a 
  // parameterkent megadott stringet
  void getPlayerState(textmap& tmResponse);

};

#endif // !defined(AFX_HEXAGONGRAPH_H__6AB20DFB_F808_488D_BB06_67054479362E__INCLUDED_)
