package au.net.moon.tSearchArchiver;
/**
 * tSearchArchiver - Use Twitter SearchAPI to get tweets and put them directly into the Search mySQL database.
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
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
// import twitter4j.TwitterFactory;

import au.net.moon.tUtils.RedirectSystemLogs;
import au.net.moon.tUtils.twitterAuthorise;

/**
 * Search api is currently limited to about 11 days of results - so no use for
 * historic info. So this program archives the search results into a mySQL
 * database to build up an archive for the search term(s).
 * <p>
 * Search API is rate limited by IP address, and white listing is based on IP
 * address - so no advantage in authenticating before doing searches
 * (not sure this is true - I see a much higher limit when using a whitelisted oauth research account)
 * <p>
 * TODO Search back-offs: query less often for searches that have no results.<br>
 * TODO Search more often for terms that are approaching 1500 limit.
 * 
 * @param args
 * @author Brenda Moon
 * @version 1.00
 */
public class SearchArchiver {
	static Statement stmt;
	static ResultSet rs;
	boolean dataBaseOk;
	static Boolean debug;

	/**
	 * Setup logging and run SearchArchiver.
	 * 
	 * @param args
	 *            none
	 */

	public static void main(String[] args) {

		debug = false;
		if (!debug) {
			new RedirectSystemLogs("tSearchArchiverLog.%g.log");
		} else {
			System.out
			.println("tSearchArchiver: Logging to system log files");
		}

		System.out.println("tStreamArchiver: Program Starting... (v1.0)");

		new SearchArchiver();

		System.out.println("tSearchArchiver: Program finished");
	}

