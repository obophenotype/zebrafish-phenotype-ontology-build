# Introduction
There are many phenotype ontologies available. For instance the Human Phenotype Ontology (HPO) is intended to provide a structured vocabulary of phenotypic abnormalities. Terms of phenotype ontologies can be associated to individuals, diseases, genes, SNPs, etc., depending on the resolution of available data and application. In contrast to the pre-composition approach, in which terms are explicitly created like in the HPO and MPO, the zebrafish community describes phenotypes of zebrafish genes using a strict pattern of terms originating from low-level ontologies (post-composition).

In order to allow existing algorithm and tools (semantic similarity, enrichment, etc.) to take advantage of this data as well, it is useful to separate the phenotypic concepts from the actual annotations. This project is an attempt in doing so.

# Details #

We create an ontology of zebrafish phenotypes obtained from http://zfin.org/downloads

Two-level approach

  * Building up the axioms
    * EQ approach
    * Preserve IDs by loading previously generated axioms


# Usage #
This project is used as part of the Monarch Initiative (http://monarchinitiative.org) and for the construction of the Uberpheno (see http://f1000research.com/articles/2-30)
