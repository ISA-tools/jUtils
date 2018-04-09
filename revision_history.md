# REVISION HISTORY

*If not otherwise specified, items refer to the base module (`jutils`)*. 

## 7.0.1-SNAPSHOT
  * Additions to `uk.ac.ebi.utils.exceptions`. 
  * `uk.ac.ebi.utils.xml.XPathReader` interface enhanced and unit-tested.

## 7.0
  * (jutils-io) `uk.ac.ebi.utils.io.Unix4jUtils` added.
  * (jutils-io) `XmlFilterUtils` added.
  * `uk.ac.ebi.utils.threading.BatchProcessor` added.
  * URI-related methods added to `uk.ac.ebi.utils.ids.IdUtils` (migrated from [java2rdf](https://github.com/EBIBioSamples/java2rdf)).
  * TupleSpliterator added and used in StreamUtils.tupleStream(), to make the result parallelizable.
  * (jutils-io) IOUtils.readFile() added, deprecated method readInputFully removed.
  
  
## 6.0
  * Splitting the original jutils into multiple function and dependency-based modules.
  * TupleIterator and StreamUtils.tupleStream() added.

### 6.0, jutils-io
  * SSLUtils added.
  * New resource access methods added to IOUtils.


## 5.1
  * New hash functions added to IOUtils.
  * IOUtils.uri () and .url() added.
  * Extensions to BatchService, to allow for customisation, including task priority option.
  * Some useful general-purpose exceptions added in uk.ac.ebi.utils.exceptions.
  * ThreadedPipedWriter removed, didn't make much sense, use EasyStream instead 
  (http://io-tools.sourceforge.net/easystream/index.html).


## 5.0
  * uk.ac.ebi.utils.runcontrol added.
  * MapCollection added.


## 4.5
  * Moving deployment to BioSD/GitHub repository.
  * Some improvements in BatchService.


## 4.4
  * Experimental migration of deployment to EBI/Nexus Maven repository.
  * XStopWatch fixed against new checkings in StopWatch (start() cannot be called twice, without reset()).
  * XStopWatch.restart() added.
 
  
## 4.3
  * Fix in MemoryUtils.checkMemory(), Runtime.maxMemory() used (instead of totalMemory()).
  * ReflectionUtils.invoke () added.


## 4.2
  * HqlUtils added.


## 4.1
  * ThreadedPipedWriter added.
  * XStopWatch added.
  * Minor changes.
 
  
## 4.0
  * BatchService and related classes added.
  * ArraySearchUtils added.
  * ExceptionUtils added.


## 3.0
  * SqlUtils added.
  * Minor bugfixing in tests. 
  * JPA dependency upgraded to version 2.
 
 
## 2.0
  * SimpleCache added.
  * Many2ManyUtils and uk.ac.ebi.utils.orm package added.
  * RegEx.toString() now yields the pattern as in Pattern.
  * AlphaNumComparator now has an option for case-sensivity.


## 1.6
  * IOUtils.getMD5() generalised into getHash().
  * XPathReader, exception handling changed.


## 1.5
  * TestEntityMgrProvider added.
  * Very minor changes in comments.

  
## 1.4
  * Minor changes made. Version upgraded to reflect the policy of increasing the second revision number upon whole 
  classes.
  * Minor methods additions and changes to existing code.
  
  
## 1.3.1 
  * uk.ac.ebi.utils.collections.AlphaNumComparator added.   
  * uk.ac.ebi.utils.test.junit.TestOutputDecorator is also a TestRule, so that it can be used on individual test 
  classes, in case you already have many other classes with the same functionality already implemented.


## 1.3
  * uk.ac.ebi.utils.test.junit.TestOutputDecorator added.


## 1.2
  * StringBufferReader removed, Apache commons has CharSequenceReader in its place.
  * ISAPair renamed to Pair, to reflect its general-purpose nature.


## 1.1
  * StringBufferReader added, ISAPair added.


## 1.0 
  * First version published as stand-alone package in Git.
  