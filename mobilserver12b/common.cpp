#include "log.h"
#include "include.h"

// ures stringeket nem tartalmaz ha az utolso parameter false
void split(char delimiter, const string& str, stringlist& str_list, bool get_empty)
{
#ifdef DEBUG
  // ellenorzom hogy az atadott lista torli-e a berakott sztringeket, ha nem akkor memory leak lesz!
  if (!str_list.get_ownobjects())
  {
    throw new exception("ERROR calling split(): stringlist.ownobjects==false -> memory leak! ");
  }
#endif
  int i;
  int len = length(str);
  string* pReszStr = new string;
  for (i=0; i<len; i++)
  {
    if (str[i]==delimiter)
    {
      if ( get_empty || (length(*pReszStr)>0) ) str_list.add(pReszStr);
      else delete pReszStr;
      pReszStr = new string;
    }
    else
    {
      *pReszStr += str[i];
    }
  }
  if ( (length(*pReszStr)>0) || ( get_empty && (len>0) && str[len-1]==delimiter ) ) str_list.add(pReszStr);
  else delete pReszStr;
}

large str2num(string str, large default_value)
{
	large num;
	try
	{
		num = stringtoie(str);
	}
	catch (econv* pErr)
	{
		num = default_value;
		delete pErr;
	}
	return num;
}

// comma separated integer string to integer list, hozzaadja a mar benne levo elemekhez!
void CSIStoIntList(const string& csis, tpodlist<large>& intlist)
{
  stringlist list_data(true);
  split(',', csis, list_data);
  int list_count = list_data.get_count();
  for (int i=0; i<list_count; i++) intlist.add(str2num( *(list_data[i]), 0));
} 

void setErrorResponse(textmap& tmResponse, string error)
{
  tmResponse.put("result","0");
  tmResponse.put("error", error);
}

// hozzáad egy key=value értéket
//void addResponseProperty(textmap& tmResponse, string key, string value)
//{
//  tmResponse.put(key,value);
//}

string getTimeString(datetime dt)
{
  const static datetime zero_time = encodedate(2005, 1, 1); // hogy beleferjen 32 bites int-be is kliens oldalon
  return dttostring(dt, "%Y,%m,%d,%H,%M,%S,") + itostring((dt-zero_time)/1000);
}

////////////////////////////////////////////////////////////////////////////////
// HTTP GET/POST adatok konvertalasa

// get vagy post paramter konvertalasa url enkodoltrol eredeti ertekere
string& cgi_convert(string& uestr)
{
  int ues_len = length(uestr);
  char* out_str = new char[ues_len+1];
  int in_idx = 0;
  int out_idx = 0;
  while (in_idx<ues_len)
  {
    char chr = uestr[in_idx];
    if ( (chr=='%') && ((in_idx+2)<ues_len) ) // 3 betu -> 1 betu  CSAK ha megvan a szukseges +2 betu
    {
      char hex_str[3];
      hex_str[0] = uestr[in_idx+1];
      hex_str[1] = uestr[in_idx+2];
      hex_str[2] = '\0';
      string hex_string(hex_str);
      large hex_large;
      try { hex_large = stringtoue(hex_string, 16); } catch (econv* pExc) { hex_large=0; delete pExc; }
      out_str[out_idx] = (char)hex_large;
      out_idx++;
      in_idx+=3;
    }
    else // 1 betu -> 1 betu
    {
      if (chr=='+') chr=' ';
      out_str[out_idx] = chr;
      out_idx++;
      in_idx++;
    }
  }
  out_str[out_idx] = '\0';
  uestr = out_str;
  delete out_str;
  return uestr;
}

// str-ben levo HTTP GET/POST parametereket tmap-be rakja
void httpparams2textmap(const string& str, textmap& tmap)
{
  stringlist parok(true);
  split('&', str, parok, false); 
  int parokszama = parok.get_count();
  for (int i=0; i<parokszama; i++)
  {
    stringlist par(true);
    split('=', *(parok[i]), par);
    if (par.get_count()==2) 
      tmap.put( cgi_convert(*(par[0])) , cgi_convert(*(par[1])) );
  }
}

void mlog(const string& msg)
{
  //LogTail::getLogTail()->add(msg);
  if (htlog == 0) return;
  try
  {
    htlog->putf("%s\n", pconst(msg));
    htlog->flush();
  }
  catch (estream* e)
  {
    delete e;
    htlog = 0;
    syslog_write(SYSLOG_ERROR, "HTTP log disabled due to failed write attempt (daemonized?)");
  }
}

