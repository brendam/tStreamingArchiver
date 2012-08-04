package au.net.moon.tGetMissingUsers;
/**
 * tGetMissingUsers - get missing Twitter users
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
import au.net.moon.tUtils.TUser;
import au.net.moon.tUtils.WriteToTwitterStreamArchiveSQL;

/**
 * Get missing Twitter users (tweets that don't have matching users) and add
 * them to users table in the mySQL database. Currently only looks at "AusVotes"
 * tweets. Twitter SearchAPI/TwapperKeeper Tweet doesn't return the to_user, so
 * can't use a tweet to get a missing to_user So this program only looks for
 * missing from_user's.
 * <p>
 * 
 * Shouldn't be needed for data that is collected via Stream api as this includes the user record.
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
public class GetMissingTwitterUsers {
	/**
	 * @param args
	 */
	Boolean dataBaseOk;
	static Statement stmt;
	static ResultSet rs;

	public static void main(String[] args) {
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("GetMissingTwitterUsers.%g.log");
		}
		System.out
				.println("GetMissingTwitterUsers: Program Starting... (v0.6)");

		new GetMissingTwitterUsers();
		System.out.println("GetMissingTwitterUsers: Program finished");
	}

	GetMissingTwitterUsers() {
		// only looked for missing "ausvotes" users at this stage
		String query = "ausvotes";
		int foundCounter = 0;
		int nameChangedCounter = 0;
		// Open database
		WriteToTwitterStreamArchiveSQL sql = new WriteToTwitterStreamArchiveSQL(
				"Status");
		if (sql.isDatabaseReady()) {
			openSQLDataBase();
			if (isDatabaseReady()) {
				ReadTwitter twitter = new ReadTwitter();
				// select tweets without user records - only once for each user
				System.out
						.println("GetMissingTwitterUsers: Searching for missing users");
				getTweetsWithoutUsers(query);
				System.out.println("GetMissingTwitterUsers: Search completed");
				// perform each search
				try {
					while (rs.next()) {
						String userName = rs.getString("from_user");
						long tweetId = rs.getLong("id");
						if (tweetId > 0) {
							TUser user = twitter.getUserFromTweet(tweetId);
							if (user != null) {
								if (sql.notInDB("users", "id",
										Long.toString(user.getId()))) {
									sql.storeUserInDB(user);
									foundCounter++;
									System.out
											.println("GetMissingTwitterUsers: user found: "
													+ user.getScreenName()
													+ " sourceAPI: "
													+ rs.getString("sourceAPI"));
									if (!userName.equals(user.getScreenName())) {
										System.err
												.println("GetMissingTwitterUsers: username found in tweet ("
														+ user.getScreenName()
														+ ") different from original tweet ("
														+ userName
														+ ") for tweetId="
														+ tweetId
														+ " sourceAPI: "
														+ rs.getString("sourceAPI"));

									}
								} else {
									// At some stage I might want to store these
									// name changes in the database?
									System.err
											.println("GetMissingTwitterUsers: tried to insert user that already exists. Userid="
													+ user.getId()
													+ ",  oldScreenName: "
													+ userName
													+ " sourceAPI: "
													+ rs.getString("sourceAPI"));
									nameChangedCounter++;
								}
							} else {
								System.out
										.println("GetMissingTwitterUsers: user not found on twitter: "
												+ userName
												+ " sourceAPI: "
												+ rs.getString("sourceAPI"));
							}
						}
					}
				} catch (SQLException e) {
					System.err.println("GetMissingTwitterUsers: e:"
							+ e.toString());
					e.printStackTrace();
				}
			}
			if (foundCounter > 0) {
				System.out.println("GetMissingTwitterUsers: Added "
						+ foundCounter + " missing users for query " + query);
			} else {
				System.out.println("GetMissingTwitterUsers: no users added");
			}
			if (nameChangedCounter > 0) {
				System.out.println("GetMissingTwitterUsers: Found "
						+ nameChangedCounter + " user name changes for query "
						+ query);
			}
		}
	}

	/**
	 * Select users that are from_user in tweets but not in users table. Updates
	 * the class 'rs' field.
	 * 
	 * @param searchKeyword
	 *            search keyword to look for tweets matching
	 */
	void getTweetsWithoutUsers(String searchKeyword) {
		// have to open a separate connection because adding the users closes
		// the result set otherwise.
		try {
			rs = stmt
					.executeQuery("select distinct from_user, tweets.id, tweets.sourceAPI from tweets inner join searchTermIndex inner join searches on tweets.id=searchTermIndex.tweetId  and searches.id=searchTermIndex.searchId left join users on users.screenName = tweets.from_user where users.screenName is null and searches.query=\""
							+ searchKeyword + "\" group by from_user;");
		} catch (SQLException e) {
			System.out.println("GetMissingTwitterUsers: e:" + e.toString());
		}
	}

	/**
	 * Open the twitter_stream_archive mySQL database
	 */
	// TODO refactor this with one in WriteToTwitterStreamArchive
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
			System.err.println("GetMissingTwitterUsers: SQL driver not found!");
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
					.println("GetMissingTwitterUsers: Database connection failed! db: "
							+ database);
			dataBaseOk = false;
			System.exit(-1);
		}
		try {
			stmt = (Statement) con.createStatement();
		} catch (SQLException e1) {
			System.err
					.println("GetMissingTwitterUsers: failed to create an empty SQL statement");
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
