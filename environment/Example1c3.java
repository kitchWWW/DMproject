import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import oracle.rdf.kv.client.jena.*;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec; 
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;

public class Example1c3 {
  
  public static void main(String[] args) throws Exception 
  {
   
    try {
      
      System.out.println("*** Example1c3 "+System.currentTimeMillis());

      String NS = "urn:x-hp-jena:eg/";
    
      Model owlExample = ModelFactory.createDefaultModel();

      Resource a = owlExample.createResource(NS+"a");
      Resource b = owlExample.createResource(NS+"b");
      
      Property p = owlExample.createProperty(NS, "parentOf");
      Property c = owlExample.createProperty(NS, "childOf");

      owlExample.add(p, OWL.inverseOf, c);
    
      a.addProperty(p, b);
  
      Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
      System.out.println( reasoner.getReasonerCapabilities());
        //prints the full vocabulary that the OWL Jena Inference Engine supports
      InfModel inf = ModelFactory.createInfModel(reasoner, owlExample);  
    
      System.out.println("Statement: " + a.getProperty(p));
      // Prints A parent of B because we said so.

      System.out.println("Statement: " + b.getProperty(c));
      //Prints NULL because the Jena OWL Inference Engine only supports a subset of the vocabulary, not including "inverseOf"
          //It does inplement an InverseFunctionalProperty, but that returns a resource, and we don't know what to do with that

    } catch(Exception e){
      e.printStackTrace();
    }

  }
} 