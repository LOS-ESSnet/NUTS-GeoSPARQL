package eu.europa.ec.eurostat.los.nuts;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The <code>NUTSModelMaker</code> class creates the NUTS as GeoSPARQL-compliant RDF model.
 * 
 * @author Franck
 */
public class NUTSModelMaker {

	public static final String GEOSPARQL_URI = "http://www.opengis.net/ont/geosparql#";
	// Useful classes and properties from the GeoSPARQL ontology
	static Resource feature = ResourceFactory.createResource(GEOSPARQL_URI + "Feature");
	static Resource geometry = ResourceFactory.createResource(GEOSPARQL_URI + "Geometry");
	static Property hasGeometry = ResourceFactory.createProperty(GEOSPARQL_URI + "hasGeometry");
	static Property asWKT = ResourceFactory.createProperty(GEOSPARQL_URI + "asWKT");
	// XKOS depth property
	static Property depth = ResourceFactory.createProperty("http://rdf-vocabulary.ddialliance.org/xkos#depth");
	static String wktDatatypeURI = GEOSPARQL_URI + "wktLiteral";

	private static Logger logger = LogManager.getLogger(NUTSModelMaker.class);

	/**
	 * Creates the GeoSPARQL model with links to COG and saves it as a Turtle file.
	 * 
	 * @param args Not used.
	 * @throws Exception In case of problem.
	 */
	public static void main(String[] args) throws Exception {

		logger.info("Creating a NUTS RDF model for scale level " + Configuration.CURRENT_SCALE);
		Model nutsModel = getNUTSModel();
		nutsModel.add(getNUTSCOGMappings());
		RDFDataMgr.write(new FileOutputStream(Configuration.NUTS_MODEL_FILE_NAME), nutsModel, Lang.TURTLE);
		logger.info("NUTS model saved under " + Configuration.NUTS_MODEL_FILE_NAME);
		logger.info("Model contains " + nutsModel.size() + " statements");
	}

	/**
	 * Creates a GeoSPARQL-compliant RDF database with data on the NUTS.
	 * 
	 * @return The NUTS RDF database as a Jena model.
	 * @throws IOException In case of problem reading the source file.
	 */
	public static Model getNUTSModel() throws IOException {

		Model nutsModel = ModelFactory.createDefaultModel();
		nutsModel.setNsPrefix("rdfs", RDFS.getURI());
		nutsModel.setNsPrefix("geo", GEOSPARQL_URI);
		nutsModel.setNsPrefix("dc", DC.getURI());
		nutsModel.setNsPrefix("dcterms", DCTerms.getURI());
		nutsModel.setNsPrefix("xkos", "http://rdf-vocabulary.ddialliance.org/xkos#");

		// Read the RAMON file to get the official NUTS names
		logger.info("Reading NUTS codes and labels from file " + Configuration.RAMON_NUTS_FILE_NAME);
		// Store the RAMON codes for verification, as well as mappings between numeric and string codes
		SortedMap<String, String> ramonCodes = new TreeMap<String, String>(); // Example: 9 -> BE213
		// Store the mappings between numeric and string code so as to be able to construct hierarchy
		SortedMap<String, String> hierarchyMappings = new TreeMap<String, String>(); // Example: BE213 -> 6
		Reader in = new FileReader(Configuration.RAMON_NUTS_FILE_NAME);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
		for (CSVRecord record : records) {
			String nutsCode = record.get("NUTS-Code");
			ramonCodes.put(record.get("Code"), nutsCode);
			String parentCode = record.get("Parent").trim();
			if (parentCode.length() > 0) hierarchyMappings.put(nutsCode, parentCode);
			Resource nutsResource = nutsModel.createResource(Configuration.nutsURI(nutsCode), feature);
			nutsResource.addProperty(DC.identifier, nutsCode);
		    nutsResource.addProperty(RDFS.label, nutsModel.createLiteral(record.get("Description"), "en")); // TODO It's actually not always English
		    nutsResource.addProperty(depth, nutsModel.createTypedLiteral(Integer.parseInt(record.get("Level")) - 1)); // This is abusive: domain of 'depth' is xkos:ClassificationLevel
		}
		// Create hierarchies
		for (String nutsCode : hierarchyMappings.keySet()) {
			Resource nutsResource = nutsModel.createResource(Configuration.nutsURI(nutsCode));
			String parentCode = ramonCodes.get(hierarchyMappings.get(nutsCode));
			Resource parentResource = nutsModel.createResource(Configuration.nutsURI(parentCode));
	    	parentResource.addProperty(DCTerms.hasPart, nutsResource);
	    	nutsResource.addProperty(DCTerms.isPartOf, parentResource);
	    	}

		// Now read the IGN file to get the geographic data
		logger.info("Reading input geographic data from file " + Configuration.IGN_NUTS_FILE_NAME);
		in = new FileReader(Configuration.IGN_NUTS_FILE_NAME);
		records = CSVFormat.TDF.withQuote(null).withHeader().parse(in);
		for (CSVRecord record : records) {
		    String nutsCode = record.get("nuts_id");
		    if (!ramonCodes.containsValue(nutsCode)) {
		    	logger.warn("NUTS " + nutsCode + " not found in RAMON file");
		    	continue;
		    }
		    Resource nutsResource = nutsModel.createResource(Configuration.nutsURI(nutsCode), feature);
		    Resource nutsGeometryResource = nutsModel.createResource(Configuration.nutsGeometryURI(nutsCode), geometry);
		    nutsGeometryResource.addProperty(RDFS.label, nutsModel.createLiteral("Geometry for NUTS " + nutsCode, "en"));
		    nutsResource.addProperty(hasGeometry, nutsGeometryResource);
		    nutsGeometryResource.addProperty(asWKT, nutsModel.createTypedLiteral(record.get("wkt"), wktDatatypeURI));
		}
		logger.debug("Input file consumed, returning Jena model");
		return nutsModel;
	}

