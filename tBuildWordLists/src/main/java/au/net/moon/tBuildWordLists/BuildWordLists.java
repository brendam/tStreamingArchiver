package au.net.moon.tBuildWordLists;

/**
 * BuildWordLists - update WordList and CoOccurenceList tables in mySQL
 * Copyright (C) 2012 Brenda Moon 
 * 
 * This program is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 **/

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import au.net.moon.tUtils.RedirectSystemLogs;

/**
 * Reads tweets from twitterStreamArchive database for a particular search term
 * and updates the WordList and CoOccurrenceList tables with the most frequent
 * words and their co-occurrence for each day. Updates the WordListTweetsLookup
 * table with the tweetId's from wordList.occursIn(word) for each entry in the
 * WordList table. All words are stored as lowercase.
 * 
 * NOTE: For large amounts of data, this approach is too slow. Better to use specific
 * indexing software like Apache Lucene to generate word indexes.
 * <p>
 * 
 * Needs config file to exist in program directory or 
 * root of classpath directory:
 * tArchiver.properties SQL parameters (and email parameters used for error email)
 * <p>
 * 
 * See tArchiver.properties.sample in distribution.
 * <p>
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * @version 0.6
 */
public class BuildWordLists {
	static Statement stmtSearches;
	static Statement stmtTweets;
	static Statement stmt;
	static ResultSet rsSearches;
	static ResultSet rsTweets;
	static ResultSet rs;
	boolean dataBaseOk;
	int maxWords;

