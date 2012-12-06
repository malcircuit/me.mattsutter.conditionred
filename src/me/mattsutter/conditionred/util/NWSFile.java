package me.mattsutter.conditionred.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;

/** Class that deals with all HTTP GET requests.  Main function is to download the most recent file 
 * 	in a product directory of the NWS server.
 * 
 * @author Matt Sutter
 *
 */
public class NWSFile {

//	private static final int MIN_FILE_NUM = 0;
	public static final int MAX_SEQ_NUM = 250;

	// Common URLs
	public static final String NWS_BASE_URL = "http://weather.noaa.gov/pub/SL.us008001/";
	public static final String RADAR_PROD_URL = "DF.of/DC.radar/";
	public static final String TOR_WARN_URL = "DF.c5/DC.textf/DS.torwf/";
	public static final String SVR_T_WARN_URL = "DF.c5/DC.textf/DS.svrwu/";
	public static final String SVR_STATE_URL = "DF.c5/DC.textf/DS.svsww/";
	public static final String FLOOD_WARN_URL = "DF.c5/DC.textf/DS.ffwwg/";
	public static final String INDEX_HTML_ARGS = "?C=M;O=D";

	public static final String DATA_DIR = "/condition_red";
	public static final String DEBUG_DIR = "/product_samples";
	public static final String SD_ERROR = "SD card is missing or not mounted";
	
	private NWSFile(){	

	}

	public static DataInputStream getDebugProduct(String file_name) throws Exception{
		DataInputStream product;
		File file;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			String file_path = Environment.getExternalStorageDirectory().toString() + DATA_DIR + DEBUG_DIR;
			file = new File(file_path, file_name);
			product = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		}
		else
			throw new Exception(SD_ERROR);

