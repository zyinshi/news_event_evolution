echo "Start to check and download updated articles..."
python downloadArticle.py

cd "/Users/zys/projects/DynamicData/article"
for timeday in */; do
cd $timeday
for file in *.html
do
  fbname=$(basename $file .html)
  newext=".xml"
  outfname=$fbname$newext
  tidy5 -file html2xml.log -asxml -numeric -quiet $file > $outfname
  tidy5 -file upper.log -q -xml -m -upper $outfname
done
echo "cleaning up html files..."
rm *.html

# echo "import by FinancialXMLParser"
# cd "/Users/zys/projects/DynamicData/FinancialXMLParsing-master"
# mvn -Dmaven.test.skip=true package 
# mvn exec:java -Dexec.mainClass="edu.ucsd.xmlparser.ParserMain" -Dexec.args=$timeday
# cd ..
done

