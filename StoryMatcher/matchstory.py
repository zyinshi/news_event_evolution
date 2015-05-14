__author__ = 'zys'

import networkx as nx
import numpy as np
import copy
from EventUtil.util import cos_sim

def match_story(stories, target, tau):
    # existing = copy.deepcopy(stories['keywords_set'])
    # match_target = copy.deepcopy(target['keywords_set'])
    comp = cos_sim(stories, target)
    return comp
# 	for i in range(len(comp)):
# 		match_score = np.max(comp[i])
# 		if match_score < tau:
# 			stories['keywords_set'].append(target['keywords_set'][i])
# 			stories['doc_set'].append(target['doc_set'][i])
# 			continue
# 		match_ind = np.argmax(comp[i])
# 		print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# # 		match_text = existing[match_ind]
# 		stories['keywords_set'][match_ind].extend(match_target[i])
# 		u = stories['doc_set'][match_ind].union(target['doc_set'][i])
# 		stories['doc_set'][match_ind] = u

def compute_distance(g1, g2):
    nmatch = 0
    for ed in g1.edges():
        end1, end2 = ed[0], ed[1]
#         if  ed[0] in g2 and ed[1] in g2: print [e for e in nx.all_neighbors(g2,ed[0])],ed[1], ed[1] in [e for e in nx.all_neighbors(g2,ed[0])], ed in g2.edges()
        if ed in g2.edges():
            nmatch += min(g1[ed[0]][ed[1]]['weight'],g2[ed[0]][ed[1]]['weight'])
    nsize = g1.size(weight = 'weight') + g2.size(weight = 'weight')
    if nsize==0.0: return 0
    return float(nmatch)/float(nsize)

def match_story_by_sen_edge(Gs, stories, target, tau):
    existing = copy.deepcopy(stories['keywords_set'])
    match_target = copy.deepcopy(target['keywords_set'])
    node_cos = match_story(existing, match_target, 0.3)

    subgs1 = []
    for sto in match_target:
        subgs1.append(nx.subgraph(Gs, sto))
    subgs0 = []
    for sto in existing:
        subgs0.append(nx.subgraph(Gs, sto))

    matched = []
    for i in range(len(subgs1)):
        matchingGraph = subgs1[i]
        dis = []
        for cand in subgs0:
            val = compute_distance(matchingGraph, cand)
            dis.append(val)
        total = np.multiply(dis, node_cos[i])
        match_score = np.max(total)

        if match_score < tau:
            stories['keywords_set'].append(target['keywords_set'][i])
            stories['doc_set'].append(target['doc_set'][i])
            continue

        print match_score
        match_ind = np.argmax(total)
        print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# 		match_text = existing[match_ind]
        stories['keywords_set'][match_ind].extend(match_target[i])
        u = stories['doc_set'][match_ind].union(target['doc_set'][i])
        stories['doc_set'][match_ind] = u
        # if ma > tau:
        #     ind = np.argmax(dis)
        #     matched.append((all_res[0]['doc_set'][i],all_res[1]['doc_set'][ind]))
        # else:
        #     matched.append(all_res[0]['doc_set'][i]);
    # return matched

def match_story_by_doc_edge(Gd, stories, target, tau):
    existing = copy.deepcopy(stories['keywords_set'])
    match_target = copy.deepcopy(target['keywords_set'])
    node_cos = match_story(existing, match_target, 0.3)

    subgs1 = []
    for sto in match_target:
        subgs1.append(nx.subgraph(Gd, sto))
    subgs0 = []
    for sto in existing:
        subgs0.append(nx.subgraph(Gd, sto))

    matched = []
    for i in range(len(subgs1)):
        matchingGraph = subgs1[i]
        dis = []
        for cand in subgs0:
            val = compute_distance(matchingGraph, cand)
            dis.append(val)
        total = np.multiply(dis, node_cos[i])
        match_score = np.max(total)

        if match_score < tau:
            stories['keywords_set'].append(target['keywords_set'][i])
            stories['doc_set'].append(target['doc_set'][i])
            continue

        match_ind = np.argmax(total)
        print match_ind,match_score

        # print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# 		match_text = existing[match_ind]
        stories['keywords_set'][match_ind].extend(match_target[i])
        u = stories['doc_set'][match_ind].union(target['doc_set'][i])
        stories['doc_set'][match_ind] = u
