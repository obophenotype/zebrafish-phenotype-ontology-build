package de.charite.zpgen;

import com.beust.jcommander.Parameter;

/**
 * The command line interface.
 *
 * @author Sebastian Bauer
 */
public class ZPGenCLIConfig {
	@Parameter(names = { "--zfin-pheno-txt-input-file" }, required = true, description = "The file containing the decomposed phenotype - gene associations (i.e. http://zfin.org/downloads/pheno.txt)")
	public String zfinPhenoTxtPath;

	@Parameter(names = { "--zfin-phenotype-txt-input-file" }, required = true, description = "The file containing the decomposed phenotype - genotype associations (i.e. http://zfin.org/downloads/phenotype.txt)")
	public String zfinPhenotypeTxtPath;

	@Parameter(names = { "-p", "--previous-ontology-file" }, required = true, description = "The last version of the ontology. Used to keep IDs!!!")
	public String previousOntologyFilePath;

	@Parameter(names = { "-o", "--ontology-output-file" }, required = true, description = "Where the ontology file (e.g. ZP.owl) is written to")
	public String ontologyOutputFilePath;

	@Parameter(names = { "-a", "--annotation-output-folder" }, required = true, description = "Where the annotation files (e.g. ZP.annot) are written to")
	public String annotationsFolder;

	@Parameter(names = { "-k", "--keep-ids" }, required = false, description = "If the output ontology file is already valid, keep the ids (ZP_nnnnnnn) stored in that file.")
	public boolean keepIds = false;

	@Parameter(names = { "--add-source-information" }, required = false, description = "If set to true, add a tab delimited source information for the class expression to the ontology.")
	public boolean addSourceInformation = false;

	@Parameter(names = { "--add-zfa-uberon-equivalence" }, required = false, description = "If set to true, the resulting ontology will contain equivalence axioms between ZFA classes and UBERON classes (this requires uberon.obo). The purpose is reasoning capabilities across multiple species.")
	public boolean addZfaUberonEquivalencies = false;

	@Parameter(names = { "-u", "--uberon-obo-file" }, required = false, description = "Required if '--add-zfa-uberon-equivalence' is used.")
	public String uberonOboFilePath;

	@Parameter(names = { "-s", "--source-information-output-file" }, required = false, description = "Add the source information to the ontology and save is also in a separate file. Implies --add-source-information set to true.")
	public String sourceInformationFile = null;

	@Parameter(names = { "--use-inheres-in-part-of" }, required = false, description = "If set to true, 'inheres in' is replaced by 'inheres in part of'.")
	public boolean useInheresInPartOf = false;

	@Parameter(names = { "--use-owlrdf-syntax" }, required = false, description = "If set to true, writes the output ontology in OWL-RDF syntax, which will otherwise be manchester functional syntax.")
	public boolean useOwlRdfSyntax = false;

	@Parameter(names = { "-h", "--help" }, help = true, description = "Shows this help")
	public boolean help;

}
