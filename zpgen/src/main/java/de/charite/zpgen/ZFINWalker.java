package de.charite.zpgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a method to walkes a ZFIN file. Calls {@link ZFINVisitor#visit(ZFINEntry)} for each encountered entry.
 * 
 * See http://zfin.org/downloads for a current format.
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public class ZFINWalker {
	/**
	 * The current file format for pheno.txt as of: 8 Sep 2013<br>
	 * 0 Gene ID<br>
	 * 1 Entrez Zebrafish Gene ID<br>
	 * 2 Entrez Human Gene ID<br>
	 * 3 ZFIN Gene Symbol<br>
	 * 4 Affected Structure or Process 1 subterm OBO ID<br>
	 * 5 Affected Structure or Process 1 subterm name<br>
	 * 6 Post-Composed Relationship ID<br>
	 * 7 Post-Composed Relationship Name<br>
	 * 8 Affected Structure or Process 1 superterm OBO ID<br>
	 * 9 Affected Structure or Process 1 superterm name<br>
	 * 10 Phenotype Keyword OBO ID<br>
	 * 11 Phenotype Quality<br>
	 * 12 Phenotype Tag<br>
	 * 13 Affected Structure or Process 2 subterm OBO ID<br>
	 * 14 Affected Structure or Process 2 subterm name<br>
	 * 15 Post-Composed Relationship ID<br>
	 * 16 Post-Composed Relationship Name<br>
	 * 17 Affected Structure or Process 2 superterm OBO ID<br>
	 * 18 Affected Structure or Process 2 superterm name<br>
	 */
	/**
	 * The current file format for phenotype.txt as of: 2015<br>
	 * 0 Genotype ID <br>
	 * 1 Genotype Name <br>
	 * 2 Start Stage ID <br>
	 * 3 Start Stage Name<br>
	 * 4 End Stage ID<br>
	 * 5 End Stage Name <br>
	 * 6 Affected Structure or Process 1 subterm ID <br>
	 * 7 Affected Structure or Process 1 subterm Name <br>
	 * 8 Post-composed Relationship ID <br>
	 * 9 Post-composed Relationship Name <br>
	 * 10 Affected Structure or Process 1 superterm ID <br>
	 * 11 Affected Structure or Process 1 superterm Name <br>
	 * 12 Phenotype Keyword ID <br>
	 * 13 Phenotype Keyword Name <br>
	 * 14 Phenotype Tag <br>
	 * 15 Affected Structure or Process 2 subterm ID <br>
	 * 16 Affected Structure or Process 2 subterm name <br>
	 * 17 Post-composed Relationship (rel) ID <br>
	 * 18 Post-composed Relationship (rel) Name <br>
	 * 19 Affected Structure or Process 2 superterm ID <br>
	 * 20 Affected Structure or Process 2 superterm name <br>
	 * 21 Publication ID <br>
	 * 22 Environment ID<br>
	 */
	private ZFINWalker() {
	};

	static int COLUMN_ZFIN_GENE_ID = 0;

	static int COLUMN_TERM1_SUBTERM_ID = 4;
	static int COLUMN_TERM1_SUBTERM_NAME = 5;
	static int COLUMN_TERM1_SUPERTERM_ID = 8;
	static int COLUMN_TERM1_SUPERTERM_NAME = 9;

	static int COLUMN_TERM2_SUBTERM_ID = 13;
	static int COLUMN_TERM2_SUBTERM_NAME = 14;
	static int COLUMN_TERM2_SUPERTERM_ID = 17;
	static int COLUMN_TERM2_SUPERTERM_NAME = 18;

	static int COLUMN_PATO_ID = 10;
	static int COLUMN_PATO_NAME = 11;
	static int COLUMN_PATO_MODIFIER = 12;

	static public void walk(InputStream input, ZFINVisitor visitor) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String line;
		while ((line = in.readLine()) != null) {
			try {
				ZFINEntry entry = new ZFINEntry();
				String[] sp = null;
				if (line.contains("|"))
					sp = line.split("\\|", -1);
				else
					sp = line.split("\t", -1);

				entry.geneZfinID = sp[COLUMN_ZFIN_GENE_ID];

				entry.entity1SupertermId = sp[COLUMN_TERM1_SUPERTERM_ID];
				entry.entity1SupertermName = sp[COLUMN_TERM1_SUPERTERM_NAME];
				entry.entity1SubtermId = sp[COLUMN_TERM1_SUBTERM_ID];
				entry.entity1SubtermName = sp[COLUMN_TERM1_SUBTERM_NAME];

				entry.entity2SupertermId = sp[COLUMN_TERM2_SUPERTERM_ID];
				entry.entity2SupertermName = sp[COLUMN_TERM2_SUPERTERM_NAME];
				entry.entity2SubtermId = sp[COLUMN_TERM2_SUBTERM_ID];
				entry.entity2SubtermName = sp[COLUMN_TERM2_SUBTERM_NAME];

				entry.patoID = sp[COLUMN_PATO_ID];
				entry.patoName = sp[COLUMN_PATO_NAME];
				entry.isAbnormal = sp[COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");

				visitor.visit(entry);
			} catch (Exception e) {
				System.out.println("Problem in line: " + line);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
