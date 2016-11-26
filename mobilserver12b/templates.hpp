/***************************************************************************************
 *
 * itt vannak a saj�t template-s cuccok, ezt a vackot include-olni kell ahol haszn�lni kell
 *
 ***************************************************************************************/

/*
 * vec elemeinek v�letlenszer� �sszekever�se, 
 * a kever�s helyben t�rt�nik az eredeti elemek �trendez�s�vel
 */
template<class T>
void shuffle(tpodlist<T>& vec, Random random)
{
	int last_idx = vec.get_count() - 1;
	while (last_idx>=1)
	{
		int rnd = random.getInt(0,last_idx); // kisorsolom az egyik elemet
		T tmp = vec[rnd]; // kicser�lem az utols� elemmel, a kisorsolt elem lesz az utols�
		vec[rnd] = vec[last_idx];
		vec[last_idx] = tmp;
		last_idx--; // a kisorsolt elemet kiz�rom a k�vetkez� sorsol�sb�l
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
