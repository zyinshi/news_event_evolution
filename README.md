# news_event_evolution
Event evolution detection based on Graph network model

On working...

### -------------check list-----------------
* Get Data corpus
  - resource
  - crawl url
  - format problem? clean html

* Import and text preprocessing
  - Stanford Parser - named entity
  - or Lucene?
  - How to get key words?

* Extract event in time window
  - how long?
  - represent graph: terms and co-occurence
    - separated or unique?
    - same node or similarity link?
  - extract event
    - community detection?
    - slow?
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