	/**
	 * Computes sameAs links between French NUTS and the COG (official geographic code).
	 * 
	 * @return The sameAs links as a Jena model. 
	 * @throws IOException In case of error reading or writing data.
	 */
	public static Model getNUTSCOGMappings() throws IOException {

		logger.info("Creating owl:sameAs mappings between NUTS and COG resources");
		// Get the list of French departments with associated COG resources, tweaked in order to maximize mappings with NUTS
		SortedMap<String, Resource> cogDepartments = fixMap(getTerritoryMap("Departement"), false);
		// For regions, matching will ignore case
		SortedMap<String, Resource> cogRegionsUpper = fixMap(getTerritoryMap("Region"), true);
		SortedSet<String> cogRegionNamesUpper = new TreeSet<String>();
		for (String regionName : cogRegionsUpper.keySet()) cogRegionNamesUpper.add(regionName.toUpperCase());

		Model mappingsModel = ModelFactory.createDefaultModel();
		mappingsModel.setNsPrefix("owl", OWL.getURI());

		logger.debug("Reading French NUTS from " + Configuration.RAMON_NUTS_FILE_NAME);
		Reader in = new FileReader(Configuration.RAMON_NUTS_FILE_NAME);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
		for (CSVRecord record : records) {
			int nutsLevel = Integer.parseInt(record.get("Level")) - 1; // NUTS level is actually 1 less than RAMON level...
			if (!record.get("NUTS-Code").startsWith("FR")) continue; // Keep only French NUTS
			String name = record.get("Description");
			logger.debug("Trying to match level " + nutsLevel + " NUTS " + record.get("NUTS-Code") + " (" + name + ")");
			// Process departments: they should match exactly, including case-wise
			if (nutsLevel == 3) {
				if (cogDepartments.containsKey(name)) {
					Resource nutsResource = mappingsModel.createResource(Configuration.nutsURI(record.get("NUTS-Code")));
					nutsResource.addProperty(OWL.sameAs, cogDepartments.get(name));
					// Departments are supposed to match only once
					cogDepartments.remove(name);
				} else logger.warn("No matching department found for level 3 NUTS " + name);
				continue;
			}
			// For other levels, try to match on regions ignoring case
			String nameUpper = name.toUpperCase();
			if (cogRegionsUpper.containsKey(nameUpper)) {
				Resource nutsResource = mappingsModel.createResource(Configuration.nutsURI(record.get("NUTS-Code")));
				nutsResource.addProperty(OWL.sameAs, cogRegionsUpper.get(nameUpper));
				// regionsUpperSet is used to record matched COG regions
				cogRegionNamesUpper.remove(nameUpper);
			} else logger.warn("No matching region found for level " + nutsLevel + " NUTS " + name);
			cogRegionsUpper.keySet().stream().anyMatch(name::equalsIgnoreCase);
		}
		// List departments that were not matched by a level 4 NUTS (there should be at least "Extra-Regio NUTS 3"
		for (String unMatched : cogDepartments.keySet()) logger.warn("COG department " + unMatched + " was not matched by any NUTS");
		for (String unMatched : cogRegionNamesUpper) logger.warn("COG region " + unMatched + " was not matched by any NUTS");
		return mappingsModel;
	}

