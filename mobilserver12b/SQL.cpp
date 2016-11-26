#include "include.h"

// ha nem ismeri a megadott SQL parancsot akkor eldob egy SQLexception-t
// a placeholder egy ? jel, minden ? jelet lecserel egy adatra a vars tombbol
int TestSQL::query(const string& query_string, variant& vars, variant& result)
{
  aclear(result);
	if (query_string=="SELECT id FROM account WHERE login=? AND passwd=?")
  {
    variant row;
    put(row, 0, variant("11"));
    put(result, 0, row);
    return 1;
  }
  else if (query_string=="SELECT id,nev,jatekosszam,temak FROM kviz_jatektipus WHERE ervenyesseg_kezdet<=now() AND ervenyesseg_veg>=now()")
  {
    // todo
  }
  else if (query_string=="SELECT id,nev,jatekosszam FROM hexawar_jatektipus WHERE ervenyesseg_kezdet<=now() AND ervenyesseg_veg>=now()")
  {
    variant row;
    put(row, 0, variant(0));
    put(row, 1, variant("3 players"));
    put(row, 2, variant(3));
    put(result, 0, row);
    aclear(row);
    put(row, 0, variant(1));
    put(row, 1, variant("5 players"));
    put(row, 2, variant(5));
    put(result, 1, row);
    return 2; // 3 es 5 jatekos jatekok
  }
  else if (query_string=="")
  {
  }
  else if (query_string=="")
  {
  }
  else if (query_string=="")
  {
  }
  else if (query_string=="")
  {
  }
  else
  {
    //throw new SQLexception("TestSQL::query: unsupported SQL query.");
    return 0;
  }
  return 0;
}

large TestSQL::affected_rows()
{
  return aff_rows;
}

////////////////////////////////////////////////////////////////////////////////

MySQL::MySQL(const string& host, const string& user, const string& password, const string& db)
{
  pConn = new pmysql(host, user, password, db);
	pConn->open();
}

MySQL::~MySQL()
{
  try { delete pConn; }
  catch (exception* pExc)
  {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Exception in MySQL::~MySQL() : "+pExc->get_message());
#endif
    delete pExc;
  }
}

int MySQL::query(const string& query_string, variant& vars, variant& result)
{

  stringlist qstr_list(true);
  split('?', query_string, qstr_list);
  string qstr;
  int qstr_list_cnt = qstr_list.get_count();
  int vidx=0;
  for (int i=0; i<qstr_list_cnt; i++)
  {
    qstr += *(qstr_list[i]);
    variant item;
    if (anext(vars, vidx, item))
    {
      switch (vartype(item))
      {
      case VAR_NULL:
        qstr += "NULL";
        break;
      case VAR_INT:
      case VAR_FLOAT:
        qstr += (const string&)item;
        break;
      case VAR_BOOL:
        qstr += ((bool)item) ? "TRUE" : "FALSE";
        break;
      case VAR_STRING:
        qstr += "'" + pConn->escape((const string&)item) + "'";
        break;
      default:
        throw new SQLexception("MySQL::query: unsupported query variable type."); 
      }
    }
  }

#if (defined LOG_DEBUG) && (LOG_LEVEL>=0)
  mlog("MySQL query: "+qstr);
#endif

  pConn->query(qstr);
  
  int sel_pos = pos("select", lowercase(query_string));
  bool is_select = ( (sel_pos>=0) && (sel_pos<6) );
  
#if (defined LOG_DEBUG) && (LOG_LEVEL>=1)
  mlog("is_select = "+string(is_select?"YES":"NO"));
#endif

  if (is_select)
  {
    aclear(result);
    variant v_row;
    int row_count = 0;
    while (!isnull(v_row=pConn->fetch_array()))
    {
      put(result, row_count, v_row);
      row_count++;
    }
    return row_count;
  }
  else
  {
    return affected_rows();
  } 
}

large MySQL::affected_rows()
{
  return pConn->affected_rows();
}

////////////////////////////////////////////////////////////////////////////////
/*
ConnectionPool* ConnectionPool::pConnectionPool = NULL;

ConnectionPool* ConnectionPool::getConnectionPool()
{
  if (pConnectionPool==NULL) pConnectionPool = new ConnectionPool;
  return pLogTail;
}

ConnectionPool::ConnectionPool()
{
  //CONN_POOL_SIZE

}*/

////////////////////////////////////////////////////////////////////////////////

SQL* LocalConnection::pSQL = NULL;
mutex LocalConnection::conn_mutex;

LocalConnection::LocalConnection()
{
  conn_mutex.lock();
  if (pSQL==NULL) 
  {
    pSQL = new MySQL("127.0.0.1", "username", "password", "db_name");
#if (defined LOG_INFO) && (LOG_LEVEL>=0)
    mlog("New MySQL connection created.");
#endif
  }
  // todo: kapcsolat el-e ellenorzes (?)
}

LocalConnection::~LocalConnection()
{
  conn_mutex.unlock();
}

// exception eseten elkapja es megprobal ujracsatlakozni, ha ezutan is exception van akkor mar kirepul a fuggvenybol
int LocalConnection::query(const string& query_string, variant& vars, variant& result) 
{ 
  try
  {
    return pSQL->query(query_string,vars,result); // emysql es SQLexception repulhet ki innen
  }
  catch (exception* pExc)
  {
#if (defined LOG_ERROR) && (LOG_LEVEL>=0)
    mlog("Exception in LocalConnection::query : "+pExc->get_message());
#endif
    delete pExc;
    delete pSQL;
    pSQL = new MySQL("127.0.0.1", "username", "password", "db_name");
#if (defined LOG_INFO) && (LOG_LEVEL>=0)
    mlog("MySQL connection recreated in LocalConnection::query");
#endif
    return pSQL->query(query_string,vars,result); // emysql es SQLexception repulhet ki innen
  }
}
