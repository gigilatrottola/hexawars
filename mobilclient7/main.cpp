#include "include.h"

#include "templates.hpp"

#define WAITFORGAME_INERTVAL (20*1000)
#define STEP_INTERVAL  (10*1000)
#define QUIT_POLL_INTERVAL (5*60*1000)

//#define WAITFORGAME_INERTVAL (1000)
//#define STEP_INTERVAL  (500)
//#define QUIT_POLL_INTERVAL (15*1000)

#define LOG_ERROR       1
#define LOG_INFO        2
#define LOG_INFO2       4
#define LOG_INFO3       8
#define LOG_INFO4       16

int g_step_count = 0;

int g_log_filter = 0;
static mutex log_mutex;
void mlog(int log_type, const string& log_str)
{
  if (g_log_filter & log_type)
  {
    log_mutex.enter();
    pout.putf(log_str+"\n");
    log_mutex.leave();
  }
}

void logTextmap(const string& title, textmap& tm)
{
  string tmstr = title+" = { ";
  int tm_cnt = tm.get_count();
  for (int i=0; i<tm_cnt; i++)
  {
    if (i>0) tmstr += ", ";
    tmstr += tm.getkey(i)+"="+tm[i];
  }
  tmstr += " }";
  mlog(LOG_INFO3, tmstr);
}

void sendMessage(string hostname, int port, string proxyname, int proxyport, textmap& sendParams, textmap& receiveParams)
{
	// http://www.windowsitlibrary.com/Content/386/18/3.html
  string sendMsgStr;
  textmap2msgstr(sendParams, sendMsgStr);
  string dir = "?P=" + sendMsgStr;
  ipstream sockClient;
  if (length(proxyname)==0)
  {
    sockClient.set_ip(phostbyname(hostname));
    sockClient.set_port(port);
    sockClient.open();
    // request the page
    sockClient.putline("GET "+dir+" HTTP/1.0");
  }
  else // if http proxy was given connect through that
  {
    sockClient.set_ip(phostbyname(proxyname));
    sockClient.set_port(proxyport);
    sockClient.open();
    // request the page from the proxy
    sockClient.putline("GET http://"+hostname+":"+itostring(port)+"/"+dir+" HTTP/1.1");
  }  
  sockClient.putline("Host: "+hostname);
  sockClient.putline("Connection: close");
  sockClient.putline("User-agent: BOT/1.0");
  sockClient.putline("");
  sockClient.flush();
  string line; 
  string ret_data;
  bool bBody=false;
  while (!sockClient.get_eof())
  {
    line = sockClient.line(16*1024); // max. 16K lehet
    if (bBody)
    {
      ret_data += line;
    }
    else
    {
      if (length(line)==0) bBody = true;
    }
  }
  sockClient.close();
  if (!bBody) throw new exception("invalid http response: invalid http header");
  //mlog(LOG_INFO4, "RECV: "+ret_data);
  int retcode = msgstr2textmap(ret_data, receiveParams);
  if (retcode!=0) throw new exception("Invalid return data from server! Code="+itostring(retcode)+" -> "+ret_data);
}

void throwOnError(textmap& receiveParams)
{
  if (receiveParams["result"]=="0")
  {
    string err_str;
    string err_code = receiveParams["error"];
    if (err_code=="-1") err_str = "internal server error";
    else if (err_code=="0") err_str = "unknown/invalid game";
    else if (err_code=="1") err_str = "unknown/invalid action";
    else if (err_code=="2") err_str = "nonexistent session id or invalid skey";
    else err_str = "error code = "+err_code;
    throw new exception("Server returned error: "+err_str);
  }
}

void sendMsg(string logtitle, textmap& sendParams, textmap& receiveParams)
{
  logTextmap("SEND", sendParams);
  sendMessage("217.20.133.5", 80,"",0, sendParams, receiveParams);
  logTextmap("RECV", receiveParams);
  throwOnError(receiveParams);
}

void getInfo()
{
	textmap sendTm;
  textmap receiveTm;
  sendTm.put("action", "getsrvinfo");
  sendMsg("getsrvinfo",sendTm, receiveTm);
}

