from readability import ParserClient
import urllib2
import re
import os
import zipfile
import csv
import StringIO
from sets import Set
import glob
from subprocess import call


def latest_file(rootfolder, extension=".export.CSV.zip"):
	return max(
			[filename[:-len(extension)]
			for dirname, dirnames, filenames in os.walk(rootfolder)
			for filename in filenames
			if filename.endswith(extension)])
	

def getEvents(startPath, outPutRoot, extension=".export.CSV.zip"):
	pageUrl = startPath + 'index.html'
	o = urllib2.Request(pageUrl);
	response = urllib2.urlopen(o);
	t=str(response.read())
	parse_re = re.compile('HREF="2015....\.export\.CSV\.zip"\>2015....\.export\.CSV\.zip')
	files = parse_re.findall(t)
	try:
		local_latest = latest_file(outPutRoot)
		print "latest file:" + local_latest
	except:
		print "No Local Files"
		local_latest = '20150430'

	count = 0
	fileToDownload = []
	for i in files:
		name = i.split('>')
		nameOnly = name[1][:-len(extension)]
		if nameOnly > local_latest:
			fileToDownload.append(name[1])
			count += 1
	print "Downloading %d new files..." %count
		

	for fi in fileToDownload:
		currentFile = startPath + fi
		print "Downloading: " + fi
		req=urllib2.Request(currentFile)
		try:
			f=urllib2.urlopen(req)
			local_file_name = outPutRoot + "/" + fi
			print local_file_name
			local_file = open(local_file_name,"wb")
			local_file.write(f.read())
			local_file.close()
		except:
			print "Cannot Download Files"

def need_extract(outPutRoot, extension, startTime, endTime):
	ret = [
		os.path.join(dirname, filename)
		for dirname, dirnames, filenames in os.walk(outPutRoot)
		for filename in filenames
		if filename.endswith(extension)
		if filename[:-len(extension)] > startTime and filename[:-len(extension)] <= endTime
		]
	return ret

def getSource(todayDay):
	# parser_client = ParserClient('b46403e44a56b3456f954ef452e46eed0f79f09b')

	# # startTime = latest_file(articleRoot)
	# startTime = "20150510"
	# files=need_extract(zipfileRoot,".export.CSV.zip",startTime)
	# for fi in files:
	# 	print fi
	# 	filehandle = open(fi, 'rb')
	# 	zf = zipfile.ZipFile(filehandle)
	# 	base = os.path.basename(fi)
	# 	pure_file_name = os.path.splitext(base)[0]

	# 	try:
	# 		data = StringIO.StringIO(zf.read(pure_file_name))
	# 	except KeyError:
	# 		print "cannot open %s in zip file" % pure_file_name
	# 	else:
	# 		todayDay = pure_file_name.split('.')[0]
	# 		todayRoot = articleRoot + "/" + todayDay
	# 		if not os.path.exists(todayRoot):
	# 				os.mkdir(todayRoot)

	num_exist_docs = len(glob.glob(todayRoot + "/*.html"))	
	print "num_exist_docs", num_exist_docs
	reader = csv.reader(data, delimiter = '\t')
	cnt = 0
	exist_urls = Set()
	for row in reader:
		# print len(row)
		eventID = row[0]
		eventDate = row[56]
		eventLoc = row[51]
		sourceUrl = row[57]

		write_file_name = todayRoot + "/" + eventID  + ".html"
		if os.path.exists(write_file_name) or os.path.exists(todayRoot + "/" + eventID  + ".xml"):
			cnt += 1
			# print "existing"
			continue
		if "US" not in row:
			# print "not US" 
			continue
		if sourceUrl in exist_urls: 
			# print "duplicate",sourceUrl
			continue

		# print sourceUrl
		parser_response = parser_client.get_article_content(sourceUrl)
		# print parser_response.content['content']
		if parser_response.content and 'content' in parser_response.content.keys():
			cnt += 1
			if cnt + num_exist_docs > 500: break
			exist_urls.add(sourceUrl)
			# print writing
			with open(write_file_name, "w") as writer:
				writer.write("<meta name = \"EVENTID\" content = \" " + eventID + "\" />" + '\n')
				writer.write("<meta name = \"DATE\" content = \" " + eventDate + "\"/>" + '\n')
				writer.write("<meta name = \"SOURCEURL\" content = \" " + sourceUrl + "\" />" + '\n')

				writer.write(parser_response.content['content'].encode("utf8") )


startPath = "http://data.gdeltproject.org/events/"
# outPutPath = "/Users/zys/projects/DynamicData/events"
# articleArchive = "/Users/zys/projects/DynamicData/article"
outPutPath = "/home/ysz/news_graph/events"
articleArchive = "/home/ysz/news_graph/article"

getEvents(startPath, outPutPath)

print "Downloading articles......"
parser_client = ParserClient('f25f302cab7c00da41e4f5f2c5b17428f60c97d5')
# startTime = latest_file(articleRoot)
startTime = "20150430"
endTime = "20150507"
files = need_extract(outPutPath, ".export.CSV.zip", startTime, endTime)
# print files
for fi in files:
	print "filename: ",fi
	filehandle = open(fi, 'rb')
	zf = zipfile.ZipFile(filehandle)
	base = os.path.basename(fi)
	pure_file_name = os.path.splitext(base)[0]
	try:
		data = StringIO.StringIO(zf.read(pure_file_name))
	except KeyError:
		print "cannot open %s in zip file" % pure_file_name
	else:
		todayDay = pure_file_name.split('.')[0]
		todayRoot = articleArchive + "/" + todayDay
		print todayRoot
		if not os.path.exists(todayRoot):
				os.mkdir(todayRoot)

	getSource(todayRoot)
	call(["sh", "process.sh", todayDay])





