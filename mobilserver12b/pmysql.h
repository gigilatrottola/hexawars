
#ifndef __PMYSQL_H__
#define __PMYSQL_H__

#include <ptypes.h>
#include <ptime.h>
#include <pinet.h>  // this includes winsock.h on windows for mysql.h to compile

#include <mysql.h>


USING_PTYPES


// --- MySQL API -------------------------------------------------------- //

//
// MySQL exception class
//

class pmysql;

class emysql: public exception 
{
protected:
    int code;
    pmysql* errconn;
public:
    emysql(pmysql* errconn, int icode, const char* imsg);
    emysql(pmysql* errconn, int icode, const string& imsg);
    virtual ~emysql();
    int get_code()                 { return code; }
    pmysql* get_errconn()   { return errconn; }
};


//
// MySQL connection
//

class pmysql: public component
{
protected:
    bool active;

    MYSQL mysql;
    string host;
    string user;
    string password;
    string db;
    int flags;
    MYSQL_RES* result;
    MYSQL_ROW row;

    void error();

public:
    pmysql();
    pmysql(const string& host, const string& user,
        const string& password, const string& db, int flags = 0);

    virtual ~pmysql();

    // getting/setting individual properties
    string get_host()               { return host; }

    // direct access to MySQL API handles
    MYSQL* get_handle()             { return &mysql; }
    MYSQL_RES* get_result()         { return result; }
    MYSQL_ROW get_row()             { return row; }

    // utilities
    string escape(const string&);

    // connect to the MySQL server
    void open();
    void open(const string& host, const string& user,
        const string& password, const string& db, int flags = 0);
    void close();

    // run a query
    void query(const char* q);      // textual query
    void query(const string& q);    // query that may contain binary data
    void queryf(const char* fmt, ...); // printf-style formatted textual query
    large affected_rows();          // for UPDATE and DELETE queries
    int field_count();

    // row access
    variant fetch_array(bool assoc = false);
    bool fetch_row();

    // field value retrieval, use fetch_row() first
    string   field(int i);
    string   field(const char* name)            { return field(find_field(name)); }
    large    int_field(int i);
    large    int_field(const char* name)        { return int_field(find_field(name)); }
    bool     bool_field(int i);
    bool     bool_field(const char* name)       { return bool_field(find_field(name)); }
    double   float_field(int i);
    double   float_field(const char* name)      { return float_field(find_field(name)); }
    datetime datetime_field(int i);
    datetime datetime_field(const char* name)   { return datetime_field(find_field(name)); }
    variant  var_field(int i);
    variant  var_field(const char* name)        { return var_field(find_field(name)); }

    // field manipulation, use one of fetch_XXX() first
    int field_length(int i);
    int field_type(int i);          // MySQL type FIELD_TYPE_XXX
    const char* field_name(int i);
    int find_field(const char* name);  // returns the field index or -1 on error

    // getting the result set for SELECT, SHOW, DESCRIBE, EXPLAIN queries
    bool use_result();              // OPTIONAL: may be called from fetch_row(), XXX_field() if not called before
    bool store_result();            // OPTIONAL: may be called from data_seek(), row_seek() or row_tell()
    void* row_tell();               // (the result is not a row number!)
    void* row_seek(void* offset);
    void data_seek(large row_number);
    void free_result();             // OPTIONAL: called automatically when necessary
};


#endif // __PMYSQL_H__