void login(string name, string password, string& id, string& skey)
{
  textmap sendTm, receiveTm;
  sendTm.put("action","login");
  sendTm.put("login",name);
  sendTm.put("passwd",password);
  sendTm.put("lang","EN");
  sendTm.put("mid", "BOT");
  sendMsg("login", sendTm, receiveTm);
  id = receiveTm["id"];
  skey = receiveTm["skey"];
}

void logout(string id, string skey)
{
  textmap sendTm, receiveTm;
  sendTm.put("action","logout");
  sendTm.put("game", "hexawar"); // akarmi lehet ures stringen kivul
  sendTm.put("id", id);
  sendTm.put("skey", skey);
  sendMsg("logout", sendTm, receiveTm);
}

void getgametypes(string id, string skey)
{
  textmap sendTm, receiveTm;
  sendTm.put("action","getgametypes");
  sendTm.put("game", "hexawar");
  sendTm.put("id", id);
  sendTm.put("skey", skey);
  sendMsg("getgametypes", sendTm, receiveTm);
  //int gtypenum = str2num(receiveTm["gtypenum"], large default_value=0)
}

bool waitforgametype(string id, string skey, string gtype, textmap& receiveTm)
{
  textmap sendTm;
  sendTm.put("action","waitforgametype");
  sendTm.put("game", "hexawar");
  sendTm.put("id", id);
  sendTm.put("skey", skey);
  sendTm.put("gtype", gtype);
  sendMsg("waitforgametype", sendTm, receiveTm);
  return (receiveTm["result"]=="2"); // true ha bekerult egy jatekba, false ha varakozasi sorban all
}

int askround(string id, string skey, string gtype, textmap& receiveTm)
{
  textmap sendTm;
  sendTm.put("action","askround");
  sendTm.put("game", "hexawar");
  sendTm.put("id", id);
  sendTm.put("skey", skey);
  sendTm.put("gtype", gtype);
  sendMsg("askround", sendTm, receiveTm);
  int result = (int)str2num(receiveTm["result"]);
  return result;
}

////////////////////////////////////////////////////////////////////////////////
class MovementSequence
{
public:
  int sereg[3];
  tpodlist<large> movementX;
  tpodlist<large> movementY;
  int begin_round; // act_index = (act_round - begin_round) * 3;
  MovementSequence(int br) { begin_round = br; sereg[2] = sereg[1] = sereg[0] = 0; }
  inline int sereg_sum() { return sereg[0]+sereg[1]+sereg[2]; }
  void maximalize(int* max_sereg) { for (int i=0; i<3; i++) if (sereg[i]>max_sereg[i]) sereg[i] = max_sereg[i]; }
  inline void wipe_sereg() { sereg[2] = sereg[1] = sereg[0] = 0; } // sereg torlese
  void set(int* p_sereg) { for (int i=0; i<3; i++) sereg[i] = p_sereg[i]; }
};
typedef tobjlist<MovementSequence> MovementSequenceList;

////////////////////////////////////////////////////////////////////////////////

