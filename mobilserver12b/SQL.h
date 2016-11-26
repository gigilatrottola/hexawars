//
// Itt vannak az adatbazis eleresehez az osztalyok
//

#ifndef _SQL_DEFINED_
#define _SQL_DEFINED_

class SQL
{
public:
	SQL() { }
	virtual int query(const string& query_string, variant& vars, variant& result) = 0; // absztrakt osztaly
  virtual large affected_rows() = 0; // utolso UPDATE/DELETE altal erintett sorok szama
	virtual ~SQL() { }
};

class SQLexception : public exception
{
public:
  SQLexception(const char* msg): exception(msg) { } 
};

class TestSQL : public SQL
{
  large aff_rows;
public:
  TestSQL(): aff_rows(0) { }
	virtual int query(const string& query_string, variant& vars, variant& result);
  virtual large affected_rows();
};


class MySQL : public SQL
{
  pmysql* pConn;
public:
  MySQL(const string& host, const string& user, const string& password, const string& db);
  virtual ~MySQL();
  virtual int query(const string& query_string, variant& vars, variant& result);
  virtual large affected_rows();
};

/*class ConnectionPool
{
private:
  static ConnectionPool* pConnectionPool;
  ConnectionPool();
public:
  static ConnectionPool* getConnectionPool();
};*/

// 1 darab perzisztens adatbaziskapcsolat ami exkluziv modban hozzaferheto: konstruktorban lock, destruktorban unlock!
// ez veszelyes modszer: nem lehet mas cucc lockja amig ez a lock el! vagy at kell gondolni mi a fasz tortenhet :D
class LocalConnection
{
  static SQL* pSQL;
  static mutex conn_mutex;
public:
  LocalConnection();
  ~LocalConnection();
  int query(const string& query_string, variant& vars, variant& result);
  large affected_rows() { return pSQL->affected_rows(); }
};

#endif