	/**
	 * Queries the COG SPARQL endpoint to get a list of territory names with associated URIs.
	 * 
	 * @param territoryType The type of territory requested (should be "Departement" or "Region").
	 * @return The list of territories as a map between names and corresponding Jena resources.
	 * @see <a href="https://jena.apache.org/documentation/query/app_api.html">Jena query API</a>.
	 */
	private static SortedMap<String, Resource> getTerritoryMap(String territoryType) {

		logger.debug("Getting the list of territories of type " + territoryType + " with asssociated RDF resources");

		SortedMap<String, Resource> territories = new TreeMap<String, Resource>();

		String query = "PREFIX igeo:<http://rdf.insee.fr/def/geo#> ";
		query += "SELECT ?territory ?name WHERE { ?territory a igeo:" + territoryType + " ; igeo:nom ?name } ORDER BY ?territory";

		logger.debug("Querying " + Configuration.SPARQL_ENDPOINT + " with query " + query);
		QueryExecution execution = QueryExecutionFactory.sparqlService(Configuration.SPARQL_ENDPOINT, query);
		ResultSet results = execution.execSelect();
		results.forEachRemaining(querySolution -> {
			territories.put(querySolution.getLiteral("?name").getLexicalForm(), querySolution.getResource("?territory"));
		});
		execution.close();

		logger.debug("Returning map with " + territories.size() + " entries");
		return territories;
	}

	/**
	 * Tweaks the list of COG territories in order to improve the matching with the NUTS.
	 * This method is totally ad hoc and specific to the present use case.
	 * 
	 * @param initialMap The map (name -> resource) of COG territories.
	 * @param capitalize Indicates if the territory names should be upper case in the returned map.
	 * @return A copy of the initial map where territory names have been modified for better matching with NUTS.
	 */
	private static SortedMap<String, Resource> fixMap(SortedMap<String, Resource> initialMap, boolean capitalize) {

		SortedMap<String, Resource> fixedMap = new TreeMap<String, Resource>();

		for (String name : initialMap.keySet()) {
			String fixedName = name;
			if ("2016".equals(Configuration.NUTS_VERSION)) {
				fixedName = name.replace('\'', '’'); // NUTS file uses curly quotes for 2016 version but not for 2013 version
				fixedName = fixedName.replace('Î', 'I'); // NUTS don't have the accent on the I				
				fixedName = fixedName.replace("Centre-", "Centre — "); // NUTS name is "Centre — Val de Loire"
				fixedName = fixedName.replace("Nord-Pas-de-Calais", "Nord-Pas de Calais"); // NUTS uses hyphens at level 4 but not above
				for (String newRegionName : Configuration.REGION_NAMES_MAPPINGS.keySet()) fixedName = fixedName.replace(newRegionName, Configuration.REGION_NAMES_MAPPINGS.get(newRegionName));
			}
			if ("2013".equals(Configuration.NUTS_VERSION)) {
				fixedName = fixedName.replace("Île-de-France", "Île de France");
				fixedName = fixedName.replace("Nord-Pas-de-Calais", "Nord - Pas-de-Calais");
			}
			
			if (capitalize) fixedName = fixedName.toUpperCase();
			fixedMap.put(fixedName, initialMap.get(name));
		}

		return fixedMap;
	}
}
