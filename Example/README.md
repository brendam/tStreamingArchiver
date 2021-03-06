##Example of how to use tStreamingArchiver

The three sample shell script files will work on linux and mac osx. 

Before they can be run, you must:

1. create a file called `twitter4j.properties` in this `Example` directory from the `twitter4j.properties.sample` file. Fill in the OAuth login details for your research Twitter account. The `jsonStoreEnabled=true` must be set to true.
2. create the `tArchiver.properties` from `tArchiver.properties.sample` with your mySQL database details and email details for error messages. Instructions for creating the mySQL databases are provided further down this document. `runStreamingArchiver.sh` can be used to collect data without needing a mySQL database, in which case you don't need to set any mySQL properties. See below for example python program `import_raw_data_to_CSV.py` to parse raw data files to csv.
3. edit `./data/searches.txt` to set the keyword and userid searches
4. `runSearchArchiver.sh` writes directly to the SearchAPI mySQL database, so if you want to use search you need to create the database and put it's details be in the `tArchiver.properties` file.

**Note:** If you are updating from an earlier version of tStreamingArchiver you must add `jsonStoreEnabled=true` to twitter4j.properties to enable saving the raw json format data.

### format of `searches.txt` file

The `searches.txt` file contains all of the searches for the Streaming API in tab delimeted format. The first column is the id of the matching search in the searches table of the mySQL database but is not used by runStreamingArchiver.sh. The second column is either the single word search keyword or a Twitter user name, the final column is either k for keyword searches or the Twitter user id number matching the Twitter user name.

The searches table in the database is used by search term indexing and to allow filtered queries by groups of search terms. So if you are using the mysql database you should enter all the terms from searches.txt into the searches table in the database. See below for creating the database.

Twitter user ids can be looked up at http://id.twidder.info/ 

###`runStreamingArchiver.sh`

This runs the program that collects the Tweets using the Twitter StreamingAPI (tStreamingArchiver-0.0.1-SNAPSHOT-jar-with-dependencies.jar)
Tweets recieved from Twitter StreamingAPI are saved into `./data/<year>/<month>/<day>/<hour>.txt` files where the time and data are in GMT, for example `./data/2012/06/05/10-56-32.txt`. Requires the `twitter4j.properties` and `searches.txt` files to be configured.
 
###`sql_import_and_process.sh`

This imports the data created by runStreamingArchiver.sh into a mySQL database.

Before you run this you must install mySQL and create the database into which to import the Tweets (see below). 

You must also edit `sql_import_and_process.sh` to set the SUBJECT and EMAIL details for error emails, and 
if you are using cron, you should also edit this line:  

`# cd /fullPathToArchiverDirectory/Example/`

to match the directory where you have installed the example files (doesn't have to be called Example)

The script includes commented out calls to run the `tUpdateSearchTermIndexing-0.0.1-SNAPSHOT-jar-with-dependencies.jar` and `tBuildWordLists-0.0.1-SNAPSHOT-jar-with-dependencies.jar`. Both of these have hard coded search terms and so need to be updated and built from source before they can be used.

###`runSearchArchiver.sh`

This runs the program that collects the Tweets using the Twitter SearchAPI (tSearchArchiver-0.0.1-SNAPSHOT-jar-with-dependencies.jar). Generally searches are only useful for getting historic data (limit of 1,500 tweets per search) or a sample of tweets for developing keywords for the streaming API. Twitter has asked that the streamAPI be used for research as it puts less load on their servers.

The searches are read from the `twitter_archive` mySQL database and Tweets recieved from Twitter SearchAPI are written directly to the database (see next section for how to create this).

###`import_raw_data_to_CSV.py`

Example python program to parse new style json raw data files to csv. Run as `python import_raw_data_to_CSV.py`. Example processes the default `smile` search data into `smile_data.csv`. Excel doesn't work well with UniCode data (at least on Mac) so you will need to use Numbers, OpenOffice or Google Spreadsheet if you want to see Unicode correctly.

###Create mySQL database

The mySQL dump file `twitter_stream_archive.sql` will create an empty database including all of the extra indexing fields. If you are going to collect a lot of data, it is better to use external indexing like Apache Solr rather than the indexing fields in the database. 

If you are going to use the searchAPI you also need to use the mySQL dump file `twitter_archive.sql` to create an empty database for holding searchAPI tweets.

You need to create a mySQL user and password with access to the databases and update `tArchiver.properties` with those and the name you have used for the databases. Two search items are created in the searches table matching the two sample searches in the provided `./data/searches.txt` file.

###Suggested cron file

Cron automates the running of the shell scripts once you are sure they are working correctly.
Create or edit your crontab file by using `crontab -e` at the terminal prompt 
(it uses the vi editor, so you need to know how to use that). I have it setup to start the `runStreamingArchiver.sh` script every second. The shell script test for a lck file to see if it is already running and exits immediately if it is.

The import to mySQL runs once a day at 23:55. By using 'nice -3' it gets a lower priority than `runStreamingArchiver.sh` to make sure that it doesn't impact the initial data capture.

The searchAPI program runs every 20 minutes.

Change pathToWorkingDirectory to be the full path to the shell script on your system.

`# m h  dom mon dow   command
20,40,0 * * * * /pathToWorkingDirectory/runSearchArchiver.sh
 * * * * * /pathToWorkingDirectory/runStreamingArchiver.sh
 55 23 * * * nice -3 /pathToWorkingDirectory sql_import_and_process.sh`

###Bugs / Requests

If you find any bugs or have any suggestions for improvements, please use the issues section on GitHub. 

###Citing this work

If you use tStreamingArchiver for research, please cite the following...

`Moon B.R. (2012). tStreamingArchiver <version number> [Software]. Available from https://github.com/brendam/tStreamingArchiver, <date of access>`

BibTex: [tStreamingArchiver.bib](https://github.com/brendam/tStreamingArchiver/blob/master/tStreamingArchiver.bib)