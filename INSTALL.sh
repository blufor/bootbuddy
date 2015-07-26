#!/bin/bash

INSTALL=`which install`
LN=`which ln`

$INSTALL -o root -g root -m 0755 lib/bb_wiz.rb /usr/local/lib/bootbuddy
$INSTALL -o root -g root -m 0775 -d /usr/local/lib/bootbuddy/uefi
$INSTALL -o root -g root -m 0775 lib/uefi/* /usr/local/lib/bootbuddy/uefi
$INSTALL -o root -g root -m 0644 bootbuddy.yaml /etc
$INSTALL -o root -g root -m 0750 bb /usr/local/sbin
$LN -nfs /usr/local/lib/bootbuddy/bb_wiz.rb /usr/local/bin/bb_wiz
