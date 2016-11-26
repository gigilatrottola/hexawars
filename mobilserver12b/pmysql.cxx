

#include <stdlib.h>  // for atof() and strtol()

#include "pmysql.h"


USING_PTYPES


// --- MySQL API [implementation ] -------------------------------------- //

//
// MySQL exception class
//

emysql::emysql(pmysql* ierrconn, int icode, const char* imsg)
    : exception(imsg), code(icode), errconn(ierrconn) {}


emysql::emysql(pmysql* ierrconn, int icode, const string& imsg)
    : exception(imsg), code(icode), errconn(ierrconn) {}


emysql::~emysql()  {}


//
// MySQL connection
//

pmysql::pmysql()
    : active(false), host(), user(), password(), db(), flags(0),
      result(nil), row(nil)
{
    mysql_init(&mysql);
}


pmysql::pmysql(const string& ihost, const string& iuser,
    const string& ipassword, const string& idb, int iflags)
        : active(false), host(ihost), user(iuser), password(ipassword), db(idb), flags(iflags),
          result(nil), row(nil)
{
    mysql_init(&mysql);
}


pmysql::~pmysql()
{
    close();
}


void pmysql::close()
{
    if (active)
    {
        free_result();
        mysql_close(&mysql);
        active = false;
    }
}


void pmysql::error()
{
    throw new emysql(this, mysql_errno(&mysql), mysql_error(&mysql));
}


string pmysql::escape(const string& from)
{
    string res;
    setlength(res, length(from) * 2 + 1);
    int reslen = mysql_real_escape_string(&mysql, unique(res), from, length(from));
    setlength(res, reslen);
    return res;
}


void pmysql::open()
{
    close();
    MYSQL* res = mysql_real_connect(&mysql, host, user, password, db, 0, NULL, flags);
    // for security reasons the password is being cleared. there is a known security
    // breach on windows: sometimes memory contents can be read through the system
    // swap file.
    clear(password);
    if (res == NULL)
        error();
    active = true;
}


void pmysql::open(const string& ihost, const string& iuser,
        const string& ipassword, const string& idb, int iflags)
{
    host = ihost;
    user = iuser;
    password = ipassword;
    db = idb;
    flags = iflags;
    open();
}


void pmysql::query(const char* q)
{
    free_result();
    if (mysql_query(&mysql, q))
        error();
}


void pmysql::query(const string& q)
{
    free_result();
    if (mysql_real_query(&mysql, q, length(q)))
        error();
}


void pmysql::queryf(const char* fmt, ...)
{
    outmemory m;
    m.open();
    va_list va;
    va_start(va, fmt);
    m.vputf(fmt, va);
    va_end(va);
    query(m.get_strdata());
}


large pmysql::affected_rows()
{
    large res = (large)mysql_affected_rows(&mysql);
    if (res == -1)
        error();
    return res;
}


bool pmysql::use_result()
{
    free_result();
    result = mysql_use_result(&mysql);
    if (result == nil)
        if (mysql_errno(&mysql) != 0)
            error();
        else
            return false;
    return true;
}


bool pmysql::store_result()
{
    free_result();
    result = mysql_store_result(&mysql);
    if (result == nil)
        if (mysql_errno(&mysql) != 0)
            error();
        else
            return false;
    return true;
}


void* pmysql::row_tell()
{
    if (result == nil)
        store_result();
    return mysql_row_tell(result);
}


void* pmysql::row_seek(void* new_offset)
{
    if (result == nil)
        store_result();
    return mysql_row_seek(result, (MYSQL_ROW_OFFSET)new_offset);
}


void pmysql::data_seek(large row_number)
{
    if (result == nil)
        store_result();
    mysql_data_seek(result, row_number);
}


void pmysql::free_result()
{
    if (result != nil)
    {
        row = nil;
        mysql_free_result(result);
        result = nil;
    }
}


int pmysql::field_count()
{
    return mysql_field_count(&mysql);
}


int pmysql::field_length(int i)
{
    if (result == nil)
        use_result();
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return 0;
    return mysql_fetch_lengths(result)[i];
}


int pmysql::field_type(int i)
{
    if (result == nil)
        use_result();
    MYSQL_FIELD* f = mysql_fetch_field_direct(result, i);
    return f->type;
}


const char* pmysql::field_name(int i)
{
    if (result == nil)
        use_result();
    MYSQL_FIELD* f = mysql_fetch_field_direct(result, i);
    return f->name;
}


int pmysql::find_field(const char* name)
{
    if (result == nil)
        use_result();
    int num_fields = mysql_num_fields(result);
    MYSQL_FIELD* fields = mysql_fetch_fields(result);
    for (int i = 0; i < num_fields; i++)
        if (strcmp(name, fields[i].name) == 0)
            return i;
    return -1;
}


inline string as_string(const char* buf, int len)
{
    return string(buf, len);
}


static large as_int(const char* buf)
{
    if (buf == nil)    // SQL NULL value
        return 0;
    bool neg = buf[0] == '-';
    if (neg)
        buf++;
    large res = stringtoi(buf);
    if (res < 0)
        res = 0;
    else if (neg)
        res = -res;
    return res;
}


static double as_float(const char* buf)
{
    if (buf == nil)    // SQL NULL value
        return 0;
    return atof(buf);
}


