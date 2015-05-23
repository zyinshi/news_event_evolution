__author__ = 'zys'

import networkx as nx
import csv
from collections import defaultdict
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd

from EventUtil.util import compute_jaccard_sim



# alpha, weight of sentence level co-occurance; beta: weight of equivalent term
def load_graph(docFile, senFile, alpha, beta, cvterm):
    Gd = nx.Graph()
    nuissians = "Associated Press"
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


def add_equivalent(Gd, beta, cvterm):
    jacSim = compute_jaccard_sim(Gd)
    for node in Gd.nodes():
        ph = node[1]
        docid = node[0]
        for target in Gd.nodes():
            if target != node and ph == target[1]:
                target_docid = target[0]
                Gd.add_edge(node, target, weight=beta * (1 + jacSim[(docid, target_docid)]))


# if docid in cvterm and target_docid in cvterm and  cvterm[docid][target_docid] != 0:
# 					Gd.add_edge(node,target, weight = beta * (1 + jacSim[(docid, target_docid)]) )
# 				else:
# 					Gd.add_edge(node,target, weight = beta * jacSim[(docid, target_docid)])


def load_doc_graph(dup_doc_files, dup_sen_files, alpha, noise_tau):
    Gd = nx.Graph()
    term_freq = defaultdict(set)

    for i in range(len(dup_doc_files)):
        with open(dup_doc_files[i], 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                if not l[1][0].isalnum() or not l[2][0].isalnum():
                    continue
                Gd.add_node(l[1].lower())
                Gd.add_node(l[2].lower())
                if Gd.has_edge(l[1].lower(), l[2].lower()):
                    Gd[l[1].lower()][l[2].lower()]['weight'] += 1
                else:
                    # Gd.add_edge(l[1].lower(), l[2].lower(), weight=float(l[3])/2)  # a,b and b,a
                    Gd.add_edge(l[1].lower(), l[2].lower(), weight=1)
                term_freq[l[1].lower()].add(l[0])
                term_freq[l[2].lower()].add(l[0])

        with open(dup_sen_files[i], 'rb') as inf:
            next(inf, '')
            fi = csv.reader(inf, skipinitialspace=True)
            for l in fi:
                if not l[1][0].isalnum() or not l[2][0].isalnum():
                    continue
                # Gd[l[1]][l[2]]['weight'] += alpha * float(l[3])
                Gd[l[1].lower()][l[2].lower()]['weight'] += alpha * float(l[3]) / 2

    # compute edge weight
    # w = d_ab * s_ab / (d_a * d_b)
    noises = set(k for k, v in term_freq.iteritems() if len(v) < noise_tau)  # prune
    Gd.remove_nodes_from(noises)
    #     print term_freq['democratic south bend mayor pete buttigieg'],Gd['democratic south bend mayor pete buttigieg']
    for u, v, d in Gd.edges(data=True):
        d['weight'] = float(d['weight']) / (len(term_freq[u]) * len(term_freq[v]))

    return Gd


def load_sen_graph(senFile):
    Gd = nx.Graph()
    with open(senFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if not l[1][0].isalnum() or not l[2][0].isalnum():
                continue
            Gd.add_node(l[1])
            Gd.add_node(l[2])
            Gd.add_edge(l[1], l[2], weight=float(l[3]))

    return Gd


def load_cv_graph(cvFile, beta):
    G = nx.Graph()
    with open(cvFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if l[1][:4] == 'year' or l[1][:4] == 'week': continue
            if l[2][:4] == 'year' or l[2][:4] == 'week': continue
            G.add_node((l[0], l[1]))
            G.add_node((l[0], l[2]))
            G.add_edge((l[0], l[1]), (l[0], l[2]), weight=float(l[3]))

    for node in G.nodes():
        ph = node[1]
        for target in G.nodes():
            if target != node and ph == target[1]:
                G.add_edge(node, target, weight=beta)
    return G


def load_cv_doc(cvFile):
    cvDict = defaultdict(defaultdict)
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