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
import collections


def main():
    dup_doc_files = glob.glob("/Users/zys/experimentdata/Doc_dup_*.csv")
    dup_sen_files = glob.glob("/Users/zys/experimentdata/Sen_dup_*.csv")
    cv_files = glob.glob("/Users/zys/experimentdata/cv_doc_*.csv")
    # dup_doc_files = glob.glob("/home/ysz/news_graph/datafiles/Doc_dup_*.csv")
    # dup_sen_files = glob.glob("/home/ysz/news_graph/datafiles/Sen_dup_*.csv")
    # cv_files = glob.glob("/home/ysz/news_graph/datafiles/cv_doc_*.csv")
    window_size = 5

    db_graph = pn.Graph()
    all_res = collections.deque(maxlen=window_size)
    local_files = collections.deque(maxlen=window_size)
    current_dates = collections.deque(maxlen=window_size)
    # date_dict = ['20150328', '20150329', '20150330', '20150331', '20150401']
    date_dict = [name.split('_')[-1].split('.')[0] for name in dup_doc_files]
    date_dict.sort()
    print date_dict

    stories = {'keywords_set': list(), 'doc_set': list()}
    global_index = 0
    # for i in range(len(dup_doc_files)):
    for i in range(1):
        res = {}

        cvterms = load_cv_doc(cv_files[i])
        # G = load_graph(dup_doc_files[i], dup_sen_files[i], 5, 3)		# network with equenvalent edge(node duplicated for different docs)
        # Gd = load_doc_graph(dup_doc_files[i], dup_doc_files[i], 5)		# pure keyword network (unique node)
        # Gs = loadSenGraph(sen_files[i])		# pure sentence co-occurence network (without doc level)

        print "building graph from: " + dup_doc_files[i] + " ..."
        G = load_graph(dup_doc_files[i], dup_sen_files[i], 5, 3, cvterms)
        # current_Gd = load_doc_graph([(dup_doc_files[i], dup_sen_files[i])], 5, 3, 2, 0.1)

        # best_par = get_partition(G, db_graph, 0.5)
        best_par = get_partition_by_cc(G, db_graph)

        outfile = "results2/res_" + date_dict[i] + ".txt"
        out_doc_file = "results2/res_doc" + date_dict[i] + ".txt"
        # outfile = "/home/ysz/news_graph/result/res_" + date_dict[i] + ".txt"
        # out_doc_file = "/home/ysz/news_graph/result/res_doc_" + date_dict[i] + ".txt"

        textlist = get_text_list(best_par)  # for doc level type (docid, text)
        doclist = get_doc_list(best_par)
        res['keywords_set'] = textlist
        res['doc_set'] = doclist
        out_to_file(outfile, best_par)
        out_to_file(out_doc_file, res['doc_set'])

        # create event node in DB
        print "Adding events to database..."
        create_event_nodes(db_graph, best_par, res['doc_set'], date_dict[i])

        all_res.append(res)
        local_files.append((dup_doc_files[i], dup_sen_files[i]))
        current_dates.append(date_dict[i])

        print "matching stories..."
        current_window_Gd = load_doc_graph(local_files, 5, 10, 5, 0.05)

        if i == 0:
            # stories['keywords_set'].append([{current_dates[0]: s} for s in all_res[0]['keywords_set']])
            # stories['doc_set'].append([{current_dates[0]: s} for s in all_res[0]['doc_set']])
            global_index = create_initial_story_nodes(current_window_Gd, db_graph, all_res[0]['keywords_set'], date_dict[i], global_index)
        else:
            global_index = match_story_by_event_comp(current_window_Gd, db_graph, global_index+1, all_res, current_dates, 0.0008, 1, stories)

    # out_to_file("results2/doc_stories_text.txt", stories['keywords_set'])
    # out_to_file("results2/doc_stories_doc.txt", stories['doc_set'])

    # # stoies based match
    # stories = copy.deepcopy(all_res[0])
    # stories['keywords_set'] = [{current_dates[0]: s} for s in stories['keywords_set']]
    # stories['doc_set'] = [{current_dates[0]: s} for s in stories['doc_set']]
    # out_to_file("results/doc_stories_text_day_0.txt", stories['keywords_set'])
    # out_to_file("results/doc_stories_doc_day_0.txt", stories['doc_set'])
    # create_initial_story_nodes(db_graph, stories['keywords_set'], current_dates[0])
    #
    # for i in range(1, window_size):
    # print i
    #     processing = copy.deepcopy(all_res[i])
    #     # match_story_by_sen_edge(Gs, stories, processing , 0.001)
    #     match_story_by_doc_edge(current_Gd, db_graph, stories, processing, date_dict[i], 0.01)
    #     out_to_file("results/doc_stories_text_day_" + str(i) + ".txt", stories['keywords_set'])
    #     out_to_file("results/doc_stories_doc_day_" + str(i) + ".txt", stories['doc_set'])


if __name__ == "__main__":
    main()













