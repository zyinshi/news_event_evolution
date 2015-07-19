# news_event_evolution
Event evolution detection based on Graph network model

On working...

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
  - **perfomance measure**