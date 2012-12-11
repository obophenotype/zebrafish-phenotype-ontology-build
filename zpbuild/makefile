#
# This makefile constructs the zp
#
# Author: Sebastian Bauer
#

ZPGEN_JAR = ../zpgen/target/zp-0.1-SNAPSHOT-jar-with-dependencies.jar
ZPGEN =java -jar $(ZPGEN_JAR)

.PHONY: all
all: zp-axioms.owl

.PHONY: phenotype.txt
phenotype.txt:
	wget -N http://zfin.org/downloads/phenotype.txt

zp-axioms.owl: phenotype.txt
	$(ZPGEN) -z $< -a zp.annot -o $@ -k

.PHONY: $(ZPGEN_JAR)
$(ZPGEN_JAR):
	cd ../zpgen && mvn package
