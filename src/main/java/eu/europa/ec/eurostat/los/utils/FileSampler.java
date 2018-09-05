package eu.europa.ec.eurostat.los.utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import eu.europa.ec.eurostat.los.nuts.Configuration;

public class FileSampler {

	/** Will extract the first lines of a file */

	public static void main(String[] args) throws Exception {

		sampleLines(0.001);

	}

	public static void extractFirstLines(int numberOfLines) throws IOException {

		PrintStream extractPrintStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(Configuration.EXTRACT_TTL)));
		Files.lines(Paths.get(Configuration.COMMUNES_TTL)).limit(numberOfLines).forEach(extractPrintStream::println);

		extractPrintStream.close();
	}

	public static void sampleLines(double rate) throws IOException {

		Random random = new Random();

		PrintStream extractPrintStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(Configuration.EXTRACT_TTL)));
		Files.lines(Paths.get(Configuration.COMMUNES_TTL)).filter(line -> (random.nextFloat() < rate)).forEach(extractPrintStream::println);

		extractPrintStream.close();
	}
}
