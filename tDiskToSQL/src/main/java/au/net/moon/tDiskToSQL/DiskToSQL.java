package au.net.moon.tDiskToSQL;
/**
 * DiskToSQL - process raw Twitter data from disk inot the mySQL dataabase.
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

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.internal.json.DataObjectFactoryUtil;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.json.DataObjectFactory;
import au.net.moon.tUtils.RedirectSystemLogs;
import au.net.moon.tUtils.SimpleSSLMail;
import au.net.moon.tUtils.WriteToTwitterStreamArchiveSQL;
import au.net.moon.tUtils.twitterFields;
/**
 * Process the raw Twitter data from disk into the mySQL database.
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
 * For testing, to run in eclipse project, needs some data generated by tStreamingArchiver - copy 
 * ./data from that eclipse project into this one.
 * 
 * @version 0.87
 * @author Brenda Moon - brenda at moon.net.au
 */
public class DiskToSQL {
	static SimpleSSLMail sendMail;

	/**
	 * Setup logging and error handling and then process the raw Twitter data.
	 * 
	 * @param args
	 *            none
	 */
	public static void main(String args[]) {
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("tDiskToSQL.%g.log");
		}
		System.out.println("tDiskToSql: Program Starting... (v0.94)");
		new DiskToSQL();
		System.out.println("tDiskToSql: Program finished");
	}

	DiskToSQL() {
		sendMail = new SimpleSSLMail();
		Object statusObj = null;
		WriteToTwitterStreamArchiveSQL sql = new WriteToTwitterStreamArchiveSQL(
				"Stream");
		if (sql.isDatabaseReady()) {
			ReadFromDisk newRead = new ReadFromDisk();
			while (newRead.nextStatus()) {
				if (twitterFields.isDeletionNotice(newRead.getStatus())) {
					sql.processDeletionNotice(newRead.getStatus());
				} else if (twitterFields.isTrackLimitation(newRead.getStatus())) {
					sql.processTrackLimitation(newRead.getStatus(), newRead.dataFileDate());
				} else if (newRead.isStatusNewType()) { 
					// new style string json object saved by DataObjectFactory.getRawJSON(status)
					try {
						statusObj = DataObjectFactory.createObject(newRead.getStatus());
						DataObjectFactoryUtil.clearThreadLocalMap(); // Clear map of all JSON that factory stores to prevent heap overflow
					} catch (TwitterException e) {
						String message = "tDiskToSQL: couldn't convert json string to JSONObject: ";
						System.err.println(message + newRead.getStatus());
						sendMail.sendMessage(message, message + newRead.getStatus());
					} 
					if (statusObj instanceof Status) {
						//	new style status object
						sql.tweetToSQL((Status) statusObj);
					} 
				} else if (twitterFields.isTweet(newRead.getStatus())) {
					sql.tweetToSQL(newRead.getStatus());
				} else {
					String message = "tDiskToSQL: unexpected Twitter response: ";
					System.err.println(message + newRead.getStatus());
					sendMail.sendMessage(message, message + newRead.getStatus());
				}
			}
		}
	}
}
