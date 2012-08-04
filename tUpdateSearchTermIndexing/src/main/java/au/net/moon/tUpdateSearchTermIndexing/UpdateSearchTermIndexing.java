package au.net.moon.tUpdateSearchTermIndexing;
/**
 * tUpdateSearchTermIndexing - update searchTermIndex table in mySQL for faster lookups
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
import java.util.Properties;

import au.net.moon.tUtils.RedirectSystemLogs;

/**
 * Update the searchTermIndex table in the mySQL database for the search terms
 * to allow faster lookup of tweets by keyword/keyphrase.<br>
 * Goes through all the active search terms in the searches table and looks for
 * new tweets which contain the search term in their tweet text field. Adds the
 * found tweet ids to the searchTermIndex table.
 * <p>
 * 
 * NOTE: For large amounts of data, this approach is too slow. Better to use specific
 * indexing software like Apache Lucene to generate search indexes.
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
 * @version 0.50
 * 
 */
public class UpdateSearchTermIndexing {
	static Statement stmtSearches;
	static Statement stmtTweets;
	static Statement stmt;
	static ResultSet rsSearches;
	static ResultSet rsTweets;
	static ResultSet rs;
	boolean dataBaseOk;

	/**
	 * Setup logging and then run the search term indexing.
	 * 
	 * @param args
	 *            none
	 */
	public static void main(String[] args) {
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("UpdateSearchTermIndexing.%g.log");
		}
		System.out
				.println("UpdateSearchTermIndexing: Program Starting... (v0.50)");

		new UpdateSearchTermIndexing();

		System.out.println("UpdateSearchTermIndexing: Program finished");

	}

	UpdateSearchTermIndexing() {
		int totalCounter = 0;
		String sqlString = null;
		openSQLDataBase();
		if (isDatabaseReady()) {
			try {
				// hard wiring new queries to get them processed....
				// rsSearches = stmtSearches
				// .executeQuery("SELECT id, query from searches where query='wind'");
				rsSearches = stmtSearches
						.executeQuery("SELECT id, query from searches where active = 1");
				// .executeQuery("select tweets.* from tweets inner join archive inner join searches on archive.search_id = searches.id and tweets.id = archive.tweet_id where query = '"
				// + keywords[i] + "'");

				while (rsSearches.next()) {
					int searchId = rsSearches.getInt("id");
					String searchQuery = rsSearches.getString("query");
					String max_tw_id = "0";
					System.out.println("searchQuery: " + searchQuery);

					try {
						rs = stmt
								.executeQuery("select max(tweetId) as max_id from  searchTermIndex where searchId = "
										+ searchId);
						if (rs.next()) {

							max_tw_id = rs.getString("max_id");
							if (max_tw_id == null) {
								max_tw_id = "0";
							}
							System.out.println("MaxID: " + max_tw_id);
						}
					} catch (SQLException e) {
						System.err
								.println("UpdateSearchTermIndexing: Error looking for maximum tweetId for "
										+ searchQuery + " in searchTermIndex");
						e.printStackTrace();
					}

					try {
						String tempQuery = searchQuery;
						// special handling for the ausvotes subqueries that
						// start with "ausvotes "
						// if this is needed for another dataset, should add
						// field to search database that controls how multiword
						// searches are treated
						// ie: as phrases or as individual keywords
						// this treats the ausvotes multiword searches as sets
						// of keywords, not phrases.
						if (tempQuery.startsWith("ausvotes ")) {
							tempQuery = tempQuery.replace(" ",
									"%' AND text like '%");
						}
						// this is now case insensitive. (text is a varbinary
						// field so by default search is case sensitive)
						sqlString = "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where id > "
								+ max_tw_id
								+ " and CONVERT(text USING utf8) COLLATE utf8_general_ci like '%"
								+ tempQuery + "%'";
						// temporary version to get through the fever set...
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2010-01-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2010-06-01' and created_at_GMT > '2010-01-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2010-12-01' and created_at_GMT > '2010-06-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2011-06-01' and created_at_GMT > '2010-12-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2011-04-01' and created_at_GMT > '2011-02-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2011-06-01' and created_at_GMT > '2011-04-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2011-11-01' and created_at_GMT > '2011-06-01'"
						// sqlString =
						// "select id,  CONVERT(created_at_GMT,Date) as theDate from tweets where created_at_GMT < '2011-11-01' and created_at_GMT > '2011-08-01'"
						// +
						// " and CONVERT(text USING utf8) COLLATE utf8_general_ci like '%"
						// + tempQuery + "%'";
						rsTweets = stmtTweets.executeQuery(sqlString);
						int counter = 0;
						while (rsTweets.next()) {
							String tweetId = rsTweets.getString("id");
							String theDate = rsTweets.getString("theDate");
							// check if tweet is already indexed, and if not
							// put it into searchTermIndex table
							try {
								rs = stmt
										.executeQuery("select id from searchTermIndex where searchId="
												+ searchId
												+ " and tweetId="
												+ tweetId);
							} catch (SQLException e) {
								System.err
										.println("UpdateSearchTermIndexing: checking for record in searchTermIndex with tweetId="
												+ tweetId
												+ " and searchId="
												+ searchId
												+ " failed at query.");
								e.printStackTrace();
							}
							try {
								if (!rs.next()) {
									sqlString = "0,";
									sqlString += " '" + searchId + "',";
									sqlString += "'" + theDate + "',";
									sqlString += " '" + tweetId + "')";
									try {
										stmt.executeUpdate("insert into searchTermIndex values  ("
												+ sqlString);
										counter++;
									} catch (SQLException e) {
										System.err
												.println("UpdateSearchTermIndexing: Insert into searchTermIndex failed.");
										System.err.println(sqlString);
										e.printStackTrace();
										System.exit(-1);
									}
								}
							} catch (SQLException e) {
								System.err
										.println("UpdateSearchTermIndexing: checking for record in searchTermIndex with tweetId="
												+ tweetId
												+ " and searchId="
												+ searchId
												+ " failed at rs.next().");
								e.printStackTrace();
							}
						}
						System.out.println("UpdateSearchTermIndexing: Indexed "
								+ counter + " tweets for searchTerm: "
								+ searchQuery);

					} catch (SQLException e) {
						System.err
								.println("UpdateSearchTermIndexing: selecting tweets failed.");
						System.err.println("sqlString: " + sqlString);
						e.printStackTrace();
						System.exit(-1);
					}
					totalCounter++;
				}

				if (rsSearches.wasNull()) {
					System.out
							.println("UpdateSearchTermIndexing: No active searches found in the table \"searches\"");
				} else {
					System.out.println("UpdateSearchTermIndexing: Found "
							+ totalCounter + " active searches");
					totalCounter = 0;
				}
			} catch (SQLException e) {
				System.err
						.println("UpdateSearchTermIndexing: select searches failed at query.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Open the mySQL database
	 */
	// TODO: refactor this & dataBaseOK below, with WriteToTwitterStreamArchive
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
			.println("UpdateSearchTermIndexing: failed to load database properties from tArchiver.properties");
			ex.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}

		// Register the JDBC driver for MySQL.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err
					.println("UpdateSearchTermIndexing: SQL driver not found!");
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
					.println("UpdateSearchTermIndexing: Database connection failed! db: "
							+ database);
			dataBaseOk = false;
			System.exit(-1);
		}
		try {
			stmt = (Statement) con.createStatement();
			stmtSearches = (Statement) con.createStatement();
			stmtTweets = (Statement) con.createStatement();
		} catch (SQLException e1) {
			System.err
					.println("UpdateSearchTermIndexing: failed to create an empty SQL statement");
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