	/**
	 * Setup logging and run BuildWordLists.
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("tBuildWordLists.%g.log");
		}
		System.out.println("tBuildWordLists: Program Starting... (v0.6) "
				+ new Date());
		new BuildWordLists();
		System.out.println("tBuildWordLists: Program finished " + new Date());
	}

	BuildWordLists() {
		// int totalCounter = 0;
		Date startDate = new Date();
		Date endDate = new Date();
		Calendar cal;
		cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		maxWords = 200;
		String sqlString = null;
		openSQLDataBase();
		if (isDatabaseReady()) {
			// At this stage only building word lists for AusVotes, so don't get
			// the other queries
			// try {
			// rsSearches = stmtSearches
			// .executeQuery("SELECT id, query from searches where active = 1");
			// while (rsSearches.next()) {
			// int searchId = rsSearches.getInt("id");
			// String searchQuery = rsSearches.getString("query");
			// String max_tw_id = "0";
			// TODO: could add this to the config file so non-programmers could use buildwordlists
			String searchQuery = "AusVotes";
			int searchId = 745;
			System.out.println("tBuildWordLists: searchQuery: " + searchQuery);

			// add the searchQuery to the stopwords list
			StopWords.AddStopWord(searchQuery);
			// find the most recent day in the WordList for that search
			// term and set startDate to one day after it
			try {
				rs = stmt
						.executeQuery("select max(date) as MaxDate from WordList where searchId = "
								+ searchId);
				if (rs.next()) {
					startDate = rs.getDate("MaxDate");
					if (startDate == null) {
						cal.set(2009, Calendar.JANUARY, 01);
					} else {
						cal.setTime(startDate);
					}
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.MILLISECOND, 0);
				}
				cal.add(Calendar.DATE, +1);
				startDate = cal.getTime();
				System.out.println("startDate: " + startDate);
			} catch (SQLException e) {
				System.err
						.println("tBuildWordLists: Error looking for maximum date for "
								+ searchQuery + " in WordList");
				e.printStackTrace();
			}

			// find the most recent day in the Tweets for that search
			// term and set lastDate to one day before it
			try {
				rs = stmt
						.executeQuery("select max(created_at_GMT) as MaxDate from tweets inner join searchTermIndex on tweets.id = searchTermIndex.tweetId where searchId ="
								+ searchId);
				if (rs.next()) {
					endDate = rs.getDate("MaxDate");
					System.out.println("endDate: " + endDate);

					if (endDate == null) {
						cal.clear();
						cal.setTime(new Date());
					} else {
						cal.setTime(endDate);
					}
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.MILLISECOND, 0);
					endDate = cal.getTime();
				}
				System.out.println("endDate: " + endDate);
			} catch (SQLException e) {
				System.err
						.println("tBuildWordLists: Error looking for maximum date for "
								+ searchQuery + " in Tweets");
				e.printStackTrace();
			}

			cal.setTime(startDate);

			// loop through processing each day twice, once for most frequent
			// words and then again to get their co-occurrences
			int counter = 0;
			WordList words = new WordList();
			CoOccurrenceList wordMap = new CoOccurrenceList();
			boolean buildingWordList = true;
			String theDate;
			String tweetDate;
			while (cal.getTime().before(endDate)) {
				theDate = df.format(cal.getTime());
				try {
					sqlString = "select tweets.id, searchTermIndex.date, tweets.text from tweets inner join searchTermIndex on tweets.id=searchTermIndex.tweetId where searchId="
							+ searchId
							+ " and searchTermIndex.date='"
							+ theDate + "' order by searchTermIndex.date";
					rsTweets = stmtTweets.executeQuery(sqlString);
					while (rsTweets.next()) {
						String tweetId = rsTweets.getString("id");
						tweetDate = rsTweets.getString("date");
						String tweetText = rsTweets.getString("text");
						if (tweetDate.equals(theDate)) {
							if (buildingWordList) {
								// process tweet text as sentence into a
								// WordList
								words.addSentence(tweetText, tweetId);
							} else {
								// process each tweet text as sentence into a
								// co-OccuranceList but only for the most
								// frequent
								// words
								wordMap.addSentence(tweetText,
										words.mostFrequentWords(maxWords));
							}
							counter++;
						} else {
							System.err
									.println("tBuildWordLists: Date in select didn't match date in tweet! (tweetDate="
											+ tweetDate
											+ " !=  theDate: "
											+ theDate + ")");
							System.exit(-1);

						}
					}
					// Process the day's data into the
					// database...
					if (buildingWordList) {
						// store the most frequent words to database
						updateWordListTable(words, searchId, theDate);
						buildingWordList = false;
						if (words.length() > 0 || counter > 0) {
							System.out
									.println("tBuildWordLists: Built wordListTable ("
											+ words.length()
											+ ") from "
											+ counter
											+ " tweets for searchTerm: "
											+ searchQuery + " on " + theDate);
						}
						// Get ready to do second run through of day so the
						// co-occurrence list can be made for the most frequent
						// words
						counter = 0;
					} else {
						updateCoOccuranceTable(wordMap, searchId, theDate);
						if (wordMap.length() > 0 || counter > 0) {
							System.out
									.println("tBuildWordLists: Built CoOccurance List ("
											+ wordMap.length()
											+ ") from "
											+ counter
											+ " tweets for searchTerm: "
											+ searchQuery + " on " + theDate);
						}
						words = new WordList();
						wordMap = new CoOccurrenceList();
						counter = 0;
						// move forward a day
						cal.add(Calendar.DATE, 1);
						buildingWordList = true;
					}

				} catch (SQLException e) {
					System.err
							.println("tBuildWordLists: selecting tweets failed.");
					System.err.println("sqlString: " + sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
			}
			// remove the searchQuery before going onto next searchTerm
			// (Currently only doing AusVotes, not all search terms)
			StopWords.RemoveStopWord(searchQuery);
			// totalCounter++;
			// }
			// if (rsSearches.wasNull()) {
			// System.out
			// .println("UpdateSearchTermIndexing: No active searches found in the table \"searches\"");
			// } else {
			// System.out.println("UpdateSearchTermIndexing: Found "
			// + totalCounter + " active searches");
			// totalCounter = 0;
			// }
			// } catch (SQLException e) {
			// System.err
			// .println("UpdateSearchTermIndexing: select searches failed at query.");
			// e.printStackTrace();
			// }
		}
	}

	/**
	 * Add words into WordList table in mySQL database for each day and search
	 * term.
	 * 
	 * @param words
	 *            list of words to be added to the table
	 * @param searchId
	 *            search id the tweets the words occurred in match
	 * @param date
	 *            the day being processed
	 */
	private void updateWordListTable(WordList words, int searchId, String date) {
		String sqlString = null;
		int wordListId = -1;
		String[] wordsArray = words.mostFrequentWords(maxWords);
		for (int i = 0; i < wordsArray.length; i++) {
			// check if word is already in database for that
			// keyword and date
			try {
				sqlString = "select id, frequency from WordList where searchId='"
						+ searchId
						+ "' and date='"
						+ date
						+ "' and word='"
						+ wordsArray[i] + "'";
				rs = stmt.executeQuery(sqlString);
			} catch (SQLException e) {
				System.err
						.println("tBuildWordLists: checking for record in WordList failed at query : "
								+ sqlString);
				e.printStackTrace();
				System.exit(-1);
			}
			try {
				if (!rs.next()) {
					// no existing record

					sqlString = "0,";
					sqlString += " '" + searchId + "',";
					sqlString += "'" + date + "',";
					sqlString += " '" + wordsArray[i] + "',";
					sqlString += " '" + words.wordFrequency(wordsArray[i])
							+ "')";
					try {
						stmt.executeUpdate("insert into WordList values  ("
								+ sqlString, Statement.RETURN_GENERATED_KEYS);
						rs = stmt.getGeneratedKeys();
						if (rs.next()) {
							wordListId = rs.getInt(1);
						} else {
							System.err
									.println("tBuildWordLists: couldn't retrieve the generated WordList id");
							System.exit(-1);
						}
					} catch (SQLException e) {
						System.err
								.println("tBuildWordLists: Insert into WordList failed.");
						System.err.println(sqlString);
						e.printStackTrace();
						System.exit(-1);
					}
				} else {
					// should I update the record here?
					if (rs.getInt("frequency") != words
							.wordFrequency(wordsArray[i])) {
						System.err
								.println("tBuildWordLists: existing WordList record frequency ("
										+ rs.getInt("frequency")
										+ ") doesn't match new frequency ("
										+ words.wordFrequency(wordsArray[i])
										+ ")! for date " + date);
						System.exit(-1);
					}
					wordListId = rs.getInt("id");
				}
			} catch (SQLException e) {
				// !rs.next
				System.err.println("tBuildWordLists: rs.next failed for search");
				System.err.println("sqlString: " + sqlString);
				e.printStackTrace();
				System.exit(-1);
			}

			// Update the WordListTweetsLookup table with the occursIn(String
			// word) list
			updateWordListTweetsLookup(wordListId,
					words.occursIn(wordsArray[i]));

		}
	}

