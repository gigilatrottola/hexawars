
#include "include.h"


Settings* Settings::pSettings = NULL;

Settings* Settings::getSettings()
{
  if (pSettings==NULL) pSettings = new Settings;
  return pSettings;
}

string Settings::get(string key)
{
  scoperead sr(rwlock_data);
  return tm_data[key];
}

void Settings::set(string key, string value)
{
#if (defined LOG_INFO) && (LOG_LEVEL>=0)
  mlog(string("Settings::set : ")+key+string("=")+value);
#endif
  scopewrite sr(rwlock_data);
  tm_data.put(key, value);
}

string Settings::log()
{
  scoperead sr(rwlock_data);
  string ret_val = "[ ";
  int tm_cnt = tm_data.get_count();
  for (int i=0; i<tm_cnt; i++)
  {
    if (i>0) ret_val += ", ";
    ret_val += tm_data.getkey(i);
    ret_val += "=";
    ret_val += tm_data[i];
  }
  ret_val += "]";
  return ret_val;
}
