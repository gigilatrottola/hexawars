/* MOBIL MODUL */

// exit() miatt kell:
#include <stdlib.h>

#include "config.h"
#include "utils.h"
#include "request.h"
#include "log.h"

#include "include.h"

/*
// visszaadja a textmap-ben levo adatokat stringben ami kikuldheto kliensnek
string textmap2outstr(textmap& tmResult)
{
  string response;
  int resp_cnt = tmResult.get_count();
  for (int i=0; i<resp_cnt; i++)
  {
    response += tmResult.getkey(i);
    response += "\n";
    response += tmResult[i];
    response += "\n"; 
  }
  return response;
}*/

////////////////////////////////////////////////////////////////////////////////////

datetime g_server_init_time;

string getUptime()
{
  datetime uptime = now() - g_server_init_time;
  int daynum, hours, mins, secs;
  daynum = days(uptime);
  decodetime(uptime, hours, mins, secs);
  return itostring(daynum) + " days  "+itostring(hours)+" hours  "+itostring(mins)+" minutes."; 
}

// ellenorzes: csak ervenyes betukbol allhat str
bool hasValidChars(const string& str)
{
  int str_len = length(str);
  for (int i=0; i<str_len; i++)
  {
    if (str[i]<=0x20) return false; // nem lehet benne szokoz sem a seggfejkedok miatt
    if (str[i]>0x7E) return false;
    if (str[i]=='\'') return false; // egyszeres idezojel se lehessen benne az SQL miatt
  }
  return true;
}

// ellenõrzi hogy a jelszó megfelel-e a kritériumoknak
bool check_password(const string& passwd)
{
	if (length(passwd)<1) return false;
	if (length(passwd)>16) return false;
  if (!hasValidChars(passwd)) return false;
	return true;
}

bool check_login(const string& login)
{
	if (length(login)<1) return false;
	if (length(login)>16) return false;
  if (!hasValidChars(login)) return false;
	return true;
}

bool check_email(const string& email) // TODO: ez meg nem igazi e-mail cim ellenorzes
{
	//if (length(email)<5) return false;
	if (length(email)>100) return false;
  if (!hasValidChars(email)) return false;
	return true;
}

bool check_lang(const string& lang)
{
	if (length(lang)>16) return false;
  if (!hasValidChars(lang)) return false;
	return true;
}

bool checkClientVersion(textmap* ptmParams, textmap& tmResponse)
{
  return true; // TODO: ellenorizni a version mezot
  // return false eseten berakni:
  // setErrorResponse(tmResponse, "100");
  // tmResponse.put("acceptver", "megengedett kliens verziok");
}

string getInfo(LocalConnection& lconn, string lang)
{
  variant vv,vr;
  string info;
  // kiolvasom az ervenyes info adatokat es osszeadom azokat
  put(vv, 0, variant(lang));
  int rlen = lconn.query("SELECT message FROM info WHERE lang=? AND begin<now() AND end>now()",vv,vr);
  if (rlen>0)
  {
    variant item;
    for (int i = 0; anext(vr, i, item); )
    {
      info += (const string&)aget(item,(large)0);
      info += ' ';
    }
  }
  return info;
}

bool is_redirected(textmap& tmResponse)
{
  string redirect = Settings::getSettings()->get("redirect");
  if (isempty(redirect)) return false;
  tmResponse.put("result","43");
  tmResponse.put("redirect", redirect);
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog(string("REDIRECT to ")+redirect);
#endif
  return true;
}

void kill_mobil()
{
  Sessions::getSessions()->deleteAllSessions();
#if (defined LOG_INFO) && (LOG_LEVEL>=0)
  mlog("KILL message received -> EXIT.");
#endif
  log_done();
  config_done();
  exit(0);
}

void srvadmin(textmap* ptmParams, textmap& tmResponse)
{
  Settings* pSettings = Settings::getSettings();
  if ((*ptmParams)["adminpass"]!=pSettings->get("adminpass"))
  {
    setErrorResponse(tmResponse, "2");
    return;
  }
  // ha jo a jelszo akkor vegrehajtjuk a kerest
  if ((*ptmParams)["do"]=="get_setting")
  {
    tmResponse.put("setting_value",pSettings->get((*ptmParams)["key"]));
    tmResponse.put("result","1");
  }
  else if ((*ptmParams)["do"]=="set_setting")
  {
    pSettings->set((*ptmParams)["key"], (*ptmParams)["value"]);
    tmResponse.put("setting_key",(*ptmParams)["key"]);
    tmResponse.put("setting_value",pSettings->get((*ptmParams)["key"]));
    tmResponse.put("result","1");
  }
  else if ((*ptmParams)["do"]=="list_settings")
  {
    tmResponse.put("SETTINGS", pSettings->log());
    tmResponse.put("result","1");
  }
  else if ((*ptmParams)["do"]=="kill_mobil")
  {
    kill_mobil();
  }
  else if ((*ptmParams)["do"]=="logtail")
  {
    tmResponse.put("LOGTAIL", LogTail::getLogTail()->getAll());
    tmResponse.put("result","1");
  }
  else
  {
	  setErrorResponse(tmResponse, "10");
  }
}

