__author__ = 'zys'

import community
import networkx as nx
import nltk, string
from collections import Counter
from collections import Set
from collections import defaultdict
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import copy


def query_related_text(db_graph, node, target):
    docid1,docid2 = node[0], target[0]
    phrase1, phrase2 = node[1], target[1]
    query_statement = "match (d: Document)-[:HAS_SENTENCE]->(s: Sentence)-[:HAS_NAMEENTITY]->(ne: NameEntityPhraseNode),\
                        (s: Sentence)<-[:HAS_CHILD]-(t: Text)\
                        where id(d)= {DID} and ne.phrase = {PHRASE} \
                        with t \
                        match (t)-[:HAS_CHILD]->(s2: Sentence)\
                        return s2.text"
    sentences1 = db_graph.cypher.execute(query_statement, {"PHRASE": phrase1, "DID": docid1})
    sentences2 = db_graph.cypher.execute(query_statement, {"PHRASE": phrase2, "DID": docid2})
    return sentences1, sentences2

def sentense_sim(sentencelist1, sentencelist2):
    stemmer = nltk.stem.porter.PorterStemmer()
    token_dict = {}
    def tokenize(text):
        tokens = nltk.word_tokenize(text)
        filtered = [w.encode('utf-8').rstrip(string.punctuation) for w in tokens if w not in nltk.corpus.stopwords.words('english')]
        stemmed = [stemmer.stem(item) for item in filtered]
        return stemmed

    corpus1 = " ".join([s[0].lower() for s in sentencelist1])
    corpus2 = " ".join([s[0].lower() for s in sentencelist2])

    token_dict[1] = corpus1
    token_dict[2] = corpus2
    tfidf = TfidfVectorizer(tokenizer=tokenize, stop_words='english')
    tfs = tfidf.fit_transform(token_dict.values())
    return cosine_similarity(tfs)[0][1]


def get_list(partition):
    result = []
    count = 0.
    for com in set(partition.values()) :
        count += 1.
        list_nodes = [nodes for nodes in partition.keys() if partition[nodes] == com]
        result.append(list_nodes)
    return result

def get_partition(G, tau, lam, mode=1):  # mode{1: depends on threshold 2: highest level}
    partition = community.best_partition(G)
    result = get_list(partition)
    coms = []
    for sub in result:
        H = G.subgraph(sub)
        coms.append(H)
    print "Detected ", len(coms), " communities."

    if mode==2:
        print "Detected ", len(coms), " communities on best partition"
        return result

    # Further division
    newPar = coms
    level = 0
    while tau > 0.4:
        oldPar = copy.deepcopy(newPar)
        newRes = []
        for c in oldPar:
            best = community.best_partition(c)
            mod = community.modularity(best, c)
            print mod

            if mod >= tau:
                sublist = get_list(best)
                newRes.extend(sublist)
            else:
                newRes.append(c.nodes())

        newPar = []
        for sub in newRes:
            H = G.subgraph(sub)
            newPar.append(H)
        level += 1
        tau -= level * lam
        print len(oldPar), len(newPar)
        if len(oldPar) == len(newPar):
            break

    print "Detected ", len(newRes), " communities in total on further sub partition"
    return newRes

