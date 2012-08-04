package au.net.moon.tTwapperKeeperImport;
/**
 * tTwapperKeeperImport - imports data from twapperkeeper archives
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import au.net.moon.tUtils.RedirectSystemLogs;
import au.net.moon.tUtils.WriteToTwitterStreamArchiveSQL;

/**
 * Imports data from twapperkeeper archives
 * 
 * @deprecated not tested with new fields in users and tweets because Twapper
 *             Keeper archives are not available anymore due to Twitter
 *             changing terms of use.
 * @author Brenda Moon - brenda at moon.net.au
 * @version 0.22
 * 
 */
public class TwapperKeeperImport {

	public static void main(String[] args) {
		// Expects data file from twapperKeeper called "twapperKeeperData.txt"
		Boolean debug = false;
		if (!debug) {
			new RedirectSystemLogs("TwapperKeeperImport.%g.log");
		}
		System.out.println("TwapperKeeperImport: Program Starting... (v0.22)");

		new TwapperKeeperImport();
		System.out.println("TwapperKeeperImport: Program finished");
	}

	TwapperKeeperImport() {
		DataInputStream in;
		BufferedReader br;
		int counter;
		File dataFile;
		String status;

		dataFile = new File("twapperKeeperData.txt");
		counter = 0;

		WriteToTwitterStreamArchiveSQL mySQL = new WriteToTwitterStreamArchiveSQL(
				"TwapperKeeper");

		if (mySQL.isDatabaseReady()) {

			// open the file
			try {
				FileInputStream fstream = new FileInputStream(dataFile);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in, "UTF8"));
				System.out.println("File Opened: " + dataFile.getPath());

				// process the file
				status = "";
				while ((status = br.readLine()) != null) {
					if (counter > 0) { // skip the first line with the field
										// names
						// process the status line here
						String[] splitStatus = status.split(",");

						HashMap<String, String> tweet = new HashMap<String, String>();
						// text
						tweet.put("text", splitStatus[0]);
						// to_user_id
						tweet.put("toUserIdSearch", splitStatus[1]);
						// from_user
						tweet.put("fromUserScreenName", splitStatus[2]);
						// id
						tweet.put("id", splitStatus[3]);
						// from_user_id
						tweet.put("fromUserIdSearch", splitStatus[4]);
						// iso_language_code - discarding this because
						// only available from search
						// System.out.println("isoLang" + splitStatus[5]);
						// source
						tweet.put("source", splitStatus[6]);
						// profile_image_url - this is part of the user
						// data, so will get it when we
						// look up the data
						tweet.put("profileImageUrl", splitStatus[7]);
						// geo_type
						if (!splitStatus[8].isEmpty()) {
							tweet.put("geoType", splitStatus[8]);
							// geo_coordinates_0
							tweet.put("latitude", splitStatus[9]);
							// geo_coordinates_1
							tweet.put("longitude", splitStatus[10]);
						}
						// created_at
						tweet.put("createdAt", splitStatus[11]);
						// time - this is the unix time version of the
						// createdAt field
						tweet.put("unixTime", splitStatus[12]);

						mySQL.tweetToSQL(tweet);
					}
					counter++;
				}

				// close the file
				in.close();

				// rename the file;
				// dataFile.renameTo(new File(dataFile.getPath() + ".bak"));

			} catch (IOException e) {
				System.err.println(e);
				System.err.println("fileName= " + dataFile.getPath() + "\n");
			}
		}
	}
}
