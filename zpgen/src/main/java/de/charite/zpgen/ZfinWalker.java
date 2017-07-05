package de.charite.zpgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a static method to walk a ZFIN file.
 * 
 * <p>
 * Calls {@link ZfinVisitor#visit(ZfinEntry)} for each encountered entry.
 * </p>
 *
 * <p>
 * See http://zfin.org/downloads for current format. This changes a lot.
 * </p>
 *
 * <h5>Phenotype-Genotype File Format</h5>
 *
 * <p>
 * The current file format for http://zfin.org/downloads/phenotype_fish.txt as of: 2015.
 * </p>
 *
 * <ul>
 * <li>0 Fish ID</li>
 * <li>1 Fish Name</li>
 * <li>2 Start Stage ID</li>
 * <li>3 Start Stage Name</li>
 * <li>4 End Stage ID</li>
 * <li>5 End Stage Name</li>
 * <li>6 Affected Structure or Process 1 subterm ID</li>
 * <li>7 Affected Structure or Process 1 subterm Name</li>
 * <li>8 Post-composed Relationship ID</li>
 * <li>9 Post-composed Relationship Name</li>
 * <li>10 Affected Structure or Process 1 superterm ID</li>
 * <li>11 Affected Structure or Process 1 superterm Name</li>
 * <li>12 Phenotype Keyword ID</li>
 * <li>13 Phenotype Keyword Name</li>
 * <li>14 Phenotype Tag</li>
 * <li>15 Affected Structure or Process 2 subterm ID</li>
 * <li>16 Affected Structure or Process 2 subterm name</li>
 * <li>17 Post-composed Relationship (rel) ID</li>
 * <li>18 Post-composed Relationship (rel) Name</li>
 * <li>19 Affected Structure or Process 2 superterm ID</li>
 * <li>20 Affected Structure or Process 2 superterm name</li>
 * <li>21 Publication ID</li>
 * <li>22 Environment ID</li>
 * </ul>
 * 
 * <h5>Phenotype-Gene File Format</h5>
 * 
 * <p>
 * The current file format for http://zfin.org/downloads/phenoGeneCleanData_fish.txt as of: Jul 2017
 * </p>
 * 
 * <ul>
 * <li>0 ID</li>
 * <li>1 Gene Symbol</li>
 * <li>2 Gene ID</li>
 * <li>3 Affected Structure or Process 1 subterm ID</li>
 * <li>4 Affected Structure or Process 1 subterm Name</li>
 * <li>5 Post-composed Relationship ID</li>
 * <li>6 Post-composed Relationship Name</li>
 * <li>7 Affected Structure or Process 1 superterm ID</li>
 * <li>8 Affected Structure or Process 1 superterm Name</li>
 * <li>9 Phenotype Keyword ID</li>
 * <li>10 Phenotype Keyword Name</li>
 * <li>11 Phenotype Tag</li>
 * <li>12 Affected Structure or Process 2 subterm ID</li>
 * <li>13 Affected Structure or Process 2 subterm name</li>
 * <li>14 Post-composed Relationship (rel) ID</li>
 * <li>15 Post-composed Relationship (rel) Name</li>
 * <li>16 Affected Structure or Process 2 superterm ID</li>
 * <li>17 Affected Structure or Process 2 superterm name</li>
 * <li>18 Fish ID</li>
 * <li>19 Fish Display Name</li>
 * <li>20 Start Stage ID</li>
 * <li>21 End Stage ID</li>
 * <li>22 Fish Environment ID</li>
 * <li>23 Publication ID</li>
 * <li>24 Figure ID</li>
 * </ul>
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public final class ZfinWalker {

  /**
   * Enumeration type for the precise file type.
   */
  public static enum ZfinFileType {
    /** Phenotype-to-gene mapping. */
    PHENO_GENES_TXT,
    /** Phenotype-to-genotype mapping name. */
    PHENO_GENOTYPES_TXT;
  }


  /** Column for fish ID. */
  private static final int PHENO_GENOTYPES_COLUMN_ZFIN_GENO_ID = 0;

  /** Column for affected Structure or Process 1 subterm ID. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_ID = 6;

  /** Column for affected Structure or Process 1 subterm name. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_NAME = 7;

  /** Column for affected Structure or Process 1 superterm ID. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_ID = 10;

  /** Column for affected Structure or Process 1 superterm name. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_NAME = 11;

  /** Column for affected Structure or Process 2 subterm ID. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_ID = 15;

  /** Column for affected Structure or Process 2 subterm name. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_NAME = 16;

  /** Column for affected Structure or Process 2 superterm ID. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_ID = 19;

  /** Column for affected Structure or Process 2 superterm name. */
  private static final int PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_NAME = 20;

  /** Column for phenotype keyword ID. */
  private static final int PHENO_GENOTYPES_COLUMN_PATO_ID = 12;

  /** Column for phenotype keyword name. */
  private static final int PHENO_GENOTYPES_COLUMN_PATO_NAME = 13;

  /** Column for phenotype tag. */
  private static final int PHENO_GENOTYPES_COLUMN_PATO_MODIFIER = 14;

  // Column indices for phenotype-to-gene columns.

  // * <ul>
  // * <li>0 ID</li>
  // * <li>1 Gene Symbol</li>
  // * <li>2 Gene ID</li>
  // * <li>3 Affected Structure or Process 1 subterm ID</li>
  // * <li>4 Affected Structure or Process 1 subterm Name</li>
  // * <li>5 Post-composed Relationship ID</li>
  // * <li>6 Post-composed Relationship Name</li>
  // * <li>7 Affected Structure or Process 1 superterm ID</li>
  // * <li>8 Affected Structure or Process 1 superterm Name</li>
  // * <li>9 Phenotype Keyword ID</li>
  // * <li>10 Phenotype Keyword Name</li>
  // * <li>11 Phenotype Tag</li>
  // * <li>12 Affected Structure or Process 2 subterm ID</li>
  // * <li>13 Affected Structure or Process 2 subterm name</li>
  // * <li>14 Post-composed Relationship (rel) ID</li>
  // * <li>15 Post-composed Relationship (rel) Name</li>
  // * <li>16 Affected Structure or Process 2 superterm ID</li>
  // * <li>17 Affected Structure or Process 2 superterm name</li>
  // * <li>18 Fish ID</li>
  // * <li>19 Fish Display Name</li>
  // * <li>20 Start Stage ID</li>
  // * <li>21 End Stage ID</li>
  // * <li>22 Fish Environment ID</li>
  // * <li>23 Publication ID</li>
  // * <li>24 Figure ID</li>

  /** Column for zebrafish gene ID. */
  private static final int PHENO_GENE_COLUMN_ZFIN_GENE_ID = 2;

  /** Column for affected structure or process 1 subterm ID. */
  private static final int PHENO_GENE_COLUMN_TERM1_SUBTERM_ID = 3;

  /** Column for affected structure or process 1 subterm name. */
  private static final int PHENO_GENE_COLUMN_TERM1_SUBTERM_NAME = 4;

  /** Column for affected structure or process 1 superterm ID. */
  private static final int PHENO_GENE_COLUMN_TERM1_SUPERTERM_ID = 7;

  /** Column for affected structure or process 1 superterm name. */
  private static final int PHENO_GENE_COLUMN_TERM1_SUPERTERM_NAME = 8;

  /** Column for affected structure or process 2 subterm ID. */
  private static final int PHENO_GENE_COLUMN_TERM2_SUBTERM_ID = 12;

  /** Column for affected structure or process 2 subterm name. */
  private static final int PHENO_GENE_COLUMN_TERM2_SUBTERM_NAME = 13;

  /** Column for affected structure or process 2 superterm ID. */
  private static final int PHENO_GENE_COLUMN_TERM2_SUPERTERM_ID = 16;

  /** Column for affected structure or process 2 superterm name. */
  private static final int PHENO_GENE_COLUMN_TERM2_SUPERTERM_NAME = 17;

  /** Column for phenotype keyword ID. */
  private static final int PHENO_GENE_COLUMN_PATO_ID = 9;

  /** Column for phenotype keyword name. */
  private static final int PHENO_GENE_COLUMN_PATO_NAME = 10;

  /** Column for phenotype tag. */
  private static final int PHENO_GENE_COLUMN_PATO_MODIFIER = 11;

  /**
   * Read ZFIN file with a {@link ZfinVisitor}.
   *
   * @param input {@link InputStream} to read from.
   * @param visitor {@link ZfinVisitor} to use for visiting.
   * @param zfinFileType {@link ZfinFileType} to select input file type.
   * @param outPositiveAnnotations {@link BufferedWriter} to write the positive annotations to.
   * @param outNegativeAnnotations {@link BufferedWriter} to write the negative annotations to.
   * @throws IOException In case of problems with I/O.
   */
  public static void walk(InputStream input, ZfinVisitor visitor, ZfinFileType zfinFileType,
      BufferedWriter outPositiveAnnotations, BufferedWriter outNegativeAnnotations)
      throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(input));
    String line;
    while ((line = in.readLine()) != null) {
      try {
        ZfinEntry entry = new ZfinEntry();
        String[] sp = null;
        if (line.contains("|")) {
          sp = line.split("\\|", -1);
        } else {
          sp = line.split("\t", -1);
        }

        if (zfinFileType.equals(ZfinFileType.PHENO_GENES_TXT)) {
          entry.genxZfinId = sp[PHENO_GENE_COLUMN_ZFIN_GENE_ID];

          entry.entity1SupertermId = sp[PHENO_GENE_COLUMN_TERM1_SUPERTERM_ID];
          entry.entity1SupertermName = sp[PHENO_GENE_COLUMN_TERM1_SUPERTERM_NAME];
          entry.entity1SubtermId = sp[PHENO_GENE_COLUMN_TERM1_SUBTERM_ID];
          entry.entity1SubtermName = sp[PHENO_GENE_COLUMN_TERM1_SUBTERM_NAME];

          entry.entity2SupertermId = sp[PHENO_GENE_COLUMN_TERM2_SUPERTERM_ID];
          entry.entity2SupertermName = sp[PHENO_GENE_COLUMN_TERM2_SUPERTERM_NAME];
          entry.entity2SubtermId = sp[PHENO_GENE_COLUMN_TERM2_SUBTERM_ID];
          entry.entity2SubtermName = sp[PHENO_GENE_COLUMN_TERM2_SUBTERM_NAME];

          entry.patoId = sp[PHENO_GENE_COLUMN_PATO_ID];
          entry.patoName = sp[PHENO_GENE_COLUMN_PATO_NAME];
          entry.isAbnormal = sp[PHENO_GENE_COLUMN_PATO_MODIFIER].equalsIgnoreCase("abnormal");

          checkPhenotypeTag(sp[PHENO_GENE_COLUMN_PATO_MODIFIER], entry);
        } else if (zfinFileType.equals(ZfinFileType.PHENO_GENOTYPES_TXT)) {
          entry.genxZfinId = sp[PHENO_GENOTYPES_COLUMN_ZFIN_GENO_ID];

          entry.entity1SupertermId = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_ID];
          entry.entity1SupertermName = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUPERTERM_NAME];
          entry.entity1SubtermId = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_ID];
          entry.entity1SubtermName = sp[PHENO_GENOTYPES_COLUMN_TERM1_SUBTERM_NAME];

          entry.entity2SupertermId = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_ID];
          entry.entity2SupertermName = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUPERTERM_NAME];
          entry.entity2SubtermId = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_ID];
          entry.entity2SubtermName = sp[PHENO_GENOTYPES_COLUMN_TERM2_SUBTERM_NAME];

          entry.patoId = sp[PHENO_GENOTYPES_COLUMN_PATO_ID];
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

  /**
   * Check the phenotype tag and perform static fix of {@code entry}.
   *
   * <p>
   * In case of problems, an error is written to <code>stderr</code>.
   * </p>
   *
   * <h5>Note</h5>
   *
   * <p>
   * Contains update code if {@code string} is {@code "absent"} and the entity 1 super term id of
   * {@code entry} is {@code "GO:0007601"}, then updates the entry.
   * </p>
   *
   * @param string The phenotype tag to check.
   * @param entry The entry to process, and check.
   */
  private static void checkPhenotypeTag(String string, ZfinEntry entry) {
    if (!(string.equals("abnormal") || string.equals("normal"))) {
      System.err.println("wrong format for entry " + entry.genxZfinId
          + " expected normal/abnormal, found '" + string + "'");
    }

    if (string.equals("absent") && entry.entity1SupertermId.equals("GO:0007601")) {
      entry.isAbnormal = true;
      entry.patoId = "PATO:0000462";
      entry.patoName = "absent";
    }
  }

  /**
   * Generate a source string from a {@link ZfinEntry}.
   *
   * @param entry The {@link ZfinEntry} to generate source string for.
   * @return {@code String} with the corresponding source string.
   */
  public static String generateSourceString(ZfinEntry entry) {
    StringBuilder source = new StringBuilder();
    source.append(entry.entity1SupertermId); // affected_structure_or_process_1_superterm_id
    source.append('\t');
    if (entry.entity1SubtermId != null) {
      source.append(entry.entity1SubtermId); // affected_structure_or_process_1_subterm_id
    }
    source.append('\t');
    source.append(entry.patoId); // phenotype_keyword_id
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
