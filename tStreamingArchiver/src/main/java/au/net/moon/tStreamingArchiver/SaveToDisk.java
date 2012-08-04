package au.net.moon.tStreamingArchiver;
/**
 * SaveToDisk - save json objects to disk
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * The JSON objects are saved to disk in a date based directory structure. Each
 * file has the time it was created in the file name.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 */
public class SaveToDisk {
	PrintWriter pw;
	int counter;
	Date saveDate;

	SaveToDisk() {
		saveDate = new Date();
		openFile();
	}

	/**
	 * Open a new data file.
	 */
	private void openFile() {
		String fileName;
		fileName = getUniqueFileName();
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
			pw = new PrintWriter(osw);
			System.out
					.println("TwitterStreamingArchiver: new output file opened: "
							+ fileName);
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("fileName=" + fileName);
		}
	}

	/**
	 * Close the data file (not used)
	 */
	// void close() {
	// pw.flush();
	// pw.close();
	// }

	/**
	 * Save the Twitter tweet/deletion notice/track limitation notice to the
	 * current data file. Open a new data file if the existing one is more than
	 * 1 hour old.
	 * 
	 * @param jsonString
	 *            the Twitter message as a json string
	 */
	public void save(String jsonString) {
		// 1 hour roll over period
		final int rollPeriod = 1;
		Date newDate = new Date();
		int hoursDifference = (int) ((newDate.getTime() - saveDate.getTime()) / (1000 * 60 * 60));
		if (hoursDifference >= rollPeriod) {
			pw.flush();
			pw.close();
			saveDate = newDate;
			openFile();
			counter = 0;
		} else {
			pw.println(jsonString);
			pw.println("=====================================");
			pw.flush();
		}
	}

	/**
	 * Get a unique file name based on the current datetime.
	 * 
	 * @return unique file name
	 */
	String getUniqueFileName() {
		String path = getPath();
		String fileName = getFileName();
		// check if file exists before overwriting it!
		while (new File(path + "/" + fileName).exists()) {
			System.out.println("FileExists: " + path + "/" + fileName);
			saveDate = new Date();
			fileName = getFileName();
			path = getPath();
		}
		try {
			// make sure directory exists
			(new File(path)).mkdirs();
		} catch (Exception e) {
			System.err.println(e);
			System.err.println("directoryPath=" + path);
		}
		fileName = path + "/" + fileName;
		return fileName;
	}

	/**
	 * Data file path based on saveDate class field.
	 * 
	 * @return data file path
	 */
	String getPath() {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return ("data/" + df.format(saveDate));
	}

	/**
	 * Data file name based on saveDate class field.
	 * 
	 * @return data file name
	 */
	String getFileName() {
		DateFormat df = new SimpleDateFormat("HH-mm-ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return (df.format(saveDate) + ".txt");
	}
}