void generateResponse(textmap* ptmParams, int& http_resp_code, textmap& tmResponse)
{
	http_resp_code = 200;
	try
	{
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
		int i;
		int params_cnt = ptmParams->get_count();
		string str = "KLIENS: ";
		for (i=0; i<params_cnt; i++)
		{
			str += ptmParams->getkey(i) + "=" + (*ptmParams)[i] + "  ";
		}
    mlog(str);
#endif
		
		// mindig elkuldott mezok:
    tmResponse.put("servertime", getTimeString(now()));
		
		if ((*ptmParams)["game"]=="") // nem játékban van hanem állítgat
		{
      // minden adminisztracios (nem jatekbeli) uzenet eseten a szerver elkuldi verziojat
      tmResponse.put("version", "1.0");
      // ellenorzom hogy tamogatott kliens verizo-e
      if (!checkClientVersion(ptmParams, tmResponse)) return;
			if ((*ptmParams)["action"]=="login")
			{
        if (!check_login((*ptmParams)["login"])) { setErrorResponse(tmResponse, "11"); return; }
				if (!check_password((*ptmParams)["passwd"])) { setErrorResponse(tmResponse, "10"); return; }
        if (!check_lang((*ptmParams)["lang"])) { setErrorResponse(tmResponse, "13"); return; }
        if (is_redirected(tmResponse)) return;
        string id;
        // kiolvsom a user adatait az account táblából, ha nincs akkor hibaüzenet
        {
          LocalConnection lconn;
          variant vars, resp;
          put(vars, 0, variant((*ptmParams)["login"]));
          int ri = lconn.query("SELECT id,passwd,now()-login_attempt_time,login_attempt_count FROM account WHERE login=?", vars, resp);
          if (ri<1) { setErrorResponse(tmResponse, "2"); return; } // nincs ilyen login
          variant sor = aget(resp,0);
          id = (const string&)aget(sor,(large)0);
          string passwd = (const string&)aget(sor,(large)1);
          large login_timespan = (large)aget(sor,2);
          large login_fail_count = (large)aget(sor,3);
          // megnezem hogy ha meg nem jelentkezhet be az elhibazott belepesi kiserletei miatt akkor hibauzenet
          if ( (login_fail_count>=5) && (login_timespan<(60*5)) )  { setErrorResponse(tmResponse, "12"); return; }
          // ellenorzom a jelszot, ha hibas akkor novelem a hibas kiserletek szamat es az idot frissitem
          if ((*ptmParams)["passwd"] != passwd)
          {
            // fel kell jegyezni a hibas belepesi probalkozast: attempt szamok novelese
            aclear(vars);
            put(vars, 0, variant((*ptmParams)["login"]));
            ri = lconn.query("UPDATE account SET login_attempt_time=now(), login_attempt_count=login_attempt_count+1 WHERE login=?",vars,resp);
            setErrorResponse(tmResponse, "2");
            return;
          }
          // nullazom a fail erteket es feljegyzem a belepesi idot
          aclear(vars);
          put(vars, 0, variant((*ptmParams)["login"]));
          ri = lconn.query("UPDATE account SET login_attempt_time=now(), login_attempt_count=0 WHERE login=?",vars,resp);
          tmResponse.put("info", getInfo(lconn, (*ptmParams)["lang"]));
        }// lconn destruktor itt lefut
				// ha van user akkor létre kell hozni a session-jét és a kódot visszaadni
        Sessions::getSessions()->createNewSessionUser(id, ptmParams, tmResponse);
			}
			else if ((*ptmParams)["action"]=="register")
			{
        if (is_redirected(tmResponse)) return;
				// ellenõrzöm hogy a jelszó megfelel-e a kritériumoknak
				if (!check_password((*ptmParams)["passwd"])) { setErrorResponse(tmResponse, "2"); return; }
				if (!check_login((*ptmParams)["login"])) { setErrorResponse(tmResponse, "3"); return; }
        if (!check_email((*ptmParams)["email"])) { setErrorResponse(tmResponse, "4"); return; }
				// adatbázisba feljegyzem új user account-ot
        // elotte kitorlok minden accountot aki meg nem jatszott egy jatekot se es mar tobb mint T ideje regisztralt
        LocalConnection lconn;
  			variant vars, resp;
  			int ri;
  			// lock segítségével kizárom párhuzamos regelésbõl adódó hibalehetõségeket
  			ri = lconn.query("LOCK TABLES account WRITE", vars, resp);
        // torlom a seggfejeket akik csak regisztraltak de nem csinalnak semmit
        aclear(vars);
        ri = lconn.query("DELETE FROM account WHERE (played<1) AND (DATE_ADD(registration_time,INTERVAL 30 DAY)<NOW())", vars, resp);
#if (defined LOG_INFO) && (LOG_LEVEL>=1)
        if (ri>0) mlog("Deleted "+itostring(ri)+" inactive players.");
#endif
				// megnézem van-e már ilyen felhasz
				aclear(vars);
				put(vars, 0, (*ptmParams)["login"]);
				ri = lconn.query("SELECT login FROM account WHERE login=?", vars, resp);
				if (ri>0)
				{
					// ha van már ilyen néven akkor hibaüzenet
           setErrorResponse(tmResponse, "4");
				}
				else
				{
					// kiszámolom következõ szabad ID-t
					string sAccId = "0";
					aclear(vars);
					ri = lconn.query("SELECT max(id)+1 FROM account", vars, resp);
					if (ri>0) sAccId = (const string&)aget(aget(resp,(large)0),(large)0);
					// felveszem a userek közé
					aclear(vars);
					put(vars, 0, variant(sAccId));
					put(vars, 1, variant((*ptmParams)["login"]));
					put(vars, 2, variant((*ptmParams)["passwd"]));
					put(vars, 3, variant((*ptmParams)["email"]));
					ri = lconn.query("INSERT INTO account (id,login,passwd,registration_time,login_count,email,points,played,pay) VALUES (?,?,?,NOW(),0,?,0,0,'')", vars, resp);
					tmResponse.put("result", "1");
				}
				aclear(vars);
				ri = lconn.query("UNLOCK TABLES", vars, resp);
			}
#ifdef DEBUG
			else if ((*ptmParams)["action"]=="getsrvinfo")
			{
        tmResponse.put("BUILD TIME", string(__DATE__) + string("  ") + string(__TIME__));
        tmResponse.put("UPTIME", getUptime());
				tmResponse.put("sess_cnt", itostring(Sessions::getSessions()->get_count()) );
				tmResponse.put("result", "1"); // siker
			}
      else if ((*ptmParams)["action"]=="srvadmin")
      {
        srvadmin(ptmParams, tmResponse);
      }
      else if ((*ptmParams)["action"]=="getdebuginfo")
			{
        // ide rakok mindenfele info kiiro szarsagot
        tmResponse.put("BUILD TIME", string(__DATE__) + string("  ") + string(__TIME__));
        tmResponse.put("COMPILER VERSION", string(__VERSION__));
        tmResponse.put("UPTIME", getUptime());
        Sessions::getSessions()->getDebugData(ptmParams, tmResponse);
        // ...
      }
#endif
			else // ismeretlen action
			{
				http_resp_code = 400; // bad request
				setErrorResponse(tmResponse, "1");
			}
		}
		else // ha valamilyen játék (game paraméter) üzenet akkor már sessionben kell legyen
		{
			Sessions::getSessions()->handleUserRequest(ptmParams, tmResponse);
		}		
	}//try
	catch (exception* pErr)
	{
		http_resp_code = 500; // internal server error
    setErrorResponse(tmResponse, "-1");
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("MOBILSERVER EXCEPTION: "+pErr->get_message());
#endif
		delete pErr;
	}
}

