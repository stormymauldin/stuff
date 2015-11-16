#!/usr/bin/perl -w

### Read test file and strip comments, empty lines and result lines.
### $ProjectHeader: use 2-1-0-release.1 Sun, 09 May 2004 13:57:11 +0200 mr $

use strict;

while (<>) {
    chomp;
    next if /^$/;		# skip empty lines
    next if /^#/;		# skip comments
    next if /^\*/;		# skip results

    print "$_\n";
}
