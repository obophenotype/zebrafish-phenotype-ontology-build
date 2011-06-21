import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
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
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

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

	public static void main(String[] args) throws OWLOntologyCreationException, IOException
	{
		if (args.length < 1)
		{
			System.err.println("A input file needs to be specified");
			System.exit(-1);
		}
		/* FIXME: Addproper command line support */
		String inputName = args[0];
		String outputOntologyName;
		String annotationFileName = "zp.annot";
		
		if (args.length > 1)
			outputOntologyName = args[1];
		else
			outputOntologyName = "zp.owl";
		
		/* Create ontology manager and IRIs */
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final IRI zpIRI = IRI.create("http://purl.obolibrary.org/obo/");

		/* Now create the zp ontology */
		final OWLOntology zp = manager.createOntology(zpIRI);
		
		/* Where to write the annotation file to */
		final BufferedWriter annotationOut = new BufferedWriter(new FileWriter(annotationFileName));
		
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
//		if (of.exists())
//		{
//			System.err.println(String.format("The output file \"%s\" does already exist! Aborting.",outputOntologyName));
//			System.exit(-1);
//		}
		
		/* FIXME: Use proper property names */
		final OWLObjectProperty inheresIn 	= factory.getOWLObjectProperty(IRI.create(zpIRI + "inheres_in"));
		final OWLObjectProperty partOf 		= factory.getOWLObjectProperty(IRI.create(zpIRI + "part_of"));
		final OWLObjectProperty towards 		= factory.getOWLObjectProperty(IRI.create(zpIRI + "towards"));
		
		final OWLObjectProperty towardsBFO 		= factory.getOWLObjectProperty(IRI.create(zpIRI + "BFO_0000070"));
		final OWLObjectProperty partOfBFO 		= factory.getOWLObjectProperty(IRI.create(zpIRI + "BFO_0000050"));
		final OWLObjectProperty inheresInBFO		= factory.getOWLObjectProperty(IRI.create(zpIRI + "BFO_0000052"));
		// mapping of 
		OWLSubObjectPropertyOfAxiom ax1 = factory.getOWLSubObjectPropertyOfAxiom(inheresIn, inheresInBFO);
		OWLSubObjectPropertyOfAxiom ax2 = factory.getOWLSubObjectPropertyOfAxiom(partOf, partOfBFO);
		OWLSubObjectPropertyOfAxiom ax3 = factory.getOWLSubObjectPropertyOfAxiom(towards, towardsBFO);
		
		manager.addAxiom(zp,ax1);
		manager.addAxiom(zp,ax2);
		manager.addAxiom(zp,ax3);
		
		final OWLObjectProperty qualifier 	= factory.getOWLObjectProperty(IRI.create(zpIRI + "qualifier"));
		final OWLClass abnormal				= factory.getOWLClass(IRI.create(zpIRI + "PATO_0000460"));
		
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
					
					// handle only abnormal entries
					if (! entry.isAbnormal)
						return true;
					
					String zpId = String.format("ZP:%07d",id);
					OWLClass zpTerm = factory.getOWLClass(OBOVocabulary.ID2IRI(zpId));
					OWLClass pato = getQualiClassForOBOID(entry.patoID);
					OWLClass cl1 = getEntityClassForOBOID(entry.entity1SupertermId);
					OWLClassExpression intersectionExpression;
					String label;

					Set<OWLClassExpression> intersectionList = new LinkedHashSet<OWLClassExpression>();
					
					intersectionList.add(pato);
					intersectionList.add(factory.getOWLObjectSomeValuesFrom(qualifier, abnormal));
					
					/* Entity 1: Create intersections */
					if (entry.entity1SubtermId!= null && entry.entity1SubtermId.length() > 0)
					{
						/* Pattern is (all-some interpretation): <pato> inheres_in (<cl2> part of <cl1>) AND qualifier abnormal*/
						OWLClass cl2 = getEntityClassForOBOID(entry.entity1SubtermId);
						

						intersectionList.add(factory.getOWLObjectSomeValuesFrom(inheresIn, 
								factory.getOWLObjectIntersectionOf(cl2,factory.getOWLObjectSomeValuesFrom(partOf, cl1))));
//						
//						intersectionExpression = factory.getOWLObjectIntersectionOf(pato,
//								factory.getOWLObjectSomeValuesFrom(inheresIn, 
//									factory.getOWLObjectIntersectionOf(cl2,factory.getOWLObjectSomeValuesFrom(partOf, cl1),
//											factory.getOWLObjectSomeValuesFrom(qualifier, abnormal))));
						
						/* Note that is language the last word is the more specific part of the composition, i.e.,
						 * we say swim bladder epithelium, which is the epithelium of the swim bladder  */
						label = "abnormally " + entry.patoName +  " " + entry.entity1SupertermName + " " + entry.entity1SubtermName;
					} 
					else
					{
						/* Pattern is (all-some interpretation): <pato> inheres_in <cl1> AND qualifier abnormal */
//						intersectionExpression = factory.getOWLObjectIntersectionOf(pato,
//								factory.getOWLObjectSomeValuesFrom(inheresIn, cl1),
//								factory.getOWLObjectSomeValuesFrom(qualifier, abnormal));
						intersectionList.add(factory.getOWLObjectSomeValuesFrom(inheresIn, cl1));
						label = "abnormally " + entry.patoName +  " " + entry.entity1SupertermName;
					}
					
					
					/* Entity 2: Create intersections */
					if (entry.entity2SupertermId!= null && entry.entity2SupertermId.length() > 0){
						
						OWLClass cl3 = getEntityClassForOBOID(entry.entity2SupertermId);
						
						if (entry.entity2SubtermId!= null && entry.entity2SubtermId.length() > 0)
						{
							/* Pattern is (all-some interpretation): <pato> inheres_in (<cl2> part of <cl1>) AND qualifier abnormal*/
							OWLClass cl4 = getEntityClassForOBOID(entry.entity2SubtermId);
							
							intersectionList.add(factory.getOWLObjectSomeValuesFrom(towards, 
									factory.getOWLObjectIntersectionOf(cl4,factory.getOWLObjectSomeValuesFrom(partOf, cl3))));
							
							/* Note that is language the last word is the more specific part of the composition, i.e.,
							 * we say swim bladder epithelium, which is the epithelium of the swim bladder  */
							label += " towards " + entry.entity2SupertermName + " " + entry.entity2SubtermName;
							
						}
						else{
							intersectionList.add(factory.getOWLObjectSomeValuesFrom(towards, cl3));
							label += " towards " + entry.entity2SupertermName;
						}
					}
						
//						/* Note that is language the last word is the more specific part of the composition, i.e.,
//						 * we say swim bladder epithelium, which is the epithelium of the swim bladder  */
//						label = "abnormal " + entry.patoName +  " " + entry.entity1SupertermName + " " + entry.entity1SubtermName;
					
					
					/* Add subclass axiom */
					intersectionExpression = factory.getOWLObjectIntersectionOf(intersectionList);
					OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(zpTerm, intersectionExpression);
					manager.addAxiom(zp,axiom);

					/* Add label */
					OWLAnnotation labelAnno = factory.getOWLAnnotation(factory.getRDFSLabel(),factory.getOWLLiteral(label));
					OWLAxiom labelAnnoAxiom = factory.getOWLAnnotationAssertionAxiom(zpTerm.getIRI(), labelAnno);
					manager.addAxiom(zp,labelAnnoAxiom);

					/*
					 * Writing the annotation file
					 */
					try {
						annotationOut.write(entry.geneZfinID+"\t"+entry.geneZfinEntrezId+"\t"+zpId+"\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					id++;
					return true;
				}
			});

			manager.saveOntology(zp, new FileOutputStream(of));
//			manager.saveOntology(zp, new OBOOntologyFormat(), System.out);
			annotationOut.close();
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