	SearchArchiver() {

		Twitter twitter;
		int pageNumber = 1;
		int waitBetweenRequests = 2000;
		// 2 sec delay between requests to avoid maxing out the API.
		Tweet tweet;
		Query query;
		QueryResult result;

		// String[] searches;
		ArrayList<String> searchQuery = new ArrayList<String>();
		ArrayList<Integer> searchId = new ArrayList<Integer>();

		int searchIndex;
		int totalTweets;
		SimpleDateFormat myFormatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss Z");

		System.out.println("tSearchArchiver: Loading search queries...");

		// Set timezone to UTC for the Twitter created at dates
		myFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		twitterAuthorise twitterAuth = new twitterAuthorise(false);
		twitter = twitterAuth.getTwitter();

		query = new Query();
		query.setRpp(100); // max number per page

		// Open the old twitter_archive database
		openSQLDataBase();

		if ( isDatabaseReady()) {

			// probably should have these in an object not separate arrays?
			try {
				rs = stmt
						.executeQuery("select * from searches where active = true");
				// perform each search
				while (rs.next()) {
					// if (searchQuery
					searchQuery.add(rs.getString("query"));
					searchId.add(rs.getInt("id"));
				}
				if (rs.wasNull()) {
					System.out
					.println("tSearchArchiver: No searches in the table \"searches\"");
					System.exit(30);
				} else {
					System.out.println("tSearchArchiver: Found "
							+ searchQuery.size() + " searches.");
				}
			} catch (SQLException e) {
				System.out.println("tSearchArchiver: e:" + e.toString());
			}

			searchIndex = 0;
			totalTweets = 0;

			// set initial value of i to start from middle of search set
			while (searchIndex < searchQuery.size()) {
				pageNumber = 1;
				query.setQuery(searchQuery.get(searchIndex));
				query.setPage(pageNumber);
				// check to see if their are any tweets already in the database for
				// this search
				long max_tw_id = 0;
				try {
					rs = stmt
							.executeQuery("select max(tweet_id) as max_id from archive where search_id = "
									+ searchId.get(searchIndex));
					if (rs.next()) {
						max_tw_id = rs.getLong("max_id");
						// System.out.println("MaxID: " + max_tw_id);
						query.setSinceId(max_tw_id);
					}
				} catch (SQLException e1) {
					System.err
					.println("tSearchArchiver: Error looking for maximum tweet_id for "
							+ query.getQuery() + " in archive");
					e1.printStackTrace();
				}
				// System.out.println("Starting searching for tweets for: " +
				// query.getQuery());

				int tweetCount = 0;
				Boolean searching = true;
				while (searching) {

					// ----
					// delay waitBetweenRequests milliseconds before making request
					// to make sure not overloading API
					try {
						Thread.sleep(waitBetweenRequests);
					} catch (InterruptedException e1) {
						System.err
						.println("tSearchArchiver: Sleep between requests failed.");
						e1.printStackTrace();
					}
					try {
						result = twitter.search(query);
					} catch (TwitterException e) {
						System.out.println(e.getStatusCode());
						System.out.println(e.toString());
						if (e.getStatusCode() == 503) {
							// TODO use the Retry-After header value to delay & then
							// retry the request
							System.out
							.println("tSearchArchiver: Delaying for 10 minutes before making new request");
							try {
								Thread.sleep(600000);
							} catch (InterruptedException e1) {
								System.err
								.println("tSearchArchiver: Sleep for 10 minutes because of API load failed.");
								e1.printStackTrace();
							}
						} else if (e.getMessage().contains(
								"page parameter out of range")) {
							// no more results, is there a better way to check this?
							System.out
							.println("tSearchArchiver Error: Page parameter out of range");
							searching = false;
							break;
						}
						result = null;
					}

					// -----

					if (result != null) {
						List<Tweet> results = result.getTweets();
						if (results.size() == 0) {
							searching = false;
						} else {
							tweetCount += results.size();
							// FIXME: get clean function from other program here
							for (int j = 0; j < results.size(); j++) {
								tweet = (Tweet) results.get(j);
								String cleanText = tweet.getText();
								cleanText = cleanText.replaceAll("'", "&#39;");
								cleanText = cleanText.replaceAll("\"", "&quot;");

								try {
									stmt.executeUpdate("insert into archive values (0, "
											+ searchId.get(searchIndex)
											+ ", '"
											+ tweet.getId() + "', now())");
								} catch (SQLException e) {
									System.err
									.println("tSearchArchiver: Insert into archive failed.");
									System.err.println(searchId.get(searchIndex)
											+ ", " + tweet.getId());

									e.printStackTrace();
								}
								// check if the tweet already exists, if not store
								// it.
								// println("tweet.getId(): " + tweet.getId());
								try {
									rs = stmt
											.executeQuery("select id from tweets where id = "
													+ tweet.getId());
								} catch (SQLException e) {
									System.err
									.println("tSearchArchiver: checking for tweet in tweets archive failed.");
									e.printStackTrace();
								}
								Boolean tweetNotInArchive = false;
								try {
									tweetNotInArchive = !rs.next();
								} catch (SQLException e) {
									System.err
									.println("tSearchArchiver: checking for tweet in archive failed at rs.next().");
									e.printStackTrace();
								}
								if (tweetNotInArchive) {
									String tempLangCode = "";
									if (tweet.getIsoLanguageCode() != null) {
										if (tweet.getIsoLanguageCode().length() > 2) {
											System.out
											.println("tSearchArchiver Error: IsoLanguageCode too long: >"
													+ tweet.getIsoLanguageCode()
													+ "<");
											tempLangCode = tweet
													.getIsoLanguageCode()
													.substring(0, 2);
										} else {
											tempLangCode = tweet
													.getIsoLanguageCode();
										}
									}
									double myLatitude = 0;
									double myLongitude = 0;
									int hasGeoCode = 0;

									if (tweet.getGeoLocation() != null) {
										System.out
										.println("GeoLocation: "
												+ tweet.getGeoLocation()
												.toString());
										myLatitude = tweet.getGeoLocation()
												.getLatitude();
										myLongitude = tweet.getGeoLocation()
												.getLongitude();
										hasGeoCode = 1;
									}
									Date tempCreatedAt = tweet.getCreatedAt();
									String myDate2 = myFormatter.format(
											tempCreatedAt, new StringBuffer(),
											new FieldPosition(0)).toString();
									totalTweets++;
									try {
										stmt.executeUpdate("insert into tweets values  ("
												+ tweet.getId()
												+ ", '"
												+ tempLangCode
												+ "', '"
												+ tweet.getSource()
												+ "', '"
												+ cleanText
												+ "', '"
												+ myDate2
												+ "', '"
												+ tweet.getToUserId()
												+ "', '"
												+ tweet.getToUser()
												+ "', '"
												+ tweet.getFromUserId()
												+ "', '"
												+ tweet.getFromUser()
												+ "', '"
												+ hasGeoCode
												+ "',"
												+ myLatitude
												+ ", "
												+ myLongitude
												+ ", now())");
									} catch (SQLException e) {
										System.err
										.println("tSearchArchiver: Insert into tweets failed.");
										System.err.println(tweet.getId() + ", '"
												+ tempLangCode + "', '"
												+ tweet.getSource() + "', '"
												+ cleanText + "', '" + myDate2
												+ "', '" + tweet.getToUserId()
												+ "', '" + tweet.getToUser()
												+ "', '" + tweet.getFromUserId()
												+ "', '" + tweet.getFromUser());

										e.printStackTrace();
									}
								}

							}
							pageNumber++;
							query.setPage(pageNumber);
						}
					}
				}

				if (tweetCount > 0) {
					System.out.println("tSearchArchiver: New Tweets Found for \""
							+ searchQuery.get(searchIndex) + "\" = " + tweetCount);
				} else {
					// System.out.println("tSearchArchiver: No Tweets Found for \""
					// + searchQuery.get(searchIndex) + "\" = " + tweetCount);
				}
				try {

					stmt.executeUpdate("update searches SET lastFoundCount="
							+ tweetCount + ", lastSearchDate=now() where id="
							+ searchId.get(searchIndex));
				} catch (SQLException e) {
					System.err
					.println("tSearchArchiver: failed to update searches with lastFoundCount="
							+ tweetCount
							+ " and datetime for search: "
							+ searchId.get(searchIndex));
					e.printStackTrace();
				}
				searchIndex++;
			}
			System.out.println("tSearchArchiver: Completed all "
					+ searchQuery.size() + " searches");
			System.out.println("tSearchArchiver: Archived " + totalTweets
					+ " new tweets");
		}
	}

