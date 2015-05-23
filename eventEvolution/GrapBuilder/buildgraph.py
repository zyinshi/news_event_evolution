__author__ = 'zys'

import networkx as nx
import csv
from collections import defaultdict
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

from EventUtil.util import compute_jaccard_sim



# alpha, weight of sentence level co-occurance; beta: weight of equivalent term
def load_graph(docFile, senFile, alpha, beta, cvterm):
    Gd = nx.Graph()
    with open(docFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if not l[1][0].isalnum() or not l[2][0].isalnum():
                continue
            Gd.add_node((l[0], l[1]))
            Gd.add_node((l[0], l[2]))
            Gd.add_edge((l[0], l[1]), (l[0], l[2]), weight=1.0)

    with open(senFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if not l[1][0].isalnum() or not l[2][0].isalnum():
                continue
            Gd[(l[0], l[1])][(l[0], l[2])]['weight'] += alpha * float(l[3]) / 2
            # Gd[(l[0],l[1])][(l[0],l[2])]['weight'] *= (float(l[3])+1) #if sen co, heavier weight for that edge (x 1.)

    add_equivalent(Gd, beta, cvterm)
    return Gd


def add_equivalent(Gd, beta, cvterm):
    jacSim = compute_jaccard_sim(Gd)
    for node in Gd.nodes():
        ph = node[1]
        doc_id = node[0]
        for target in Gd.nodes():
            if target != node and ph == target[1]:
                target_doc_id = target[0]
                if doc_id in cvterm and target_doc_id in cvterm and cvterm[doc_id][target_doc_id] != 0:
                    Gd.add_edge(node, target, weight=beta * (1 + jacSim[(doc_id, target_doc_id)]))
                else:
                    Gd.add_edge(node, target, weight=jacSim[(doc_id, target_doc_id)])



def load_doc_graph(docFile, senFile, alpha):
    Gd = nx.Graph()
    term_freq = defaultdict(int)

    with open(docFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if not l[1][0].isalnum() or not l[2][0].isalnum():
                continue
            Gd.add_node(l[1])
            Gd.add_node(l[2])
            Gd.add_edge(l[1], l[2], weight=float(l[3]) * 4)  # a,b and b,a
            term_freq[l[1]] += float(l[3])
            term_freq[l[2]] += float(l[3])

    with open(senFile, 'rb') as inf:
        next(inf, '')
        fi = csv.reader(inf, skipinitialspace=True)
        for l in fi:
            if not l[1][0].isalnum() or not l[2][0].isalnum():
                continue
            # Gd[l[1]][l[2]]['weight'] += alpha * float(l[3])
            Gd[l[1]][l[2]]['weight'] *= (1 + float(l[3]))

    # compute edge weight
    # w = d_ab * s_ab / (d_a * d_b)
    for u, v, d in Gd.edges(data=True):
        d['weight'] /= (term_freq[u] * term_freq[v])
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


def load_cv(cvFile, beta):
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