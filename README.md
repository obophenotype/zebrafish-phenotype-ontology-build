# Introduction
There are many phenotype ontologies available. For instance the Human Phenotype Ontology is intended to provide a structured vocabulary of phenotypic abnormalities due to Human disease. Terms of phenotype ontologies can be associated to individuals, OMIM diseases, genes, SNPs, etc., depending on the resolution of available data and application. In contrast to the precomposition approach, in which terms are explicitly created like in the Human Phenotype Ontology, the zebrafish community describes phenotypes of zebrafish genes using a strict pattern of terms originating from low-level ontologies.

In order to allow existing algorithm and tools (semantic similarity, enrichment, etc.) to take advantage of this data as well, it is useful to separate the phenotypic concepts from the actual annotations. This project is an attempt in doing so.

# Details #

The goal is to create an ontology in a way that matches the manual creation of the ontology.

Two-level approach

  * Building up the axioms
    * EQ approach
    * Preserve IDs by loading previously generated axioms

  * Inference:
    * Build up the is-a hierarchy
    * Identify more equivalent classes
    * Propagate obsolete ids
    * Propagate consider ids
    * Propagate other relations

# Usage #
This project is used as part of the monarch initiative (http://monarchinitiative.org) and for the construction of the Uberpheno (see http://f1000research.com/articles/2-30)
