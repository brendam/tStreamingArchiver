package au.net.moon.tSearchImport;
/**
 * tSearchImport - Import SearchAPI results from Search mySQL database into StreamAPI mySQL database
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
import java.util.HashMap;
import java.util.Properties;

import au.net.moon.tUtils.RedirectSystemLogs;
import au.net.moon.tUtils.SearchFilter;
import au.net.moon.tUtils.WriteToTwitterStreamArchiveSQL;

/**
 * Import SearchAPI results from the Search mySQL database into the main mySQL
 * database. Looks at whole database every time!
 * Contains hard coded searches that won't be relevant for other people
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
 * @version 0.45
 */
public class SearchImport {
	static Statement stmt;
	static ResultSet rs;
	boolean dataBaseOk;

	/**
	 * Setup logging and run SearchImport.
	 * 
	 * @param args
	 *            none
	 */
	public static void main(String[] args) {
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("SearchImport.%g.log");
		}
		System.out.println("SearchImport: Program Starting... (v0.45)");

		new SearchImport();

		System.out.println("SearchImport: Program finished");

	}

	SearchImport() {
		int counter = 0;
		HashMap<String, String> tweet;
		// open the search file
		SearchFilter searchFilter = new SearchFilter();
		// get all the single keyword searches from the searches.txt file
		String[] tempFilters = searchFilter.trackArray();
		String[] keywords = new String[tempFilters.length + 1];
		System.arraycopy(tempFilters, 0, keywords, 0, tempFilters.length);
		// add the two word keyword used in search and not in stream...
		// "Murray Darling" is currently the only search running (because it is
		// two word).
		// TODO: Look at whether searchAPI new multi word support would let
		// "murray darling" work through search now.
		// TODO: Hard coded search - Find better way to handle this!
		keywords[tempFilters.length] = "Murray Darling";
		tempFilters = null;

		// Open the old twitter_archive database
		openSQLDataBase();
		WriteToTwitterStreamArchiveSQL mySQL = new WriteToTwitterStreamArchiveSQL(
				"Search");
		if (mySQL.isDatabaseReady() && isDatabaseReady()) {
			for (int i = 0; i < keywords.length; i++) {
				// loop through searches.txt selecting the tweets from the
				// search database & writing them to the new database
				try {
					// TODO: need to empty the database after import
					// TODO: this database still has varchar field, so will be
					// loosing any asian characters in tweets gathered through
					// search.
					// TODO: if change tweet field in search database to
					// varbinary need to change this search to be case
					// insensitive.
					rs = stmt
							.executeQuery("select tweets.* from tweets inner join archive inner join searches on archive.search_id = searches.id and tweets.id = archive.tweet_id where query = '"
									+ keywords[i] + "'");
					while (rs.next()) {
						tweet = new HashMap<String, String>();
						tweet.put("text", rs.getString("text"));
						tweet.put("id", rs.getString("id"));
						tweet.put("source", rs.getString("source"));
						tweet.put("createdAt", rs.getString("created_at"));
						tweet.put("toUserIdSearch", rs.getString("to_user_id"));
						tweet.put("inReplyToScreenName",
								rs.getString("to_user"));
						tweet.put("fromUserIdSearch",
								rs.getString("from_user_id"));
						tweet.put("fromUserScreenName",
								rs.getString("from_user"));
						tweet.put("hasGeoCode", rs.getString("hasGeoCode"));
						tweet.put("latitude", rs.getString("latitude"));
						tweet.put("longitude", rs.getString("longitude"));
						// not storing iso_language_code or record_add_date
						mySQL.tweetToSQL(tweet);
						counter++;
						System.out.println("Another record added: " + tweet);
					}
					if (rs.wasNull()) {
						System.out
								.println("SearchImport: No deletion notices in the table \"deletionNotices\"");
					} else {
						System.out.println("SearchImport: Found " + counter++
								+ " tweets for keyword " + keywords[i]);
						counter = 0;
					}
				} catch (SQLException e) {
					System.err.println("SearchImport: select for search "
							+ keywords[i] + " failed at query.");
					e.printStackTrace();
				}
			}
		}
	}

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
			.println("SearchImport: failed to load database properties from tArchiver.properties");
			ex.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
		
		// Register the JDBC driver for MySQL.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("SearchImport: SQL driver not found!");
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
			System.err.println("SearchImport: Database connection failed! db: "
					+ searchAPIdatabase);
			dataBaseOk = false;
		}
		try {
			// stmt = (Statement) con.createStatement();
			// the connection below will be slower, but copes with the
			// large datasets of the original import
			stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);

		} catch (SQLException e1) {
			System.err
					.println("SearchImport: failed to create an empty SQL statement");
			e1.printStackTrace();
			dataBaseOk = false;
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
