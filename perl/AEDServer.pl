#!/usr/bin/perl -w

# AEDServer in Perl, server-side
# Xenxin@Ufqi
# Wed Dec 27 20:40:28 CST 2017

use strict;
use warnings;
use utf8;
no warnings 'utf8';
binmode( STDIN,  ':encoding(utf8)' );
binmode( STDOUT, ':encoding(utf8)' );
binmode( STDERR, ':encoding(utf8)' );

use IO::Async::Listener;
use IO::Async::Loop;

my %serverConfig = (
		addr => {
			family => 'inet',
			socktype => 'stream',
			port => 8881,
			ip => '127.0.0.1',
			},
		#service  => "echo",
		#socktype => 'stream',
		);

my $loop = IO::Async::Loop->new;

# my $service = undef;

my $listener = IO::Async::Listener->new(
		on_stream => sub {
			my ( undef, $stream ) = @_;

			$stream->configure(
				on_read => sub {
					my ($self, $buffref, $eof) = @_;
					print "recv:[$$buffref]\n";
					#while( $$buffref =~ s/^(.*)\n// ) {
					while( $$buffref ne '') {
						print "recv:[$$buffref] from client.\n";
						$self->write( $$buffref );
						$$buffref = '';
					}
					$$buffref = "";
					return 0;
					},
				);

			$loop->add( $stream );

			},
		);

$loop->add( $listener );

$listener->listen(%serverConfig)->get; 

print "$loop is running....\n";
$loop->run;
