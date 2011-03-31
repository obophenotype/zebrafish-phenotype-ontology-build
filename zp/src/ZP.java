import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Main class for ZP which constructs an zebrafish phenotype ontology from
 * decomposed phenotype - gene associations.
 * 
 * The purpose of this tool is to create an ontology from the definition that
 * can be find in this file (source http://zfin.org/data_transfer/Downloads/phenotype.txt).
 * The file is tab separated. The columns have following headings (see
 * http://zfin.org/zf_info/downloads.html#phenotype)
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
 * @author Sebastian Bauer
 */
public class ZP
{
	static boolean verbose = true;
	static Logger log = Logger.getLogger(ZP.class.getName());

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("A input file needs to be specified");
			System.exit(-1);
		}
		/* FIXME: Addproper command line support */
		String inputName = args[0];
		
		/* Open input file */
		File f = new File(inputName);
		if (f.isDirectory())
		{
			System.err.println("Input file must be a file (as the name suggests) and not a directory.");
			System.exit(-1);
		}
		
		if (!f.exists())
		{
			System.err.println(String.format("Specified file \"%s\" doesn't exists!",inputName));
			System.exit(-1);
		}
		
		/* Now read the file */
		try
		{
			InputStream is; 

			try
			{
				is = new GZIPInputStream(new FileInputStream(f));
				is.read();
			} catch(IOException ex)
			{
				is = new FileInputStream(f);
			}
			
			ZFINWalker.walk(is, new ZFINVisitor()
			{
				public boolean visit(ZFINEntry entry)
				{
					System.out.println(entry.patoID);
					return true;
				}
			});

		} catch (FileNotFoundException e)
		{
			System.err.println(String.format("Specified file \"%s\" doesn't exists!",inputName));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
