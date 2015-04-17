package de.charite.zpgen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SetOntologyID;

import com.beust.jcommander.JCommander;
import com.google.common.collect.ImmutableSetMultimap;

/**
 * Main class for ZP which constructs an zebrafish phenotype ontology from decomposed phenotype - gene associations.
 * 
 * The purpose of this tool is to create an ontology from the definitions that can be found in this file (source
 * http://zfin.org/downloads/pheno.txt).
 * 
 * @author Sebastian Bauer
 * @author Sebastian Koehler
 * @author Heiko Dietze
 */
public class ZPGen {
	static private Logger log = Logger.getLogger(ZPGen.class.getName());

	public static void main(String[] args) throws OWLOntologyCreationException, IOException, InterruptedException {
		ZPGenCLIConfig zpCLIConfig = new ZPGenCLIConfig();
		JCommander jc = new JCommander(zpCLIConfig);
		jc.parse(args);

		jc.setProgramName(ZPGen.class.getSimpleName());
		if (zpCLIConfig.help) {
			jc.usage();
			System.exit(0);
		}

		final boolean addSourceInformation = zpCLIConfig.addSourceInformation || zpCLIConfig.sourceInformationFile != null;
		final String zfinFilePath = zpCLIConfig.zfinFilePath;
		final String previousOntologyFilePath = zpCLIConfig.previousOntologyFilePath;
		final String ontologyOutputFilePath = zpCLIConfig.ontologyOutputFilePath;
		final String annotFilePath = zpCLIConfig.annotFilePath;
		final boolean keepIds = zpCLIConfig.keepIds;
		final boolean useInheresInPartOf = zpCLIConfig.useInheresInPartOf;
		final boolean useOwlRdfSyntax = zpCLIConfig.useOwlRdfSyntax;

		final boolean addZfaUberonEquivalencies = zpCLIConfig.addZfaUberonEquivalencies;
		final String uberonOboFilePath = zpCLIConfig.uberonOboFilePath;

		/* Create ontology manager and IRIs */
		final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		final IRI zpIRI = IRI.create("http://purl.obolibrary.org/obo/upheno/zp.owl");
		final IRI purlOboIRI = IRI.create("http://purl.obolibrary.org/obo/");

		/* Load the previous zp, if requested */
		final OWLOntology zp;
		if (keepIds) {
			File ontoFile = new File(previousOntologyFilePath);
			if (ontoFile.exists()) {
				zp = manager.loadOntologyFromOntologyDocument(ontoFile);
			}
			else {
				// log.info("Ignoring non-existent file \"" +
				// ontologyOutputFilePath + "\" for keeping the ids");
				// zp = manager.createOntology(zpIRI);
				log.severe("Could not find file \"" + previousOntologyFilePath + "\" for keeping the ids");
				throw new IllegalArgumentException("Keeping IDs was requested, but no previous file \"" + previousOntologyFilePath
						+ "\" was found! Prefer to stop here...");
			}
		}
		else {
			zp = manager.createOntology(zpIRI);
		}

		/*
		 * Add version IRI by using the date of construction
		 */
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		IRI versionIRI = IRI.create("http://purl.obolibrary.org/obo/upheno/releases/" + sdf.format(date) + "/zp.owl");
		manager.applyChange(new SetOntologyID(zp, new OWLOntologyID(zpIRI, versionIRI)));

		/*
		 * If user wants to have equivalence axioms between ZFA-classes and UBERON-classes we need the uberon.obo to create a mapping here.
		 */
		ImmutableSetMultimap<String, String> zfa2uberon = null;
		if (addZfaUberonEquivalencies) {

			log.info("creating ZFA-UBERON mapping");
			if (uberonOboFilePath == null) {
				log.severe("No uberon-file was provided for creating the ZFA-UBERON-mapping.");
				throw new IllegalArgumentException(
						"ZFA-UBERON-mapping requested, but no uberon.obo file was provided! Use option --uberon-obo-file. Prefer to stop here...");
			}
			File uberonOntoFile = new File(uberonOboFilePath);
			if (!uberonOntoFile.exists()) {
				log.severe("Could not find file \"" + uberonOboFilePath + "\" for creating the ZFA-UBERON-mapping.");
				throw new IllegalArgumentException("ZFA-UBERON-mapping requested, but no uberon.obo file \"" + uberonOboFilePath
						+ "\" was found! Prefer to stop here...");
			}
			Zfa2UberonMapper zfa2uberonMapper = new Zfa2UberonMapper(uberonOboFilePath);
			zfa2uberon = zfa2uberonMapper.getZfa2UberonMapping();
		}

		/* Instanciate the zpid db */
		final ZPIDDB zpIdDB = new ZPIDDB(zp);

		/* Where to write the annotation file to */
		final BufferedWriter annotationOut = new BufferedWriter(new FileWriter(annotFilePath));

		/* Obtain the default data factory */
		final OWLDataFactory factory = manager.getOWLDataFactory();

		/* Open zfin input file */
		File f = new File(zfinFilePath);
		if (f.isDirectory()) {
			System.err.println(String.format(
					"Specified zfin parameter value \"%s\" must point to a file (as the name suggests) and not a directory.", zfinFilePath));
			System.exit(-1);
		}

		if (!f.exists()) {
			System.err.println(String.format("Specified file \"%s\" doesn't exist!", zfinFilePath));
			System.exit(-1);
		}

		// was before BFO_0000070
		final OWLObjectProperty towards = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "RO_0002503"));

		final OWLObjectProperty partOf = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "BFO_0000050"));

		// I have for now replaced the BFO-properties with the RO-properties
		final OWLObjectProperty inheresProperty;
		if (useInheresInPartOf) {
			// inheres in part of
			inheresProperty = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "RO_0002314"));
		}
		else {
			// inheres in (was before BFO_0000052)
			inheresProperty = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "RO_0000052"));
		}

		final OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "BFO_0000051"));

		/* RO_0002180 = "has qualifier" (previously used) */
		/* RO_0002573 = "has modifier" (used in most recent version) */
		// final OWLObjectProperty has_qualifier =
		// factory.getOWLObjectProperty(IRI.create(zpIRI + "RO_0002180"));
		final OWLObjectProperty has_modifier = factory.getOWLObjectProperty(IRI.create(purlOboIRI + "RO_0002573"));
		final OWLClass abnormal = factory.getOWLClass(IRI.create(purlOboIRI + "PATO_0000460"));

		/* Now walk the file and create instances on the fly */
		try {
			InputStream is;

			/* Open input file. Try gzip compression first */
			try {
				is = new GZIPInputStream(new FileInputStream(f));
				if (is.markSupported())
					is.mark(10);
				is.read();
				if (is.markSupported())
					is.reset();
				else {
					is.close();
					is = new GZIPInputStream(new FileInputStream(f));
				}
			} catch (IOException ex) {
				/*
				 * We did not succeed in open the file and reading in a byte. We assume that the file is not compressed.
				 */
				is = new FileInputStream(f);
			}

			/*
			 * Constructs an OWLClass and Axioms for each zfin entry. We expect the reasoner to collate the classes properly. We also emit
			 * the annotations here.
			 */
			class ZFIN implements ZFINVisitor {
				/**
				 * Returns an entity class for the given obo id. This is a simple wrapper for OBOVocabulary.ID2IRI(id) but checks whether
				 * the term stems from a supported ontology.
				 * 
				 * @param id
				 * @return
				 */
				private OWLClass getEntityClassForOBOID(String id) {
					if (id.startsWith("GO:") || id.startsWith("ZFA:") || id.startsWith("BSPO:") || id.startsWith("MPATH:"))
						return factory.getOWLClass(OBOVocabulary.ID2IRI(id));

					throw new RuntimeException("Unknown ontology prefix for name \"" + id + "\"");
				}

				/**
				 * Returns an quality class for the given obo id. This is a simple wrapper for OBOVocabulary.ID2IRI(id) but checks whether
				 * the term stems from a supported ontology.
				 * 
				 * @param id
				 * @return
				 */
				private OWLClass getQualiClassForOBOID(String id) {
					if (id.startsWith("PATO:"))
						return factory.getOWLClass(OBOVocabulary.ID2IRI(id));

					throw new RuntimeException("Qualifier must be a pato term");
				}

				public boolean visit(ZFINEntry entry) {
					/* we only handle 'abnormal' entries */
					if (!entry.isAbnormal)
						return true;

					/*
					 * Important: exclude useless annotation that look like this ...ZFA:0001439|anatomical
					 * system|||||||PATO:0000001|quality|abnormal|
					 */
					if (entry.entity1SupertermId.equals("ZFA:0001439") && entry.patoID.equals("PATO:0000001") && entry.entity1SubtermId.equals("")
							&& entry.entity2SupertermId.equals("") && entry.entity2SubtermId.equals(""))
						return true;

					OWLClass pato = getQualiClassForOBOID(entry.patoID);
					OWLClass cl1 = getEntityClassForOBOID(entry.entity1SupertermId);
					OWLClassExpression intersectionExpression;
					String label;

					Set<OWLClassExpression> intersectionList = new LinkedHashSet<OWLClassExpression>();

					intersectionList.add(pato);
					// we now use has_modifier (this was has_qualifier before)
					intersectionList.add(factory.getOWLObjectSomeValuesFrom(has_modifier, abnormal));

					/* Entity 1: Create intersections */
					if (entry.entity1SubtermId != null && entry.entity1SubtermId.length() > 0) {
						/*
						 * Pattern is (all-some interpretation): <pato> inheres_in (<cl2> part of <cl1>) AND qualifier abnormal
						 */
						OWLClass cl2 = getEntityClassForOBOID(entry.entity1SubtermId);

						intersectionList.add(factory.getOWLObjectSomeValuesFrom(inheresProperty,
								factory.getOWLObjectIntersectionOf(cl2, factory.getOWLObjectSomeValuesFrom(partOf, cl1))));

						/*
						 * Note that is language the last word is the more specific part of the composition, i.e., we say swim bladder
						 * epithelium, which is the epithelium of the swim bladder
						 */
						label = "abnormal(ly) " + entry.patoName + " " + entry.entity1SupertermName + " " + entry.entity1SubtermName;
					}
					else {
						/*
						 * Pattern is (all-some interpretation): <pato> inheres_in <cl1> AND qualifier abnormal
						 */
						intersectionList.add(factory.getOWLObjectSomeValuesFrom(inheresProperty, cl1));
						label = "abnormal(ly) " + entry.patoName + " " + entry.entity1SupertermName;
					}

					/* Entity 2: Create intersections */
					if (entry.entity2SupertermId != null && entry.entity2SupertermId.length() > 0) {

						OWLClass cl3 = getEntityClassForOBOID(entry.entity2SupertermId);

						if (entry.entity2SubtermId != null && entry.entity2SubtermId.length() > 0) {
							/*
							 * Pattern is (all-some interpretation): <pato> inheres_in (<cl2> part of <cl1>) AND qualifier abnormal
							 */
							OWLClass cl4 = getEntityClassForOBOID(entry.entity2SubtermId);

							intersectionList.add(factory.getOWLObjectSomeValuesFrom(towards,
									factory.getOWLObjectIntersectionOf(cl4, factory.getOWLObjectSomeValuesFrom(partOf, cl3))));

							/*
							 * Note that is language the last word is the more specific part of the composition, i.e., we say swim bladder
							 * epithelium, which is the epithelium of the swim bladder
							 */
							label += " towards " + entry.entity2SupertermName + " " + entry.entity2SubtermName;

						}
						else {
							intersectionList.add(factory.getOWLObjectSomeValuesFrom(towards, cl3));
							label += " towards " + entry.entity2SupertermName;
						}
					}

					/* Create intersection */
					intersectionExpression = factory.getOWLObjectIntersectionOf(intersectionList);

					OWLClassExpression owlSomeClassExp = factory.getOWLObjectSomeValuesFrom(hasPart, intersectionExpression);

					IRI zpIRI = zpIdDB.getZPId(owlSomeClassExp);
					String zpID = OBOVocabulary.IRI2ID(zpIRI);
					OWLClass zpTerm = factory.getOWLClass(zpIRI);

					/* Make term equivalent to the intersection */
					OWLEquivalentClassesAxiom axiom = factory.getOWLEquivalentClassesAxiom(zpTerm, owlSomeClassExp);
					manager.addAxiom(zp, axiom);

					/* Add label */
					OWLAnnotation labelAnno = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(label));
					OWLAxiom labelAnnoAxiom = factory.getOWLAnnotationAssertionAxiom(zpTerm.getIRI(), labelAnno);
					manager.addAxiom(zp, labelAnnoAxiom);

					/* Add source information */
					if (addSourceInformation) {
						addSourceInformation(zpTerm, entry, zp);
					}

					/*
					 * Writing the annotation file
					 */
					try {
						annotationOut.write(entry.geneZfinID + "\t" + zpID + "\t" + label + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}

					return true;
				}
			}

			ZFIN zfinVisitor = new ZFIN();

			/* The zp entry that defines the root */
			ZFINEntry rootEntry = new ZFINEntry();
			rootEntry.geneZfinID = "DUMMY";
			rootEntry.isAbnormal = true;
			rootEntry.patoID = "PATO:0000001";
			rootEntry.patoName = "quality";
			rootEntry.entity1SupertermId = "ZFA:0100000";
			rootEntry.entity1SupertermName = "zebrafish anatomical entity";
			rootEntry.entity2SupertermId = "";
			zfinVisitor.visit(rootEntry);

			ZFINWalker.walk(is, zfinVisitor);

			// if requested, add the equivalence axioms between ZFA-class and
			// UBERON-classes
			if (zfa2uberon != null && zfa2uberon.keySet().size() > 0) {
				for (Entry<String, String> zfa2uberonEntry : zfa2uberon.entries()) {

					String zfaIdObo = zfa2uberonEntry.getKey();
					String uberonIdObo = zfa2uberonEntry.getValue();

					OWLClass zfaClass = factory.getOWLClass(OBOVocabulary.ID2IRI(zfaIdObo));
					OWLClass uberonClass = factory.getOWLClass(OBOVocabulary.ID2IRI(uberonIdObo));

					OWLEquivalentClassesAxiom equivZfaUberonAxiom = factory.getOWLEquivalentClassesAxiom(zfaClass, uberonClass);
					manager.addAxiom(zp, equivZfaUberonAxiom);
				}
			}

			/* Write output files */
			File of = new File(ontologyOutputFilePath);
			if (useOwlRdfSyntax) {
				// save in owl/rdf syntax
				manager.saveOntology(zp, new RDFXMLOntologyFormat(), new FileOutputStream(of));
				log.info("Wrote \"" + of.toString() + "\" in OWL/RDF syntax");
			}
			else {
				// save in manchester functional syntax
				manager.saveOntology(zp, new OWLFunctionalSyntaxOntologyFormat(), new FileOutputStream(of));
				log.info("Wrote \"" + of.toString() + "\" in Manchester functional syntax");
			}

			annotationOut.close();
			if (zpCLIConfig.sourceInformationFile != null) {
				saveSourceInformation(zp, zpCLIConfig.sourceInformationFile);
			}
		} catch (FileNotFoundException e) {
			System.err.println(String.format("Specified input file \"%s\" doesn't exist!", zfinFilePath));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Custom IRI for the annotation property for the definition of the class expression.
	 */
	static final IRI definitionSourcePropertyIRI = IRI.create("http://zfin/definition/source_information");

	/**
	 * Add the source information for the definition of the equivalent class expression for the given ZP class.
	 * 
	 * @param cls
	 * @param entry
	 * @param zp
	 */
	private static void addSourceInformation(OWLClass cls, ZFINEntry entry, OWLOntology zp) {
		OWLOntologyManager m = zp.getOWLOntologyManager();
		OWLDataFactory f = m.getOWLDataFactory();
		OWLAnnotationProperty definitionSourceProperty = f.getOWLAnnotationProperty(definitionSourcePropertyIRI);

		// search for existing source information
		Set<OWLAnnotationAssertionAxiom> annotations = zp.getAnnotationAssertionAxioms(cls.getIRI());
		boolean hasSourceInformation = false;
		for (OWLAnnotationAssertionAxiom ann : annotations) {
			if (definitionSourceProperty.equals(ann.getProperty())) {
				hasSourceInformation = true;
				break;
			}
		}
		// only add new information if no previous one exists
		if (hasSourceInformation == false) {
			StringBuilder source = new StringBuilder();
			source.append(entry.entity1SupertermId); // affected_structure_or_process_1_superterm_id
			source.append('\t');
			if (entry.entity1SubtermId != null) {
				source.append(entry.entity1SubtermId); // affected_structure_or_process_1_subterm_id
			}
			source.append('\t');
			source.append(entry.patoID); // phenotype_keyword_id
			source.append('\t');
			source.append("PATO:0000460"); // phenotype_modifier, currently
											// always abnormal
			source.append('\t');
			if (entry.entity2SupertermId != null) {
				source.append(entry.entity2SupertermId);// affected_structure_or_process_2_superterm_id
			}
			source.append('\t');
			if (entry.entity2SubtermId != null) {
				source.append(entry.entity2SubtermId); // affected_structure_or_process_2_subterm_id
			}
			OWLAnnotation sourceAnno = f.getOWLAnnotation(definitionSourceProperty, f.getOWLLiteral(source.toString()));
			OWLAxiom labelAnnoAxiom = f.getOWLAnnotationAssertionAxiom(cls.getIRI(), sourceAnno);
			m.addAxiom(zp, labelAnnoAxiom);
		}
	}

	/**
	 * Save the source information for all ZP classes in a separate file.
	 * 
	 * @param zp
	 * @param fileName
	 */
	private static void saveSourceInformation(OWLOntology zp, String fileName) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			OWLOntologyManager m = zp.getOWLOntologyManager();
			OWLDataFactory f = m.getOWLDataFactory();
			OWLAnnotationProperty definitionSourceProperty = f.getOWLAnnotationProperty(definitionSourcePropertyIRI);
			OWLAnnotationProperty rdfsLabel = f.getRDFSLabel();
			for (OWLClass cls : zp.getClassesInSignature()) {
				String zpID = OBOVocabulary.IRI2ID(cls.getIRI());
				if (zpID.startsWith("ZP:") == false) {
					// Ignore non ZP classes
					continue;
				}
				String label = null;
				String source = null;

				// Check annotations for source information and label
				Set<OWLAnnotationAssertionAxiom> annotations = zp.getAnnotationAssertionAxioms(cls.getIRI());
				for (OWLAnnotationAssertionAxiom ann : annotations) {
					OWLAnnotationProperty prop = ann.getProperty();
					if (definitionSourceProperty.equals(prop)) {
						OWLAnnotationValue value = ann.getValue();
						if (value instanceof OWLLiteral) {
							source = ((OWLLiteral) value).getLiteral();
						}
						break;
					}
					else if (rdfsLabel.equals(prop)) {
						OWLAnnotationValue value = ann.getValue();
						if (value instanceof OWLLiteral) {
							label = ((OWLLiteral) value).getLiteral();
						}
					}
				}

				// write the information
				if (label != null && source != null) {
					writer.append(zpID);
					writer.append('\t');
					writer.append(label);
					writer.append('\t');
					writer.append(source);
					writer.append('\n');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// close quietly
				}
			}
		}
	}
}
