#!/usr/bin/perl -w

### Read test file and generate file with expected output.
### $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $

use strict;

my $basename = shift;

while (<>) {
    chomp;
    next if /^$/;		# skip empty lines
    next if /^#/;		# skip comments
    
    if (/^\*(.*)/) {
	print "$1\n";
    } else {
	print "$basename.cmd> $_\n";
    }
}
