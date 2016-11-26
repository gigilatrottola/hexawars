#!/usr/bin/perl

use DBI;

use strict;

use vars qw($delszigetH);

sub htmloldal_kiiras_cserevel
{
print "Content-type: text/html\n\n";
my $fajlnev = shift(@_);
my %csere_html = @_;
my $sor;
if (open(HTMLFAJL,"../html/$fajlnev"))
{
while($sor=<HTMLFAJL>) 
{
   if (substr($sor,0,5) eq 'ADAT:') { print($csere_html{substr($sor,5,-1)}."\n"); }
   else { print $sor; }
}
} else
{
   print '<HTML><HEAD><TITLE>Nincs meg a sz�ks�ges HTML f�jl!!!</TITLE></HEAD>';
   print '<BODY><H1>Nem tal�ltam meg a filet :(</H1></BODY></HTML>';
}
close(HTMLFAJL);
}

sub cgi_convert {
  my $p = shift;
  $p =~ tr/+/ /; #minden + jelet sz�k�zre cser�l
  $p =~ s/%([0-9a-f]{2})/pack("c",hex($1))/ige; # hexa k�dokat vissza
  return $p;
}
sub getQuery
{
   my $query = '';
   if ($ENV{'REQUEST_METHOD'} eq 'GET')
   {
         $query = $ENV{'QUERY_STRING'};
   }
   if($ENV{'REQUEST_METHOD'} eq 'POST')
   {
         read(STDIN, $query, $ENV{'CONTENT_LENGTH'});
   }
   my @parameters = split('&',$query);
   my ($p,$v); my %input;
 for( @parameters ){
 ($p,$v) = split('=');
 $p = &cgi_convert($p);
 $v = &cgi_convert($v);
 $input{$p} = $v;
 }
   return %input;
}

sub hibaOldal # ha hiba t�rt�nt a hozz�sz�l�sban akkor vissza a h. oldalra
{  
   my $temanev =  shift(@_);
   my %csere_html;
   $csere_html{'�zenetek'} = join('<BR>',@_);
   $csere_html{'�zenetek'} .= '<br><font size="-1">To <a href="javascript:history.back()">correct</a> click the back button of your browser.</font>';
   $csere_html{'t�man�v'} = $temanev;
   $csere_html{'melyikt�ma'} = '<INPUT type="hidden" name="temanev" value="'.$temanev.'">';
   &htmloldal_kiiras_cserevel('forum_hozzaszolas.htm',%csere_html);
   exit;
}

sub hibaOldal2 # ha hiba t�rt�nt a hozz�sz�l�sban akkor vissza a h. oldalra
{  
   my $temanev =  shift(@_);
   my %csere_html;
   $csere_html{'�zenetek'} = join('<BR>',@_);
   $csere_html{'�zenetek'} .= '<br><font size="-1">To <a href="javascript:history.back()">correct</a> click the back button of your browser.</font>';
   &htmloldal_kiiras_cserevel('forum_temainditas.htm',%csere_html);
   exit;
}