#ifdef MOBILTEST
extern int thread_count;
int mobil_called_count  = 0;
int mobil_running_count = 0;
#endif

void handle_mobil(request_rec& req)
{

#ifdef MOBILTEST
  pincrement(&mobil_called_count);
  pincrement(&mobil_running_count);
#endif

#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("===handle_mobil()===============================================================");
#endif
	//req.sockout->putf("KLIENS IP : %s\n", pconst(iptostring(req.client_ip)) );
	//TODO: csak adott mobilszolgáltatók IP címérõl érkezett kérésekre reagáljon
	// többire: req.rsp_forbidden();
	req.parse_request_line(); // ez feltölti a req.uri-t
	//pout.putline("REQUEST URI: ["+req.uri+"]");
	// ?-el bezárólag mindent törlök, csak GET paraméterek kellenek
	string params = req.uri;
  // uri: ....?P=xxx ahol xxx a spec. base64 string amiben a textmap van
  int param_pos = pos('=', params); // "..../?P=xxx" eseten jo, tobb parameternel hibas lenne
  if (param_pos>=0) del(params, 0, param_pos+1);
#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("param = ["+params+"]");
#endif
	// adatok
  textmap tmParams;
  textmap tmResponse;
  int http_resp_code = 200; // alapban siker kód
  //httpparams2textmap(params, tmParams); <- ez mar nem kell mert csak 1 db. P nevu parameter van mindig
  // es a kodolas miatt nem tartalmaz speci karaktereket se 
  // kiolvasom a kapott adatokat textmap-be:
  int retcode = msgstr2textmap(params, tmParams);
  if (retcode==0)
  {  
	  generateResponse( &tmParams , http_resp_code , tmResponse );
  }
  else // hibas adat jott
  {
    http_resp_code = 400; // bad request
    setErrorResponse(tmResponse, "-2");
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Invalid param (errcode="+itostring(retcode)+"), request was: "+req.uri);
#endif
  }
	/////////////////////////////////////////////////////////////////////////////////////////////
  req.keep_alive = false; // we don't know the content length
  // all responses must start with begin_response() and end with end_response()
  req.begin_response(http_resp_code, "OK");
  // use put_xxx functions (see request.h) to send response headers back to the
  // client. these functions do nothing if the request version was HTTP/0.9
  req.put_content_type("text/plain");
  // end_headers() must be called when you're done with the headers.
  // if the method was HEAD, it throws an ehttp exception so that the
  // rest of your code won't be executed
  req.end_headers();

#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
	int resp_cnt = tmResponse.get_count();
	string str = "SZERVER: ";
	for (int i=0; i<resp_cnt; i++)
	{
    string key = tmResponse.getkey(i);
	  str += key;
    str += "=";
    str += (key=="LOGTAIL") ? "***adatok:)***" : tmResponse[i];
    str += "  ";
    //str += tmResponse.getkey(i) + "=" + tmResponse[i] + "  ";
	}
    mlog(str);
#endif

  /////////////////////////////////////////////////////////////////////////////////////////////
  // you can write to the client socket using req.sockout, which is an outstm object
  string responseString;
  textmap2msgstr(tmResponse, responseString);
	req.sockout->putline(responseString);

#ifdef MOBILTEST
  pdecrement(&mobil_running_count);
  if (mobil_called_count % 10000 == 0)
  {
    datetime diff = now() - g_server_init_time;
    int hours, mins, secs, msecs;
    decodetime(diff, hours, mins, secs, msecs);
    string tms = "time: ";
    tms += itostring(hours);
    tms += ":";
    tms += itostring(mins);
    tms += ":";
    tms += itostring(secs);
    tms += ":";
    tms += itostring(mins);
    mlog(tms+"  thread_count = "+itostring(thread_count)+
         "  mobil_called_count = "+itostring(mobil_called_count)+"  mobil_running_count = "+itostring(mobil_running_count));
  }
#endif

	/////////////////////////////////////////////////////////////////////////////////////////////
  // end_response() throws an ehttp exception. the request info is being logged.
  req.end_response();
}

void init_mobil()
{
  g_server_init_time = now();
  Settings::getSettings()->set("adminpass","admin_password");
  mlog("mobilserver started at "+getTimeString(g_server_init_time));
#ifdef DEBUG
  mlog("DEBUG version.");
#else
  mlog("RELEASE version.");
#endif
  mlog("Log level: "+itostring(LOG_LEVEL));
#ifdef LOG_INFO
  mlog("Logging info.");
#endif
#ifdef LOG_ERROR
  mlog("Logging errors.");
#endif
#ifdef LOG_WARNING
  mlog("Logging warnings.");
#endif
#ifdef LOG_DEBUG
  mlog("Logging debug data.");
#endif
#ifdef BOTBOTPLAY
  mlog("BOTBOTPLAY On.");
#endif
#ifdef MOBILTEST
  mlog("MOBILTEST On.");
#endif
}
