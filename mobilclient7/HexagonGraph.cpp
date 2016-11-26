#include "include.h" 

////////////////////////////////////////////////////////////////////////////////
// HexaWarGraph

HexaWarGraph::HexaWarGraph(const textmap& tm): HexagonGraph<HexaWarNodeData>(tm)
{
  tpodlist<large> list;
  CSIStoIntList(tm["hg_hatter"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].node_data.hatter = (int)list[i];
  else throw new exception("HexaWarGraph: hg_hatter has invalid size");

  list.clear();
  CSIStoIntList(tm["hg_epulet"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].node_data.epulet = (int)list[i];
  else throw new exception("HexaWarGraph: hg_epulet has invalid size");
}

void HexaWarGraph::getGraphData(textmap& tmResponse)
{
  HexagonGraph<HexaWarNodeData>::getGraphData(tmResponse);
  string str;

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].node_data.hatter); }
  tmResponse.put("hg_hatter", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].node_data.epulet); }
  tmResponse.put("hg_epulet", str);
}

void HexaWarGraph::setPlayerState(const textmap& tm)
{
  HexagonGraph<HexaWarNodeData>::setPlayerState(tm);

  tpodlist<large> list;
  CSIStoIntList(tm["hg_sereg0"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].node_data.sereg[0] = (int)list[i];
  else throw new exception("HexaWarGraph::setPlayerState: hg_sereg0 has invalid size");

  list.clear();
  CSIStoIntList(tm["hg_sereg1"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].node_data.sereg[1] = (int)list[i];
  else throw new exception("HexaWarGraph::setPlayerState: hg_sereg1 has invalid size");

  list.clear();
  CSIStoIntList(tm["hg_sereg2"], list);
  if (list.get_count()==node_cnt) for (int i=0; i<node_cnt;i++) nodes[i].node_data.sereg[2] = (int)list[i];
  else throw new exception("HexaWarGraph::setPlayerState: hg_sereg2 has invalid size");
}

void HexaWarGraph::getPlayerState(textmap& tmResponse)
{
  HexagonGraph<HexaWarNodeData>::getPlayerState(tmResponse);
  string str;

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].node_data.sereg[0]); }
  tmResponse.put("hg_sereg0", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].node_data.sereg[1]); }
  tmResponse.put("hg_sereg1", str);

  clear(str);
  for (int i=0; i<node_cnt; i++) { if (i>0) str+=","; str+=itostring(nodes[i].node_data.sereg[2]); }
  tmResponse.put("hg_sereg2", str);
}
