# news_event_evolution
Event evolution detection based on Graph network model

On working...
### Dependencies and Tools:
* Crawl tool : https://www.readability.com/developers/api/parser
* Tiny-html5: https://github.com/htacg/tidy-html5
* Document-graph converter: https://github.com/arz2013/FinancialXMLParsing
* Neo4J 2.0.4
* Louvain Method: https://sites.google.com/site/findcommunities/
* Networkx, Numpy, Scipy, scikit-learn, pandas, py2neo

### Modules:
* download_article: 
  - Get event list from event database GDELT
  - Crawl aritcle from internet based on sourceUrl from GDELT
  
  `Set startdate and enddata, archive directory, count of articles per day`

  `python download_article.py`

* Modified XMLParser (Original version from https://github.com/arz2013/FinancialXMLParsing)
  - Convert textual document into graph structure
  - Extract named entities and Cvalue terms
  
  `Refer to Readme for dependencies `

  `Set raw data directory`
  
* Neo4J Queries:  
  - Examples for necessary preprocess for data in Graph database
  - Examples for getting data of interest (co-occurence relationship)
  
* eventEvolution: main module for event detection and evolution detection

   `Download Louvain Method python module package http://perso.crans.org/aynaud/communities/index.html`
   
  ` Install packages Networkx, Numpy, Scipy, scikit-learn, pandas, py2neo`
  - GraphBuilder: build co-occurrence graph based on input data with Networkx
  - EventExtractor: get subgraphs in one time period as events (modified Louvain Method); Update database (Add events)
  - StoryMatcher: match events along time to build evolution dependency graph; Update database (Add evolution dependencies)

### -------------check list-----------------
* Get Data corpus 
  - resource 
  - crawl url
  - format problem? clean html

* Import and text preprocessing
  - Stanford Parser - named entity, cvalue terms
  - How to get key words? noun : name :<del> verb </del>

* Extract event in time window
  - represent graph: terms and co-occurence
    - same node or similarity link? link 
    - sentence level : doc level? both
  - extract event
    - community detection? louvain
    - **slow?**
    - hierarchy? iteratively
  - **how to measure quantitively**

* Evolution story
  - incrementally within window
  - similarity and merge? term level :<del> graph comparison: "distance" </del>
  - window size? depends
  - **how to measure**
  
* global
  - data size
  - **speed**
<<<<<<< HEAD
  - **perfomance measure**
