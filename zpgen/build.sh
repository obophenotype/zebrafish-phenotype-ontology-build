#!/bin/bash


## step 1 is the most important step. we need the latest zp-version!
# get the previous version of zp, because we retain IDs (rename this file)
rm -f zp.owl
wget http://compbio.charite.de/hudson/job/zp-owl/lastSuccessfulBuild/artifact/zp.owl
#wget http://compbio.charite.de/tl_files/groupmembers/koehler/zp.owl
mv zp.owl zp_previous.owl


# get the required data from ZFIN
#rm phenoGeneCleanData_fish.txt
#rm phenotype_fish.txt
wget -N http://zfin.org/downloads/phenoGeneCleanData_fish.txt
wget -N http://zfin.org/downloads/phenotype_fish.txt


mvn clean install package


# run the normal build 
java -jar target/zp-0.1-SNAPSHOT-jar-with-dependencies.jar --zfin-pheno-txt-input-file phenoGeneCleanData_fish.txt --zfin-phenotype-txt-input-file phenotype_fish.txt -p zp_previous.owl -o zp.owl -a ./ --add-source-information -s zp.annot_sourceinfo --keep-ids

