import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Simple zp id registry.
 * 
 * @author Sebastian Bauer
 */
public class ZPIDDB
{
	private static Logger log = Logger.getAnonymousLogger();

	/** Next id to be assigned in case of an unknown class expression */
	private int nextId = 1;

	/** Every class expression got's an own distinct id, which is stored here */
	private HashMap<OWLClassExpression,IRI> class2Id = new HashMap<OWLClassExpression,IRI>();

	/**
	 * Constructs an empty zp id database. 
	 */
	public ZPIDDB()
	{
		this(null);
	}

	/**
	 * Constructs a zp id data base and filling it
	 * with previously assigned ids gathered from
	 * the given ontology.
	 *  
	 * @param zp
	 */
	public ZPIDDB(OWLOntology zp)
	{
		if (zp != null)
		{
			for (OWLAxiom ax : zp.getAxioms(AxiomType.EQUIVALENT_CLASSES))
			{
				if (ax instanceof OWLEquivalentClassesAxiom)
				{
					OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) ax;
					List<OWLClassExpression> exprList = eq.getClassExpressionsAsList();
					if (exprList.size() != 2)
					{
						log.warning("Unknown format in equivalence axiom: " + eq);
						continue;
					}
					OWLClassExpression cl1 = exprList.get(0);
					OWLClassExpression cl2 = exprList.get(1);

					OWLClass zpClass = null;

					if (cl1 instanceof OWLClass) zpClass = (OWLClass)cl1;
					else
					{
						log.warning("Unknown format in equivalence axiom: " + eq);
						continue;						
					}
					
					IRI zpIRI = zpClass.getIRI();
					String zpID = OBOVocabulary.IRI2ID(zpIRI);
					if (!zpID.startsWith("ZP:"))
					{
						log.warning("Unknown term name in equivalence axiom: " + eq);
						continue;						
					}
					int id = Integer.parseInt(zpID.substring(3));
					if (id >= nextId) nextId = id + 1;

					class2Id.put(cl2, zpIRI);
				}
			}
			log.info((nextId - 1) + " previous ids recovered");
		}
	}

	/**
	 * Returns the Id (as IRI) of the given owlSomeClassExp.
	 * This may be a new one if classExpression was not seen
	 * before.
	 * 
	 * @param owlSomeClassExp
	 * @return
	 */
	public IRI getZPId(OWLClassExpression classExpression)
	{
		IRI zpIdIRI = class2Id.get(classExpression);
		if (zpIdIRI != null) return zpIdIRI;
		String zpId = String.format("ZP:%07d",nextId++);
		zpIdIRI = OBOVocabulary.ID2IRI(zpId);
		class2Id.put(classExpression, zpIdIRI);
		return zpIdIRI;
	}
}
