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
          
    System.out.println("*** Example1c2 "+System.currentTimeMillis());
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];

    // the rulebase id used for inferred triples 
    int iRuleBaseId     = Integer.parseInt(args[3]); 
   
    Model rdfsExample;
    String NS = "urn:x-hp-jena:eg/";
    if(false){
        try{
        OracleNoSqlConnection conn 
              = OracleNoSqlConnection.createInstance("kvstore",
                                                      "localhost", 
                                                      "5000");
        System.out.println("*** Login Credentials: "+szStoreName +", "+szHostName+", "+szHostPort);   
        rdfsExample = OracleModelNoSql.createOracleDefaultModelNoSql(conn);

      }catch(Exception e){
        System.out.println("*** Failed to connect to their server, using our own for now");
        rdfsExample = ModelFactory.createDefaultModel(); //Works for usage without KV-3.etc
      }
    }else{
      rdfsExample = ModelFactory.createDefaultModel(); //Works for usage without KV-3.etc
    }
    
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

     String prefix = 
               " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#" +
               "include_rulebase_id=" + iRuleBaseId + ">";
            
      String szQuery = prefix + " select ?x ?y ?z WHERE {?x ?y ?z} ";
          
      System.out.println("Execute query " + szQuery);
          
      Query query = QueryFactory.create(szQuery) ;
      QueryExecution qexec = QueryExecutionFactory.create(query, rdfsExample);
          
      try {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);
          }
      finally {
            qexec.close();
          }



  }
} 