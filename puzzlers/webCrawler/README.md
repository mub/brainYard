
## Requirements:


### Original wording

Write a java application that accepts a URL (`www.yahoo.com`, for example). Crawl the page the URL references. Determine all the words that aren't part of the HTML on the page and the frequency counts for each. The results should be stored in a database. 

### Clarifications 1

Write a Java application as follows:

* <b>Input</b> - a URL with a full format of `protocol://host:port/dir/path?param=value` or partial format `host:port/dir/path?param=value` specification i.e. either `http://www.yahoo.com` or `www.yahoo.com`. If the protocol prefix is missing, assume `http`. 
    * The following protocol(s) should be supported:
         * `HTTP`

* <b>Output</b> - word frequencies stored in the database, in an entity with the 2 fields, indexed and PK'ed by `word`:
    * `word`: `VARCHAR(48)`
    * `frequency`: `INTEGER`.

* <b>Algorithm</b>

    * Parse the body if the Web page, extract the words and frequencies into the data structure described in the <b>Output</b> section. Ignore the words that don't match a "word specification" described in the <b>Configuration</b> section.
    * *Recursively* apply the above step to any URL encountered on the page. All the word frequencies should apply to the all URLs scanned so far throughout, i.e. note that the URL is not part of the deliverable data structure described in the <b>Output</b> section.
        * Do not enter URLs with the `host:port/dir/path` combination already scanned.
        * Do not implement any security aspects such as log ins, certificates etc. If the web site refuses connection for any reason, log an error and exit

* <b>Configuration</b>

Configuration should be made available to the runnable via a `YAML` - formatted plaintext file.

* Maximum URL depth: do not scan the whole Web. 
* "Word specification" :: A RegEx of the word definition, what exactly constitutes a word. Example: `\w{3,}`
* A List of RegEx of the URLs to be ignored. Example: `^https:\/\/.+$`
* A list of ignored words such as "the", "off", "out" etc.
* Odometer setting: flushing frequency (TBD)
  

## Implementation notes.

### The Database choice

* <b>Plaintext file</b> -- won't work: need to update frequencies in a multi-tasking environment with odometer..
* a simple <b>embeddable RDBMS</b> such as Apache Derby or H2 - seems like a logical choice.

### Persistence layer

Make it very thin with minimal abstraction. A method of registering a new word encounter like 
</br>`public void encountered(final String word, final int count) {....}`<br/> -- should suffice. Any implementation could be plugged into it as needed.

### Caching and flushing

The "words encountered" event should be made thread-safe with the parallel crawlers on the Java side. An alternative would be reliance on a RDBMS "Isolation level" but that's a slippery slope. It would place the issue where it does not logically belong and make concrete persistence layer update/replacement more difficult.

