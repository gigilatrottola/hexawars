#if !defined(_RANDOM_H__INCLUDED_)
#define _RANDOM_H__INCLUDED_

////////////////////////////////////////////////////////////////////////////////
// 
// Veletlenszam generalashoz:
// minden szalban letre kell hozni egy objektumot, a konstruktor mutex-el
// szinkronizal, egy static master_seed ertekbol kiszamitja az adott 
// objektum sajat seed-jet es a tovabbiakban a tobbi peldanytol fuggetlenul  
// szalbiztosan es gyorsan hivhato. A master_seed a rendszeridobol jon letre,
// a tobbi seed belole szarmazik, igy biztosithato hogy egyidoben letrehozott
// objektumok is teljesen mas veletlenszam szekvnciakat adjanak es minden peldany
// szekvenciaja teljesen fuggetlen a tobbitol.
// 
////////////////////////////////////////////////////////////////////////////////

class Random
{
private:
  static unsigned short master_seed[3];
  static int master_seed_initialized; // 0 ha nem, 1 ha mar megvan
  static mutex rndsync_mutex;

  unsigned short seed[3]; // az adott peldany seed-je

public:
	Random(); // szalbiztos konstruktor
	int getInt(int min, int max); // nem szalbiztos, minden szalnak sajat objektumot kell csinalnia!
};

#endif // !defined(_RANDOM_H__INCLUDED_)
