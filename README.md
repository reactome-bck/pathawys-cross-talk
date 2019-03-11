[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Pathway cross-talk
The aim of this project is to find molecules (proteins or chemicals) that are not yet curated in Reactome that can bridge
two existing pathways.

This approach should help curators to identify *low-hanging fruit* molecules to enter in Reactome and also create meaningful
connections between different domains of biology that are not yet annotated in our database. 

## Strategy:
The first step to identify a *target* **interactor** that bridges two pathways is to find a molecule that interacts with
two existing *reference entities* (*A* and *B*). An initial requirement for the script is that this interacting molecule
does not have to be curated in Reactome. For this step, we use data integrated from the [IntAct database](https://www.ebi.ac.uk/intact/)
and stored directly in the [UndirectedInteraction](https://reactome.org/content/schema/UndirectedInteraction) class
instances in our [graph database](https://reactome.org/dev/graph-database).

Let's start sketching the described interaction between two existing *reference entities* (*A* and *B*) and the *target*
**interactor** bridge (which is the *new molecule* to be curated in Reactome) like:
```
                          ___score___    ___score___           
                         |     A     |  |     B     |          
                         |           |  |           |          
                      reference - interactor - reference    
                       entity       bridge      entity         
                         A          ^^^^^^        B                  
```
Based on IntAct data, each interaction has a score assigned. The score for the interaction between the *reference entity
A* and the **interactor bridge** is *score A*. The same principle applies for the interaction between the **interactor
bridge** and *reference entity B* (*score B*).

Extending the sketch presented above, the next step is to include physical entities. *Physical entity A* and *Physical
entity B*  point to *reference entity A* and *reference entity B* respectively. The caveat here is that both physical
entities **must share compartment** and **must not have modified residues**.

```
               _______/     SHARE COMPARTMENT AND     \_______
              |       \ DO NOT HAVE MODIFIED RESIDUES /       |
              |                                               |
              |           ___score___    ___score___          |
              |          |     A     |  |     B     |         |
              |          |           |  |           |         |
           physical > reference - interactor - reference < physical
            entity     entity       bridge      entity      entity
              A          A          ^^^^^^        B           B
```
It is also important to note that to ensure that the interaction can take place as defined in IntAct, the corresponding
physical entities do not have to be part of complexes or entity sets for the time being.
```
               _______/     SHARE COMPARTMENT AND     \_______
              |       \ DO NOT HAVE MODIFIED RESIDUES /       |
              |                                               |
              |           ___score___    ___score___          |
              |          |     A     |  |     B     |         |
              |          |           |  |           |         |
           physical > reference - interactor - reference < physical
            entity     entity       bridge      entity      entity
              A          A          ^^^^^^        B           B
              |                                               |
              |_____________ DIRECTLY IN DIAGRAM _____________|
```
Finally, it is time to include the pathways for which the **interactor bridge** will be reported. Being *pathway A* the
one where *physical entity A* is present and *pathway B* for *physical entity B*. 

```
     ________________________ NOT NESTED EVENTS _________________________        
    |                                                                    |      
    |          _______/     SHARE COMPARTMENT AND     \_______           |      
    |         |       \ DO NOT HAVE MODIFIED RESIDUES /       |          |      
    |         |                                               |          |      
    |         |           ___score___    ___score___          |          |      
    |         |          |     A     |  |     B     |         |          |      
    |         |          |           |  |           |         |          |      
 pathway > physical > reference - interactor - reference < physical < pathway   
    A       entity     entity       bridge      entity      entity       B      
    |         A          A          ^^^^^^        B           B          |      
    |         |                                               |          |      
    |         |_____________ DIRECTLY IN DIAGRAM _____________|          |      
    |                                                                    |      
    |___________________________ SAME SPECIES ___________________________|      
```
It is important to mention
two main points:

1. Both pathways have to belong to the same species (although for the time being only 'Homo sapiens' pathways are reported)
2. These pathways must not be nested.  

## Result report explained:
For this explanation, we use the result of executing the script against data for release 67 using a threshold score of
0.75. Every pair of bridgeable pathways are reported as follow (in a csv file): 

| Report entry    | &nbsp;                                           | &nbsp;                   | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      |                             |                      
| ---             | ---                                              | ---                      | ---                                                                                        | ---                                           | ---                                                                                         | ---                         |
| R-HSA-193704    | p75 NTR receptor-mediated signalling             | &nbsp;                   | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      | &nbsp;                      |
| R-HSA-5633008   | TP53 Regulates Transcription of Cell Death Genes | &nbsp;                   | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      | &nbsp;                      |
| Score: 0.90602  | &nbsp;                                           | &nbsp;                   | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      | Common physical entities: 0 |
| &nbsp;          | &nbsp;                                           | UniProt:Q07817-1 BCL2L1  | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:O43521 BCL2L11                                                                     | <-[0.935]- UniProt:Q07817-1 BCL2L1 -[0.969]-> | UniProt:Q07812 BAX                                                                          | Score: 0.90602              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BCL2L11 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-2976673)	 | &nbsp;                                        | [BAX [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-139916)       | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:O43521 BCL2L11                                                                     | <-[0.935]- UniProt:Q07817-1 BCL2L1 -[0.862]-> | UniProt:P55957 BID                                                                          | Score: 0.80597              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BCL2L11 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-2976673)  | &nbsp;                                        | [BID(1-195) [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-50825) | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:O43521 BCL2L11                                                                     | <-[0.935]- UniProt:Q07817-1 BCL2L1 -[0.855]-> | UniProt:Q9BXH1 BBC3                                                                         | Score: 0.79943              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BCL2L11 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-2976673)  | &nbsp;                                        | [BBC3 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-140220)      | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:Q92934 BAD                                                                         | <-[0.807]- UniProt:Q07817-1 BCL2L1 -[0.969]-> | UniProt:Q07812 BAX                                                                          | Score: 0.78198              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BAD [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-50662)        | &nbsp;                                        | [BAX [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-139916)       | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:Q92934 BAD                                                                         | <-[0.807]- UniProt:Q07817-1 BCL2L1 -[0.862]-> | UniProt:P55957 BID                                                                          | Score: 0.69563              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BAD [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-50662)        | &nbsp;                                        | [BID(1-195) [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-50825) | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:Q92934 BAD                                                                         | <-[0.807]- UniProt:Q07817-1 BCL2L1 -[0.855]-> | UniProt:Q9BXH1 BBC3                                                                         | Score: 0.68999              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BAD [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-50662)        | &nbsp;                                        | [BBC3 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-140220)      | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | UniProt:Q92843 B2CL2     | &nbsp;                                                                                     | &nbsp;                                        | &nbsp;                                                                                      | &nbsp;                      |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | UniProt:O43521 BCL2L11                                                                     | <-[0.855]- UniProt:Q92843 B2CL2 -[0.750]->    | UniProt:P55957 BID                                                                          | Score: 0.64125              |
| &nbsp;          | &nbsp;                                           | &nbsp;                   | [BCL2L11 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-2976673)	 | &nbsp;                                        | [BID(1-195) [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-50825) | &nbsp;                      |
		
The first two lines show *pathway A* and *pathway B* (identifier and name). The third line shows (1) the highest combined
score amongst the different interactions found for each **interactor bridge** and their interacting molecules in each pathway,
(2) the common physical entities already existing between *pathway A* and *pathway B* and (3) when there are common physical
entities between *pathway A* and *pathway B*, those are also enumerated.

The following lines for an entry report the *molecule* acting as the **interactor bridge** (e.g. "UniProt:Q07817-1 BCL2L1")
followed with all the found interactions between it and participating molecules in *pathway A* and *pathway B*. Let's use
one of them as example:

```
UniProt:O43521 BCL2L11 <-[0.935]- UniProt:Q07817-1 BCL2L1 -[0.969]-> UniProt:Q07812 BAX
BCL2L11 [cytosol]                                                    BAX [cytosol] 
```
The above says that "UniProt:O43521 BCL2L11" is present in *pathway A* and it interacts with the **interactor bridge** 
with a score of 0.935. At the same time, the **interactor bridge** interacts with "UniProt:Q07812 BAX" (present in 
*pathway B*) with a score of 0.969. The combined score for this interaction is shown in the next column. It is calculated
by multiplying both *score A* and *score B*.

[BCL2L11 [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-193704&SEL=R-HSA-2976673) and [BAX [cytosol]](https://reactome.org/PathwayBrowser/#/R-HSA-5633008&SEL=R-HSA-139916)
are clickable so it is easier to see the target existing molecules directly in the pathways that are target for cross-talk
in this entry.

## Usage:
This project can be used as a standalone compiled tool:

```
java -jar interactor-bridge-jar-with-dependencies.jar --help

Usage:
  Main [--help] [(-h|--host) <host>]
  [(-p|--port) <port>] [(-u|--user) <user>] [(-s|--score) <score>] (-o|--output)
  <output> (-k|--password) <password>

Reports interactors as possible bridges between pathways in Reactome

  [--help]
        Prints this help message.

  [(-h|--host) <host>]
        The neo4j host (default: localhost)

  [(-p|--port) <port>]
        The neo4j port (default: 7474)

  [(-u|--user) <user>]
        The neo4j user (default: neo4j)

  [(-s|--score) <score>]
        Threshold score for IntAct interactions (range [0.45, 1]) (default: 0.45)

  (-o|--output) <output>
        Output file

  (-k|--password) <password>
        The neo4j password (default: neo4j)

```