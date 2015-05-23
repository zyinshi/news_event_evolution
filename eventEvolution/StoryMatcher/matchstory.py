__author__ = 'zys'

import networkx as nx
import numpy as np
import copy
from EventUtil.util import cos_sim
from itertools import chain
# import bottleneck as bn
import py2neo as pn


def create_one_story_node(db_graph, date_time, eid, story_local_index, match_score, next_ind):
    stat = "MERGE (s:Story {start_date:{DT}, end_date:{DT}, local_index:{SI}}) with s " \
           "MATCH (e:Event {date_time:{DT}, eid:{I}}), (ls:Story {local_index:{LSI}})" \
           "MERGE (s)-[:START_EVENT {weight:1}]->(e)" \
           "MERGE (s)-[:HAS_EVENT {weight:1}]->(e)" \
           "MERGE (s)-[:SIMILAR {weight:{W}}]->(ls)"
    db_graph.cypher.execute(stat, {"DT": date_time, "I": eid, "SI": next_ind, "LSI": story_local_index, "W": match_score})


def create_initial_story_nodes(db_graph, init_stories, date_time):
    tx = db_graph.cypher.begin()
    stat = "MERGE (s:Story {start_date:{DT}, end_date:{DT}, local_index:{I}}) with s " \
           "MATCH (e:Event {date_time:{DT}, eid:{I}}) " \
           "MERGE (s)-[:START_EVENT {weight:1}]->(e)" \
           "MERGE (s)-[:HAS_EVENT {weight:1}]->(e)"

    for i in range(len(init_stories)):
        # print i, date_time
        tx.append(stat, {"DT": date_time, "I": i})
    tx.process()
    tx.commit()


def link_events_and_story(db_graph, date_time, event_id, story_local_index, match_score, last_event_date):
    stat = "MATCH (s:Story {local_index:{SI}})-[:HAS_EVENT {weight:{W}}]->(le:Event {date_time:{LDT}}), (e:Event {date_time:{DT}, eid:{I}})" \
           "MERGE (s)-[r:HAS_EVENT]->(e) MERGE (le)-[:NEXT_EVENT {weight:{W}}]->(e) SET s.end_date = {DT}"
    db_graph.cypher.execute(stat, {"DT": date_time, "I": event_id, "SI": story_local_index, "LDT": last_event_date, "W": match_score})


def match_story(existing, match_target):
    comp = cos_sim(existing, match_target)
    return comp


def compute_distance(g1, g2):
    nmatch = 0
    for ed in g1.edges():
        end1, end2 = ed[0], ed[1]
#         if  ed[0] in g2 and ed[1] in g2: print [e for e in nx.all_neighbors(g2,ed[0])],ed[1], ed[1] in [e for e in nx.all_neighbors(g2,ed[0])], ed in g2.edges()
        if ed in g2.edges():
            nmatch += min(g1[ed[0]][ed[1]]['weight'],g2[ed[0]][ed[1]]['weight'])
    nsize = g1.size(weight='weight') + g2.size(weight='weight')
    if nsize == 0.0:
        return 0
    return float(nmatch)/float(nsize)


def node_distance(stories, target, sim_tau):
    # existing = copy.deepcopy(stories['keywords_set'])
    existing = [list(chain.from_iterable(s_dic.values())) for s_dic in stories['keywords_set']]
    match_target = copy.deepcopy(target['keywords_set'])

    node_cos = match_story(existing, match_target)

    match_score = np.max(node_cos, 1)
    match_ind = np.argmax(node_cos, 1)

    for i in range(len(match_target)):
        print match_ind[i], match_score[i]
        if match_score[i] < sim_tau:
            stories['keywords_set'].append(target['keywords_set'][i])
            stories['doc_set'].append(target['doc_set'][i])
            continue
        # print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# 		match_text = existing[match_ind]
        else:
            print stories['doc_set'][match_ind[i]], target['doc_set'][i]
            stories['keywords_set'][match_ind[i]].extend(match_target[i])
            u = stories['doc_set'][match_ind[i]].union(target['doc_set'][i])
            stories['doc_set'][match_ind[i]] = u
    return node_cos


def edge_distance(Gd, db_graph, stories, target, day_index, sim_tau):
    # existing = copy.deepcopy(stories['keywords_set'])
    existing = [list(chain.from_iterable(s_dic.values())) for s_dic in stories['keywords_set']]
    match_target = copy.deepcopy(target['keywords_set'])

    node_cos = match_story(existing, match_target)

    subgs_tar = []
    for sto in match_target:
        subgs_tar.append(nx.subgraph(Gd, sto))
    subgs_exs = []
    for sto in existing:
        subgs_exs.append(nx.subgraph(Gd, sto))

    for i in range(len(subgs_tar)):
        matchingGraph = subgs_tar[i]
        edge_dis = []
        for j in range(len(subgs_exs)):
            cand = subgs_exs[j]
            edge_val = compute_distance(matchingGraph, cand)
            node_val = node_cos[i]
            edge_dis.append(edge_val)
        total = np.multiply(edge_dis, node_val)
        # match_score = np.max(total)
        # match_ind = np.argmax(total)
        match_ind_top = total[0].argsort()[-3:][::-1]
        match_score_top = total[0][match_ind_top]

        for w in range(3):
            match_score = match_score_top[w]
            match_ind = match_ind_top[w]
            print match_ind, match_score

            if match_score < sim_tau and w > 0:
                break
            if match_score < sim_tau and match_score > 0 and w == 0:
                stories['keywords_set'].append({day_index: target['keywords_set'][i]})
                stories['doc_set'].append({day_index: target['doc_set'][i]})
                create_one_story_node(db_graph, day_index, i, match_ind, match_score, len(stories['doc_set'])-1)
                break

            elif match_score >= sim_tau:
                last_event_date = max(stories['doc_set'][match_ind].keys())
                stories['keywords_set'][match_ind][day_index] = match_target[i]
                stories['doc_set'][match_ind][day_index] = target['doc_set'][i]
                link_events_and_story(db_graph, day_index, i, match_ind, match_score, last_event_date)


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
        print  match_ind, stories['doc_set'][match_ind], target['doc_set'][i]
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


def match_story_by_doc_edge(Gd, db_graph, stories, target, day_index, sim_tau):
    edge_distance(Gd, db_graph, stories, target, day_index, sim_tau)

