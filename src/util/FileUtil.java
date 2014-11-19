package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Provides basic file related utility methods for opening, closing, reading, appending, writing text with or without line breaks, etc. 
 * @author gsanthan
 *
 */
public class FileUtil {	

	/**
	 * Writer for the Log file initialized to null and set by openLogFile
	 */
	static BufferedWriter log = null;
	
	/**
	 * Opens the specified file using a BufferedWriter
	 * @param fileName
	 * @return Writer for the opened file
	 * @throws IOException
	 */
	public static BufferedWriter openFile(String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
		return writer;
	}
	
	/**
	 * Opens the specified file using a BufferedReader
	 * @param fileName
	 * @return Reader for the opened file
	 * @throws IOException
	 */
	public static BufferedReader openFileForRead(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		return reader;
	}
	
	/**
	 * Opens the specified file using a BufferedWriter in append mode
	 * @param fileName
	 * @return Writer for the opened file
	 * @throws IOException
	 */
	public static BufferedWriter openFileForAppend(String fileName) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName),true));
		return writer;
	}

	/**
	 * Opens the specified file using a BufferedWriter in append mode, appends the line at the end of the file, and closes the file
	 * @param fileName
	 * @param line 
	 * @throws IOException
	 */
	public static void appendLineToFile(String fileName, String line) throws IOException {
		BufferedWriter writer = openFileForAppend(fileName);
		writeLineToFile(writer, line);
		closeFile(writer);
	}
	
	/**
	 * Writes the given line followed by a new line using the specified writer
	 * @param writer
	 * @param line
	 * @throws IOException
	 */
	public static void writeLineToFile(BufferedWriter writer, String line) throws IOException {
		writer.write(line);
		writer.newLine();
		writer.flush();
	}
	
	/**
	 * Writes the given line without new line using the specified writer
	 * @param writer
	 * @param line
	 * @throws IOException
	 */
	public static void writeToFile(BufferedWriter writer, String line) throws IOException {
		writer.write(line);
	}
	
	/**
	 * Closes the writer
	 * @param writer
	 * @throws IOException
	 */
	public static void closeFile(BufferedWriter writer) throws IOException {
		writer.close();
		writer=null;
	}

	/**
	 * Writes the specified string into the log file
	 * @param string
	 * @throws IOException
	 */
	public static void logResult(String string) throws IOException {
		writeToFile(log, string);
	}
	
	/**
	 * Opens the log file for writing in append mode and returns a BufferedWriter
	 * @param file
	 * @return Writer for the opened result file
	 * @throws IOException
	 */
	public static BufferedWriter openResultFile(String file) throws IOException {
		log = openFileForAppend(file+"final-results.txt");
		return log;
	}
	
	/**
	 * Opens the log file + suffix for writing in append mode and returns a BufferedWriter
	 * @param file
	 * @param suffix
	 * @return Writer for the opened result file
	 * @throws IOException
	 */
	public static BufferedWriter openResultFileWithSuffix(String file, String suffix) throws IOException {
		log = openFileForAppend(file+suffix);
		return log;
	}
	
	/**
	 * Loses the log file
	 * @throws IOException
	 */
	public static void closeResultFile() throws IOException {
		closeFile(log);
	}
	
	/**
	 * Writes a new line into the log file
	 * @throws IOException
	 */
	public static void logNewLine() throws IOException {
		writeLineToFile(log, "");
	}
	
	/**
	 * Copies the sourceFile to the destFile.
	 * If the destFile does not exist, a new one is created.
	 * Uses a FileChannel 'fast copy' implementation.
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		 if(!destFile.exists()) {
		  destFile.createNewFile();
		 }

		 FileChannel source = null;
		 FileChannel destination = null;
		 try {
		  source = new FileInputStream(sourceFile).getChannel();
		  destination = new FileOutputStream(destFile).getChannel();
		  destination.transferFrom(source, 0, source.size());
		 }
		 finally {
		  if(source != null) {
		   source.close();
		  }
		  if(destination != null) {
		   destination.close();
		  }
		}
	}
	
	/**
	 * Opens the given file and writes the specified lines one at a time, each followed by a new line. 
	 * @param lines
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeLinesToFile(String[] lines, String fileName) throws IOException {
		BufferedWriter writer = FileUtil.openFile(fileName);
		for (int i = 0; i < lines.length; i++) {
			FileUtil.writeLineToFile(writer, lines[i]);
		}
		FileUtil.closeFile(writer);
	}
	
	public static boolean deleteFileIfExists(String fileName) {
		File f = new File(fileName);
		if(f.exists()) {
			return f.delete();
		}
		return false;
	}
	
	public static String appendPaddedWordsAsLineToFile(String fileName, String[] words, int[] padLengths) throws IOException {
		BufferedWriter writer = openFileForAppend(fileName);
		String line = "";
		for(int i=0; i<words.length; i++) {
			if(words[i].matches("-?\\d+(\\.\\d+)?\\s+")) {
				line += StringUtil.padWithSpace(words[i],padLengths[i]);
			} else {
				line += StringUtil.padWithRightSpace(words[i],padLengths[i]);
			}
		}
		writeLineToFile(writer, line);
		closeFile(writer);
		return line;
	}
}
