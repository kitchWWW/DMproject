import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import oracle.rdf.kv.client.jena.*;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec; 
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;

public class Example1c2 {
  
  public static void main(String[] args) throws Exception 
  {
          
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];

    // the rulebase id used for inferred triples 
    int iRuleBaseId     = Integer.parseInt(args[3]); 

   
    try {
      // OracleNoSqlConnection conn 
      //      = OracleNoSqlConnection.createInstance("kvstore",
      //                                             "localhost", 
      //                                             "5000");
      // System.out.println("*** Login Credentials: "+szStoreName +", "+szHostName+", "+szHostPort);   

      String NS = "urn:x-hp-jena:eg/";

      //Model rdfsExample = OracleModelNoSql.createOracleDefaultModelNoSql(conn);
      Model rdfsExample = ModelFactory.createDefaultModel(); //Works for usage without KV-3.etc

      Property p = rdfsExample.createProperty(NS, "p");
      Property q = rdfsExample.createProperty(NS, "q");
      rdfsExample.add(p, RDFS.subPropertyOf, q);
      rdfsExample.createResource(NS+"a").addProperty(p, "foo");
      
      Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
      InfModel inf = ModelFactory.createInfModel(reasoner, rdfsExample);   

      Resource a = inf.getResource(NS+"a");

      System.out.println("Statement: " + a.getProperty(p));
//    Because we added, should put:  Statement: [urn:x-hp-jena:eg/a, urn:x-hp-jena:eg/p, "foo"]

      System.out.println("Statement: " + a.getProperty(q));
//    Because inference, should put:  Statement: [urn:x-hp-jena:eg/a, urn:x-hp-jena:eg/q, "foo"]


    } catch(Exception e){
      e.printStackTrace();
    }

  }
} 