		return product;
	}
	
	/**
	 * Downloads the requested product file.  Keeps it in memory.
	 * @param _database
	 * @param product_type
	 * @param angle
	 * @param site
	 * @param polling - Whether or not you are using animation.
	 * @return The requested file in the form of a BufferedReader.
	 */
	public static DataInputStream getProductfile(int product_type ,int angle, 
			String site, int seq_num) throws Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ DatabaseQuery.getProductURL(product_type, angle) 
							+ DatabaseQuery.getSiteURL(site);
		
		String file_name = String.format("sn.%04d", seq_num);
		
		return getProductFile(full_url + file_name);
	}
	
	/**
	 * Downloads the requested product file.  Keeps it in memory.
	 * @param _database
	 * @param product_type
	 * @param angle
	 * @param site
	 * @param polling - Whether or not you are using animation.
	 * @return The requested file in the form of a BufferedReader.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static DataInputStream getProductfile(String prod_url, String site, int seq_num) throws IOException, URISyntaxException{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ prod_url
							+ DatabaseQuery.getSiteURL(site);
		
		String file_name = String.format("sn.%04d", seq_num);
		
		return getProductFile(full_url + file_name);
	}
	
	/**
	 * Downloads the requested product file.  Keeps it in memory.
	 * @param _database
	 * @param product_type
	 * @param angle
	 * @param site
	 * @param polling - Whether or not you are using animation.
	 * @return The requested file in the form of a BufferedReader.
	 */
	public static DataInputStream getProductfile(int product_type ,int angle, 
			String site, boolean polling) throws Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ DatabaseQuery.getProductURL(product_type, angle) 
							+ DatabaseQuery.getSiteURL(site);
		
		String index_url = full_url + INDEX_HTML_ARGS;
		
		String file_name;
		
		if (polling)
			file_name = String.format("sn.%04d", findMostRecent(getHttpFile(index_url)));
		else
			file_name = "sn.last";
		
		return getProductFile(full_url + file_name);
	}


	public static DataInputStream getProductfile(String site, String product_url, boolean polling) throws Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ product_url 
							+ DatabaseQuery.getSiteURL(site);
		
		String index_url = full_url + INDEX_HTML_ARGS;
		
		String file_name;
		
		if (polling)
			file_name = String.format("sn.%04d", findMostRecent(getHttpFile(index_url)));
		else
			file_name = "sn.last";

		return getProductFile(full_url + file_name);
	}

	//TODO: documentation
	/**
	 * Downloads the requested product file.  Keeps it in memory.
	 * @param _database
	 * @param product_type
	 * @param angle
	 * @param city 
	 * @param state (two letter acronym, e.g. "AK")
	 * @param polling - Whether or not you are using animation.
	 * @return The requested file in the form of a BufferedReader.
	 */
	public static DataInputStream getProductFile(int product_type, int angle, String city, 
			String state, boolean polling) throws Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ DatabaseQuery.getProductURL(product_type, angle) 
							+ DatabaseQuery.getSiteURL(city, state);

		String index_url = full_url + INDEX_HTML_ARGS;

		String file_name;

		if (polling)
			file_name = String.format(Locale.US, "sn.%04d", findMostRecent(getHttpFile(index_url)));
		else
			file_name = "sn.last";

		return getProductFile(full_url + file_name);
	}
	
	public static int findMostRecent(String site, String product_url) throws IOException, URISyntaxException, Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ product_url 
							+ DatabaseQuery.getSiteURL(site);
		
		return findMostRecent(getHttpFile(full_url + INDEX_HTML_ARGS));
	}
	
	public static int findMostRecent(int product_type, int angle, String city, 
			String state, boolean polling) throws IOException, URISyntaxException, Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ DatabaseQuery.getProductURL(product_type, angle) 
							+ DatabaseQuery.getSiteURL(city, state);

		return findMostRecent(getHttpFile(full_url + INDEX_HTML_ARGS));
	}
	
	public static int findMostRecent(int product_type ,int angle, 
			String site, boolean polling) throws IOException, URISyntaxException, Exception{
		String full_url = 	NWS_BASE_URL 
							+ RADAR_PROD_URL 
							+ DatabaseQuery.getProductURL(product_type, angle) 
							+ DatabaseQuery.getSiteURL(site);

		return findMostRecent(getHttpFile(full_url + INDEX_HTML_ARGS));
	}
	
	//TODO: Fix documentation
	/** Takes a index.html file 
	 * (as supplied by a HTTPClient in the form of a BufferedReader) 
	 * and parses it to find the most recently updated 
	 * file in the directory. This was only designed to 
	 *	work within the context of the NWS servers.
	 * 
	 * @param index  - This is the index.html for the directory.
	 * 
	 * @return The file number of the most recent file (e.g. sn.0124).  
	 * If something went wrong it returns 0.
	 */
	private static int findMostRecent(BufferedReader index) throws Exception, IOException{
		String line;
		
		// Build the patterns that we need to match.
		final Pattern pat = Pattern.compile("sn.[0-9]{4}");
		final Pattern num = Pattern.compile("[0-9]{4}");
		final Pattern fail = Pattern.compile("</html>");
		Matcher match;
		
		String filename = null;
		
		// Loops through each line of index.html until it finds a match.
		// It's assuming the index is ordered by descending date.
		
		while(true){
			line = index.readLine();
			
			if (line == null)
				throw new Exception("Invalid file");
			
			match = pat.matcher(line);
			if (match.find()){
				filename = match.group();

				match = num.matcher(filename);
				if (match.find())
					return stringToInt(match.group());
			}

			match = fail.matcher(line);
			if (match.find())
				throw new Exception("Invalid file");
		}
	}

	//TODO: documentation
	/**
	 * Downloads the file at the given URL.
	 * @param url
	 * @return The file in the form of a DataInputStream.
	 * @throws IOException If something really screwed up.
	 * @throws URISyntaxException If the URL is malformed.
	 */
	public static DataInputStream getProductFile(String url) throws IOException, URISyntaxException{
		HttpClient client = new DefaultHttpClient();

		// Build a HTTP GET request packet.
		HttpGet request = new HttpGet();
		request.setURI(new URI(url));

		// Actually send the request.
		HttpResponse response = client.execute(request);

		DataInputStream out = new DataInputStream(response.getEntity().getContent());

		return out;
	}
	
	//TODO: Fix documentation
	/**
	 * Downloads the file at the given URL.
	 * @param url
	 * @return The file in the form of a BufferedReader.
	 * @throws IOException If something really screwed up.
	 * @throws URISyntaxException If the URL is malformed.
	 */
	public static BufferedReader getHttpFile(String url) throws IOException, URISyntaxException{
		HttpClient client = new DefaultHttpClient();

		// Build a HTTP GET request packet.
		HttpGet request = new HttpGet();
		request.setURI(new URI(url));

		// Actually send the request.
		HttpResponse response = client.execute(request);

		// Grab the interesting bits out of the response.
		return new BufferedReader(
				new InputStreamReader(response.getEntity().getContent())
		);
	}
	
	//TODO: documentation
	/**
	 * Takes a string of a set of numbers, e.g. "4950", and turns
	 * it into an actual integer.
	 * @param number  - String of the number you want.
	 * @return The integer value the characters in the string represent.  
	 */
	private static int stringToInt(String number) throws NumberFormatException{
		return Integer.parseInt(number.trim());
	}

	/**
	 * Reads the specified number of halfwords.
	 * @param data - The DataInputStream you want to read from.
	 * @param halfwords - The number of halfwords (2 bytes) you want to read from the buffer. 
	 * @return Integer array of the values of each halfword.
	 */
	public static int[] readHalfWords( DataInputStream data, int halfwords ){
		int[] buffer = new int[halfwords];
		int one;
		int two;
		
		for (int i=0; i < halfwords; i++){
			try{
				one = (int) data.readUnsignedByte();
				two = (int) data.readUnsignedByte();
				buffer[i] = (one << 8) + two;
			}
			catch (IOException e){
				//Log.e("readWords()", "Read failed", e);
			}
		}
		
		return buffer;
	}
}
