<?
$toplistak = array(
	'points' => 'Points earned',
	'played' => 'Games played'
);
$kapcsolat = mysql_connect( "address", "user", "passwd" );
if (!$kapcsolat) die( "Cannot connect to database: ".mysql_error() );
mysql_select_db( "mobil" ) or die( "Cannot open database: ".mysql_error() );

function getGamesPlayed()
{
	$q = mysql_query("SELECT sum(played) FROM account WHERE (toplist_exclude!='1')") or die(mysql_error());
	if ( $r = mysql_fetch_row( $q ) ) return $r[0];
	else return "0";
}
function getPlayerCount()
{
	$q = mysql_query("SELECT count(*) FROM account WHERE (toplist_exclude!='1')") or die(mysql_error());
	if ( $r = mysql_fetch_row( $q ) ) return $r[0];
	else return "0";
}

function getDbTime()
{
	$q = mysql_query("SELECT now()") or die(mysql_error());
	if ( $r = mysql_fetch_row( $q ) ) return $r[0];
	else return "0";
}

// megnezi adott perce generalt-e mar ilyen toplistat, ha nem akkor general egy html fajlt es feljegyzi az idot
// ha igen akkor nem csinal semmit, a meglevo html file jo lesz
// visszateresi ertek az adott parameterekhez tartozo file nev
function generateToplist($melyik,$range)
{
	// parameterek ellenorzese
	global $toplistak;
	$megvan = false;
	foreach ( $toplistak as $kulcs => $ertek ) { if ($melyik==$kulcs) { $megvan=true; break; } }
	if (!$megvan) die("Invalid toplist type");
	$range = (int)$range;
	if ( ($range<0) || ($range>9) ) die("Invalid range");
	$filenev = 'generated/hexawar_'.$melyik.'_'.$range.'.html';

	// elozo generalas adatra rakereses, ha van friss file akkor return
	$sql = 'SELECT melyik FROM hexawar_toplist WHERE melyik="'.$melyik.'" AND tartomany='.$range.' AND DATE_ADD(idopont,INTERVAL 15 MINUTE)>NOW()';
	$q = mysql_query($sql);
	$r = mysql_fetch_row( $q );
	if ($r)
	{
		mysql_query("UPDATE hexawar_toplist SET viewed=viewed+1 WHERE melyik='".$melyik."' AND tartomany=".$range);
		return $filenev;
	}
	
	// toplista html file generalasa
	$tartkezdet = $range*100;
	$tartomany =  $tartkezdet . ',100'; // 100 jatekos listazasa
	$sql = "SELECT login, points, played FROM account WHERE (toplist_exclude!='1') ORDER BY";
	if ($melyik=='points')
	{
		$sql .= ' points';
	}
	else
	{
		$sql .= ' played';
	}
	$sql .= ' DESC LIMIT '.$tartomany;
	// file megnyitása
	$hfh = fopen($filenev, "w") or die("Cant open file for writing");
	flock($hfh, LOCK_EX) or die("Can't lock file");
	fwrite($hfh,'<html><link rel="stylesheet" href="/images/hexawars.css" type="text/css"><head><title>Hexawars homepage</title><meta http-equiv="Content-Type" content="text/html; charset=utf-8"><meta name="description" content="Hexawars mobile multiplayer strategy game" /><meta name="keywords" content="mobile, game, multiplayer, premium, nokia, ericsson, sony" /></head><BODY bgcolor="#111111"><center><table width="697" height="678" border="0" cellpadding="0" cellspacing="0" BACKGROUND="/images/middle_bg.gif"><tr><td colspan=2><center><img src="/images/hexakepk.gif" width="697" height="141" alt=""></center></td></tr><tr><td colspan=2 BACKGROUND="/images/myhatter.jpg"><B><center><!--A HREF="http://www.hexawars.com/"><IMG SRC="images/hf2.gif" WIDTH="20" HEIGHT="12" BORDER="0" ALT="Magyar"></A> &nbsp;&nbsp; <A HREF="http://www.hexawars.com/"><IMG SRC="images/ef2.gif" WIDTH="20" HEIGHT="12" BORDER="0" ALT="English"></A--></center><A HREF="http://www.hexawars.com">Home</A> | <A HREF="http://www.hexawars.com/rules.htm">Rules</A> | <A HREF="http://www.hexawars.com/howtoplay.htm">How to play</A> | <A HREF="http://www.hexawars.com/toplist/hexawar_toplist.php">Toplist</A> | <A HREF="http://www.hexawars.com/cgi-bin/forum_main.pl">Forums</A></td><tr valign="top"><td width="591" BACKGROUND="/images/middle_bg.gif"><TABLE cellspacing="10"><TR>	<TD>	<font color=FF6600 size=5><center>HexaWars Top Players<br><br>	<font color=FF6600 size=3><b>') or die("Can't write to file");
	//fwrite($hfh,'<link rel="stylesheet" href="../hexawars.css" type="text/css">');
	//fwrite($hfh,'</head><body>');
	//fwrite($hfh,'<center><H2> HexaWars Top Players </H2>');
	fwrite($hfh,'<b>'.getPlayerCount().'</b> players played <b>'.getGamesPlayed().'</b> games, generated: <b>'.getDbTime().'</b><br/><br/>');
	
	// toplista kivalaszto form
	fwrite($hfh, '<form name="urlap" method="post" action="http://' . $_SERVER['HTTP_HOST'] . $_SERVER['PHP_SELF'] .'">');
	fwrite($hfh, 'Order by: <select name="melyik">');
	foreach ( $toplistak as $kulcs => $ertek )
	{
		fwrite($hfh, '<option value="'.$kulcs.'" '.(($kulcs==$melyik)?'selected':'').'>'.$ertek.'</option>');
	}
	fwrite($hfh, '</select>');
	fwrite($hfh, '<select name="range">');
	for ($i=0; $i<10; $i++)
	{
		$tartval = ' '.($i*100+1).' - '.(($i+1)*100).' ';
		fwrite($hfh, '<option value="'.$i.'" '.(($i==$range)?'selected':'').'>'.$tartval.'</option>');
	}
	fwrite($hfh, '</select>');
	fwrite($hfh, '<input type="submit" value="Show"></input></form><p>');

	// tablazat
	//fwrite($hfh,"<table border='1'>");
	fwrite($hfh,'<table bgcolor=7B0601 border="0"><tr><td class="papircim1">Rank</td><td class="papircim1">Player Name</td><td class="papircim1">Points Earned</td><td class="papircim1">Games Played</td></tr>');
	$q = mysql_query($sql);
	$i = $tartkezdet + 1;
	while ($row=mysql_fetch_row($q))
	{
		fwrite($hfh,'<tr><td class="papir1">'.$i.'</TD><td class="papir1a">'.$row[0].'</TD><td class="papir1">'.$row[1].'</TD><td class="papir1">'.$row[2].'</tr>');
		$i++;
	}
	fwrite($hfh,"</table>");
	mysql_free_result($q);

	// feljegyzem a generalas idopontjat
	mysql_query("UPDATE hexawar_toplist SET idopont=now(), viewed=viewed+1, generated=generated+1 WHERE melyik='".$melyik."' AND tartomany=".$range);
	if (mysql_affected_rows()<1)
		mysql_query("INSERT INTO hexawar_toplist SET melyik='".$melyik."', tartomany=".$range.", idopont=now(), viewed=1, generated=1");

	fwrite($hfh,'</TD>
</TR>
</TABLE>


</td>


<td width="130">


<center><FONT SIZE="2">c r e a t e d &nbsp;&nbsp;b y :</FONT><BR><A HREF="http://www.onebitheroes.com"><IMG SRC="/images/onebitlogo.gif" WIDTH="100" HEIGHT="70" BORDER="0" ALT=""></A></center>
</font>
<P>
<script type="text/javascript"><!--
google_ad_client = "pub-8673506107232803";
google_ad_width = 120;
google_ad_height = 600;
google_ad_format = "120x600_as";
google_ad_type = "text_image";
google_ad_channel ="";
google_color_border = "6699CC";
google_color_bg = "003366";
google_color_link = "FFFFFF";
google_color_text = "AECCEB";
google_color_url = "AECCEB";
//--></script>

<script type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>

</td></tr>
</table>

</td>
</tr>
</table>
</body>
</html>');
	fclose($hfh);
	return $filenev;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
$melyik = isset($_REQUEST['melyik']) ? $_REQUEST['melyik'] : 'points';
$range  = isset($_REQUEST['range'])  ? $_REQUEST['range']  : 0;
$fname = generateToplist($melyik,$range);
mysql_close($kapcsolat);
// redirectelek a megfelelo oldalra
header( "Location: http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/" . $fname);
?>