package eu.europa.ec.eurostat.los.nuts;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NUTSChecker {

	private static Logger logger = LogManager.getLogger(NUTSChecker.class);

	public static void main(String[] args) throws IOException {
		checkIGNvsRAMON();
	}

	/**
	 * Checks the coherence of NUTS identification between IGN file and RAMON.
	 * @throws IOException 
	 */
	public static void checkIGNvsRAMON() throws IOException {

		SortedMap<String, String> ignNUTS = new TreeMap<String, String>();
		SortedMap<String, String> ramonNUTS = new TreeMap<String, String>();

		// Read RAMON NUTS
		logger.info("Reading RAMON NUTS from file " + Configuration.RAMON_NUTS_2013_FILE_NAME);
		Reader in = new FileReader(Configuration.RAMON_NUTS_2013_FILE_NAME);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
		for (CSVRecord record : records) {
			int nutsLevel = Integer.parseInt(record.get("Level")) - 1; // NUTS level is actually 1 less than RAMON level...
			ramonNUTS.put(record.get("NUTS-Code"), Integer.toString(nutsLevel) + " | " + record.get("Description"));
		}
		// Read IGN NUTS
		logger.info("Reading IGN NUTS from file " + Configuration.IGN_NUTS_FILE_NAME);
		in = new FileReader(Configuration.IGN_NUTS_FILE_NAME);
		records = CSVFormat.TDF.withQuote(null).withHeader().parse(in);
		for (CSVRecord record : records) ignNUTS.put(record.get("nuts_id"), record.get("stat_level") + " | " + record.get("name_latn"));

		// Codes in RAMON and not in IGN
		for (String ramonCode : ramonNUTS.keySet()) {
			if (!ignNUTS.containsKey(ramonCode)) System.out.println("Key in RAMON but not in IGN: " + ramonCode + ": " + ramonNUTS.get(ramonCode));
		}
		// Codes in IGN and not in RAMON
		for (String ignCode : ignNUTS.keySet()) {
			if (!ramonNUTS.containsKey(ignCode)) System.out.println("Key IGN but not in RAMON: " + ignCode + ": " + ignNUTS.get(ignCode));
		}
	}

}
