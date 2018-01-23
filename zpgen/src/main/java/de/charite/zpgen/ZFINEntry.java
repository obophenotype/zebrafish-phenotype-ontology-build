package de.charite.zpgen;

/**
 * A simple zfin entry.
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 */
public class ZFINEntry {
	/**
	 * Gene or Geno
	 */
	public String genxZfinID;

	public String entity1SupertermId;
	public String entity1SupertermName;

	public String entity1SubtermId;
	public String entity1SubtermName;
	
	public String entity1RelationshipId;
	public String entity1RelationshipName;

	public String entity2SupertermId;
	public String entity2SupertermName;

	public String entity2SubtermId;
	public String entity2SubtermName;
	
	public String entity2RelationshipId;
	public String entity2RelationshipName;

	public String patoID;
	public String patoName;

	public boolean isAbnormal;

	public String sourceString;

}