// modositott BASE64 kodolas:
// a sortorogetes 76 karakterenkent kiszedve
// a karakterkodok osszevisza random :)
// az utolso 65. karakter a toltelek amikor nem 3-al oszthato hossz volt
static const char* charTab = "HexaWAR.OnEbiTh3roS,CJYGzDvd4F2IMwBN7PQ0U8VXZ1cfgjklm9Kpq65stuyL+";
            
// raw_data: ebben van az forras adat
// data_len: data hossza
// encoded : ebbe kerul az adat, foglal neki helyet amit fel kell szabaditani delete-el
void spec_base64_encode(const unsigned char* raw_data, const int data_len, char*& encoded)
{
  // 3 chars -> 4 chars
  const int remaining_chars = data_len % 3;
  const int for_chars = data_len - remaining_chars;
  encoded = new char[for_chars/3*4+(remaining_chars?4:0)+1];
  int res_i = 0; // encoded tomb indexelo
  int i = 0; // raw tomb indexelo
  unsigned int d;
  for (i=0; i<for_chars; i+=3)
  {
    d = (((unsigned int)raw_data[i]) << 16) | (((unsigned int)raw_data[i+1]) << 8) | ((unsigned int)raw_data[i+2]);
    encoded[res_i]   = charTab[(d >> 18) & 63];
    encoded[res_i+1] = charTab[(d >> 12) & 63];
    encoded[res_i+2] = charTab[(d >> 6) & 63];
    encoded[res_i+3] = charTab[d & 63];
    res_i += 4;
  }
  if (remaining_chars==1) // 2 toltelek kell
  {
    d = ((unsigned int)raw_data[i]) << 16;
    encoded[res_i]   = charTab[(d >> 18) & 63];
    encoded[res_i+1] = charTab[(d >> 12) & 63];
    encoded[res_i+2] = '+'; 
    encoded[res_i+3] = '+';
    res_i += 4;
  }
  else if (remaining_chars==2) // 1 toltelek
  {
    d = (((unsigned int)raw_data[i]) << 16) | (((unsigned int)raw_data[i+1]) << 8);
    encoded[res_i]   = charTab[(d >> 18) & 63];
    encoded[res_i+1] = charTab[(d >> 12) & 63];
    encoded[res_i+2] = charTab[(d >> 6) & 63];
    encoded[res_i+3] = '+';
    res_i += 4;
  }
  encoded[res_i] = '\0';
}

static unsigned char spec_base64_decode(const unsigned char c)
{
  switch (c)
	{
	case 'H': return 0;
	case 'e': return 1;
	case 'x': return 2;
	case 'a': return 3;
	case 'W': return 4;
	case 'A': return 5;
	case 'R': return 6;
	case '.': return 7;
	case 'O': return 8;
	case 'n': return 9;
	case 'E': return 10;
	case 'b': return 11;
	case 'i': return 12;
	case 'T': return 13;
	case 'h': return 14;
	case '3': return 15;
	case 'r': return 16;
	case 'o': return 17;
	case 'S': return 18;
	case ',': return 19;
	case 'C': return 20;
	case 'J': return 21;
	case 'Y': return 22;
	case 'G': return 23;
	case 'z': return 24;
	case 'D': return 25;
	case 'v': return 26;
	case 'd': return 27;
	case '4': return 28;
	case 'F': return 29;
	case '2': return 30;
	case 'I': return 31;
	case 'M': return 32;
	case 'w': return 33;
	case 'B': return 34;
	case 'N': return 35;
	case '7': return 36;
	case 'P': return 37;
	case 'Q': return 38;
	case '0': return 39;
	case 'U': return 40;
	case '8': return 41;
	case 'V': return 42;
	case 'X': return 43;
	case 'Z': return 44;
	case '1': return 45;
	case 'c': return 46;
	case 'f': return 47;
	case 'g': return 48;
	case 'j': return 49;
	case 'k': return 50;
	case 'l': return 51;
	case 'm': return 52;
	case '9': return 53;
	case 'K': return 54;
	case 'p': return 55;
	case 'q': return 56;
	case '6': return 57;
	case '5': return 58;
	case 's': return 59;
	case 't': return 60;
	case 'u': return 61;
	case 'y': return 62;
	case 'L': return 63;
	//case '+': return 0; // toltelek
	default : return 0; // error, don't care...
  }
}

