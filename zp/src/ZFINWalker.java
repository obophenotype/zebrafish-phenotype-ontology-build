import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Walkes a ZFIN file.
 * 
 * @author Sebastian Bauer
 */
public class ZFINWalker
{
	private ZFINWalker() {};
	
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
