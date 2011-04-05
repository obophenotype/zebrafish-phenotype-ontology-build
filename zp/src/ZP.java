import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * Main class for ZP which constructs an zebrafish phenotype ontology from
 * decomposed phenotype - gene associations.
 * 
 * The purpose of this tool is to create an ontology from the definition that
 * can be found in this file (source http://zfin.org/data_transfer/Downloads/phenotype.txt).
 * The input file is tab separated.
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
		String outputOntologyName;
		
		if (args.length > 1)
			outputOntologyName = args[1];
		else
			outputOntologyName = "zp.owl";
		
		/* Create ontology manager and IRIs */
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final IRI zpIRI = IRI.create("http://charite.de/zp.owl");

		/* Now create the zp ontology */
		final OWLOntology zp = manager.createOntology(zpIRI);
		
		/* Obtain the default data factory */
		final OWLDataFactory factory = manager.getOWLDataFactory(); 
		
		/* Open zfin input file */
		File f = new File(inputName);
		if (f.isDirectory())
		{
			System.err.println("Input file must be a file (as the name suggests) and not a directory.");
			System.exit(-1);
		}
		
		if (!f.exists())
		{
			System.err.println(String.format("Specified file \"%s\" doesn't exist!",inputName));
			System.exit(-1);
		}
		
		
		/* Check for output file. TODO: Add force option */
		File of = new File(outputOntologyName);
		if (of.exists())
		{
			System.err.println(String.format("The output file \"%s\" does already exist! Aborting.",outputOntologyName));
			System.exit(-1);
		}
		
		/* FIXME: Use proper property names */
		final OWLObjectProperty inheresIn = factory.getOWLObjectProperty(IRI.create("#inheres_in"));
		final OWLObjectProperty partOf = factory.getOWLObjectProperty(IRI.create("#part_of"));
		
		/* Now walk the file and create instances on the fly */
		try
		{
			InputStream is;

			/* Open input file. Try gzip compression first */
			try
			{
				is = new GZIPInputStream(new FileInputStream(f));
				if (is.markSupported())
					is.mark(10);
				is.read();
				if (is.markSupported())
					is.reset();
				else
					is = new GZIPInputStream(new FileInputStream(f));
			} catch(IOException ex)
			{
				/* We did not succeed in open the file and reading in a byte. We assume that
				 * the file is not compressed.
				 */
				is = new FileInputStream(f);
			}

			/* Constructs an OWLClass and Axioms for each zfin entry. We expect the reasoner to
			 * collate the classes properly. We also emit the annotations here. */
			ZFINWalker.walk(is, new ZFINVisitor()
			{
				int id;

				/**
				 * Returns an entity class for the given obo id. This is a simple wrapper
				 * for OBOVocabulary.ID2IRI(id) but checks whether the term stems from
				 * a supported ontology.
				 * 
				 * @param id
				 * @return
				 */
				private OWLClass getEntityClassForOBOID(String id)
				{
					if (id.startsWith("GO:") || id.startsWith("ZFA:") || id.startsWith("BSPO:"))
						return factory.getOWLClass(OBOVocabulary.ID2IRI(id));

					throw new RuntimeException("Unknown ontology prefix for name \"" + id + "\"");
				}

				/**
				 * Returns an quality class for the given obo id.  This is a simple wrapper
				 * for OBOVocabulary.ID2IRI(id) but checks whether the term stems from
				 * a supported ontology.
				 * 
				 * @param id
				 * @return
				 */
				private OWLClass getQualiClassForOBOID(String id)
				{
					if (id.startsWith("PATO:")) return factory.getOWLClass(OBOVocabulary.ID2IRI(id));

					throw new RuntimeException("Qualifier must be a pato term");
				}

				public boolean visit(ZFINEntry entry)
				{
					OWLClass zpTerm = factory.getOWLClass(OBOVocabulary.ID2IRI(String.format("ZP:%07d",id)));
					OWLClass pato = getQualiClassForOBOID(entry.patoID);
					OWLClass cl1 = getEntityClassForOBOID(entry.term1ID);
					OWLClassExpression intersectionExpression;
					String label;

					/* Create intersections */
					if (entry.term2ID != null && entry.term2ID.length() > 0)
					{
						/* Pattern is (all-some interpretation): <pato> inheres_in (<cl2> part of <cl1>) */
						OWLClass cl2 = getEntityClassForOBOID(entry.term2ID);
						intersectionExpression = factory.getOWLObjectIntersectionOf(pato,
								factory.getOWLObjectSomeValuesFrom(inheresIn, 
									factory.getOWLObjectIntersectionOf(cl2,factory.getOWLObjectSomeValuesFrom(partOf, cl1))));
						
						/* Note that is language the last word is the more specific part of the composition, i.e.,
						 * we say swim bladder epithelium, which is the epithelium of the swim bladder  */
						label = entry.patoName +  " " + entry.term1Name + " " + entry.term2Name;
					} else
					{
						/* Pattern is (all-some interpretation): <pato> inheres_in <cl1> */
						intersectionExpression = factory.getOWLObjectIntersectionOf(pato,
								factory.getOWLObjectSomeValuesFrom(inheresIn, cl1));
						label = entry.patoName +  " " + entry.term1Name;
					}

					/* Add subclass axiom */
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(zpTerm, intersectionExpression);
					manager.addAxiom(zp,axiom);

					/* Add label */
					OWLAnnotation labelAnno = factory.getOWLAnnotation(factory.getRDFSLabel(),factory.getOWLLiteral(label));
					OWLAxiom labelAnnoAxiom = factory.getOWLAnnotationAssertionAxiom(zpTerm.getIRI(), labelAnno);
					manager.addAxiom(zp,labelAnnoAxiom);

					id++;
					return true;
				}
			});

			manager.saveOntology(zp, new FileOutputStream(of));
//			manager.saveOntology(zp, new OBOOntologyFormat(), System.out);
		} catch (FileNotFoundException e)
		{
			System.err.println(String.format("Specified input file \"%s\" doesn't exist!",inputName));
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
