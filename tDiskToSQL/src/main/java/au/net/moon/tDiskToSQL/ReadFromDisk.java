package au.net.moon.tDiskToSQL;
/**
 * ReadFromDisk - read the save Twitter JSON data from the disk
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Load the saved raw Twitter data from disk in date-time order (oldest first).
 * The JSON objects have been saved to disk in a date based directory structure,
 * each file has the time it was created in the file name (HH-mm-ss.txt). When
 * finished with each file, rename it from .txt to .bak to prevent reprocessing.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 */

public class ReadFromDisk {
	private DataInputStream in;
	private BufferedReader br;
	// counter is being used, don't know why eclipse thinks it isn't
	private int counter;
	private String mySeperator = ("=====================================");
	private ArrayList<File> dataFiles;
	private java.util.Iterator<File> itr;
	private File dataFile;
	private String status;

	ReadFromDisk() {
		// initialise the list of dataFiles;
		dataFiles = findDataFiles(new File("data/"));
		System.out.println("Files Found: " + dataFiles.size());
		if (dataFiles.size() < 1) {
			System.out.println("tDiskToSql: no data files to process");
		} else {
			Collections.sort(dataFiles, new Comparator<File>() {
				public int compare(File file1, File file2) {
					if (file2.lastModified() < file1.lastModified()) {
						return 1;
					} else {
						return -1;
					}
				}
			});

			// remove the newest file from the list
			// because it may still be being written to by the archiver
			dataFiles.remove(dataFiles.size() - 1);
			if (dataFiles.size() > 0) {
				// initialise the iterator for the files
				itr = dataFiles.iterator();
				// Open the first file
				dataFile = itr.next();
				openFile(dataFile);
			} else {
				System.out.println("tDiskToSql: no data files to process");
			}
		}
	}

	/**
	 * Open the selected data file.
	 * 
	 * @param fileName
	 *            the <CODE>File</CODE> to open
	 */
	private void openFile(File fileName) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in, "UTF8"));
			System.out.println("tDiskToSql: File Opened: " + fileName.getPath());
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("\ntDiskToSql: fileName= " + fileName.getPath() + "\n");
		}
	}

	/**
	 * Close the selected data file & rename it to .bak
	 * 
	 * @param fileName
	 *            The <CODE>File</CODE> to close
	 */
	private void close(File fileName) {
		try {
			in.close();
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("fileName=" + fileName.getPath());
		}

		fileName.renameTo(new File(fileName.getPath() + ".bak"));
		// System.out.println("TwitterDiskToSql: new file name: " + fileName.getPath() + ".bak");
	}

	/**
	 * Get next available status to process (updates status property).
	 * 
	 * @return <CODE>true</CODE> if there is another status available,
	 *         <CODE>false</CODE> otherwise
	 */
	public Boolean nextStatus() {
		String strLine = null;
		Boolean nextStatus = true;
		status = "";
		try {
			if (br == null) {
				// no file has been opened
				nextStatus = false;
			}
			while (nextStatus && status.length() == 0) {
				Boolean buildStatus = true;
				while (buildStatus && (strLine = br.readLine()) != null) {
					if (!strLine.equals(mySeperator)) {
						status += strLine;
					} else {
						buildStatus = false;
					}
					counter++;
				}
				if (strLine == null) {
					// get next file
					close(dataFile);
					if (itr.hasNext()) {
						dataFile = itr.next();
						openFile(dataFile);
					} else {
						// no more data
						nextStatus = false;
					}
				}
			}
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("fileName=" + dataFile.getPath());
		}
		return (nextStatus);
	}

	/**
	 * Get the current Twitter status json string
	 * 
	 * @return a single Twitter status json string
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Get the <CODE>Date</CODE> of the data file from the file name. The files
	 * are named HH-mm-ss.txt and held in a directory structure /yyyy/MM/dd/ and
	 * this is used to calculate a <CODE>Date</CODE>
	 * 
	 * @return <CODE>Date</CODE> of the current data file.
	 */
	private Date fileNameAsDate() {
		Date fileNameDate = new Date();
		SimpleDateFormat myFormatterIn = new SimpleDateFormat(
				"'data'/yyyy/MM/dd/HH-mm-ss'.txt'");
		myFormatterIn.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			fileNameDate = myFormatterIn.parse((dataFile.getPath()));
		} catch (ParseException e1) {
			System.err
			.println("tDiskToSql: parsing created at date failed. (Stream API)");
			e1.printStackTrace();
			System.exit(-1);
		}
		return fileNameDate;
	}

	/**
	 * @return <CODE>Date</CODE> of the current data file
	 */
	public Date dataFileDate() {
		return fileNameAsDate();
	}

	/**
	 * Iterate down from the top directory provided finding all the files to be
	 * processed.
	 * 
	 * @param dir
	 *            The directory to find the files in
	 * @return <CODE>ArrayList</CODE> of data files to be processed.
	 */
	private static ArrayList<File> findDataFiles(File dir) {
		String pattern = ".txt";
		ArrayList<File> dataFiles = new ArrayList<File>();
		File listFile[] = dir.listFiles();
		if (listFile != null) {
			for (int i = 0; i < listFile.length; i++) {
				if (listFile[i].isDirectory()) {
					dataFiles.addAll(findDataFiles(listFile[i]));
				} else {
					if (listFile[i].getName().endsWith(pattern)) {
						if (!listFile[i].getPath().endsWith("searches.txt")) {
							dataFiles.add(listFile[i]);
						}
					}
				}
			}
		}
		return dataFiles;
	}

}
