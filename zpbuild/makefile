#
# This makefile constructs the zp
#
# Author: Sebastian Bauer
#

ZPGEN_JAR = ../zpgen/target/zp-0.1-SNAPSHOT-jar-with-dependencies.jar
ZPGEN =java -jar $(ZPGEN_JAR)
OORT =java -jar owltools-oort-all.jar

# where external ontologies are stored locally
EXTERNAL?=external

.PHONY: all
all: zp-axioms.owl

.PHONY: phenotype.txt
phenotype.txt:
	wget -N http://zfin.org/downloads/phenotype.txt

zp-axioms.owl: phenotype.txt $(ZPGEN_JAR)
	$(ZPGEN) -z $< -a zp.annot -o $@ -k

zp.owl: zp-axioms.owl
	mkdir -p $(EXTERNAL)
	cd $(EXTERNAL) ; cat ../external_ontologies | sed "/^#.*$$/d" | wget --retry-connrefused -N -i -
#	$(OORT) $< external/go.obo -outdir release

.PHONY: $(ZPGEN_JAR)
$(ZPGEN_JAR):
	cd ../zpgen && mvn package