	/**
	 * Add words into WordListTweetsLookup table in mySQL database for each
	 * wordList.<br>
	 * Each wordListId is unique for a particular day and search term.
	 * 
	 * @param wordListId
	 *            id of the wordList that the tweets belong to
	 * @param occursIn
	 *            list of tweet ids that belong to the wordlist
	 */
	private void updateWordListTweetsLookup(int wordListId, Object[] occursIn) {
		String sqlString = null;
		if (wordListId > 0 && occursIn.length > 0) {
			for (int i = 0; i < occursIn.length; i++) {
				// check if tweet is already in database for that
				// wordListId
				try {
					sqlString = "select id from WordListTweetsLookup where wordListId='"
							+ wordListId
							+ "' and tweetId='"
							+ occursIn[i]
							+ "'";
					rs = stmt.executeQuery(sqlString);
				} catch (SQLException e) {
					System.err
							.println("tBuildWordLists: checking for record in WordListTweetsLookup failed at query : "
									+ sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
				try {
					if (!rs.next()) {
						sqlString = "0,";
						sqlString += " '" + wordListId + "',";
						sqlString += " '" + occursIn[i] + "')";
						try {
							stmt.executeUpdate("insert into WordListTweetsLookup values  ("
									+ sqlString);
						} catch (SQLException e) {
							System.err
									.println("tBuildWordLists: Insert into WordListTweetsLookup failed.");
							System.err.println(sqlString);
							e.printStackTrace();
							System.exit(-1);
						}
					} else {
						// don't need to do anything if it already exists
					}
				} catch (SQLException e) {
					// !rs.next
					System.err
							.println("tBuildWordLists: rs.next failed for search");
					System.err.println("sqlString: " + sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} else {
			System.err
					.println("tBuildWordLists: can't update WordListTweetsLookup table without valid wordListId ("
							+ wordListId
							+ ") and occursIn array (length="
							+ occursIn.length + ").");
			System.exit(-1);
		}
	}

	/**
	 * Add words into CoOccurancesList table in mySQL database for each day and
	 * search term.
	 * 
	 * @param wordMap
	 *            <CODE>CoOccuranceList</CODE> object with all the co-occurring
	 *            words for that search id and date
	 * @param searchId
	 *            search id the tweets the words occurred in match
	 * @param date
	 *            the day being processed
	 */
	private void updateCoOccuranceTable(CoOccurrenceList wordMap, int searchId,
			String date) {
		String sqlString = null;

		if (wordMap.length() > 0) {
			Set<String> wordPairKeys = wordMap.keySet();
			String key;
			String[] keyParts = new String[2];
			Iterator<String> it = wordPairKeys.iterator();
			while (it.hasNext()) {
				key = it.next();
				keyParts = key.split("-");
				// check if co-occurance is already in database for that
				// keyword and date
				try {
					sqlString = "select frequency from CoOccuranceList where searchId='"
							+ searchId
							+ "' and date='"
							+ date
							+ "' and word1='"
							+ keyParts[0]
							+ "' and word2='"
							+ keyParts[1] + "'";
					rs = stmt.executeQuery(sqlString);
				} catch (SQLException e) {
					System.err
							.println("tBuildWordLists: checking for record in CoOccuranceList failed at query : "
									+ sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
				try {
					if (!rs.next()) {
						sqlString = "0,";
						sqlString += " '" + searchId + "',";
						sqlString += "'" + date + "',";
						sqlString += " '" + keyParts[0] + "',";
						sqlString += " '" + keyParts[1] + "',";
						sqlString += " '"
								+ wordMap.wordCoFrequency(keyParts[0],
										keyParts[1]) + "')";
						try {
							stmt.executeUpdate("insert into CoOccuranceList values  ("
									+ sqlString);
						} catch (SQLException e) {
							System.err
									.println("tBuildWordLists: Insert into CoOccuranceList failed.");
							System.err.println(sqlString);
							e.printStackTrace();
							System.exit(-1);
						}
					} else {
						// compare old & new frequency here
						if (rs.getInt("frequency") != wordMap.wordCoFrequency(
								keyParts[0], keyParts[1])) {
							System.err
									.println("BuildWordLists: existing CoWordList record frequency ("
											+ rs.getInt("frequency")
											+ ") doesn't match new frequency ("
											+ wordMap.wordCoFrequency(
													keyParts[0], keyParts[1])
											+ ")!");
							System.exit(-1);
						}

					}
				} catch (SQLException e) {
					// !rs.next
					System.err
							.println("tBuildWordLists: rs.next failed for search");
					System.err.println("sqlString: " + sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}

	/**
	 * Open the twitter_stream_archive mySQL database
	 */
	// This is lifted straight from UpdateSearchTermIndexing (I've changed
	// it to add slow, lower memory use style connection for tweets)
	// and a separate connection for tweets so can jump back to start of day and
	// re-process them for co-occurence
	// TODO: re-factor this & UpdateSearchTermIndexing one with one in
	// WriteToTwitterStreamArchive
	private void openSQLDataBase() {
		dataBaseOk = true;
		
		String database = null;
		String dbUser = null;
		String dbPass = null;

		// SQL variables
		Properties prop = new Properties();
		try {
			//load a properties file
			prop.load(new FileInputStream("tArchiver.properties"));
			// Email account for error messages
			database = prop.getProperty("database");
			dbUser = prop.getProperty("dbUser");
			dbPass = prop.getProperty("dbPassword");
		} catch (IOException ex) {
			System.err
			.println("tBuildWordLists: failed to load database properties from tArchiver.properties");
			ex.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}

		// Register the JDBC driver for MySQL.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err
					.println("tBuildWordLists: SQL driver not found!");
			e1.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
		stmt = null;
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
					+ database, dbUser, dbPass);
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("tBuildWordLists: Database connection failed! db: "
							+ database);
			dataBaseOk = false;
			System.exit(-1);
		}
		try {
			stmt = con.createStatement();
			stmtSearches = (Statement) con.createStatement();
		} catch (SQLException e1) {
			System.err
					.println("tBuildWordLists: failed to create an empty SQL statement");
			e1.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
		// second connection for tweets
		Connection con2 = null;
		try {
			con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
					+ database, dbUser, dbPass);
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("tBuildWordLists: Database 2 connection failed! db: "
							+ database);
			dataBaseOk = false;
			System.exit(-1);
		}
		try {
			// Has to use a separate connection to the database,
			// "No statements may be issued when any streaming result sets are open and in use on a given connection."
			stmtTweets = (Statement) con2.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					// java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmtTweets.setFetchSize(Integer.MIN_VALUE);
		} catch (SQLException e1) {
			System.err
					.println("tBuildWordLists: failed to create an empty SQL statement");
			e1.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
	}

	/**
	 * Check if database is ready to use.
	 * 
	 * @return <CODE>true</CODE> if the database is open and ready to use,
	 *         <CODE>false</CODE> otherwise
	 */
	private Boolean isDatabaseReady() {
		return dataBaseOk;
	}

}
