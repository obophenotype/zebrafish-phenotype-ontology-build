package de.charite.zpgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a method to walk a ZFIN file. Calls {@link ZFINVisitor#visit(ZfinEntry)} for each
 * encountered entry.
 * 
 * See http://zfin.org/downloads for current format. This changes a lot...
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public class ZFINWalker {

  private ZFINWalker() {};

  public static enum ZFIN_FILE_TYPE {
    PHENO_GENES_TXT, PHENO_GENOTYPES_TXT
  };

  /**
   * The current file format for http://zfin.org/downloads/phenotype_fish.txt as of: 2015<br>
   * 0 Fish ID<br>
   * 1 Fish Name<br>
   * 2 Start Stage ID<br>
   * 3 Start Stage Name<br>
   * 4 End Stage ID<br>
   * 5 End Stage Name<br>
   * 6 Affected Structure or Process 1 subterm ID<br>
   * 7 Affected Structure or Process 1 subterm Name<br>
   * 8 Post-composed Relationship ID<br>
   * 9 Post-composed Relationship Name<br>
   * 10 Affected Structure or Process 1 superterm ID<br>
   * 11 Affected Structure or Process 1 superterm Name<br>
   * 12 Phenotype Keyword ID<br>
   * 13 Phenotype Keyword Name<br>
   * 14 Phenotype Tag<br>
   * 15 Affected Structure or Process 2 subterm ID<br>
   * 16 Affected Structure or Process 2 subterm name<br>
   * 17 Post-composed Relationship (rel) ID<br>
   * 18 Post-composed Relationship (rel) Name<br>
   * 19 Affected Structure or Process 2 superterm ID<br>
   * 20 Affected Structure or Process 2 superterm name<br>
   * 21 Publication ID<br>
   * 22 Environment ID<br>
   */
  private static final int PHENO_GENOTYPES_COLUMN_ZFIN_GENO_ID = 0;
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_ID = 6;
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_NAME = 7;
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_ID = 10;
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_NAME = 11;

  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_ID = 15;
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_NAME = 16;
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_ID = 19;
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_NAME = 20;

  private static final int PHENO_GENOTYPES_COLUMN_PATO_ID = 12;
  private static final int PHENO_GENOTYPES_COLUMN_PATO_NAME = 13;
  private static final int PHENO_GENOTYPES_COLUMN_PATO_MODIFIER = 14;

  /**
   * The current file format for http://zfin.org/downloads/phenoGeneCleanData_fish.txt as of: Jul
   * 2017<br>
   * 0 ID<br>
   * 1 Gene Symbol<br>
   * 2 Gene ID<br>
   * 3 Affected Structure or Process 1 subterm ID<br>
   * 4 Affected Structure or Process 1 subterm Name<br>
   * 5 Post-composed Relationship ID<br>
   * 6 Post-composed Relationship Name<br>
   * 7 Affected Structure or Process 1 superterm ID<br>
   * 8 Affected Structure or Process 1 superterm Name<br>
   * 9 Phenotype Keyword ID<br>
   * 10 Phenotype Keyword Name<br>
   * 11 Phenotype Tag<br>
   * 12 Affected Structure or Process 2 subterm ID<br>
   * 13 Affected Structure or Process 2 subterm name<br>
   * 14 Post-composed Relationship (rel) ID<br>
   * 15 Post-composed Relationship (rel) Name<br>
   * 16 Affected Structure or Process 2 superterm ID<br>
   * 17 Affected Structure or Process 2 superterm name<br>
   * 18 Fish ID<br>
   * 19 Fish Display Name<br>
   * 20 Start Stage ID<br>
   * 21 End Stage ID<br>
   * 22 Fish Environment ID<br>
   * 23 Publication ID<br>
   * 24 Figure ID
   */
  private static final int PHENO_GENE_COLUMN_ZFIN_GENE_ID = 2;
  private static final int PHENO_GENE_COLUMN_TERM1_SUBTERM_ID = 3;
  private static final int PHENO_GENE_COLUMN_TERM1_SUBTERM_NAME = 4;
  private static final int PHENO_GENE_COLUMN_TERM1_SUPERTERM_ID = 7;
  private static final int PHENO_GENE_COLUMN_TERM1_SUPERTERM_NAME = 8;

  private static final int PHENO_GENE_COLUMN_TERM2_SUBTERM_ID = 12;
  private static final int PHENO_GENE_COLUMN_TERM2_SUBTERM_NAME = 13;
  private static final int PHENO_GENE_COLUMN_TERM2_SUPERTERM_ID = 16;
  private static final int PHENO_GENE_COLUMN_TERM2_SUPERTERM_NAME = 17;

  private static final int PHENO_GENE_COLUMN_PATO_ID = 9;
  private static final int PHENO_GENE_COLUMN_PATO_NAME = 10;
  private static final int PHENO_GENE_COLUMN_PATO_MODIFIER = 11;

  static public void walk(InputStream input, ZFINVisitor visitor, ZFIN_FILE_TYPE zfinFileType,
      BufferedWriter outPositiveAnnotations, BufferedWriter outNegativeAnnotations)
      throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(input));
    String line;
    while ((line = in.readLine()) != null) {
      try {
        ZfinEntry entry = new ZfinEntry();
        String[] sp = null;
        if (line.contains("|"))
          sp = line.split("\\|", -1);
        else
          sp = line.split("\t", -1);

        if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENO_GENES_TXT)) {
          entry.genxZfinId = sp[PHENO_GENE_COLUMN_ZFIN_GENE_ID];

          entry.entity1SupertermId = sp[PHENO_GENE_COLUMN_TERM1_SUPERTERM_ID];
          entry.entity1SupertermName = sp[PHENO_GENE_COLUMN_TERM1_SUPERTERM_NAME];
          entry.entity1SubtermId = sp[PHENO_GENE_COLUMN_TERM1_SUBTERM_ID];
          entry.entity1SubtermName = sp[PHENO_GENE_COLUMN_TERM1_SUBTERM_NAME];

          entry.entity2SupertermId = sp[PHENO_GENE_COLUMN_TERM2_SUPERTERM_ID];
          entry.entity2SupertermName = sp[PHENO_GENE_COLUMN_TERM2_SUPERTERM_NAME];
          entry.entity2SubtermId = sp[PHENO_GENE_COLUMN_TERM2_SUBTERM_ID];
          entry.entity2SubtermName = sp[PHENO_GENE_COLUMN_TERM2_SUBTERM_NAME];

          entry.patoID = sp[PHENO_GENE_COLUMN_PATO_ID];
          entry.patoName = sp[PHENO_GENE_COLUMN_PATO_NAME];
          entry.isAbnormal = sp[PHENO_GENE_COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");

          checkPhenotypeTag(sp[PHENO_GENE_COLUMN_PATO_MODIFIER], entry);

        } else if (zfinFileType.equals(ZFIN_FILE_TYPE.PHENO_GENOTYPES_TXT)) {

          entry.genxZfinId = sp[PHENO_GENOTYPES_COLUMN_ZFIN_GENO_ID];

          entry.entity1SupertermId = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_ID];
          entry.entity1SupertermName = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_NAME];
          entry.entity1SubtermId = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_ID];
          entry.entity1SubtermName = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_NAME];

          entry.entity2SupertermId = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_ID];
          entry.entity2SupertermName = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_NAME];
          entry.entity2SubtermId = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_ID];
          entry.entity2SubtermName = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_NAME];

          entry.patoID = sp[PHENO_GENOTYPES_COLUMN_PATO_ID];
          entry.patoName = sp[PHENO_GENOTYPES_COLUMN_PATO_NAME];
          entry.isAbnormal = sp[PHENO_GENOTYPES_COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");
          checkPhenotypeTag(sp[PHENO_GENOTYPES_COLUMN_PATO_MODIFIER], entry);
        } else {
          throw new IllegalArgumentException("Unrecognized zfin-file-type: " + zfinFileType);
        }

        // create the source string NOW
        entry.sourceString = generateSourceString(entry);

        visitor.visit(entry, outPositiveAnnotations, outNegativeAnnotations);
      } catch (Exception e) {
        System.out.println("Problem in line: " + line);
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static void checkPhenotypeTag(String string, ZfinEntry entry) {
    if (!(string.equals("abnormal") || string.equals("normal"))) {
      System.err.println("wrong format for entry " + entry.genxZfinId
          + " expected normal/abnormal, found '" + string + "'");
    }
    if (string.equals("absent") && entry.entity1SupertermId.equals("GO:0007601")) {
      entry.isAbnormal = true;
      entry.patoID = "PATO:0000462";
      entry.patoName = "absent";
    }
  }

  public static String generateSourceString(ZfinEntry entry) {
    StringBuilder source = new StringBuilder();
    source.append(entry.entity1SupertermId); // affected_structure_or_process_1_superterm_id
    source.append('\t');
    if (entry.entity1SubtermId != null) {
      source.append(entry.entity1SubtermId); // affected_structure_or_process_1_subterm_id
    }
    source.append('\t');
    source.append(entry.patoID); // phenotype_keyword_id
    source.append('\t');

    // phenotype_modifier, currently always abnormal or normal
    if (entry.isAbnormal) {
      source.append("PATO:0000460"); // abnormal
    } else {
      source.append("PATO:0000461"); // normal
    }
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
