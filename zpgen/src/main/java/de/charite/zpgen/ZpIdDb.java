package de.charite.zpgen;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple database for ZPO identifiers.
 * 
 * @author Sebastian Bauer
 * @author Heiko Dietze
 */
public class ZpIdDb {

  /**
   * The {@link Logger} to use for log genreation.
   */
  private static Logger LOGGER = Logger.getAnonymousLogger();

  /** Next id to be assigned in case of an unknown class expression. */
  private int nextId = 1;

  /**
   * Every class expression gets an own distinct id, which is stored here.
   */
  private HashMap<OWLClassExpression, IRI> class2Id = new HashMap<OWLClassExpression, IRI>();

  /**
   * Constructs an empty zp id database.
   */
  public ZpIdDb() {
    this(null);
  }

  /**
   * Constructs a the data base and fill it with previously assigned ids, themselves gathered from
   * the given ontology.
   * 
   * @param zp {@link OWLOntology} with the previous version of the zebrafish phenotype ontology.
   */
  public ZpIdDb(OWLOntology zp) {
    if (zp == null) {
      return; // do nothing, short-circuit
    }

    for (OWLAxiom ax : zp.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
      if (ax instanceof OWLEquivalentClassesAxiom) {
        // Only consider equivalence class axioms must have two entries.
        final OWLEquivalentClassesAxiom eq = (OWLEquivalentClassesAxiom) ax;
        final List<OWLClassExpression> exprList = eq.getClassExpressionsAsList();
        if (exprList.size() != 2) {
          LOGGER.warning("Unknown format in equivalence axiom: " + eq);
          continue;
        }

        // Obtain shortcut to first entry and perform cast to OWLClass.
        final OWLClassExpression cl1 = exprList.get(0);
        final OWLClass zpClass;
        if (cl1 instanceof OWLClass) {
          zpClass = (OWLClass) cl1;
        } else {
          LOGGER.warning("Unknown format in equivalence axiom: " + eq);
          continue;
        }

        // Get ID from cast-to OWLClass.
        final IRI zpIri = zpClass.getIRI();
        final String zpId = OBOVocabulary.IRI2ID(zpIri);
        if (!zpId.startsWith("ZP:")) {
          LOGGER.warning("Unknown term name in equivalence axiom: " + eq);
          continue;
        }

        // Parse numeric integer and increment nextId if necessary.
        int id = Integer.parseInt(zpId.substring(3));
        if (id >= nextId) {
          nextId = id + 1;
        }

        // Finally, put into the class-to-id map.
        final OWLClassExpression cl2 = exprList.get(1);
        class2Id.put(cl2, zpIri);
      }
    }
    LOGGER.info((nextId - 1) + " previous ids recovered");
  }

  /**
   * Returns the ID (as IRI) of the given owlSomeClassExp.
   *
   * <p>
   * This may be a new one if classExpression was not seen before.
   * </p>
   *
   * @param classExpression Expression to use for obtaining the {@link IRI}.
   * @return {@link IRI} resulting from performing query following {@code classExpression}.
   */
  public IRI getZpId(OWLClassExpression classExpression) {
    // Try to obtain IRI from classExpression. If there already is such an ID/IRI, then return it.
    IRI zpIdIri = class2Id.get(classExpression);
    if (zpIdIri != null) {
      return zpIdIri;
    }

    // Otherwise, build new ID, put into class-to-id map and return.
    final String zpId = String.format("ZP:%07d", nextId++);
    zpIdIri = OBOVocabulary.ID2IRI(zpId);
    class2Id.put(classExpression, zpIdIri);
    return zpIdIri;
  }

  /**
   * Check whether {@code classExpression} already has an IRI/ID.
   *
   * @param classExpression The query to use.
   * @return {@code true} if there is an ID for the object with this expression and {@code false}
   *         otherwise.
   */
  public boolean isAlreadyContained(OWLClassExpression classExpression) {
    return class2Id.get(classExpression) != null;
  }

}
