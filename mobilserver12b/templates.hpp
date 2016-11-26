/***************************************************************************************
 *
 * itt vannak a saját template-s cuccok, ezt a vackot include-olni kell ahol használni kell
 *
 ***************************************************************************************/

/*
 * vec elemeinek véletlenszerû összekeverése, 
 * a keverés helyben történik az eredeti elemek átrendezésével
 */
template<class T>
void shuffle(tpodlist<T>& vec, Random random)
{
	int last_idx = vec.get_count() - 1;
	while (last_idx>=1)
	{
		int rnd = random.getInt(0,last_idx); // kisorsolom az egyik elemet
		T tmp = vec[rnd]; // kicserélem az utolsó elemmel, a kisorsolt elem lesz az utolsó
		vec[rnd] = vec[last_idx];
		vec[last_idx] = tmp;
		last_idx--; // a kisorsolt elemet kizárom a következõ sorsolásból
	}
}

// a vec elemeit kiirom egy stringbe, szamokat kell tartalmazzon amelyeket elfogad az itostring
template<class T>
string IntListtoCSIS(tpodlist<T>& vec)
{
  string str;
  int vec_size = vec.get_count();
  for (int i=0; i<vec_size; i++)
  {
    if (i>0) str += ',';
    str += itostring(vec[i]);
  }
  return str;
}
