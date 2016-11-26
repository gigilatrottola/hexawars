#!/usr/bin/perl

use DBI;

print "Content-type: text/html\n\nCreate forum script<br>";

$dbh = DBI->connect('DBI:mysql:???;host=localhost', '???', '???');

if (!defined $dbh)
{
   die "Nem siker�lt csatlakozni a delsziget adatb�zishoz: ::errstr\n";
}
else
{
   print "Sikeres csatlakoz�s a delsziget adatb�zishoz!\n\n";
}

#$sql1="CREATE TABLE `forum` (`temanev` varchar(40) NOT NULL default '',`hozzaszolas` text NOT NULL,`idopont` datetime default NULL,`login` varchar(10) NOT NULL default '',`nev` varchar(20) NOT NULL default '',`remoteaddr` varchar(40) default NULL,`remotehost` varchar(40) default NULL,`remoteident` varchar(40) default NULL,`remoteuser` varchar(40) default NULL,`tipus` int(11) NOT NULL default '0',KEY `temanev` (`temanev`,`idopont`))";
#$sql2="CREATE TABLE forum_account (nev varchar(50) NOT NULL,jelszo varchar(50) NOT NULL,email varchar(100) NOT NULL)";

#$rv = $dbh->do($sql1);
#if (!defined $rv) { print "forum tabla l�trehoz�sa nem siker�lt!\n"; print "Hibaok: ".$dbh->errstr."\n"; } 
#else { print "Sikeres forum tabla l�trehoz�s.\n"; }

#$rv = $dbh->do($sql2);
#if (!defined $rv) { print "forum_account tabla l�trehoz�sa nem siker�lt!\n"; print "Hibaok: ".$dbh->errstr."\n"; } 
#else { print "Sikeres forum_account tabla l�trehoz�s.\n"; }

$sql = 'ALTER TABLE forum_account ADD activation_code varchar(16) NOT NULL';
$rv = $dbh->do($sql);
if (!defined $rv) { print "nem siker�lt!\n"; print "Hibaok: ".$dbh->errstr."\n"; } 
else { print "Sikeres muvelet.\n"; }

###########################################################################
$dbh->disconnect; print "\nDisconnected.\n";



