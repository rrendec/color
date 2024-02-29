#!/bin/bash

dir=$(dirname "$(readlink -f "$0")")
exec appletviewer "$dir/color.html"
