import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a method to walkes a ZFIN file.
 * Calls {@link ZFINVisitor#visit(ZFINEntry)} for each encountered entry.
 * 
 * 	0 ZDB-GENE-030131-7487
	1 563421
	2 154796
	3 amot4
	4 ZFA:0001285
	5 intersegmental vessel
	6 ZFA:0009036
	7 blood vessel endothelial cell
	8 ZFA:0001285
	9 intersegmental vessel
	10 ZFA:0009036
	11 blood vessel endothelial cell
	12 PATO:0001531
	13 cellular adhesivity
	14 abnormal
	15
 * 
 * @author Sebastian Bauer
 */
public class ZFINWalker
{
	private ZFINWalker() {};
	
	static int COLUMN_ZFIN_GENE_ID 			= 0;

	static int COLUMN_TERM1_SUPERTERM_ID 	= 4;
	static int COLUMN_TERM1_SUPERTERM_NAME 	= 5;
	static int COLUMN_TERM1_SUBTERM_ID 		= 6;
	static int COLUMN_TERM1_SUBTERM_NAME 	= 7;	
	static int COLUMN_TERM2_SUPERTERM_ID 	= 8;
	static int COLUMN_TERM2_SUPERTERM_NAME 	= 9;
	static int COLUMN_TERM2_SUBTERM_ID 		= 10;
	static int COLUMN_TERM2_SUBTERM_NAME 	= 11;

	static int COLUMN_PATO_ID 				= 12;
	static int COLUMN_PATO_NAME 				= 13;
	static int COLUMN_PATO_ABNORMAL_NORMAL 	= 14;
	
	static public void walk(InputStream input, ZFINVisitor visitor) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String line;
		while ((line = in.readLine()) != null)
		{
			
			ZFINEntry entry = new ZFINEntry();
			
			String [] sp = line.split("\\|");

			entry.geneZfinID = sp[COLUMN_ZFIN_GENE_ID];

			entry.entity1SupertermId 	= sp[COLUMN_TERM1_SUPERTERM_ID];
			entry.entity1SupertermName 	= sp[COLUMN_TERM1_SUPERTERM_NAME];
			entry.entity1SubtermId 		= sp[COLUMN_TERM1_SUBTERM_ID];
			entry.entity1SubtermName 	= sp[COLUMN_TERM1_SUBTERM_NAME];
			
			entry.entity2SupertermId 	= sp[COLUMN_TERM2_SUPERTERM_ID];
			entry.entity2SupertermName 	= sp[COLUMN_TERM2_SUPERTERM_NAME];
			entry.entity2SubtermId 		= sp[COLUMN_TERM2_SUBTERM_ID];
			entry.entity2SubtermName 	= sp[COLUMN_TERM2_SUBTERM_NAME];

			entry.patoID 		= sp[COLUMN_PATO_ID];
			entry.patoName 		= sp[COLUMN_PATO_NAME];
			entry.isAbnormal 	= sp[COLUMN_PATO_ABNORMAL_NORMAL].equalsIgnoreCase("abnormal");
			visitor.visit(entry);
		}
	}
}
