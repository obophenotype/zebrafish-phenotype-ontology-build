package de.charite.zpgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * Tests ZPIDDB.
 * 
 * @author Sebastian Bauer
 */
public class ZpIdDbTest {

  @Test
  public void testZpIdDb() throws OWLOntologyStorageException, OWLOntologyCreationException {
    /* Create a small test ontology */
    final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    final OWLDataFactory factory = manager.getOWLDataFactory();
    final IRI zpIri = IRI.create("http://purl.obolibrary.org/obo/");
    final OWLObjectProperty partOf =
        factory.getOWLObjectProperty(IRI.create(zpIri + "BFO_0000050"));

    final OWLOntology testOntology = manager.createOntology(IRI.create("test"));

    ZpIdDb zpIdDb = new ZpIdDb();

    OWLClass cl1 = factory.getOWLClass(IRI.create("TEST:1"));
    OWLClass cl2 = factory.getOWLClass(IRI.create("TEST:2"));
    OWLClass cl3 = factory.getOWLClass(IRI.create("TEST:3"));

    OWLClassExpression expr1 =
        factory.getOWLObjectIntersectionOf(cl1, factory.getOWLObjectSomeValuesFrom(partOf, cl2));
    IRI zp1Iri = zpIdDb.getZpId(expr1);
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000001"), zp1Iri);
    manager.addAxiom(testOntology,
        factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(zp1Iri), expr1));

    OWLClassExpression expr2 =
        factory.getOWLObjectIntersectionOf(cl1, factory.getOWLObjectSomeValuesFrom(partOf, cl3));
    IRI zp2Iri = zpIdDb.getZpId(expr2);
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000002"), zp2Iri);
    manager.addAxiom(testOntology,
        factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(zp2Iri), expr2));

    OWLClass cl1a = factory.getOWLClass(IRI.create("TEST:1"));
    OWLClassExpression expr1a =
        factory.getOWLObjectIntersectionOf(cl1a, factory.getOWLObjectSomeValuesFrom(partOf, cl2));
    IRI zp1aIri = zpIdDb.getZpId(expr1a);
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000001"), zp1aIri);
    manager.addAxiom(testOntology,
        factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(zp1Iri), expr1a));

    OWLClass pato = factory.getOWLClass(IRI.create("PATO:1"));
    OWLClassExpression expr3 = factory.getOWLObjectIntersectionOf(pato, cl1,
        factory.getOWLObjectSomeValuesFrom(partOf, cl3));
    IRI zp3Iri = zpIdDb.getZpId(expr3);
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000003"), zpIdDb.getZpId(expr3));
    manager.addAxiom(testOntology,
        factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(zp3Iri), expr3));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    manager.saveOntology(testOntology, baos);
    manager.saveOntology(testOntology, System.out);
    byte[] output = baos.toByteArray();

    /* And now the second pass, i.e., load the ontology again and create some old and new classes */
    ByteArrayInputStream bais = new ByteArrayInputStream(output);
    final OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
    OWLOntology testOntology2 = manager2.loadOntologyFromOntologyDocument(bais);

    ZpIdDb zpIdDB2 = new ZpIdDb(testOntology2);
    OWLDataFactory factory2 = manager.getOWLDataFactory();
    OWLClass cl21 = factory2.getOWLClass(IRI.create("TEST:1"));
    OWLClass cl22 = factory2.getOWLClass(IRI.create("TEST:2"));
    OWLClass cl23 = factory2.getOWLClass(IRI.create("TEST:3"));
    OWLClass cl24 = factory2.getOWLClass(IRI.create("TEST:4"));

    final OWLObjectProperty partOf2 =
        factory2.getOWLObjectProperty(IRI.create(zpIri + "BFO_0000050"));

    OWLClassExpression expr22 =
        factory2.getOWLObjectIntersectionOf(cl1, factory.getOWLObjectSomeValuesFrom(partOf, cl23));
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000002"), zpIdDB2.getZpId(expr22));

    OWLClassExpression expr21 = factory2.getOWLObjectIntersectionOf(cl21,
        factory.getOWLObjectSomeValuesFrom(partOf2, cl22));
    Assert.assertEquals(zp1Iri, zpIdDB2.getZpId(expr21));

    OWLClassExpression expr24 = factory2.getOWLObjectIntersectionOf(cl24,
        factory.getOWLObjectSomeValuesFrom(partOf2, cl22));
    Assert.assertEquals(OBOVocabulary.ID2IRI("ZP:0000004"), zpIdDB2.getZpId(expr24));

  }
}
