#!/usr/bin/env bash
timeday=$1
#ARTICLE="/home/ysz/news_graph/article"
ARTICLE="/Users/zys/project/article"
IMPORTTOOL="/home/ysz/FinancialXMLParsing"
cd $ARTICLE
cd $timeday
for file in *.html
do
  fbname=$(basename $file .html)
  newext=".xml"
  outfname=$fbname$newext
  tidy5 -file html2xml.log -asxml -numeric -quiet $file > $outfname
  tidy5 -file upper.log -q -xml -m -upper $outfname
done
# echo "cleaning up html files..."
# rm *.html

echo "import by FinancialXMLParser: " $timeday
cd $IMPORTTOOL
mvn -Dmaven.test.skip=true package 
mvn exec:java -Dexec.mainClass="edu.ucsd.xmlparser.ParserMain" -Dexec.args=$timeday