sub main
{  
 
   my $LHSZ = 20; # laponk�nti hozz�sz�l�sok sz�ma
   
   my %in=&getQuery; # �sszes bemeneti adat ebbe ker�l
   ### ellen�rz�m mindegyiket hogy csak adott karaktereket tartalmazhassanak
   my $kulcs;
   foreach $kulcs (keys %in)
   {
      if ($kulcs ne 'hozzaszolas')
      {
         unless ($in{$kulcs} !~ /[^\w\d\s������������������@!:,\.\(\)\[\]\*\+\-]+/)
         {  
            my $temanev = '';
            if (defined $in{'temanev'}) { $temanev = $in{'temanev'}; }
            else { $temanev = $in{'melyik'}; }
            &hibaOldal($temanev,'Illegal character was given','Illegal character was given!');
         }
      }
   }#foreach
   ###
   my $temanev = '';
   if (defined $in{'temanev'}) 
   { 
   ### �j hozz�sz�l�s
   $temanev = $in{'temanev'};
   if ( (length($temanev)>40) or (length($temanev)<3) )
   {
      &hibaOldal2($temanev,'Topic name is too short or too long! Minimal length is 3 and maximal 40 characters.');
   }
   my $hozzaszolas = $in{'hozzaszolas'};
   my $login = $in{'login'};
   my $jelszo = $in{'jelszo'};
   # most j�nnek a tartalmi ellen�rz�sek
   # $hozzaszolas hossz ellen�rz�se:
   if ( (length($hozzaszolas)<2) or (length($hozzaszolas)>10000) ) {
      &hibaOldal($temanev,'The post was too short or too long!');
   }

   # speci�lis html karakterek cser�je:
   $hozzaszolas =~ s/&/&amp;/gm;
   $hozzaszolas =~ s/"/&quot;/gm; #"
   $hozzaszolas =~ s/'/&quot;/gm; #'
   $hozzaszolas =~ s/</&lt;/gm;
   $hozzaszolas =~ s/>/&gt;/gm;
   $hozzaszolas =~ s/\n/<br>/gm;  # �j sor -> sort�r�s
   
   # a hozz�sz�l�s szavai max. N karakteresek lehetnek, ha hosszabb akkor nem engedem
   my @szavak = split(' ',$hozzaszolas); # szavak ment�n sz�tv�gom
   foreach (@szavak)
   {
      if (length($_)>100)
      {
         &hibaOldal($temanev,'Word too long!');
      }
   }

   # a tov�bbiakhoz meg kell nyitni az adatb�zist
   $delszigetH = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');
   if (!defined $delszigetH) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   # $temanev ellen�rz�s: l�tez� t�ma kell legyen vagy �j t�ma ind�t�sa
   my $ujTema=0; # ez jelzi hogy l�tez� vagy �j
   my $sth = $delszigetH->prepare("SELECT idopont FROM forum WHERE temanev=?");
   if (!defined $sth) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   if (!defined $sth->execute($temanev)) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; };
   unless ($sth->fetchrow_array) { $ujTema=1; } # ha nincs ilyen t�m�j� hozz�sz�l�s akkor ez �j t�ma
   $sth->finish;
   
   # $login �s $jelszo ellen�rz�se, n�v kiolvas�sa
   my $sth2 = $delszigetH->prepare("SELECT nev,jelszo FROM forum_account WHERE nev=?");
   if (!defined $sth2) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   if (!defined $sth2->execute($login)) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   my ($kari_nev,$kari_jelszo);
   unless (($kari_nev,$kari_jelszo)=$sth2->fetchrow_array)
   {
      if ($ujTema) { &hibaOldal2($temanev,'Bad username or password!'); }
      else { &hibaOldal($temanev,'Bad username or password!'); }
   }
   $sth2->finish;
   if ($kari_jelszo ne $jelszo)
   {
      if ($ujTema) { &hibaOldal2($temanev,'Bad username or password!'); }
      else { &hibaOldal($temanev,'Bad username or password!'); }
   }
   # minden ok, be�rom az �j hozz�sz�l�st
   # a hozz�sz�l�s tartalmazhat sp�ci karaktereket amik zavarj�k
   my $quotes_hozzaszolas = $delszigetH->quote($hozzaszolas);
   my $remoteaddr=$ENV{'REMOTE_ADDR'};
   my $remotehost=$ENV{'REMOTE_HOST'};
   my $remoteident=$ENV{'REMOTE_IDENT'};
   my $remoteuser=$ENV{'REMOTE_USER'};
	####
	# Figyelem a kett�z�st!!!
	####
	my $sth3 = $delszigetH->prepare("SELECT nev FROM forum WHERE temanev='".$temanev."' and hozzaszolas=".$quotes_hozzaszolas." and nev='".$kari_nev."' ");
   if (!defined $sth3) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   if (!defined $sth3->execute) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   my ($csekknev);
   $csekknev=$sth3->fetchrow_array;
   if(length($csekknev)>2)
   {
	  $sth3->finish;
	  print "Content-type: text/html\n\nDouble post!"; exit;
   }
   $sth3->finish;

   my $sql="INSERT INTO forum SET temanev='$temanev',hozzaszolas=$quotes_hozzaszolas,idopont=NOW(),login='$login',nev='$kari_nev', remoteaddr='$remoteaddr', remotehost='$remotehost', remoteident='$remoteident', remoteuser='$remoteuser'";
   my $rv = $delszigetH->do($sql);
   if (!defined $rv) { &hibaOldal($temanev,"Hozz�sz�l�s feljegyz�se nem siker�lt!\n","Hibaok: ".$DBI::errstr."\n"); }
   } else {
      $temanev = $in{'melyik'}; 
      $delszigetH = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');
      if (!defined $delszigetH) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   }
   
   my %csere_html;
   $csere_html{'melyikt�ma'}.= 'var melyik_tema=\''.$temanev.'\';';
   $csere_html{'t�man�v'}.=$temanev;
   
   my $index=0; # alap�rtelmez�s: els� lapon tart�zkodik (legut�bbi hozz�sz�l�sok ezen vannak)
   if (defined $in{'index'}) { $index=int($in{'index'}); }
   
   my $sth4 = $delszigetH->prepare("SELECT count(*) FROM forum WHERE temanev=?");
   if (!defined $sth4) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   if (!defined $sth4->execute($temanev)) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   my ($hozzaszolasSzam) = $sth4->fetchrow_array;
   $sth4->finish;
   my $maxIndex = int(($hozzaszolasSzam-1)/$LHSZ);
   if ($index<0) { $index=$maxIndex; }
   if ($index>$maxIndex) { $index=0; }
   
   $csere_html{'navig�l�s'} = ''; # ebbe ker�l a navig�l�shoz sz�ks�ges html k�d
   if ($index>0)
   {
      $csere_html{'navig�l�s'} .= ' <a href="javascript:masikOldal('.($index-1).')">&lt;&lt;</a> ';
   }
   my $idx;
   foreach $idx (0..$maxIndex)
   {
      if ($idx!=$index)
      {
         $csere_html{'navig�l�s'} .= ' <a href="javascript:masikOldal('.$idx.')">'.($idx+1).'</a> ';
      }
      else
      {
         $csere_html{'navig�l�s'} .= ' <font size="+1">'.($idx+1).'</font> ';
      }
   }
   if ($index<$maxIndex)
   {
      $csere_html{'navig�l�s'} .= ' <a href="javascript:masikOldal('.($index+1).')">&gt;&gt;</a> ';
   }
   
   my $selectKezdet = $index * $LHSZ;
   my $sth3 = $delszigetH->prepare("SELECT nev,idopont,hozzaszolas FROM forum WHERE temanev=? ORDER BY idopont DESC LIMIT $selectKezdet,$LHSZ");
   if (!defined $sth3) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }
   if (!defined $sth3->execute($temanev)) { print "Content-type: text/html\n\nAdatb�zis hiba: $DBI::errstr\n"; exit; }

   my $db=0;
   my ($nev,$idopont,$hozzaszolas);
   my $class0 = 'class="papircim1"';
   my $class1 = 'class="papircim2"';
   while (($nev,$idopont,$hozzaszolas)=$sth3->fetchrow_array)
   {
      $db++;
      $csere_html{'hozz�sz�l�sok'}.='<tr><td '.(($db%2)?$class0:$class1).'>'.$nev.'<td '.(($db%2)?$class0:$class1).'>'.$idopont.'</tr>';
      $csere_html{'hozz�sz�l�sok'}.='<tr><td '.(($db%2)?$class0:$class1).' colspan="2">'.$hozzaszolas.'</tr>';
   }
   if ($db==0) { $csere_html{'t�m�k'}='<tr><td>There are no posts in this topic.<td>-<td>-<td>0</tr>'; }
   $sth3->finish;

   &htmloldal_kiiras_cserevel('forum_tema.htm',%csere_html);

   $delszigetH->disconnect;
}

###########################################################################
### program kezdete:
&main;