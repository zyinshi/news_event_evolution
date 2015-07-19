__author__ = 'zys'
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
import time

def load_cv_doc_batch(cvFiles):
    cvDict = defaultdict(defaultdict)
    for i in range(len(cvFiles)):
        cvFile = cvFiles[i]
        with open(cvFile, 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                cvDict[l[0]][l[1].lower()] = float(l[2])
    df = pd.DataFrame(cvDict).T.fillna(0)
    map_row = list(df.index)
    map_col = list(df.columns.values)
    tfidf = TfidfTransformer(norm=u'l2', use_idf=True, smooth_idf=True, sublinear_tf=False)
    data = tfidf.fit_transform(df.values)
    terms_weight = pd.DataFrame(data.todense())
    doc_cos_sim_on_cv = cosine_similarity(terms_weight)
    docTerms = pd.DataFrame(doc_cos_sim_on_cv, index=map_row, columns=map_row)
    # 	docTerms.to_sparse()
    docTerms = docTerms.T.to_dict()
    return docTerms

def load_graph_batch(docFiles, senFiles, alpha, beta, cvterm):
    Gd = nx.Graph()
    nuissians = "Associated Press"

    for i in range(len(docFiles)):
        docFile = docFiles[i]
        senFile = senFiles[i]
        with open(docFile, 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                if not l[1][0].isalnum() or not l[2][0].isalnum() or nuissians in l[1] or nuissians in l[2]:
                    continue
                Gd.add_node((l[0], l[1].lower()))
                Gd.add_node((l[0], l[2].lower()))
                Gd.add_edge((l[0], l[1].lower()), (l[0], l[2].lower()), weight=1.0)

        with open(senFile, 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                if not l[1][0].isalnum() or not l[2][0].isalnum() or nuissians in l[1] or nuissians in l[2]: continue
                Gd[(l[0], l[1].lower())][(l[0], l[2].lower())]['weight'] += alpha * float(l[3]) / 2
                # Gd[(l[0],l[1])][(l[0],l[2])]['weight'] *= (float(l[3])+1) #if sen co, heavier weight for that edge (x 1.)

    add_equivalent(Gd, beta, cvterm)
    return Gd


def load_doc_graph_batch(docFiles, senFiles, alpha, noise_tau, noise_edge_tau, weight_tau):
    Gd = nx.Graph()
    term_freq = defaultdict(set)
#     term_itidf = defaultdict()

    for i in range(len(docFiles)):
        dup_doc_files = docFiles[i]
        dup_sen_files = senFiles[i]
        with open(dup_doc_files, 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                # print l[0]
                if not l[1][0].isalnum() or not l[2][0].isalnum():
                    continue
                Gd.add_node(l[1].lower())
                Gd.add_node(l[2].lower())
                if Gd.has_edge(l[1].lower(), l[2].lower()):
                    Gd[l[1].lower()][l[2].lower()]['weight'] += 1
                    Gd[l[1].lower()][l[2].lower()]['doc'].add(l[0])
                else:
#                     Gd.add_edge(l[1].lower(), l[2].lower(), weight=float(l[3])/2)  # a,b and b,a
                    Gd.add_edge(l[1].lower(), l[2].lower(), weight=1, doc=set())
                    Gd[l[1].lower()][l[2].lower()]['doc'].add(l[0])
#                 term_itidf[l[1].lower()] += float(l[3])
#                 term_itidf[l[2].lower()] += float(l[3])
                term_freq[l[1].lower()].add(l[0])
                term_freq[l[2].lower()].add(l[0])

        with open(dup_sen_files, 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                if not l[1][0].isalnum() or not l[2][0].isalnum():
                    continue
                # Gd[l[1]][l[2]]['weight'] += alpha * float(l[3])
                Gd[l[1].lower()][l[2].lower()]['weight'] += alpha * float(l[3])/2
    # compute edge weight
    # w = d_ab * s_ab / (d_a * d_b)
    noises = set(k for k, v in term_freq.iteritems() if len(v) < noise_tau) # prune
    Gd.remove_nodes_from(noises)
    for e in Gd.edges(data=True):
#         print e[2]['weight']
        if e[2]['weight'] < noise_edge_tau:
            Gd.remove_edge(e[0], e[1])
    small_deg = [node for node, degree in Gd.degree().items() if degree < 3]
    Gd.remove_nodes_from(small_deg)

    for u, v, d in Gd.edges(data=True):
        d['weight'] = float(d['weight']) / (len(term_freq[u]) * len(term_freq[v]))
#         print u,v,d
#         print d['weight']
        if d['weight'] < weight_tau:
            Gd.remove_edge(u, v)
    return Gd


def main():
    dup_doc_files = glob.glob("/Users/zys/project/Doc_dup_test.csv")
    dup_sen_files = glob.glob("/Users/zys/project/Sen_dup_test.csv")
    cv_files = glob.glob("/Users/zys/project/cv_doc_test.csv")

    db_graph = pn.Graph()
    res = {}

    cvterms = load_cv_doc_batch(cv_files[:3])
    # for w in [0.1,0.3,0.4,0.5,0.6,0.7,0.8,0.9]:
    for w in [0.5]:
        start = time.time()
        G = load_graph_batch(dup_doc_files[:3], dup_sen_files[:3], 1, 5, cvterms)
        print("--- building : %s seconds ---" % (time.time() - start))
        start = time.time()
        best_par = get_partition_by_cc(G, db_graph, w)
        print("--- event : %s seconds ---" % (time.time() - start))

        outfile = "/Users/zys/project/res_test_"+str(w)+"_5.txt"
        out_doc_file = "/Users/zys/project/res_doc_test"+str(w)+".txt"
        textlist = get_text_list(best_par)  # for doc level type (docid, text)
        doclist = get_doc_list(best_par)
        res['keywords_set'] = textlist
        res['doc_set'] = doclist
        out_to_file(outfile, best_par)
        out_to_file(out_doc_file, res['doc_set'])

    #
    # for w in [0.1,0.3,0.5,0.7,0.9]:
    # Gd = load_doc_graph_batch(dup_doc_files, dup_sen_files, 1, 0, 3, 0.5)
    # cc = nx.connected_component_subgraphs(Gd)
    # print "detected: %s cc", nx.number_connected_components(Gd)
    #
    # cc_cluster = []
    # for cg in cc:
    #     if cg.number_of_edges() == 0: continue
    #     ds = set()
    #     for e in cg.edges(data=True):
    #         print e
    #         docs = e[2]['doc']
    #         for d in docs:
    #             ds.add(d)
    #     cc_cluster.append(ds)
    # out_to_file("/Users/zys/project/res_cc"+str(30.5)+".txt", cc_cluster)



if __name__ == "__main__":
    main()