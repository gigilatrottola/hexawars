package HexaWars;

import java.util.Enumeration;
import java.util.Hashtable;

/*
 * Created on 2006.04.17.
 */

/**
 * @author Delic ?d?m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TransferCode {
  
//modositott BASE64 kodolas:
  // a sortorogetes 76 karakterenkent kiszedve
  // a karakterkodok osszevisza random :)
  // az utolso 65. karakter a toltelek amikor nem 3-al oszthato hossz volt
  private static final char[] charTab = "HexaWAR.OnEbiTh3roS,CJYGzDvd4F2IMwBN7PQ0U8VXZ1cfgjklm9Kpq65stuyL+".toCharArray();

  public static StringBuffer spec_base64_encode(byte[] raw_data)
  {
    // 3 chars -> 4 chars
    final int remaining_chars = raw_data.length % 3;
    final int for_chars = raw_data.length - remaining_chars;
    StringBuffer encoded = new StringBuffer(for_chars/3*4+((remaining_chars!=0)?4:0));
    int i = 0; // raw tomb indexelo
    int d; // 32 bites tarolo bittologatashoz
    for (i=0; i<for_chars; i+=3)
    {
      d = ((((int)raw_data[i]) & 0x0ff) << 16) | ((((int)raw_data[i+1]) & 0x0ff) << 8) | (((int)raw_data[i+2]) & 0x0ff);
      encoded.append(charTab[(d >>> 18) & 63]);
      encoded.append(charTab[(d >>> 12) & 63]);
      encoded.append(charTab[(d >>> 6) & 63]);
      encoded.append(charTab[d & 63]);
    }
    if (remaining_chars==1) // 2 toltelek kell
    {
      d = (((int)raw_data[i]) & 0x0ff) << 16;
      encoded.append(charTab[(d >>> 18) & 63]);
      encoded.append(charTab[(d >>> 12) & 63]);
      encoded.append('+'); 
      encoded.append('+');
    }
    else if (remaining_chars==2) // 1 toltelek
    {
        d = ((((int)raw_data[i]) & 0x0ff) << 16) | ((((int)raw_data[i+1]) & 0x0ff) << 8);
        encoded.append(charTab[(d >>> 18) & 63]);
        encoded.append(charTab[(d >>> 12) & 63]);
        encoded.append(charTab[(d >>> 6) & 63]);
        encoded.append('+');
    }
    return encoded;
  }

  private static int spec_base64_decode(char c)
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

  public static byte[] spec_base64_decode(String encoded)
  {
    int encoded_len = encoded.length();
    int data_len = encoded_len/4*3;
    // utolso 1 vagy 2 karakter lehet toltelek, ekkor lecsokkentem az ervenyes hosszat
    if (encoded.charAt(encoded_len-2)=='+') data_len -= 2;
    else if (encoded.charAt(encoded_len-1)=='+') data_len--;
    byte[] raw_data = new byte[data_len];
    int i = 0;
    int raw_i = 0;
    int nopad_border = encoded_len - 4;
    for (i=0; i<nopad_border; i+=4)
    {
      raw_data[raw_i]   = (byte)((spec_base64_decode(encoded.charAt(i))<<2) | (spec_base64_decode(encoded.charAt(i+1))>>>4));
      raw_data[raw_i+1] = (byte)((spec_base64_decode(encoded.charAt(i+1))<<4) | (spec_base64_decode(encoded.charAt(i+2))>>>2));
      raw_data[raw_i+2] = (byte)((spec_base64_decode(encoded.charAt(i+2))<<6) | (spec_base64_decode(encoded.charAt(i+3))));
      raw_i+=3;
    }
    if (raw_i<data_len) {
      raw_data[raw_i]   = (byte)((spec_base64_decode(encoded.charAt(i))<<2) | (spec_base64_decode(encoded.charAt(i+1))>>>4));
      raw_i++;
      if (raw_i<data_len) {
        raw_data[raw_i] = (byte)((spec_base64_decode(encoded.charAt(i+1))<<4) | (spec_base64_decode(encoded.charAt(i+2))>>>2));
        raw_i++;
        if (raw_i<data_len) {
          raw_data[raw_i] = (byte)((spec_base64_decode(encoded.charAt(i+2))<<6) | (spec_base64_decode(encoded.charAt(i+3))));
        }
      }
    }
    return raw_data;
  }
  
  private static final byte[] CRC8_TABLE =
    new byte[] {
        (byte) 0x00,
        (byte) 0x07,
        (byte) 0x0E,
        (byte) 0x09,
        (byte) 0x1C,
        (byte) 0x1B,
        (byte) 0x12,
        (byte) 0x15,
        (byte) 0x38,
        (byte) 0x3F,
        (byte) 0x36,
        (byte) 0x31,
        (byte) 0x24,
        (byte) 0x23,
        (byte) 0x2A,
        (byte) 0x2D,
        (byte) 0x70,
        (byte) 0x77,
        (byte) 0x7E,
        (byte) 0x79,
        (byte) 0x6C,
        (byte) 0x6B,
        (byte) 0x62,
        (byte) 0x65,
        (byte) 0x48,
        (byte) 0x4F,
        (byte) 0x46,
        (byte) 0x41,
        (byte) 0x54,
        (byte) 0x53,
        (byte) 0x5A,
        (byte) 0x5D,
        (byte) 0xE0,
        (byte) 0xE7,
        (byte) 0xEE,
        (byte) 0xE9,
        (byte) 0xFC,
        (byte) 0xFB,
        (byte) 0xF2,
        (byte) 0xF5,
        (byte) 0xD8,
        (byte) 0xDF,
        (byte) 0xD6,
        (byte) 0xD1,
        (byte) 0xC4,
        (byte) 0xC3,
        (byte) 0xCA,
        (byte) 0xCD,
        (byte) 0x90,
        (byte) 0x97,
        (byte) 0x9E,
        (byte) 0x99,
        (byte) 0x8C,
        (byte) 0x8B,
        (byte) 0x82,
        (byte) 0x85,
        (byte) 0xA8,
        (byte) 0xAF,
        (byte) 0xA6,
        (byte) 0xA1,
        (byte) 0xB4,
        (byte) 0xB3,
        (byte) 0xBA,
        (byte) 0xBD,
        (byte) 0xC7,
        (byte) 0xC0,
        (byte) 0xC9,
        (byte) 0xCE,
        (byte) 0xDB,
        (byte) 0xDC,
        (byte) 0xD5,
        (byte) 0xD2,
        (byte) 0xFF,
        (byte) 0xF8,
        (byte) 0xF1,
        (byte) 0xF6,
        (byte) 0xE3,
        (byte) 0xE4,
        (byte) 0xED,
        (byte) 0xEA,
        (byte) 0xB7,
        (byte) 0xB0,
        (byte) 0xB9,
        (byte) 0xBE,
        (byte) 0xAB,
        (byte) 0xAC,
        (byte) 0xA5,
        (byte) 0xA2,
        (byte) 0x8F,
        (byte) 0x88,
        (byte) 0x81,
        (byte) 0x86,
        (byte) 0x93,
        (byte) 0x94,
        (byte) 0x9D,
        (byte) 0x9A,
        (byte) 0x27,
        (byte) 0x20,
        (byte) 0x29,
        (byte) 0x2E,
        (byte) 0x3B,
        (byte) 0x3C,
        (byte) 0x35,
        (byte) 0x32,
        (byte) 0x1F,
        (byte) 0x18,
        (byte) 0x11,
        (byte) 0x16,
        (byte) 0x03,
        (byte) 0x04,
        (byte) 0x0D,
        (byte) 0x0A,
        (byte) 0x57,
        (byte) 0x50,
        (byte) 0x59,
        (byte) 0x5E,
        (byte) 0x4B,
        (byte) 0x4C,
        (byte) 0x45,
        (byte) 0x42,
        (byte) 0x6F,
        (byte) 0x68,
        (byte) 0x61,
        (byte) 0x66,
        (byte) 0x73,
        (byte) 0x74,
        (byte) 0x7D,
        (byte) 0x7A,
        (byte) 0x89,
        (byte) 0x8E,
        (byte) 0x87,
        (byte) 0x80,
        (byte) 0x95,
        (byte) 0x92,
        (byte) 0x9B,
        (byte) 0x9C,
        (byte) 0xB1,
        (byte) 0xB6,
        (byte) 0xBF,
        (byte) 0xB8,
        (byte) 0xAD,
        (byte) 0xAA,
        (byte) 0xA3,
        (byte) 0xA4,
        (byte) 0xF9,
        (byte) 0xFE,
        (byte) 0xF7,
        (byte) 0xF0,
        (byte) 0xE5,
        (byte) 0xE2,
        (byte) 0xEB,
        (byte) 0xEC,
        (byte) 0xC1,
        (byte) 0xC6,
        (byte) 0xCF,
        (byte) 0xC8,
        (byte) 0xDD,
        (byte) 0xDA,
        (byte) 0xD3,
        (byte) 0xD4,
        (byte) 0x69,
        (byte) 0x6E,
        (byte) 0x67,
        (byte) 0x60,
        (byte) 0x75,
        (byte) 0x72,
        (byte) 0x7B,
        (byte) 0x7C,
        (byte) 0x51,
        (byte) 0x56,
        (byte) 0x5F,
        (byte) 0x58,
        (byte) 0x4D,
        (byte) 0x4A,
        (byte) 0x43,
        (byte) 0x44,
        (byte) 0x19,
        (byte) 0x1E,
        (byte) 0x17,
        (byte) 0x10,
        (byte) 0x05,
        (byte) 0x02,
        (byte) 0x0B,
        (byte) 0x0C,
        (byte) 0x21,
        (byte) 0x26,
        (byte) 0x2F,
        (byte) 0x28,
        (byte) 0x3D,
        (byte) 0x3A,
        (byte) 0x33,
        (byte) 0x34,
        (byte) 0x4E,
        (byte) 0x49,
        (byte) 0x40,
        (byte) 0x47,
        (byte) 0x52,
        (byte) 0x55,
        (byte) 0x5C,
        (byte) 0x5B,
        (byte) 0x76,
        (byte) 0x71,
        (byte) 0x78,
        (byte) 0x7F,
        (byte) 0x6A,
        (byte) 0x6D,
        (byte) 0x64,
        (byte) 0x63,
        (byte) 0x3E,
        (byte) 0x39,
        (byte) 0x30,
        (byte) 0x37,
        (byte) 0x22,
        (byte) 0x25,
        (byte) 0x2C,
        (byte) 0x2B,
        (byte) 0x06,
        (byte) 0x01,
        (byte) 0x08,
        (byte) 0x0F,
        (byte) 0x1A,
        (byte) 0x1D,
        (byte) 0x14,
        (byte) 0x13,
        (byte) 0xAE,
        (byte) 0xA9,
        (byte) 0xA0,
        (byte) 0xA7,
        (byte) 0xB2,
        (byte) 0xB5,
        (byte) 0xBC,
        (byte) 0xBB,
        (byte) 0x96,
        (byte) 0x91,
        (byte) 0x98,
        (byte) 0x9F,
        (byte) 0x8A,
        (byte) 0x8D,
        (byte) 0x84,
        (byte) 0x83,
        (byte) 0xDE,
        (byte) 0xD9,
        (byte) 0xD0,
        (byte) 0xD7,
        (byte) 0xC2,
        (byte) 0xC5,
        (byte) 0xCC,
        (byte) 0xCB,
        (byte) 0xE6,
        (byte) 0xE1,
        (byte) 0xE8,
        (byte) 0xEF,
        (byte) 0xFA,
        (byte) 0xFD,
        (byte) 0xF4,
        (byte) 0xF3 };

  public static byte crc8(byte[] data, int len, byte val) {
    for (int i=0; i<len; i++) 
      val = CRC8_TABLE[(val ^ data[i]) &0xFF ];
    return val;
  }
  
  public static StringBuffer textmap2msgstr(Hashtable ht)
  {
    int buf_size = 0;
    for (Enumeration e = ht.keys(); e.hasMoreElements(); ) {
      String key = (String)e.nextElement();
      String value = (String)ht.get(key);
      buf_size += key.length() + value.length() + 2; // 2 db. '\0' string terminator 
    }
    buf_size++; // a CRC-8 plussz 1 byte 
    byte buffer[] = new byte[buf_size];
    // bemasolom a bufferbe a Hashtable teljes tartalmat  
    byte str[]; 
    int buff_i = 0; // buffer indexeleshez es a merete
    for (Enumeration e = ht.keys(); e.hasMoreElements(); ) {
      String key = (String)e.nextElement();
      String value = (String)ht.get(key);
      str = key.getBytes(); //character encoding???
      for (int i=0; i<str.length; i++ ) { buffer[buff_i]=str[i]; buff_i++; }
      buffer[buff_i]=0; buff_i++;
      str = value.getBytes();
      for (int i=0; i<str.length; i++ ) { buffer[buff_i]=str[i]; buff_i++; }
      buffer[buff_i]=0; buff_i++;
    }
    // CRC-8 szamitasa es berakas buffer vegere
    byte crc_val = crc8(buffer, buf_size-1, (byte)0);
    buffer[buf_size-1]=crc_val;
    // spec. base64 kiszamolasa buffer alapjan
    return spec_base64_encode(buffer);
  }



  // Hibakodok:
  // 1 : invalid string length
  // 2 : invalid CRC
  // 3 : invalid number of strings
  public static int msgstr2textmap(String msgstr, Hashtable ht)
  {
    int strlen = msgstr.length();
    if (strlen==0) return 0; // semmi nincs benne
    if (strlen%4!=0) return 1; // invalid string length
    byte raw_data[] = spec_base64_decode(msgstr);
    int data_len = raw_data.length-1; // crc-8 nem kell 
    byte stored_crc = raw_data[data_len];
    byte crc_value  = crc8(raw_data, data_len, (byte)0);
    if (crc_value!=stored_crc) return 2; // invalid CRC 
    int str_count = 0;
    for (int i=0; i<data_len; i++)
      if (raw_data[i]==0) str_count++;
    if ( (str_count<2) || (str_count%2!=0) ) return 3; // invalid number of strings
    int offset = 0;
    int length = 0;
    boolean inKey = true;
    String key = "";
    String value = "";
    for (int i=0; i<data_len; i++) {
      if (raw_data[i]==0) {
        if (inKey) {
          if (length>0) key = new String(raw_data,offset,length);
          else key = "";
        } else {
          if (length>0) value = new String(raw_data,offset,length);
          else value = "";
          ht.put(key,value);
        }
        inKey = !inKey;
        length = 0;
        offset = i+1;
      } else {
        length++;
      }
    }
    return 0;
  }
  
}

