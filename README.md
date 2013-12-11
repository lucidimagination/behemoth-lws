behemoth-lws
============

Integration of Behemoth from Digital Pebble with LucidWorks Search

Usage Instructions
------------------

* Compile the repository using maven command `mvn compile`
* Generate the hadoop job files using command `mvn package`
* The generated job file will be located at `target` directory.

Running Hadoop job
------------------

  Examples:
  
  Indexing to single node solr 
  
     hadoop jar target/behemoth-lucidworks-1.0-SNAPSHOT-job.jar  com.digitalpebble.behemoth.solr.LucidWorksIndexerJob -Dlw.metadata=true -Ddynamic.fields=true -Dlw.annotations=true -i /output/behe_tika_lang -o http://localhost:8983/solr/collection1
     
  Indexing to solrcloud 
    
     hadoop jar target/behemoth-lucidworks-1.0-SNAPSHOT-job.jar  com.digitalpebble.behemoth.solr.LucidWorksIndexerJob -Dlw.metadata=true -Ddynamic.fields=true -Dlw.annotations=true -Dsolr.zkhost=localhost:2181 -Dsolr.zk.collection=testCollection -i /output/behe_tika_lang -o http://localhost:8983/solr/collection1
     
   The output parameter `-o` in the above example is not considered even though it has to be defined when parameter `solr.zkhost` is present.
    
  The parameters `lw.metadata`, `lw.annotations` need to be enabled for behemoth metadata and annotations to be indexed.
    
  If the parameter `dynamic.fields` is set to true, then all the metadata fields will be added as dynamic fields in Solr with prefix `attr_`. 
  
  All the above three parameters are set to `false` by default.
