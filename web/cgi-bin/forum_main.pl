#!/usr/bin/perl

use DBI;

use strict;

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
   print '<HTML><HEAD><TITLE>Nincs meg a szükséges HTML fájl!!!</TITLE></HEAD>';
   print '<BODY><H1>Nem találtam meg a filet :(</H1></BODY></HTML>';
}
close(HTMLFAJL);
}

sub main
{
   my $delszigetH = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');
   if (!defined $delszigetH) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }

   my $sth = $delszigetH->prepare("SELECT temanev,max(idopont),count(*) FROM forum GROUP BY temanev");
   if (!defined $sth) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
   $sth->execute;
   my %csere_html;
   my @idopontok;
   my $db=0;
   my ($temanev,$idopont,$szam);
   my %temak;
   while (($temanev,$idopont,$szam)=$sth->fetchrow_array)
   {
      $db++;
      push @idopontok,$idopont;
      $temak{$idopont} = '<tr><td class="papir1a"><a href="javascript:tema(\''.$temanev.'\')">'.$temanev.'</a><td class="papircim1">'.$idopont.'<td class="papircim1">'.$szam.'</tr>';
   }
   @idopontok = reverse(sort(@idopontok)); # rendezés idõrendbe
   
   foreach $idopont (@idopontok)
   {
      $csere_html{'temak'} .= $temak{$idopont};
   }
   
   if ($db==0) { $csere_html{'temak'}='<tr><td rowspan="3" class="papir1a">There are no topics yet.</tr>'; }
   $sth->finish;

   &htmloldal_kiiras_cserevel('forum_main.htm',%csere_html);

   $delszigetH->disconnect;
}

###########################################################################
### program kezdete:
&main;