int move(string id, string skey, string gtype, HexaWarGraph& hwg, MovementSequenceList& mov_seq_list, const int& my_index, textmap& receiveTm)
{
  Random rnd;
  int actround = (int)str2num(receiveTm["actround"]);
  MovementSequence* act_mozgas = new MovementSequence(actround);
  /////////////////////////////////////////////
  const int hwg_node_count = hwg.getNodeCount();
  for (int i=0; i<hwg_node_count; i++)
  {
    HexagonNode<HexaWarNodeData>* pNode = hwg.getNode(i);
    // nullazom a hasznossag ertekeket
    pNode->node_data.cel_hasznossag = 0;
    pNode->node_data.kezdes_hasznossag = 0;
    // annyira hasznos valahol kezdeni amennyi katonam ott allomasozik
    if (my_index==pNode->jatekos) // ha sajat sereg
      pNode->node_data.kezdes_hasznossag += pNode->node_data.sereg_sum();
  }

  // vegigmegyek a mozgasi szekvenciakon amik mar bent vannak
  // ahol van ervenyes sereg azt a szekveciat
  for (int mov_seq_idx=mov_seq_list.get_count()-1; mov_seq_idx>=0; mov_seq_idx--)
  {
    MovementSequence* p_mov_seq = mov_seq_list[mov_seq_idx];
    int last_index = p_mov_seq->movementX.get_count() - 1;
    int seq_index = (actround - p_mov_seq->begin_round) * 3;
    if (seq_index>=last_index) mov_seq_list.del(mov_seq_idx); // torlom a korabban mar befejezett szekvenciat
    else {
      HexagonNode<HexaWarNodeData>* pNode = hwg.getNode(p_mov_seq->movementX[seq_index],p_mov_seq->movementY[seq_index]);
      if (pNode==NULL) throw new exception("move: node not found!");
      if (my_index!=pNode->jatekos) p_mov_seq->wipe_sereg();
      if (p_mov_seq->sereg_sum()==0) // ha megsemmisult a seregem
        mov_seq_list.del(mov_seq_idx);
      else { // van itt mozgo seregem
        p_mov_seq->maximalize(pNode->node_data.sereg);
        // a sereg meretevel aranyosan csokkentem a mozgas hasznossagat: ha mar van mozgas akkor nem akarom 
        // megbolygatni, ez meglevo mozgas elpazarlasa es ugyanakkor leblokkolhatja a legnagyobb seregre minden
        // mozgasomat
        pNode->node_data.kezdes_hasznossag -= p_mov_seq->sereg_sum();
      }
    }
  }

  large max_hasznossag = 0; // csak 0-nal nagyobb hasznossagu helyrol indul sereg
  HexagonNode<HexaWarNodeData>* pKiinduloNode = NULL;
  for (int i=0; i<hwg_node_count; i++)
  {
    HexagonNode<HexaWarNodeData>* pNode = hwg.getNode(i);
    if (pNode->node_data.kezdes_hasznossag>max_hasznossag)
    {
      max_hasznossag = pNode->node_data.kezdes_hasznossag;
      pKiinduloNode = pNode;
    }
  }
   
  if (pKiinduloNode!=NULL) // ha van hasznos kiindulo hely, ha nincs akkor nincs mozgas se
  {
    int my_sereg = pKiinduloNode->node_data.sereg_sum();
    for (int i=0; i<hwg_node_count; i++)
    {
      HexagonNode<HexaWarNodeData>* pNode = hwg.getNode(i);
      if (pNode->jatekos==my_index) // sajat terulet
      {
        if (pNode->node_data.epulet==0)
          pNode->node_data.cel_hasznossag = 20; // sajat mezo
        else
          pNode->node_data.cel_hasznossag = 40; // sajat var
      }
      else if (pNode->jatekos==-1) // senki foldje
      {
        if (pNode->node_data.epulet==0)
          pNode->node_data.cel_hasznossag = 60; // ures mezo
        else
          pNode->node_data.cel_hasznossag = 100; // ures var
      }
      else // ellenfel kezeben van a terulet
      { 
        if (pNode->node_data.epulet==0) {
          if (pNode->node_data.sereg_sum()>=my_sereg)
            pNode->node_data.cel_hasznossag = -100; // erosebb ellenfel mezo
          else
            pNode->node_data.cel_hasznossag = 25; // gyengebb ellenfel mezo
        } else {
          if (pNode->node_data.sereg_sum()>=my_sereg)
            pNode->node_data.cel_hasznossag = -50; // erosebb ellenfel var
          else
            pNode->node_data.cel_hasznossag = 50; // gyengebb ellenfel var
        }
      }
    }

    act_mozgas->set(pKiinduloNode->node_data.sereg); // mindenki megy innen
    act_mozgas->movementX.add(pKiinduloNode->x); // berakom a kiindulasi pontot
    act_mozgas->movementY.add(pKiinduloNode->y);
    HexagonNode<HexaWarNodeData>* pActStart = pKiinduloNode;
    for (int path_i = 1; path_i<=g_step_count; path_i++)
    {
      HexagonNode<HexaWarNodeData>* pActTarget = NULL;
      // kiszamolom merre a leghasznosabb menni, az a node kerul a pActTarget-be
      // ha semerre se jo akkor marad NULL
      // minden szomszed hasznossagat megnezem
      max_hasznossag = 0; // csak pozitiv hasznossag eseten megyek valahova
      int rnd_start_idx = rnd.getInt(0,1000);
      for (int szidx=0; szidx<6; szidx++)
      {
        HexagonNode<HexaWarNodeData>* pSzomszed = hwg.getNeighbourNode(pActStart, (rnd_start_idx+szidx)%6);
        if (pSzomszed!=NULL)
        {
          large szomszed_hasznossag = pSzomszed->node_data.cel_hasznossag;
          if (szomszed_hasznossag>max_hasznossag)
          {
            max_hasznossag = szomszed_hasznossag;
            pActTarget = pSzomszed;
          }
        }
      }

      // ha sikerult targetet talalnom akkor feljegyzem
      // ha nem akkor vege a keresesnek
      if (pActTarget!=NULL)
      {
        act_mozgas->movementX.add(pActTarget->x);
        act_mozgas->movementY.add(pActTarget->y);
        pActTarget->node_data.cel_hasznossag -= 1000; // visszamenni nem hasznos!        
        pActStart = pActTarget;
      }
      else
        break;
    }
  }

  /////////////////////////////////////////////
  if (act_mozgas->movementX.get_count()<2) // nem sikerult meg egy elmozdulast se kitalalni, nincs elmozdulas
  {
    delete act_mozgas;
    return askround(id,skey,gtype,receiveTm);
  }
  /////////////////////////////////////////////
  mov_seq_list.add(act_mozgas);
  //
  textmap sendTm;
  sendTm.put("action","move");
  sendTm.put("game", "hexawar");
  sendTm.put("id", id);
  sendTm.put("skey", skey);
  sendTm.put("gtype", gtype);
  sendTm.put("hg_pathx", IntListtoCSIS<large>(act_mozgas->movementX));
  sendTm.put("hg_pathy", IntListtoCSIS<large>(act_mozgas->movementY));
  sendTm.put("hg_katona0", itostring(act_mozgas->sereg[0]));
  sendTm.put("hg_katona1", itostring(act_mozgas->sereg[1]));
  sendTm.put("hg_katona2", itostring(act_mozgas->sereg[2]));
  sendMsg("move", sendTm, receiveTm);
  int result = (int)str2num(receiveTm["result"]);
  return result;
}

