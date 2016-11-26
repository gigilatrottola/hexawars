#ifndef MOBILESRV_INCLUDEALL_H
#define MOBILESRV_INCLUDEALL_H

#include <ptypes.h>
#include <ptime.h>
#include <pasync.h>
#include <pinet.h>

USING_PTYPES

// kulonbozo logolasokhoz define-ok letrehozasa, forditasi idoben kell megadni a logolasi paramtereket
#define LOG_ERROR
#define LOG_INFO
#define LOG_WARNING
//#define LOG_DEBUG
// szint definialasa, csak az ennel kisebbegyenlo szintu cuccok logolodnak, 0+ szamok
#define LOG_LEVEL 100
// logolas pelda: 
// #if (defined LOG_ERROR) && (LOG_LEVEL>=0)
// mlog(...)
// #endif

// ez a handle_mobil() szamlalast es a szamok logolasat jelenti
//#define MOBILTEST
// a botok egymassal is jatszanak, jatekot kezdemenyeznek 20sec varas nelkul
//#define BOTBOTPLAY

// common.cpp-ban definiált pár segédfüggvény:
typedef tobjlist<string> stringlist;
extern void split(char delimiter, const string& str, stringlist& str_list, bool get_empty=true);
extern large str2num(string str, large default_value=0);
extern void CSIStoIntList(const string& csis, tpodlist<large>& intlist);
extern string getTimeString(datetime dt);
extern void setErrorResponse(textmap& tmResponse, string error);
extern void httpparams2textmap(const string& str, textmap& tmap);
extern void mlog(const string& msg);

extern void textmap2msgstr(const textmap& tm, string& msgstr);
extern int msgstr2textmap(const string& msgstr, textmap& tm);


#include "Random.h"

#include "pmysql.h"

#include "SQL.h"

#include "Settings.h"

#include "LogTail.h"

#include "SessionUser.h"

#include "Sessions.h"

#include "GameManager.h"

#include "QuizGameManager.h"

#include "GameData.h"

#include "QuizGraph.h"

#include "QuizGameData.h"

#include "WaitingQueues.h"

#include "HexagonGraph.h"

#include "HexaWarGameData.h"

#include "HexaWarGameManager.h"

#endif
