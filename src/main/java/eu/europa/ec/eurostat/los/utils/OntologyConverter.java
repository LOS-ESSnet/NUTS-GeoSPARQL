package eu.europa.ec.eurostat.los.utils;

import java.io.FileOutputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import eu.europa.ec.eurostat.los.nuts.Configuration;

public class OntologyConverter {

	public static void main(String[] args) throws Exception {

		OntModel geoSPARQLOntology = ModelFactory.createOntologyModel();
		geoSPARQLOntology.read(Configuration.GEOSPARQL_ONTOLOGY_FILE_RDF);
		RDFDataMgr.write(new FileOutputStream(Configuration.GEOSPARQL_ONTOLOGY_FILE_TTL), geoSPARQLOntology, Lang.TURTLE);
	}
}
