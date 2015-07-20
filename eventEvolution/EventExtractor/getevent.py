__author__ = 'zys'

import community
import networkx as nx
import nltk
import string
from collections import Counter
from collections import Set
from collections import defaultdict
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import copy
import py2neo as pn
import time


def query_related_text(db_graph, node, target):
    docid1, docid2 = node[0], target[0]
    phrase1, phrase2 = node[1], target[1]
    query_statement = "match (d: Document)-[:HAS_SENTENCE]->(s: Sentence)-[:HAS_NAMEENTITY]->(ne: NameEntityPhraseNode), (s: Sentence)<-[:HAS_CHILD]-(t: Text)\
                        where id(d)= toInt({DID}) and LOWER(ne.phrase) = {PHRASE} \
                        with t \
                        match (t)-[:HAS_CHILD]->(s2: Sentence)\
                        return s2.text"
    sentences1 = db_graph.cypher.execute(query_statement, {"PHRASE": phrase1, "DID": docid1})
    sentences2 = db_graph.cypher.execute(query_statement, {"PHRASE": phrase2, "DID": docid2})
    return sentences1, sentences2


# Refine Equivalent edges by similarity between expression(sentences) surrounding two entities
def sentense_sim(sentencelist1, sentencelist2, phrase1, phrase2):
    stemmer = nltk.stem.porter.PorterStemmer()
    token_dict = {}
    def tokenize(text):
        tokens = nltk.word_tokenize(text)
        filtered = [w.rstrip(string.punctuation) for w in tokens if w not in nltk.corpus.stopwords.words('english')]
        stemmed = [stemmer.stem(item) for item in filtered]
        return stemmed

    corpus1 = " ".join([s[0].lower() for s in sentencelist1])
    corpus2 = " ".join([s[0].lower() for s in sentencelist2])

    if corpus1 == "" or corpus2 == "":
        return 0

    token_dict[1] = corpus1
    token_dict[2] = corpus2


    tfidf = TfidfVectorizer(tokenizer=tokenize, stop_words='english')
    tfs = tfidf.fit_transform(token_dict.values())
    sim = cosine_similarity(tfs)

#     vec = CountVectorizer(tokenizer=tokenize, stop_words='english')
#     tfs = vec.fit_transform(token_dict.values())
#     sim = cosine_similarity(tfs)
#     print corpus1, corpus2, sim[0][1]
    return sim[0][1]



def get_list(partition):
    result = []
    count = 0.
    for com in set(partition.values()) :
        count += 1.
        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
        result.append(list_nodes)
    return result


def refine_cluster(c, orig, tau, db_graph):
    nodes = c.nodes()
    # print c.number_of_nodes()
#     tfidf = TfidfVectorizer(tokenizer=tokenize, stop_words='english')
    for i in range(len(nodes)):
        node = nodes[i]
        doc_id, ph = node[0], node[1]
        for j in range(i+1, len(nodes)):
            target = nodes[j]
            if target != node and ph == target[1]:
                if not orig.has_edge(node, target):
                    continue
                # Refine Equivalent edges by similarity between expression(sentences) surrounding two entities
                sentences1, sentences2 = query_related_text(db_graph, node, target)
                sen_sim = sentense_sim(sentences1, sentences2, node[1], target[1])

                if sen_sim <= tau:
                    print "removed: ", node, target
                    orig.remove_edge(node, target)


def refine_sub_cluster(c, db_graph, tau):
    nodes = c.nodes()
    for i in range(len(nodes)):
        node = nodes[i]
        doc_id, ph = node[0], node[1]
        for j in range(i+1, len(nodes)):
            target = nodes[j]
            if target != node and ph == target[1]:
                if not c.has_edge(node, target):
                    continue
                sentences1, sentences2 = query_related_text(db_graph, node, target)
                sen_sim = sentense_sim(sentences1, sentences2, node[1], target[1])
                # print node, target, sen_sim

                if sen_sim <= tau:
                    # print "removed: ", node, target
                    c.remove_edge(node, target)


# Iterative Louvain Method with Cluter Refine
def get_partition(G, db_graph, tau):
    iter = 0
    oldmod = 0
    refined_G = copy.deepcopy(G)

    while iter < 10:
        iter += 1
        partition = community.best_partition(refined_G)
        mod = community.modularity(partition, refined_G)
        print oldmod, mod
        if mod <= oldmod:
            break

        oldmod = mod
        result = get_list(partition)
        coms = []
        for sub in result:
            H = refined_G.subgraph(sub)
            coms.append(H)

        print iter, ": Detected ", len(coms), " communities."

        for c in coms:
            refine_cluster(c, refined_G, tau, db_graph)

    print "Detected ", len(result), " communities in total on further sub partition"
    return result


# To improve speed: use connected component for cluster refining
def get_partition_by_cc(G, db_graph, tau):
    start = time.time()
    partition = community.best_partition(G)
    result = get_list(partition)
    coms = []
    for sub in result:
        H = G.subgraph(sub)
        coms.append(H)
    print "Detected ", len(coms), " communities."
    print("--- --- 1 pass event match: %s seconds ---" % (time.time() - start))

    # Further division
    newPar = coms
    level = 0
    while level < 1:
        oldPar = copy.deepcopy(newPar)
        newRes = []
        for c in oldPar:
            start = time.time()
            refine_sub_cluster(c, db_graph, tau)
            print("--- --- cluster refine: %s seconds ---" % (time.time() - start))
            sublist = nx.connected_components(c)
            newRes.extend(sublist)

        newPar = []
        for sub in newRes:
            H = G.subgraph(sub)
            newPar.append(H)
        level += 1

        if len(oldPar) == len(newPar): break

    print "Detected ", len(newRes), " communities in total on further sub partition"
    return newRes


# Update Database
def create_event_nodes(db_graph, best_par, doc_list, date_time):
    tx = db_graph.cypher.begin()
    for i in range(len(best_par)):
        doc_statement = "MERGE (e:Event {date_time:{DT}, eid:{I}}) with e " \
                        "MATCH (d:Document) WHERE id(d)={DID}" \
                        "MERGE (e)-[r:CONTAIN_DOC]->(d)"
        keywords_list = []
        doc_list = []
        for nod in best_par[i]:
            keywords_list.append(nod[1])
            doc_list.append(nod[0])

        for did in doc_list[i]:
            tx.append(doc_statement, {"DT": date_time, "I": i, "DID": did})

        text_statement = "MATCH (e:Event {date_time:{DT}, eid:{I}})" \
                         "SET e.keywords={L}"
        tx.append(text_statement, {"DT": date_time, "I": i, "L": keywords_list})
    tx.process()
    tx.commit()



