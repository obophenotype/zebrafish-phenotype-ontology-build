import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a method to walkes a ZFIN file. The columns of these
 * tab-separated files have following headings (see http://zfin.org/zf_info/downloads.html#phenotype).
 * Calls {@link ZFINVisitor#visit(ZFINEntry)} for each encountered entry.
 * 
 *  0 Genotype ID
 *  1 Genotype Name
 *  2 Start Stage ID
 *  3 Start Stage Name
 *  4 End Stage ID
 *  5 End Stage Name
 *  6 Affected Structure or Process 1 ID
 *  7 Affected Structure or Process 1 Name
 *  8 Affected Structure or Process 2 ID
 *  9 Affected Structure or Process 2 Name
 *  10 Phenotype Keyword ID
 *  11 Phenotype Keyword Name
 *  12 Phenotype Modifier
 *  13 Publication ID
 *  14 Environment ID  
 *
 * @see http://zfin.org/zf_info/downloads.html#phenotype
 * @author Sebastian Bauer
 */
public class ZFINWalker
{
	private ZFINWalker() {};
	
	static int COLUMN_GENE_ID = 0;
	static int COLUMN_GENE_NAME = 1;

	static int COLUMN_TERM1_ID = 6;
	static int COLUMN_TERM1_NAME = 7;

	static int COLUMN_TERM2_ID = 8;
	static int COLUMN_TERM2_NAME = 9;
	
	static int COLUMN_PATO_ID = 10;
	static int COLUMN_PATO_NAME = 11;
	
	static public void walk(InputStream input, ZFINVisitor visitor) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String line;
		while ((line = in.readLine()) != null)
		{
			ZFINEntry entry = new ZFINEntry();
			
			String [] sp = line.split("\\t");

			entry.geneID = sp[COLUMN_GENE_ID];
			entry.geneName = sp[COLUMN_GENE_NAME];

			entry.term1ID = sp[COLUMN_TERM1_ID];
			entry.term1Name = sp[COLUMN_TERM1_NAME];

			entry.term2ID = sp[COLUMN_TERM2_ID];
			entry.term2Name = sp[COLUMN_TERM2_NAME];

			entry.patoID = sp[COLUMN_PATO_ID];
			entry.patoName = sp[COLUMN_PATO_NAME];
			
			visitor.visit(entry);
		}
	}
}
