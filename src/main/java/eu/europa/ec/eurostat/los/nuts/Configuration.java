package eu.europa.ec.eurostat.los.nuts;

import java.util.HashMap;
import java.util.Map;

public class Configuration {

	// NB: source is at http://schemas.opengis.net/geosparql/1.0/geosparql_vocab_all.rdf
	public static final String GEOSPARQL_ONTOLOGY_FILE_RDF = "src/main/resources/data/geosparql_vocab_all.rdf";
	public static final String GEOSPARQL_ONTOLOGY_FILE_TTL = "src/main/resources/data/geosparql_vocab_all.ttl";

	public static final String COMMUNES_TTL = "src/main/resources/data/communesComplet.ttl";
	public static final String EXTRACT_TTL = "src/main/resources/data/communesComplet-extract.ttl";

	static String SPARQL_ENDPOINT = "http://rdf.insee.fr/sparql";
	public final static String[] AVAILABLE_SCALES = {"01", "03", "10", "20", "60"};
	public final static String CURRENT_SCALE = "01";

	public final static String NUTS_VERSION = "2013";

	// CSV file with NUTS borders in WKT, provided by IGN
	public static String IGN_NUTS_FILE_NAME = "src/main/resources/data/nuts" + CURRENT_SCALE + "m" + NUTS_VERSION + "wkt.csv";
	// Downloaded from http://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=LST_CLS_DLD&StrNom=NUTS_2016&StrLanguageCode=EN&StrLayoutCode=HIERARCHIC#
	public static String RAMON_NUTS_2013_FILE_NAME = "src/main/resources/data/NUTS_2013_20180814_130754.csv";
	// Downloaded from https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=LST_CLS_DLD&StrNom=NUTS_2016L&StrLanguageCode=EN&StrLayoutCode=HIERARCHIC#
	public static String RAMON_NUTS_2016_FILE_NAME = "src/main/resources/data/NUTS_2016_20190308_200523.csv";
	public static String RAMON_NUTS_FILE_NAME = NUTS_VERSION.equals("2013") ? RAMON_NUTS_2013_FILE_NAME : RAMON_NUTS_2016_FILE_NAME;

	// Output file
	public static final String NUTS_MODEL_FILE_NAME = "src/main/resources/data/nuts-" + NUTS_VERSION + "-" + CURRENT_SCALE + ".ttl";

	public static Map<String, String> REGION_NAMES_MAPPINGS = new HashMap<String, String>();
	static {
		REGION_NAMES_MAPPINGS.put("Grand Est", "ALSACE-CHAMPAGNE-ARDENNE-LORRAINE");
		REGION_NAMES_MAPPINGS.put("Hauts-de-France", "NORD-PAS DE CALAIS-PICARDIE");
		REGION_NAMES_MAPPINGS.put("Nouvelle-Aquitaine", "AQUITAINE-LIMOUSIN-POITOU-CHARENTES");
		REGION_NAMES_MAPPINGS.put("Occitanie", "LANGUEDOC-ROUSSILLON-MIDI-PYRÉNÉES");
	}

	public static String nutsURI(String nutsCode) {
		// return "http://ec.europa.eu/nuts/" + nutsCode.toLowerCase();
		return "http://ld.linked-open-statistics.org/data/conceptscheme/NutsRegion/" + nutsCode.toUpperCase(); // For Roma hackathon
	}
	public static String nutsGeometryURI(String nutsCode) {
		return nutsURI(nutsCode) + "/geometry";
	}
}
