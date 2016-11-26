#!/usr/bin/perl

use DBI;

use strict;
use Mail::Sendmail; # aktivációs kód küldéshez kell


use vars qw($delszigetH);

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

sub checkInput
{
	my $input = shift;
	if ($input =~ /[^\w\d]+/i) { return 0; }
	if ( (length($input)<1) or (length($input)>50) ) { return 0; }
	return 1;
}

# http://aspn.activestate.com/ASPN/Cookbook/Rx/Recipe/68432
sub ValidEmailAddr { #check if e-mail address format is valid
	my $mail = shift;                                                  #in form name@host
	return 0 if ( $mail !~ /^[0-9a-zA-Z\.\-\_]+\@[0-9a-zA-Z\.\-]+$/ ); #characters allowed on name: 0-9a-Z-._ on host: 0-9a-Z-. on between: @
	return 0 if ( $mail =~ /^[^0-9a-zA-Z]|[^0-9a-zA-Z]$/);             #must start or end with alpha or num
	return 0 if ( $mail !~ /([0-9a-zA-Z]{1})\@./ );                    #name must end with alpha or num
	return 0 if ( $mail !~ /.\@([0-9a-zA-Z]{1})/ );                    #host must start with alpha or num
	return 0 if ( $mail =~ /.\.\-.|.\-\..|.\.\..|.\-\-./g );           #pair .- or -. or -- or .. not allowed
	return 0 if ( $mail =~ /.\.\_.|.\-\_.|.\_\..|.\_\-.|.\_\_./g );    #pair ._ or -_ or _. or _- or __ not allowed
	return 0 if ( $mail !~ /\.([a-zA-Z]{2,3})$/ );                     #host must end with '.' plus 2 or 3 alpha for TopLevelDomain (MUST be modified in future!)
	return 1;
}

sub intRand
{
   my $min = $_[0];
   my $max = $_[1];
   my $r = $min + rand($max+1-$min);
   return int($r);
}

sub main
{
	my %in=&getQuery; # összes bemeneti adat ebbe kerül
	my %csere_html;
	if ( (defined $in{'username'}) and (defined $in{'password'}) and (defined $in{'email'}) )
	{
		my $username = $in{'username'};
		my $password = $in{'password'};
		my $email    = $in{'email'};
		if (!checkInput($username)) { $csere_html{'uzenetek'} = '<font color="red"><b> Invalid username! </b></font>'; }
		elsif (!checkInput($password)) { $csere_html{'uzenetek'} = '<font color="red"><b> Invalid password! </b></font>'; }
		elsif (!ValidEmailAddr($email)) { $csere_html{'uzenetek'} = '<font color="red"><b> Invalid e-mail address! </b></font>'; }
		else
		{
			$delszigetH = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');
			if (!defined $delszigetH) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			my $rv = $delszigetH->do("LOCK TABLES forum_account WRITE");
			if (!defined $rv) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			my $sth = $delszigetH->prepare("SELECT nev FROM forum_account WHERE nev=?");
			if (!defined $sth) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			if (!defined $sth->execute($username)) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			my ($nev);
			if (($nev)=$sth->fetchrow_array) { $csere_html{'uzenetek'} = '<font color="red"><b> This username is already in use, please choose another. </b></font>'; }
			else
			{
				my $aktivacios_kod = &intRand(0,1000000000); # 9 jegyû szám (általában)
				my $targy = 'Hexawars forum activation code';
				my $uzenet = 'Click on the following URL to activate your account: http://www.hexawars.com/cgi-bin/forum_create_acc.pl?username='.$username.'&activation_code='.$aktivacios_kod;
				my $mailszerver = "localhost";
				my %mail = ('To'      => $email,
							'From'    => 'HexaWars',
							'Subject' => $targy,
							'Message' => $uzenet,
							'smtp'    => $mailszerver );
				if (!&sendmail(%mail))
				{
					$csere_html{'uzenetek'} = 'Error sending the mail: '.$Mail::Sendmail::error;
				}
				else
				{
					$rv = $delszigetH->do("INSERT INTO forum_account SET nev='".$username."', jelszo='".$password."', activation_code='".$aktivacios_kod."'");
					if (!defined $rv) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
					$csere_html{'uzenetek'} = '<font color="red"><b> Your account has been created, you can activate it by clicking on the link in the e-mail sent.</b></font>';
				}
			}
			$rv = $delszigetH->do("UNLOCK TABLES");
			if (!defined $rv) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			$delszigetH->disconnect;
		}
	}
	elsif ( (defined $in{'username'}) and (defined $in{'activation_code'}) )
	{
		my $username = $in{'username'};
		my $aktivacios_kod = $in{'activation_code'};
		if (!checkInput($username)) { $csere_html{'uzenetek'} = '<font color="red"><b> Invalid username! </b></font>'; }
		elsif (!checkInput($aktivacios_kod)) { $csere_html{'uzenetek'} = '<font color="red"><b> Invalid activation code! </b></font>'; }
		else
		{
			$delszigetH = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');
			if (!defined $delszigetH) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			my $rv = $delszigetH->do("UPDATE forum_account SET activation_code='' WHERE nev='$username' AND activation_code='$aktivacios_kod'");
			if (!defined $rv) { print "Content-type: text/html\n\nAdatbázis hiba: $DBI::errstr\n"; exit; }
			if ($rv<1) { $csere_html{'uzenetek'} = '<font color="red"><b> Wrong activation code or username or account already activated.</b></font>'; }
			else { $csere_html{'uzenetek'} = '<font color="red"><b> Account activated, <A href="http://www.hexawars.com/cgi-bin/forum_main.pl">go to topic list</A>. </b></font>'; }
			$delszigetH->disconnect;
		}
	}
	else
	{
		$csere_html{'uzenetek'} = 'An activation code will be sent to the specified e-mail address. Click on the link in the e-mail to activate the user account.';
	}
	&htmloldal_kiiras_cserevel('forum_create_acc.htm',%csere_html);
}

###########################################################################
### program kezdete:
&main;
