package au.net.moon.tUtils;
/**
 * SearchFilter - load the search and track filters from ./data/searches.txt
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Load the search and track filters from ./data/searches.txt.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class SearchFilter {
	long[] searchIds;
	String[] searchQueries;
	Date searchFileDate;
	String fullFileName = "data/searches.txt";

	public SearchFilter() {
		System.out
				.println("tStreamingArchiver: Loading search queries...");
		loadSearches();
	}

	/**
	 * Reload searches if the searches.txt file date is newer than currently
	 * loaded searches.
	 * 
	 * @return <CODE>true</CODE> if the searches file date is newer than the
	 *         loaded searches, <CODE>false</CODE> otherwise
	 */
	public boolean update() {
		boolean newSearches = false;
		File filename = new File(fullFileName);
		if (filename.exists()) {
			if (searchFileDate.before(new Date(filename.lastModified()))) {
				System.out
						.println("Searches File is newer than loaded searches...");
				loadSearches();
				newSearches = true;
			}
		}
		return newSearches;
	}

	/**
	 * Read in the searches.txt file and put search keywords into searchQueries
	 * class field and user ids into searchIds class field
	 */
	public void loadSearches() {
		// original searches.txt text file was made by running SQL query
		// "select id, query from searches where active = 1 and type = 'atlas'"
		// on old searchAPI database

		ArrayList<String> searchQueryAL = new ArrayList<String>();
		ArrayList<Integer> searchPeopleIdsAL = new ArrayList<Integer>();
		try {
			File filename = new File(fullFileName);
			if (filename.exists()) {
				searchFileDate = new Date(filename.lastModified());
				BufferedReader input = new BufferedReader(new FileReader(
						fullFileName));
				try {
					String[] lineParts;
					int i = 0;
					String line = null; // not declared within while loop
					while ((line = input.readLine()) != null) {
						// split up the line here
						lineParts = line.split("\t");
						if (lineParts.length == 3) {
							// add people and keywords to keyword search
							// otherwise miss out on oldstyle RT and reply
							searchQueryAL.add(lineParts[1]);
							if (!lineParts[2].equals("k")) {
								searchPeopleIdsAL.add(Integer
										.parseInt(lineParts[2]));
							}
						} else {
							System.err
									.println("Error: Unexpected data in searches.txt : "
											+ line);
						}
						i++;
					}
					searchQueries = new String[searchQueryAL.size()];
					searchQueryAL.toArray(searchQueries);
					searchIds = new long[searchPeopleIdsAL.size()];
					searchPeopleIdsAL.toArray();
					for (int x = 0; x < searchIds.length; x++) {
						searchIds[x] = searchPeopleIdsAL.get(x).intValue();
					}

					System.out.println("Found " + i + " search strings ("
							+ searchQueries.length
							+ ") search ids (usernames) (" + searchIds.length
							+ ")");
				} finally {
					input.close();
				}
			} else {
				System.out.println("Error: searches.txt file missing.");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Return the search keywords.
	 * 
	 * @return list of search queries
	 */
	public String[] trackArray() {
		return searchQueries;
	}

	/**
	 * Return the Twitter id's to search for.
	 * 
	 * @return list of Twitter user ids to track
	 */
	public long[] followArray() {
		return searchIds;
	}
}
