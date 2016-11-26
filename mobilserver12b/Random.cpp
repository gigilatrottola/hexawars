#include <stdlib.h>
#include <time.h>

#include "include.h"

// a master seed nincs inicializalva az elso konstruktor hivas elott
int Random::master_seed_initialized = 0;
unsigned short Random::master_seed[3];
mutex Random::rndsync_mutex;

Random::Random() 
{
  scopelock lock(rndsync_mutex); // globalis valtozok hasznalata elott lockolas
  if (!master_seed_initialized)
  {
    time_t t = time(NULL);
    for (int i=0; i<3; i++) { master_seed[i] = (unsigned short)t; t>>=8; }
    master_seed_initialized = 1;
  }
  // peldany seedjet szamoljuk ki a master_seedbol
  for (int i=0; i<3; i++) seed[i] = (unsigned short)nrand48(master_seed);
}

int Random::getInt(int min, int max) 
{	
  return (int)( ( nrand48(seed) % (max-min+1) ) + min );
}