void spec_base64_decode(const unsigned char* encoded, const int encoded_len, unsigned char*& raw_data, int& data_len)
{
  raw_data = new unsigned char[encoded_len/4*3];
  int i = 0;
  data_len = 0;
  //int tri;
  for (i=0; i<encoded_len; i+=4)
  {
    raw_data[data_len]   = (spec_base64_decode(encoded[i])<<2) | (spec_base64_decode(encoded[i+1])>>4);
    raw_data[data_len+1] = (spec_base64_decode(encoded[i+1])<<4) | (spec_base64_decode(encoded[i+2])>>2);
    raw_data[data_len+2] = (spec_base64_decode(encoded[i+2])<<6) | (spec_base64_decode(encoded[i+3]));
    data_len+=3;
  }
  // utolso 1 vagy 2 karakter lehet toltelek, ekkor lecsokkentem az ervenyes hosszat
  if (encoded[encoded_len-2]=='+') data_len -= 2;
  else if (encoded[encoded_len-1]=='+') data_len--;
}

// a CRC-8 checksum-ot kiszamolo fuggveny, egyik legprimitivebb de legalabb gyors :)
// forras: http://www.jonelo.de/java/jacksum/
static unsigned char CRC8_TABLE[] = {
  0x00, 0x07, 0x0E, 0x09, 0x1C, 0x1B, 0x12, 0x15, 0x38, 0x3F, 0x36, 0x31, 0x24, 0x23, 0x2A, 0x2D,
  0x70, 0x77, 0x7E, 0x79, 0x6C, 0x6B, 0x62, 0x65, 0x48, 0x4F, 0x46, 0x41, 0x54, 0x53, 0x5A, 0x5D,
  0xE0, 0xE7, 0xEE, 0xE9, 0xFC, 0xFB, 0xF2, 0xF5, 0xD8, 0xDF, 0xD6, 0xD1, 0xC4, 0xC3, 0xCA, 0xCD,
  0x90, 0x97, 0x9E, 0x99, 0x8C, 0x8B, 0x82, 0x85, 0xA8, 0xAF, 0xA6, 0xA1, 0xB4, 0xB3, 0xBA, 0xBD,
  0xC7, 0xC0, 0xC9, 0xCE, 0xDB, 0xDC, 0xD5, 0xD2, 0xFF, 0xF8, 0xF1, 0xF6, 0xE3, 0xE4, 0xED, 0xEA,
  0xB7, 0xB0, 0xB9, 0xBE, 0xAB, 0xAC, 0xA5, 0xA2, 0x8F, 0x88, 0x81, 0x86, 0x93, 0x94, 0x9D, 0x9A,
  0x27, 0x20, 0x29, 0x2E, 0x3B, 0x3C, 0x35, 0x32, 0x1F, 0x18, 0x11, 0x16, 0x03, 0x04, 0x0D, 0x0A,
  0x57, 0x50, 0x59, 0x5E, 0x4B, 0x4C, 0x45, 0x42, 0x6F, 0x68, 0x61, 0x66, 0x73, 0x74, 0x7D, 0x7A,
  0x89, 0x8E, 0x87, 0x80, 0x95, 0x92, 0x9B, 0x9C, 0xB1, 0xB6, 0xBF, 0xB8, 0xAD, 0xAA, 0xA3, 0xA4,
  0xF9, 0xFE, 0xF7, 0xF0, 0xE5, 0xE2, 0xEB, 0xEC, 0xC1, 0xC6, 0xCF, 0xC8, 0xDD, 0xDA, 0xD3, 0xD4,
  0x69, 0x6E, 0x67, 0x60, 0x75, 0x72, 0x7B, 0x7C, 0x51, 0x56, 0x5F, 0x58, 0x4D, 0x4A, 0x43, 0x44,
  0x19, 0x1E, 0x17, 0x10, 0x05, 0x02, 0x0B, 0x0C, 0x21, 0x26, 0x2F, 0x28, 0x3D, 0x3A, 0x33, 0x34,
  0x4E, 0x49, 0x40, 0x47, 0x52, 0x55, 0x5C, 0x5B, 0x76, 0x71, 0x78, 0x7F, 0x6A, 0x6D, 0x64, 0x63,
  0x3E, 0x39, 0x30, 0x37, 0x22, 0x25, 0x2C, 0x2B, 0x06, 0x01, 0x08, 0x0F, 0x1A, 0x1D, 0x14, 0x13,
  0xAE, 0xA9, 0xA0, 0xA7, 0xB2, 0xB5, 0xBC, 0xBB, 0x96, 0x91, 0x98, 0x9F, 0x8A, 0x8D, 0x84, 0x83,
  0xDE, 0xD9, 0xD0, 0xD7, 0xC2, 0xC5, 0xCC, 0xCB, 0xE6, 0xE1, 0xE8, 0xEF, 0xFA, 0xFD, 0xF4, 0xF3 };

