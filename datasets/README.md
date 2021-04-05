-----------------------------------------------------------------------
-- DATA USED FOR JOINTLY EMBEDDING KNOWLEDGE GRAPHS AND LOGICAL RULES--
-----------------------------------------------------------------------

------------------
OUTLINE:
1. Introduction
2. Content
3. Data Format
4. Data Statistics
5. How to Cite
6. Contact
------------------


------------------
1. INTRODUCTION:
------------------

There are two datasets WN18 and FB122 used for Jointly Embedding Knowledge Graphs and Logical Rules. 
WN18 is a subgraph of WordNet containing 18 relations. FB122 is composed of 122 Freebase relations regarding 
the topics of “people”, “location”, and “sports”, extracted from FB15K.


------------------
2. CONTENT:
------------------

The data archive contains 1 README file + 2 folders:
  - README: the specification document
  - Folder wn18: the WN18 data set
  - Folder fb122: the FB122 data set

Each folder contains 6 files:
  - {dataset}_triples.train
  - {dataset}_triples.valid
  - {dataset}_triples.test
  - {dataset}_triples.neg.valid
  - {dataset}_triples.neg.test
  - {dataset}_rule
  
The 3 files {dataset}_triples.train/valid/test contain the observed triples
(training/validation/test sets). They are used in both link prediction and
triple classification.

The 2 files {dataset}_triples.neg.valid/test contain the negative triples
constructed for positive ones in the validation/test sets, with a positive-to-negative ratio of 1:10. 
They are used only in triple classification.

The file {dataset}_rule contains logical rules created for the dataset, which can be used for gounding 
to obtain ground rule sets(i.e. propositional statements).


------------------
3. DATA FORMAT
------------------

The {dataset}_triples.* files contain one triple per line, stored in a tab ('\t')
separated format. The first element is the head entity, the second the relation,
and the third the tail entity.

The {dataset}_rule file contains one rule per line. Two types of rules are stored in this file. 
The first type is stored in the form of r_s(x,y)==>r_t(x,y) or r_s(x,y)==>r_t(y,x).
The second type is stored in the form of r_s1(x,y)&&r_s2(y,z)==>r_t(x,y).
Here, r_s and r_t denote relations, x/y/z denote variable that can be instantiated with the
concrete entities to get ground rules, ==> represents logical implication, and && represents logical conjunction.
For example, given a rule Capital-Of(x,y)==>Located-In(x,y), we can instantiate it with the
concrete entities of Paris and France, resulting in ground rule as Capital-Of(Paris,France)==>Located-In(Paris,France).


------------------
4. DATA STATISTICS
------------------

The WN18 data set consists of 40,943 entities and 18 relations among them.
The training set contains 141,442 triples, the validation set 5,000 triples,
and the test set 5,000 triples. 14 rules are created for WN18.

The FB122 data set consists of 9,738 entities and 122 relations among them.
The training set contains 91,638 triples, the validation set 9,595 triples,
and the test set 11,244 triples. 47 rules are created for FB122.

All triples are unique and we made sure that all entities/relations appearing in
the validation or test sets were occurring in the training set.


------------------
5. HOW TO CITE
------------------

When using this data, one should cite the original paper:  
  @inproceedings{guo2016:KALE,  
    title     = {Jointly Embedding Knowledge Graphs and Logical Rules},  
    author    = {Shu Guo and Quan Wang and Lihong Wang and Bin Wang and Li Guo},  
    booktitle = {Proceedings of the 2016 Conference on Empirical Methods in Natural Language Processing},  
    year      = {2016},  
    pages      = {192-202}<br> 
  }


------------------  
6. CONTACT
------------------

For all remarks or questions please contact Quan Wang:
wangquan (at) iie (dot) ac (dot) cn .