// eldobhat exception-t ha valami beszarik
void playOneGame(const string& id, const string& skey, const string& gtpid)
{
  textmap receiveTm;
  while (!waitforgametype(id, skey, gtpid, receiveTm)) psleep(WAITFORGAME_INERTVAL);
  // mozgasaim tarolasa
  MovementSequenceList mov_seq_list(true);
  // megkaptam a palyat is, feljegyzem
  HexaWarGraph hwg(receiveTm);
  hwg.setPlayerState(receiveTm);
  int my_index = (int)str2num(receiveTm["hg_sajatsorszam"]);
  int player_index = 0;
  string player_name;
  string game_players("Playing game with: ");
  while (!isempty(player_name=receiveTm["hg_jnev"+itostring(player_index)]))
  {
    if (player_index>0) game_players += ", ";
    game_players += player_name;
    player_index++;
  }
  game_players += "   me = "+receiveTm["hg_sajatsorszam"];
  mlog(LOG_INFO2, game_players);
  // indul a jatek
  int round_result = 2; // alapban lezart kornek szamit a kezdetek kezdete
  while (round_result!=3) // ==3 ha vege a jateknak
  {
    if (round_result==1) // kor meg nem ert veget
    {
      round_result = askround(id, skey, gtpid, receiveTm);
    }
    else if (round_result==2) // kor vegetert, az eredmeny a receiveTm-ben
    {
      hwg.setPlayerState(receiveTm);
      round_result = move(id, skey, gtpid, hwg, mov_seq_list, my_index, receiveTm);
    }
    psleep(STEP_INTERVAL);
  }
}

static int games_played = 0;
static mutex played_mutex;
static int getPlayed() { int i; played_mutex.enter(); i=games_played; played_mutex.leave(); return i; }
static void changePlayed(int change) { played_mutex.enter(); games_played += change; played_mutex.leave(); }

// return:
// 0 - siker
// 1 - hiba
int playGames(int game_count, const string& name, const string& passwd, const string& gtpid)
{
  int retval = 0;
  try
  {
    string id, skey;
    login(name,passwd,id,skey);
    for (int i=0; i<game_count; i++) 
    { 
      playOneGame(id,skey,gtpid);
      changePlayed(1);
    }
    logout(id,skey);
    retval = 0;
  }
  catch (exception* pExc)
  {
    mlog(LOG_ERROR, "Exception: "+pExc->get_message());
    delete pExc;
    retval = 1;
  }
  return retval;
}

