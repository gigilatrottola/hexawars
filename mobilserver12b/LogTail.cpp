#include "include.h"

LogTail* LogTail::pLogTail = NULL;

LogTail* LogTail::getLogTail()
{
  if (pLogTail==NULL) pLogTail = new LogTail;
  return pLogTail;
}

void LogTail::add(string msg)
{
  scopewrite sr(rwlock_data);
  last_line = next_line(last_line);
  data[last_line] = msg;
}

string LogTail::getAll()
{
  scoperead sr(rwlock_data);
  string tail_str;
  int act_line = last_line;
  for (int counter=0; counter<LOGTAIL_LENGTH; counter++)
  {
    if (!isempty(data[act_line]))
    {
      tail_str += data[act_line];
      tail_str += '\n';
    }
    act_line = prev_line(act_line);
  }
  return tail_str;
}
