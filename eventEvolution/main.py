import sys
sys.path.append('/usr/local/lib/python2.7/site-packages/')
sys.path
import glob
import os
import copy
import py2neo as pn
import pandas as pd
from EventUtil.util import *
from GraphBuilder.buildgraph import *
from EventExtractor.getevent import *
from StoryMatcher.matchstory import *


def main():
    dup_doc_files = glob.glob("/Users/zys/project/testDoc_dup_*.csv")
    dup_sen_files = glob.glob("/Users/zys/project/testSen_dup_*.csv")
    # sen_files = glob.glob("/Users/zys/project/Sen_*.csv")
    # doc_files = glob.glob("/Users/zys/project/Doc_*.csv")
    cv_files = glob.glob("/Users/zys/project/cv_doc_*.csv")

    db_graph = pn.Graph()
    all_res = []
    date_dict = ['20150328', '20150329', '20150330', '20150331', '20150401']

    for i in range(len(dup_doc_files)):
    # for i in range(2):
        res = {}
        cvterms = load_cv_doc(cv_files[i])
        # G = load_graph(dup_doc_files[i], dup_sen_files[i], 5, 3)		# network with equenvalent edge(node duplicated for different docs)
        # Gd = load_doc_graph(dup_doc_files[i], dup_doc_files[i], 5)		# pure keyword network (unique node)
        # Gs = loadSenGraph(sen_files[i])		# pure sentence co-occurence network (without doc level)

        print "building graph from: " + dup_doc_files[i] + " ..."
        G = load_graph(dup_doc_files[i], dup_sen_files[i], 5, 3, cvterms)

        best_par = get_partition(G, db_graph, 0.7, 0)

        outfile = "results/res_"+str(i+1)+".txt"
        out_doc_file = "results/res_doc"+str(i+1)+".txt"
        textlist = get_text_list(best_par)	    # for doc level type (docid, text)
        doclist = get_doc_list(best_par)
        res['keywords_set'] = textlist
        res['doc_set'] = doclist
        out_to_file(outfile, best_par)
        out_to_file(out_doc_file, res['doc_set'])
        # create event node in DB
        print "Adding events to database..."
        create_event_nodes(db_graph, best_par, res['doc_set'], date_dict[i])

        all_res.append(res)
        # textlist.append(best_par)	#for sentence level type (text only)

    window_size = 5
    stories = copy.deepcopy(all_res[0])
    stories['keywords_set'] = [{date_dict[0]: s} for s in stories['keywords_set']]
    stories['doc_set'] = [{date_dict[0]: s} for s in stories['doc_set']]

    out_to_file("results/doc_stories_text_day_0.txt", stories['keywords_set'])
    out_to_file("results/doc_stories_doc_day_0.txt", stories['doc_set'])
    create_initial_story_nodes(db_graph, stories['keywords_set'], date_dict[0])

    all_Gd = load_doc_graph(dup_doc_files, dup_sen_files, 5, 2)

    for i in range(1, window_size):
        print i
        processing = copy.deepcopy(all_res[i])
        # match_story_by_sen_edge(Gs, stories, processing , 0.001)
        match_story_by_doc_edge(all_Gd, db_graph, stories, processing, date_dict[i], 0.01)
        out_to_file("results/doc_stories_text_day_" + str(i) + ".txt", stories['keywords_set'])
        out_to_file("results/doc_stories_doc_day_" + str(i) + ".txt", stories['doc_set'])


if __name__ == "__main__":
    main()













