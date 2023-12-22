#!/bin/bash

git add .
# 英語で日付を出力
git commit -m "`date +'%b %d, %Y'`"

git push origin main
