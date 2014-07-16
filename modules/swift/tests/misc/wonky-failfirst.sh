#!/bin/bash

cd ../language-behaviour

./generate-tc.data

cat tc.data ../sites/tc.data > tmp.tc.data.sites

SITE=wonky/wonky-failfirst.xml

echo testing site configuration: $SITE

export SWIFT_TEST_PARAMS="-sites.file ../sites/${SITE} -tc.file tmp.tc.data.sites"

./run 001-echo

