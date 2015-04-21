# from getEvent import get_list

def cosSim(baseSet, target):
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

def matchStory(stories, target, tau):
	existing = copy.deepcopy(stories['keywords_set'])
	match_target = copy.deepcopy(target['keywords_set'])
	comp = cosSim(existing, match_target)
	
	for i in range(len(comp)):
		match_score = np.max(comp[i])
		if match_score < tau:
			stories['keywords_set'].append(target['keywords_set'][i])
			stories['doc_set'].append(target['doc_set'][i])
			continue
		match_ind = np.argmax(comp[i])
		print  match_ind,stories['doc_set'][match_ind],target['doc_set'][i]
# 		match_text = existing[match_ind]
		stories['keywords_set'][match_ind].extend(match_target[i])
		u = stories['doc_set'][match_ind].union(target['doc_set'][i])
		stories['doc_set'][match_ind] = u
	