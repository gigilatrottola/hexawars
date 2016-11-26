#ifndef MOBILESRV_INCLUDEALL_H
#define MOBILESRV_INCLUDEALL_H

#include <ptypes.h>
#include <ptime.h>
#include <pasync.h>
#include <pinet.h>
#include <pstreams.h>


USING_PTYPES

// common.cpp-ban definiált pár segédfüggvény:
typedef tobjlist<string> stringlist;
extern void split(char delimiter, const string& str, stringlist& str_list, bool get_empty=true);
extern large str2num(string str, large default_value=0);
extern void CSIStoIntList(const string& csis, tpodlist<large>& intlist);
extern string getTimeString(datetime dt);
extern void setErrorResponse(textmap& tmResponse, string error);
extern void httpparams2textmap(const string& str, textmap& tmap);
extern void textmap2msgstr(const textmap& tm, string& msgstr);
extern int msgstr2textmap(const string& msgstr, textmap& tm);

#include "Random.h"

#include "HexagonGraph.h"

#endif
