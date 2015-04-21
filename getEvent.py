import community
import networkx as nx
import csv
import glob
import os
import numpy as np
import nltk, string
from collections import Counter
from collections import Set
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.metrics.pairwise import cosine_similarity
import copy

# alpha: weight of sentence level co-occurance; beta: weight of equivalent term
def loadGraph(docFile, senFile, alpha, beta):	
	Gd=nx.Graph()    
	with open(docFile, 'rb') as inf:
		next(inf, '')
		fi = csv.reader(inf , skipinitialspace=True)
		for l in fi:
			if not l[1][0].isalnum() or not l[2][0].isalnum(): continue
			Gd.add_node((l[0],l[1]))
			Gd.add_node((l[0],l[2]))
			Gd.add_edge((l[0],l[1]),(l[0],l[2]),weight = float(l[3]))
	
	with open(senFile, 'rb') as inf:
		next(inf, '')
		fi = csv.reader(inf , skipinitialspace=True)
		for l in fi:
			if not l[1][0].isalnum() or not l[2][0].isalnum(): continue
			Gd[(l[0],l[1])][(l[0],l[2])]['weight'] += alpha * float(l[3])
	
	for node in Gd.nodes():
		ph = node[1]
		for target in Gd.nodes():
			if target!=node and ph==target[1]:
				Gd.add_edge(node,target, weight = beta)
	return Gd

def loadSenGraph(senFile, alpha):	
	Gd=nx.Graph()    
	with open(senFile, 'rb') as inf:
		next(inf, '')
		fi = csv.reader(inf , skipinitialspace=True)
		for l in fi:
			if not l[1][0].isalnum() or not l[2][0].isalnum(): continue
			Gd.add_node(l[1])
			Gd.add_node(l[2])
			Gd.add_edge(l[1],l[2],weight = float(l[3]))
	
	return Gd

def loadCV(cvFile):
	G = nx.Graph()
	with open(cvFile, 'rb') as inf:
		next(inf, '')
		fi = csv.reader(inf , skipinitialspace=True)
		for l in fi:
			if l[1][:4]=='year' or l[1][:4]=='week': continue
			if l[2][:4]=='year' or l[2][:4]=='week': continue			
			G.add_node((l[0],l[1]))
			G.add_node((l[0],l[2]))
			G.add_edge((l[0],l[1]),(l[0],l[2]), weight = float(l[3]))

	for node in G.nodes():
		ph = node[1]
		for target in G.nodes():
			if target!=node and ph==target[1]:
				Gd.add_edge(node,target, weight = beta)
	return G

def get_list(partition):
	size = float(len(set(partition.values())))
	result = []
	count = 0.
	for com in set(partition.values()) :
		count = count + 1.
		list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
		result.append(list_nodes)
	return result

def getPartition(G, tau, mode = 1): # mode{1: depends on threshold 2: highest level; 3: all sub;}
	partition = community.best_partition(G)
	result = get_list(partition)
	coms = []
	for sub in result:
		H = G.subgraph(sub)
		coms.append(H)
	print "Detected ", len(coms), " communities."
	# Further division
	final_res = []
	cnt = 0
	total = 0
	if mode==2:
		print "Detected ", len(coms), " communities on best partition"
		# r = []
		# r.append(result)
		return result

	for c in coms:
		cnt += 1
		# print "Partition ", cnt, " level of dendogram with ", c.number_of_nodes(), " nodes"
		best = community.best_partition(c)
		subcnt = len(set(best.values()))
		mod = community.modularity(best, c)
		if mode == 3:
			l = get_list(best)
			final_res.extend(l)
			total += len(l)			
			
		elif mode == 1:
			if mod >= tau:
				l = get_list(best)
				final_res.extend(l)
				total += len(l)			
			else:
				final_res.append(c.nodes())
				total += 1
	print "Detected ", total, " communities in total on further sub partition"
	return final_res


def outToFile(outputFile, result):
	f = open(outputFile, "w")
	for item in result:
		print>>f, item
		# print>>f,'\n'

def getTextList(partition):
	textlist = []
	for sub in partition:
		textlist.append([node[1] for node in sub])
	return textlist

def getDocList(partition):
	doclist = []
	for sub in partition:
		doclist.append(set([node[0] for node in sub]))
	return doclist


def cosSim(baseSet, target):
	vectorizer = DictVectorizer()
	cos_sim = []
	base = [Counter(res) for res in baseSet]
	for i in range(len(target)):		
		keywords = [Counter(target[i])]
		keywords.extend(base)
		vec = vectorizer.fit_transform(keywords)
		sim = cosine_similarity(vec[0], vec[1:])
# 		print sim.shape
		cos_sim.append(sim)
	# return ((tfidf * tfidf.T).A)[0,1]
# 	print cos_sim[0].shape
	return cos_sim

def matchStory(stories, target, tau):
	existing = copy.deepcopy(stories['keywords_set'])
	match_target = copy.deepcopy(target['keywords_set'])
	comp = cosSim(existing, match_target)
	
	for i in range(len(comp)):
		match_score = np.max(comp[i])
		if match_score < tau:
			stories['keywords_set'].append(target['keywords_set'][i])
			stories['doc_set'].append(target['doc_set'][i])
			continue
		match_ind = np.argmax(comp[i])
		print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# 		match_text = existing[match_ind]
		stories['keywords_set'][match_ind].extend(match_target[i])
		u = stories['doc_set'][match_ind].union(target['doc_set'][i])
		stories['doc_set'][match_ind] = u
	
	

def main():
	doc_files = glob.glob("/Users/zys/project/testDoc_dup_*.csv")
	sen_files = glob.glob("/Users/zys/project/testSen_dup_*.csv")
	all_res = []
	for i in range(len(doc_files)):
		res = {}
		G = loadGraph(doc_files[i], sen_files[i], 5, 3)
		# G = loadSenGraph(sen_files[i], 5)
		best_par = getPartition(G, 0.6, 1)
		outfile = os.path.dirname(doc_files[i])+"/res_"+str(i+1)+".txt"
		outToFile(outfile, best_par)
		textlist = getTextList(best_par)	#for doc level type (docid, text)
		doclist = getDocList(best_par)
		res['keywords_set'] = textlist
		res['doc_set'] = doclist
		all_res.append(res)
		# textlist.append(best_par)	#for sentence level type (text only)
	windowSize = 5
	stories = copy.deepcopy(all_res[0])
	for i in range(1,windowSize):
	    print i
	    processing = copy.deepcopy(all_res[i])
	    matchStory(stories, processing , 0.3)
	
	outToFile("stories_text.txt", stories['keywords_set'])
	outToFile("stories_doc.txt", stories['doc_set'])



if __name__ == "__main__":
	main()













