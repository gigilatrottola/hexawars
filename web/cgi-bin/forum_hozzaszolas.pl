#!/usr/bin/perl

use DBI;

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

sub cgi_convert {
  my $p = shift;
  $p =~ tr/+/ /; #minden + jelet szóközre cserél
  $p =~ s/%([0-9a-f]{2})/pack("c",hex($1))/ige; # hexa kódokat vissza
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

###########################################################################
### program kezdete:

### összes bemeneti adat ebbe kerül
%in=&getQuery;

if ( defined($in{'ujtema'}) and ($in{'ujtema'} eq 'igen') )
{
   &htmloldal_kiiras_cserevel('forum_temainditas.htm',%csere_html);
}
else
{
   $temanev = $in{'melyik'};
   unless ($temanev !~ /[^\w\d\síÍöÖüÜóÓõÕúÚéÉáÁûÛ@!:,\.\(\)\[\]\*\+\-]+/)
   {
      $csere_html{'üzenetek'} = 'Illegal character in topic title!';
      $temanev = '-';
   }

   $csere_html{'témanév'} = $temanev;

   $csere_html{'melyiktéma'} = '<INPUT type="hidden" name="temanev" value="'.$temanev.'">';

   &htmloldal_kiiras_cserevel('forum_hozzaszolas.htm',%csere_html);
}
