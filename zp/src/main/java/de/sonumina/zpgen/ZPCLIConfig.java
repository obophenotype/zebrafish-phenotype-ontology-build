package de.sonumina.zpgen;

import com.beust.jcommander.Parameter;

public class ZPCLIConfig
{
	
//	Option zfinFileOpt = new Option("z", "zfin-file", true, "The ZFIN file (e.g., http://zfin.org/data_transfer/Downloads/phenotype.txt)");
//	options.addOption(zfinFileOpt);
//	Option ontoFileOpt = new Option("o","ontology-file", true, "Where the ontology file (e.g. ZP.owl) is written to.");
//	options.addOption(ontoFileOpt);
//	Option annotFileOpt = new Option("a", "annotation-file", true, "Where the annotation file (e.g. ZP.annot) is written to.");
//	options.addOption(annotFileOpt);
//	Option keepOpt = new Option("k", "keep-ids", false, "If the file on the output is already a valid ZP.owl file, keep the ids (ZP_nnnnnnn) stored in that file.");
//	options.addOption(keepOpt);
//	Option help = new Option( "h", "help",false, "Print this (help-)message.");
//	options.addOption(help);
//
//
	@Parameter(names={"-z","--zfin-input-file"},required=true,description="The file containing the decomposed phenotype - gene associations (e.g., http://zfin.org/data_transfer/Downloads/phenotype.txt)")
	public String zfinFilePath;
	
	@Parameter(names={"-o","--ontology-output-file"},required=true,description="Where the ontology file (e.g. ZP.owl) is written to")
	public String ontoFilePath;
	
	@Parameter(names={"-a","--annotation-output-file"},required=true,description="Where the annotation file (e.g. ZP.annot) is written to")
	public String annotFilePath;
	
	@Parameter(names={"-k","--keep-ids"},required=false,description="If the output ontology file is already valid, keep the ids (ZP_nnnnnnn) stored in that file.")
	public boolean keepIds = false;
	
	@Parameter(names={"-h","--help"},help=true,description="Shows this help")
	public boolean help;
}