	// TODO: This is used here and in SearchImport.java - refactor into Utils.	
	/**
	 * Open the searchAPI mySQL database
	 */
	private void openSQLDataBase() {
		dataBaseOk = true;
		String searchAPIdatabase = null;
		String dbUser = null;
		String dbPass = null;

		// SQL variables
		Properties prop = new Properties();
		try {
			//load a properties file
			prop.load(new FileInputStream("tArchiver.properties"));
			// Email account for error messages
			searchAPIdatabase = prop.getProperty("searchAPIdatabase");
			dbUser = prop.getProperty("dbUser");
			dbPass = prop.getProperty("dbPassword");
		} catch (IOException ex) {
			System.err
			.println("SearchArchiver: failed to load database properties from tArchiver.properties");
			ex.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}

		// Register the JDBC driver for MySQL.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("SearchArchiver: SQL driver not found!");
			e1.printStackTrace();
			dataBaseOk = false;
		}
		stmt = null;
		rs = null;
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
					+ searchAPIdatabase, dbUser, dbPass);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("SearchArchiver: Database connection failed! db: "
					+ searchAPIdatabase);
			dataBaseOk = false;
		}
		try {
			// stmt = (Statement) con.createStatement();
			// the connection below will be slower, but copes with the
			// large datasets of the original import
			// TODO: these lines are different in SearchImport which only needs read only access
			stmt = con.createStatement();
			// stmt.setFetchSize(Integer.MIN_VALUE);

		} catch (SQLException e1) {
			System.err
			.println("SearchArchiver: failed to create an empty SQL statement");
			e1.printStackTrace();
			dataBaseOk = false;
		}
	}

	// TODO: This is used here and in SearchImport.java - refactor into Utils.	
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