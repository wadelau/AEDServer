#!/usr/bin/perl -w

# AEDServer in Perl, client-side
# Xenxin@Ufqi
# Wed Dec 27 20:40:28 CST 2017

use strict;
use warnings;
use utf8;
no warnings 'utf8';
binmode( STDIN,  ':encoding(utf8)' );
binmode( STDOUT, ':encoding(utf8)' );
binmode( STDERR, ':encoding(utf8)' );

use IO::Async::Stream;
use IO::Async::Loop;

my $loop = IO::Async::Loop->new;

$loop->connect(
		host     => "localhost",
		service  => 8881,
		socktype => 'stream',

		on_stream => sub {
			my ( $stream ) = @_;

			$stream->configure(
				on_read => sub {
					my ( $self, $buffref, $eof ) = @_;
					print "recv:[$$buffref]\n";
					while( $$buffref =~ s/^(.*)\n// ) {
						print "Received a line [$1] from server.\n";
					}
					$$buffref = '';
					return 0;
					},	
				);

			$stream->write( "An initial line here from client, 2nd.\n" );

			$loop->add( $stream );
			
			# read from STDIN
			$loop->add( IO::Async::Stream->new_for_stdin(
				on_read => sub {
						my ( $self, $buffref, $eof ) = @_;
						while( $$buffref =~ s/^(.*)\n// ) {
							print "You typed a line [$1]\n";
							$stream->write($1);
							$$buffref = '';
						}
						return 0;
					},

				) 
			);

			},

		on_resolve_error => sub { die "Cannot resolve - $_[-1]\n"; },
		on_connect_error => sub { die "Cannot connect - $_[0] failed $_[-1]\n"; },

		);

print "connecting....\n";
$loop->run;
