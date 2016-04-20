README

Hi! This is Brian Ellis and Deidre Crous. We did a cool thing with examples, and now are ready to try and hook it up to carnot.

To run the different examples:

>>> ./comp [example sufix]

The resulting output will appear in environment/out.txt

An interesting example is Example1c2.java, which is the only example to hold interesting inference usage.
Currently, it only demonstrates RDFS.subPropertyOf, but the reasoner should support everything defined in jena.vocabulary.RDFS
To run this example:

1) run the kv-3.5.2 database
>>> ./kv_connector.sh

2) run the compile script.
>>> ./comp 1c2

A very disappointing example is Example1c3.java, which demonstrates the shortcomings of the Jena OWL Reasoner. It prints all the OWL vocabulary it implements, and then attempts an inference that we hoped would be trivial (inverseOf) which it does not implement. See results by:

>>> ./comp 1c3
