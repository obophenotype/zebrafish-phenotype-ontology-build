package de.charite.zpgen;

// TODO: add getters, setters, use private visibility

/**
 * A record type for representing an entry from ZFIN.
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public final class ZfinEntry {

  /** Gene or Genotype ID. */
  public String genxZfinId;

  /** Super term ID of entity 1. */
  public String entity1SupertermId;

  /** Super term name of entity 1. */
  public String entity1SupertermName;

  /** Sub term ID of entity 1. */
  public String entity1SubtermId;

  /** Sub term name of entity 1. */
  public String entity1SubtermName;

  /** Super term ID of entity 2. */
  public String entity2SupertermId;

  /** Super term name of entity 2. */
  public String entity2SupertermName;

  /** Sub term ID of entity 2. */
  public String entity2SubtermId;

  /** Sub term ID of entity 2. */
  public String entity2SubtermName;

  /** Pato identifier. */
  public String patoId;

  /** Pato name. */
  public String patoName;

  /** Whether or not the entry is marked as "abnormal". */
  public boolean isAbnormal;

  /** Source identifier string. */
  public String sourceString;

}
