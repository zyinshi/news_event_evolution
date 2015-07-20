import sys

sys.path.append('/usr/local/lib/python2.7/site-packages/')
sys.path
import glob
from EventUtil.util import *
from GraphBuilder.buildgraph import *
from EventExtractor.getevent import *
from StoryMatcher.matchstory import *
import collections


def main():

    # DATA files (csv):
    #   dup_doc_file: "docid","name1","name2","weight"
    #   dup_sen_file: "docid","name1","name2","weight"
    #   cv_file: "docid","cvterm","freq"

    dup_doc_files = glob.glob("/home/ysz/news_graph/datafiles/Doc_dup_*.csv")
    dup_sen_files = glob.glob("/home/ysz/news_graph/datafiles/Sen_dup_*.csv")
    cv_files = glob.glob("/home/ysz/news_graph/datafiles/cv_doc_*.csv")
    window_size = 5     # window size for evolution discovery

    db_graph = pn.Graph()   # Initial DB connection, make sure Neo4J DB server is running

    # Fix Size Queue: window for evolution detection
    all_res = collections.deque(maxlen=window_size)
    local_files = collections.deque(maxlen=window_size)
    current_dates = collections.deque(maxlen=window_size)
    date_dict = [name.split('_')[-1].split('.')[0] for name in dup_doc_files]
    date_dict.sort()
    print date_dict

    stories = {'keywords_set': list(), 'doc_set': list()}
    global_index = 0
    for i in range(len(dup_doc_files)):
        res = {}
        # ############# Compute documents similarity by CValue term vector #############
        cvterms = load_cv_doc(cv_files[i])

        # ############# Build Duplicated Co-occurrence Graph ##########################
        print "building graph from: " + dup_doc_files[i] + " ..."
        # network with equenvalent edge(node duplicated for different docs)
        G = load_graph(dup_doc_files[i], dup_sen_files[i], 5, 3, cvterms)

        # Alternatives (discarded):
        # Gd = load_doc_graph([(dup_doc_files[i], dup_sen_files[i])], 5, 3, 2, 0.1)		# pure keyword network (unique node)
        # Gs = load_sen_graph(sen_files[i])		# pure sentence co-occurence network (without doc level)

        # ############# Extract Events for each time period ##########################
        best_par = get_partition(G, db_graph, 0.5)  # Louvian method to get event subgraphs iteratively
        # best_par = get_partition_by_cc(G, db_graph)   # Connected Components for cluster refine in each iteration - faster

        # # Output to txt files for record
        outfile = "results2/res_" + date_dict[i] + ".txt"
        out_doc_file = "results2/res_doc" + date_dict[i] + ".txt"

        textlist = get_text_list(best_par)  # for doc level type (docid, text)
        doclist = get_doc_list(best_par)
        res['keywords_set'] = textlist
        res['doc_set'] = doclist
        out_to_file(outfile, best_par)
        out_to_file(out_doc_file, res['doc_set'])

        # # Create event node in DB
        print "Adding events to database..."
        create_event_nodes(db_graph, best_par, res['doc_set'], date_dict[i])

        all_res.append(res)
        local_files.append((dup_doc_files[i], dup_sen_files[i]))
        current_dates.append(date_dict[i])

        # ############# Detect Evolution, build evolving dependencies ##########################
        print "matching stories..."
        current_window_Gd = load_doc_graph(local_files, 5, 10, 5, 0.05)     # build key graph within window

        if i == 0:
            # stories['keywords_set'].append([{current_dates[0]: s} for s in all_res[0]['keywords_set']])
            # stories['doc_set'].append([{current_dates[0]: s} for s in all_res[0]['doc_set']])
            global_index = create_initial_story_nodes(current_window_Gd, db_graph, all_res[0]['keywords_set'], date_dict[i], global_index)
        else:
            global_index = match_story_by_event_comp(current_window_Gd, db_graph, global_index+1, all_res, current_dates, 0.0008, 1, stories)



    # # stories based match - discarded

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













