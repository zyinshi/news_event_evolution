# news_event_evolution
Event evolution detection based on Graph network model

On working...

### -------------check list-----------------
* <del> Get Data corpus </del>
  - resource 
  - crawl url
  - format problem? clean html

* Import and text preprocessing
  - <del> Stanford Parser - named entity</del>
  - <del> or Lucene? </del>
  - How to get key words? noun : name : verb

* Extract event in time window
  - how long?
  - represent graph: terms and co-occurence
    - separated or unique?
    - same node or similarity link?
    - sentence level : doc level
  - extract event
    - <del> community detection? </del>
    - <del> slow? </del>
    - hierarchy?
  - how to measure quantitively

* Evolution story
  - iteratively
  - similarity and merge? term level : graph comparison: "distance"
  - window size?
  - how to measure
* global
  - data size
  - speed
  - perfomance measure
