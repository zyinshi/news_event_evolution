__author__ = 'zys'


from collections import Counter
from collections import Set
from collections import defaultdict
from sklearn.feature_extraction import DictVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


def compute_jaccard_sim(Gd):
    jacSim = defaultdict(int)
    docs = defaultdict(set)
    for n in Gd.nodes():
        docs[n[0]].add(n[1])

    for d1 in docs.keys():
        for d2 in docs.keys():
            if d1 == d2: continue
            if (d1, d2) in jacSim or (d2, d1) in jacSim:
                continue
            sim = len(docs[d1].intersection(docs[d2])) / float(len(docs[d1].union(docs[d2])))
            jacSim[(d1, d2)] = sim

    return jacSim

def cos_sim(baseSet, target):
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

def out_to_file(outputFile, result):
    f = open(outputFile, "w")
    for item in result:
        print>>f, item
        # print>>f,'\n'

def get_text_list(partition):
    textlist = []
    for sub in partition:
        textlist.append([node[1] for node in sub])
    return textlist

def get_doc_list(partition):
    doclist = []
    for sub in partition:
        doclist.append(set([int(node[0]) for node in sub]))
    return doclist
