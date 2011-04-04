import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.coode.owlapi.obo.parser.OBOConsumer;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
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
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Main class for ZP which constructs an zebrafish phenotype ontology from
 * decomposed phenotype - gene associations.
 * 
 * The purpose of this tool is to create an ontology from the definition that
 * can be find in this file (source http://zfin.org/data_transfer/Downloads/phenotype.txt).
 * The file is tab separated.
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
		
		final OWLObjectProperty inheresIn = factory.getOWLObjectProperty(IRI.create("#inheres_in"));
		final OWLObjectProperty partOf = factory.getOWLObjectProperty(IRI.create("#part_of"));
		
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

				/**
				 * Returns an entity class for the given obo id.
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
				 * Returns an quality class for the given obo id.
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
						OWLClass cl2 = getEntityClassForOBOID(entry.term2ID);
						intersectionExpression = factory.getOWLObjectIntersectionOf(pato,
								factory.getOWLObjectSomeValuesFrom(inheresIn, 
									factory.getOWLObjectIntersectionOf(cl1,factory.getOWLObjectSomeValuesFrom(partOf, cl2))));
						label = entry.patoName +  " " + entry.term1Name + " " + entry.term2Name;
					} else
					{
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

//			manager.saveOntology(zp,System.out);
//			manager.saveOntology(zp);
			manager.saveOntology(zp, new OBOOntologyFormat(), System.out);
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
