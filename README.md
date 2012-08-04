tStreamingArchiver is a set of programs for archiving Tweets using the Twitter API and moving them into a mySQL database. It is written in the Java language and licensed under GPL 2.0.

tStreamingArchiver uses external libraries - twitter4j, mysql-connector-java and javax.mail. 

It is setup as a group of 10 eclipse projects using maven to bring in their dependencies.

* gpl-2.0.txt - the terms of license of this software
* readme.md - this file
* tUtils - shared code used by different modules
* Example - sample setup with shell scripts, configuration files and runnable jar files
* Main modules:
    * tStreamingArchiver - use twitter streamingAPI to get Tweets into text files on disk
    * tDiskToSQL - import the tweets from disk into mySQL
* Indexing modules: (better to use Apache Solr instead)
    * tUpdateSearchTermIndexing - create indexes in mySQL for the search terms from the searches table. Has some hard coded search terms which should be adjusted for your purposes and a new jar file created.
    * tBuildWordLists - create word co-occurance lists in mySQL. Has hard coded search terms which should be adjusted for your purposes and a new jar file created.
    * tSearchArchiver - user twitter searchAPI to get tweets and put them directly into a mySQL database. This is an older module and hasn't been fully refactored into the new suite. Use tSearchImport to bring the searchAPI tweets into the main mySQL database.
* Other modules:
    * tSearchImport - import tweets from searchAPI mySQL database. Before the streamAPI existed, I was collecting data using the searchAPI.
    * tGetMissingUsers - fill in any missing users for tweets imported from searchAPI or TwapperKeeper
* Discontinued modules:
    * tTwapperKeeperImport - import twapperKeeper archives

###How To Get Started

If you want to build the source code, it is setup to work with Eclipse ([http://eclipse.org/]) and the maven eclipse feature ([http://www.eclipse.org/m2e/]).

If you just want to use the programs, then you only need the Example directory and the instructions in the README.md file in that directory. You do need to have Java installed (for example the Sun Java [http://java.com/en/download/index.jsp]).

###Bugs / Requests

If you find any bugs or have any suggestions for improvements, please use the issues section on GitHub. 

###Citing this work

If you use tStreamingArchiver in publications, please cite the following...

`Moon B.R. (2012). tStreamingArchiver <version number> [Software]. Available from http://brendam.github.com/tStreamingArchiver/, <date of access>`