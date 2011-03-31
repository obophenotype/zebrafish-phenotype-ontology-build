import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.owlapi.explanation.util.OntologyUtils;

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

	public static void main(String[] args) throws OWLOntologyCreationException
	{
		if (args.length < 1)
		{
			System.err.println("A input file needs to be specified");
			System.exit(-1);
		}
		/* FIXME: Addproper command line support */
		String inputName = args[0];
		
		/* Create ontology manager and IRIs */
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final IRI zpIRI = IRI.create("http://charite.de/zp.owl");
		final IRI goIRI = IRI.create("http://charite.de/go.owl"); // FIXME
		final IRI zfaIRI = IRI.create("http://charite.de/zfa.owl"); // FIXME
		final IRI patoIRI = IRI.create("http://charite.de/pato.owl"); // FIXME

		final IRI documentIRI = IRI.create("file:/tmp/zp.owl");
		/* Set up a mapping, which maps the ontology to the document IRI */
		SimpleIRIMapper mapper = new SimpleIRIMapper(zpIRI, documentIRI);
		manager.addIRIMapper(mapper); 

		/* Now create the ontology */
		final OWLOntology zp = manager.createOntology(zpIRI);
		
		/* Obtain the default data factory */
		final OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		/* Open zf input file */
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
		
		/* Now walk the file and create instances on the fly */
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
				int id;

				private OWLClass getClass(IRI prefix, String id)
				{
					return factory.getOWLClass(IRI.create(prefix + "#" + id));
				}
				
				private OWLClass getClassForOBO(String id)
				{
					if (id.startsWith("GO:")) return getClass(goIRI,id.substring(3));
					else if (id.startsWith("ZFA:")) return getClass(zfaIRI,id.substring(4));

					throw new RuntimeException("Unknown ontology prefix for name \"" + id + "\"");
				}

				private OWLClass getQualiClassForOBO(String id)
				{
					if (id.startsWith("PATO:")) return getClass(goIRI,id.substring(5));
					throw new RuntimeException("Qualifier must be a pato term");
				}

				public boolean visit(ZFINEntry entry)
				{
					OWLClass zpTerm = getClass(zpIRI,String.format("%07d",id));
					OWLClass pato = getQualiClassForOBO(entry.patoID);
					OWLClass cl1 = getClassForOBO(entry.term1ID);
					OWLClassExpression intersectionExpression;

					if (entry.term2ID != null && entry.term2ID.length() > 0)
					{
						intersectionExpression = factory.getOWLObjectIntersectionOf(zpTerm); 
					} else
					{
						intersectionExpression = factory.getOWLObjectIntersectionOf(zpTerm);
					}
					
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(zpTerm, intersectionExpression);

					AddAxiom addAx = new AddAxiom(zp, axiom);
					manager.applyChange(addAx);

					id++;
					return true;
				}
			});

			//manager.saveOntology(zp,System.out);
			manager.saveOntology(zp);
		} catch (FileNotFoundException e)
		{
			System.err.println(String.format("Specified file \"%s\" doesn't exists!",inputName));
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
