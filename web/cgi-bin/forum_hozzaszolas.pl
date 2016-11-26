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

###########################################################################
### program kezdete:

### �sszes bemeneti adat ebbe ker�l
%in=&getQuery;

if ( defined($in{'ujtema'}) and ($in{'ujtema'} eq 'igen') )
{
   &htmloldal_kiiras_cserevel('forum_temainditas.htm',%csere_html);
}
else
{
   $temanev = $in{'melyik'};
   unless ($temanev !~ /[^\w\d\s������������������@!:,\.\(\)\[\]\*\+\-]+/)
   {
      $csere_html{'�zenetek'} = 'Illegal character in topic title!';
      $temanev = '-';
   }

   $csere_html{'t�man�v'} = $temanev;

   $csere_html{'melyikt�ma'} = '<INPUT type="hidden" name="temanev" value="'.$temanev.'">';

   &htmloldal_kiiras_cserevel('forum_hozzaszolas.htm',%csere_html);
}