void crc8(const unsigned char* data, const int len, unsigned char& val)
{
  for (int i=0; i<len; i++) val = CRC8_TABLE[val ^ data[i]];
}

// textmap -> message string
// lepesek:
// 0. kiszamolom a buffer szukseges hosszat
// 1. minden key-value par berakas a bufferbe: key '\0' value '\0' formaban, '\0' az elvalaszto karakter
// 2. CRC-8 kiszamolasa es a buffer vegere rakasa
// 3. spec_base64 vegrehajtasa a encoded bufferbe
void textmap2msgstr(const textmap& tm, string& msgstr)
{
  int tm_size = tm.get_count();
  int buf_size = 0;
  for (int elem_i=0; elem_i<tm_size; elem_i++)
  {
    buf_size += length(tm.getkey(elem_i)) + length(tm[elem_i]) + 2; // 2 db. '\0' string terminator
  }
  buf_size++; // a CRC-8 plussz 1 byte
  unsigned char* buffer = new unsigned char[buf_size];
  // bemasolom a bufferbe a textmap teljes tartalmat
  unsigned char* ptr = buffer; // mutato memoria helyre ahol kovetkezo adat kezdodik
  int str_len;
  for (int elem_i=0; elem_i<tm_size; elem_i++)
  {
    memcpy(ptr, pconst(tm.getkey(elem_i)), str_len=length(tm.getkey(elem_i)));
    ptr += str_len;
    *ptr = '\0';
    ptr++;
    memcpy(ptr, pconst(tm[elem_i]), str_len=length(tm[elem_i]));
    ptr += str_len;
    *ptr = '\0';
    ptr++;
  }
  // CRC-8 szamitasa es berakas buffer vegere
  *ptr = 0; // CRC init value
  crc8(buffer, buf_size-1, *ptr);
  // spec. base64 kiszamolasa buffer alapjan
  char* encoded;
  spec_base64_encode(buffer, buf_size, encoded);
  msgstr = encoded;
  delete buffer;
  delete encoded;
}

// message string -> textmap ha 0-val tert vissza vagy hibakod 1-...
// lepesek: 
// 1. spec. base64 dekodolasa bufferbe
// 2. crc-8 ellenorzese
// 3. buffer adatok kiolvasas egy textmapba
// Hibakodok:
// 1 : invalid string length
// 2 : invalid CRC
// 3 : invalid number of strings
int msgstr2textmap(const string& msgstr, textmap& tm)
{
  tm.clear();
  int msgstr_len = length(msgstr);
  if (msgstr_len==0) return 0; // semmi nincs benne
  if (msgstr_len%4!=0) return 1; // invalid string length
  unsigned char* raw_data; int data_len;
  spec_base64_decode((unsigned char*)pconst(msgstr), msgstr_len, raw_data, data_len);
  unsigned char stored_crc = raw_data[data_len-1];
  unsigned char crc_value  = 0;
  crc8(raw_data, data_len-1, crc_value);
  if (crc_value!=stored_crc) return 2; // invalid CRC
  data_len--; // crc-8 nem kell
  int str_count = 0;
  for (int i=0; i<data_len; i++)
    if (raw_data[i]=='\0') str_count++;
  if ( (str_count<2) || (str_count%2!=0) ) return 3; // invalid number of strings
  str_count /= 2; // parok szama
  unsigned char* ptr = raw_data;
  for (int i=0; i<str_count; i++)
  {
    string key((char*)ptr);
    ptr += length(key) + 1;
    string value((char*)ptr);
    ptr += length(value) + 1;
    tm.put(key, value);
  }
  delete raw_data;
  return 0;
}