//
// timestamp/date/time conversion:
//
// TIMESTAMP(14)  'YYYYMMDDHHMMSS'
// TIMESTAMP(12)  'YYMMDDHHMMSS'
// TIMESTAMP(10)  'YYMMDDHHMM'
// TIMESTAMP(8)   'YYYYMMDD'
// TIMESTAMP(6)   'YYMMDD'
// TIMESTAMP(4)   'YYMM'
// TIMESTAMP(2)   'YY'
// DATETIME       'YYYY-MM-DD HH:MM:SS'
// DATE           'YYYY-MM-DD'
// TIME           'HH:MM:SS'
// YEAR           'YYYY'

static datetime as_datetime(const char* buf, int len, int type)
{
    if (buf == nil)    // SQL NULL value
        return invdatetime;

    int year = 1, month = 1, day = 1;
    int hour = 0, min = 0, sec = 0;

    switch (type)
    {
    case FIELD_TYPE_TIMESTAMP:
        {
            large d = stringtoi(buf);
            if (len >= 12)          // seconds
            {
                sec = int(d % 100);
                d = d / 100;
            }
            if (len >= 10)          // minutes and hours
            {
                min = int(d % 100);
                d = d / 100;
                hour = int(d % 100);
                d = d / 100;
            }
            if (len >= 6)           // day
            {
                day = int(d % 100);
                d = d / 100;
            }
            if (len >= 4)           // month
            {
                month = int(d % 100);
                d = d / 100;
            }
            year = int(d);              // the rest is year
            if (len != 8 && len != 14)  // two-digit year
            {
                if (year < 70) year += 2000;
                else year += 1900;
            }
        }
        break;

    case FIELD_TYPE_DATE:           // 'YYYY-MM-DD'
    case FIELD_TYPE_TIME:           // 'HH:MM:SS'
    case FIELD_TYPE_DATETIME:       // 'YYYY-MM-DD HH:MM:SS'
        {
            char* e;
            if (type == FIELD_TYPE_DATE || type == FIELD_TYPE_DATETIME)
            {
                if (len < 10)
                    return invdatetime;
                year = strtol(buf, &e, 10);
                month = strtol(buf + 5, &e, 10);
                day = strtol(buf + 8, &e, 10);
                if (type == FIELD_TYPE_DATETIME)
                    buf += 11;
            }
            if (type == FIELD_TYPE_TIME || type == FIELD_TYPE_DATETIME)
            {
                if (strlen(buf) < 8)
                    return invdatetime;
                hour = strtol(buf, &e, 10);
                min = strtol(buf + 3, &e, 10);
                sec = strtol(buf + 6, &e, 10);
            }
        }
        break;

    case FIELD_TYPE_YEAR:           // 'YYYY'
        year = int(stringtoi(buf));
        break;

    default:
        return invdatetime;
    }

    return encodedate(year, month, day) + encodetime(hour, min, sec);
}


static variant as_variant(const char* buf, int len, int type)
{
    if (buf == nil)    // SQL NULL value
        return nullvar;

    switch (type)
    {
    case FIELD_TYPE_NULL:
        return nullvar;

    case FIELD_TYPE_TINY:
    case FIELD_TYPE_SHORT:
    case FIELD_TYPE_LONG:
    case FIELD_TYPE_INT24:
    case FIELD_TYPE_LONGLONG:
        return as_int(buf);

    case FIELD_TYPE_FLOAT:
    case FIELD_TYPE_DOUBLE:
        return as_float(buf);

    case FIELD_TYPE_TIMESTAMP:
    case FIELD_TYPE_DATE:
    case FIELD_TYPE_TIME:
    case FIELD_TYPE_DATETIME:
    case FIELD_TYPE_YEAR:
        return as_datetime(buf, len, type);

    default:
        return as_string(buf, len);
    }
}


bool pmysql::fetch_row()
{
    if (result == nil)
        use_result();
    row = mysql_fetch_row(result);
    if (row == nil)
        if (mysql_errno(&mysql) != 0)
            error();
        else
            return false;
    return true;
}


variant pmysql::fetch_array(bool assoc)
{
    if (!fetch_row())
        return nullvar;
    variant res;
    int num_fields = int(mysql_num_fields(result));
    MYSQL_FIELD* fields = mysql_fetch_fields(result);
    ::ulong* lengths = mysql_fetch_lengths(result);
    for (int i = 0; i < num_fields; i++)
    {
        MYSQL_FIELD* f = fields + i;
        if (assoc)
            put(res, f->name, as_variant(row[i], lengths[i], f->type));
        else
            put(res, i, as_variant(row[i], lengths[i], f->type));
    }
    return res;
}


string pmysql::field(int i)
{
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return nullstring;
    if (row == nil)
        return nullstring;
    ::ulong* lengths = mysql_fetch_lengths(result);
    return as_string(row[i], lengths[i]);
}


large pmysql::int_field(int i)
{
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return 0;
    if (row == nil)
        return 0;
    return as_int(row[i]);
}


bool pmysql::bool_field(int i)
{
    return int_field(i) != 0;
}


double pmysql::float_field(int i)
{
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return 0;
    if (row == nil)
        return 0;
    return as_float(row[i]);
}


datetime pmysql::datetime_field(int i)
{
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return invdatetime;
    if (row == nil)
        return invdatetime;
    MYSQL_FIELD* f = mysql_fetch_field_direct(result, i);
    ::ulong* lengths = mysql_fetch_lengths(result);
    return as_datetime(row[i], lengths[i], f->type);
}


variant pmysql::var_field(int i)
{
    if (i < 0 || i >= int(mysql_num_fields(result)))
        return nullvar;
    if (row == nil)
        return nullvar;
    MYSQL_FIELD* f = mysql_fetch_field_direct(result, i);
    ::ulong* lengths = mysql_fetch_lengths(result);
    return as_variant(row[i], lengths[i], f->type);
}