class PlayThread: public thread
{
public:
  static int instanceCount;
  static mutex cnt_mutex;
  static void changeInstanceCount(int change);
  static int getInstanceCount();
  PlayThread(): thread(false) { }
  int game_count;
  string player_name;
  string player_passwd;
  string gtpid;
  virtual void execute();
  virtual void cleanup();
};
mutex PlayThread::cnt_mutex;
int PlayThread::instanceCount = 0;
void PlayThread::changeInstanceCount(int change)
{
  cnt_mutex.enter();
  instanceCount+=change;
  cnt_mutex.leave();
}
int PlayThread::getInstanceCount()
{
  cnt_mutex.enter();
  int ic = instanceCount;
  cnt_mutex.leave();
  return ic;
}
void PlayThread::execute()
{
  changeInstanceCount(1);
  mlog(LOG_INFO, "Player "+player_name+" started playing his "+itostring(game_count)+" games.");
  playGames(game_count, player_name, player_passwd, gtpid);
}
void PlayThread::cleanup()
{
  changeInstanceCount(-1); 
  mlog(LOG_INFO, "Player "+player_name+" finished.");
}

void printonoffstate(const string& log_str, int log_type)
{
  pout.putf(log_str+" is "+((g_log_filter & log_type)?"ON\n":"OFF\n"));
}

int main(int argc, char *argv[])
{
  if (argc<7)
  {
    pout.putf(
      "HexaWar playing bot.\n"
      "Parameters: begin_index end_index game_count gametype_id log_level step_count\n"
      "Index interval: [12..1011]\n"
      "Log level bits: error=1, info=2, detailed info=4 (0=log nothing)\n"
      "Step count: number of steps in one client move\n"
      "Valid gametype_id values: '0' (2 players beginner), '1' (2 players normal) '2' (3 players beginner), '3' (3 players normal)\n");
    return -1;
  }

  g_log_filter = (int)str2num(string(argv[5]), 0);
  printonoffstate("LOG_ERROR",LOG_ERROR);
  printonoffstate("LOG_INFO",LOG_INFO);
  printonoffstate("LOG_INFO2",LOG_INFO2);
  printonoffstate("LOG_INFO3",LOG_INFO3);
  printonoffstate("LOG_INFO4",LOG_INFO4);

  int begin_index = (int)str2num(string(argv[1]), -1);
  if ( (begin_index<12) || (begin_index>1011) ) { pout.putf("Invalid begin_index!\n"); return 1; }
  int end_index = (int)str2num(string(argv[2]), -1);
  if ( (end_index<12) || (end_index>1011) ) { pout.putf("Invalid end_index!\n"); return 1; }
  if (end_index < begin_index) { pout.putf("end_index<begin_index!\n"); return 1; }
  int game_count = (int)str2num(string(argv[3]), -1);
  if (game_count<1) { pout.putf("Invalid game_count!\n"); return 1; }
  string gtype(argv[4]);
  if ( (gtype != "0") && (gtype != "1") && (gtype != "2") && (gtype != "3") ) { pout.putf("Invalid gametype_id!\n"); return 1; }
  g_step_count = (int)str2num(string(argv[6]),-1);
  if ((g_step_count<1) || (g_step_count>15)) { pout.putf("Invalid step_count. Must be inside [1..15]\n"); return 1; }

  int player_count = end_index - begin_index + 1;
  PlayThread player_thread_array[player_count];
  pout.putf("Created "+itostring(player_count)+" thread objects.\n");
  for (int i=begin_index; i<=end_index; i++)
  {
    psleep(111); // kis delay
    // jatszo szal beallitasa majd elinditasa
    player_thread_array[i-begin_index].game_count = game_count;
    player_thread_array[i-begin_index].player_name = "testu" + itostring(i);
    player_thread_array[i-begin_index].player_passwd = "???";
    player_thread_array[i-begin_index].gtpid = gtype;
    player_thread_array[i-begin_index].start();
  }
  while (1)
  {
    psleep(QUIT_POLL_INTERVAL);
    int ic = PlayThread::getInstanceCount();
    mlog(LOG_INFO, "PlayThread instance count = "+itostring(ic)+"   games played = "+itostring(getPlayed()));
    if (ic<1) return 0;
  }
}
