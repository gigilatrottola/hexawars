#if !defined(_LOGTAIL_H__INCLUDED_)
#define _LOGTAIL_H__INCLUDED_

#define LOGTAIL_LENGTH  500

class LogTail
{
private:
  static LogTail* pLogTail;
  rwlock  rwlock_data;
  string data[LOGTAIL_LENGTH];
  int last_line;
  LogTail(): last_line(0) { }
  inline int next_line(int line) { return (line>=(LOGTAIL_LENGTH-1)) ? 0 : (line+1); }
  inline int prev_line(int line) { return (line<=0) ? (LOGTAIL_LENGTH-1) : (line-1); }
public:
  static LogTail* getLogTail();
  void add(string msg); // egy log bejegyzes feljegyzese
  string getAll(); // teljes tartalom visszaadasa
};

#endif // !defined(_LOGTAIL_H__INCLUDED_)
