package de.charite.zpgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a method to walkes a ZFIN file. Calls {@link ZFINVisitor#visit(ZFINEntry)} for each encountered entry.
 * 
 * See http://zfin.org/downloads for a current format (pheno.txt and phenotype.txt).
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public class ZFINWalker {

	private ZFINWalker() {
	};

	public static enum ZFIN_FILE_TYPE {
		PHENO_TXT, PHENOTYPE_TXT
	};

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
	private static final int PHENOTYPE_TXT_COLUMN_ZFIN_GENO_ID = 0;
	private static final int PHENOTYPE_TXT_COLUMN_TERM1_SUBTERM_ID = 6;
	private static final int PHENOTYPE_TXT_COLUMN_TERM1_SUBTERM_NAME = 7;
	private static final int PHENOTYPE_TXT_COLUMN_TERM1_SUPERTERM_ID = 10;
	private static final int PHENOTYPE_TXT_COLUMN_TERM1_SUPERTERM_NAME = 11;

	private static final int PHENOTYPE_TXT_COLUMN_TERM2_SUBTERM_ID = 15;
	private static final int PHENOTYPE_TXT_COLUMN_TERM2_SUBTERM_NAME = 16;
	private static final int PHENOTYPE_TXT_COLUMN_TERM2_SUPERTERM_ID = 19;
	private static final int PHENOTYPE_TXT_COLUMN_TERM2_SUPERTERM_NAME = 20;

	private static final int PHENOTYPE_TXT_COLUMN_PATO_ID = 12;
	private static final int PHENOTYPE_TXT_COLUMN_PATO_NAME = 13;
	private static final int PHENOTYPE_TXT_COLUMN_PATO_MODIFIER = 14;

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
	private static final int PHENO_TXT_COLUMN_ZFIN_GENE_ID = 0;
	private static final int PHENO_TXT_COLUMN_TERM1_SUBTERM_ID = 4;
	private static final int PHENO_TXT_COLUMN_TERM1_SUBTERM_NAME = 5;
	private static final int PHENO_TXT_COLUMN_TERM1_SUPERTERM_ID = 8;
	private static final int PHENO_TXT_COLUMN_TERM1_SUPERTERM_NAME = 9;

	private static final int PHENO_TXT_COLUMN_TERM2_SUBTERM_ID = 13;
	private static final int PHENO_TXT_COLUMN_TERM2_SUBTERM_NAME = 14;
	private static final int PHENO_TXT_COLUMN_TERM2_SUPERTERM_ID = 17;
	private static final int PHENO_TXT_COLUMN_TERM2_SUPERTERM_NAME = 18;

	private static final int PHENO_TXT_COLUMN_PATO_ID = 10;
	private static final int PHENO_TXT_COLUMN_PATO_NAME = 11;
	private static final int PHENO_TXT_COLUMN_PATO_MODIFIER = 12;

	static public void walk(InputStream input, ZFINVisitor visitor, ZFIN_FILE_TYPE zfinFileType, BufferedWriter outPositiveAnnotations,
			BufferedWriter outNegativeAnnotations) throws IOException {
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

				if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENO_TXT)) {
					entry.genxZfinID = sp[PHENO_TXT_COLUMN_ZFIN_GENE_ID];

					entry.entity1SupertermId = sp[PHENO_TXT_COLUMN_TERM1_SUPERTERM_ID];
					entry.entity1SupertermName = sp[PHENO_TXT_COLUMN_TERM1_SUPERTERM_NAME];
					entry.entity1SubtermId = sp[PHENO_TXT_COLUMN_TERM1_SUBTERM_ID];
					entry.entity1SubtermName = sp[PHENO_TXT_COLUMN_TERM1_SUBTERM_NAME];

					entry.entity2SupertermId = sp[PHENO_TXT_COLUMN_TERM2_SUPERTERM_ID];
					entry.entity2SupertermName = sp[PHENO_TXT_COLUMN_TERM2_SUPERTERM_NAME];
					entry.entity2SubtermId = sp[PHENO_TXT_COLUMN_TERM2_SUBTERM_ID];
					entry.entity2SubtermName = sp[PHENO_TXT_COLUMN_TERM2_SUBTERM_NAME];

					entry.patoID = sp[PHENO_TXT_COLUMN_PATO_ID];
					entry.patoName = sp[PHENO_TXT_COLUMN_PATO_NAME];
					entry.isAbnormal = sp[PHENO_TXT_COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");

					// fix bug with 7 entries that use wrong phenotype-tag (usually only normal/abnormal allowed)
					checkPhenotypeTag(sp[PHENO_TXT_COLUMN_PATO_MODIFIER], entry);
				}
				else if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENOTYPE_TXT)) {

					entry.genxZfinID = sp[PHENOTYPE_TXT_COLUMN_ZFIN_GENO_ID];

					entry.entity1SupertermId = sp[PHENOTYPE_TXT_COLUMN_TERM1_SUPERTERM_ID];
					entry.entity1SupertermName = sp[PHENOTYPE_TXT_COLUMN_TERM1_SUPERTERM_NAME];
					entry.entity1SubtermId = sp[PHENOTYPE_TXT_COLUMN_TERM1_SUBTERM_ID];
					entry.entity1SubtermName = sp[PHENOTYPE_TXT_COLUMN_TERM1_SUBTERM_NAME];

					entry.entity2SupertermId = sp[PHENOTYPE_TXT_COLUMN_TERM2_SUPERTERM_ID];
					entry.entity2SupertermName = sp[PHENOTYPE_TXT_COLUMN_TERM2_SUPERTERM_NAME];
					entry.entity2SubtermId = sp[PHENOTYPE_TXT_COLUMN_TERM2_SUBTERM_ID];
					entry.entity2SubtermName = sp[PHENOTYPE_TXT_COLUMN_TERM2_SUBTERM_NAME];

					entry.patoID = sp[PHENOTYPE_TXT_COLUMN_PATO_ID];
					entry.patoName = sp[PHENOTYPE_TXT_COLUMN_PATO_NAME];
					entry.isAbnormal = sp[PHENOTYPE_TXT_COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");

				}
				else {
					throw new IllegalArgumentException("Unrecognized zfin-file-type: " + zfinFileType);
				}

				// create the source string NOW
				entry.sourceString = generateSourceString(entry);

				// fix bug with 7 entries that use wrong phenotype-tag (usually only normal/abnormal allowed)
				if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENO_TXT)) {
					checkPhenotypeTag(sp[PHENO_TXT_COLUMN_PATO_MODIFIER], entry);
				}
				else if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENOTYPE_TXT)) {
					checkPhenotypeTag(sp[PHENOTYPE_TXT_COLUMN_PATO_MODIFIER], entry);
				}

				visitor.visit(entry, outPositiveAnnotations, outNegativeAnnotations);
			} catch (Exception e) {
				System.out.println("Problem in line: " + line);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private static void checkPhenotypeTag(String string, ZFINEntry entry) {
		if (!(string.equals("abnormal") || string.equals("normal"))) {
			System.err.println("wrong format for entry " + entry.genxZfinID + " expected normal/abnormal, found '" + string + "'");
		}
		if (string.equals("absent") && entry.entity1SupertermId.equals("GO:0007601")) {
			entry.isAbnormal = true;
			entry.patoID = "PATO:0000462";
			entry.patoName = "absent";
		}
	}

	public static String generateSourceString(ZFINEntry entry) {
		StringBuilder source = new StringBuilder();
		source.append(entry.entity1SupertermId); // affected_structure_or_process_1_superterm_id
		source.append('\t');
		if (entry.entity1SubtermId != null) {
			source.append(entry.entity1SubtermId); // affected_structure_or_process_1_subterm_id
		}
		source.append('\t');
		source.append(entry.patoID); // phenotype_keyword_id
		source.append('\t');
		source.append("PATO:0000460"); // phenotype_modifier, currently
										// always abnormal
		source.append('\t');
		if (entry.entity2SupertermId != null) {
			source.append(entry.entity2SupertermId);// affected_structure_or_process_2_superterm_id
		}
		source.append('\t');
		if (entry.entity2SubtermId != null) {
			source.append(entry.entity2SubtermId); // affected_structure_or_process_2_subterm_id
		}
		return source.toString();
	}
}
