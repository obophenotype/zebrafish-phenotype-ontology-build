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

	public String entity2SupertermId;
	public String entity2SupertermName;

	public String entity2SubtermId;
	public String entity2SubtermName;

	public String patoID;
	public String patoName;

	public boolean isAbnormal;

	/**
	 * Creates the concatenated string representation of the ID's of the classes
	 * used, e.g. "PATO:0023,ZFA:0234" or
	 * "PATO:0023,ZFA:3332,PATO:3322,GO:00032"
	 * 
	 * @return
	 */
	public String getEntryAsStringOfIds() {
		String separator = ",";
		StringBuffer buffer = new StringBuffer();

		if (patoID != null)
			buffer.append(patoID + separator);
		if (entity1SupertermId != null)
			buffer.append(entity1SupertermId + separator);
		if (entity1SubtermId != null)
			buffer.append(entity1SubtermId + separator);
		if (entity2SupertermId != null)
			buffer.append(entity2SupertermId + separator);
		if (entity2SubtermId != null)
			buffer.append(entity2SubtermId + separator);

		return buffer.toString();

	}

}
