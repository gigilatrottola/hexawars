#if !defined(_SETTINGS_H__INCLUDED_)
#define _SETTINGS_H__INCLUDED_

class Settings
{
private:
  static Settings* pSettings;
  rwlock  rwlock_data;
  textmap tm_data;
  Settings() { } // private constructor for singleton model
public:
  static Settings* getSettings();
  string get(string key); // get the value associated with key or empty string
  void set(string key, string value); // set a key-value pair, add new or replace existing or delete existing if value is empty
  string log(); // write to a string all the content
};

#endif // !defined(_SETTINGS_H__INCLUDED_)
