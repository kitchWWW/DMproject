import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

public class Example1c {
  
  public static void main(String[] args) throws Exception 
  {
          
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];

    // the rulebase id used for inferred triples 
    int iRuleBaseId     = Integer.parseInt(args[3]); 

    System.out.println("*** Login Credentials: "+szStoreName +", "+szHostName+", "+szHostPort);   

    try {
      OracleNoSqlConnection conn 
           = OracleNoSqlConnection.createInstance("kvstore",
                                                  "localhost", 
                                                  "5000");

      System.out.println("*** Created Instance");
      // This object handle a model associated to a default graph
      Model model = OracleModelNoSql.createOracleDefaultModelNoSql(conn);
      System.out.println("*** Opened a model");

      OracleGraphNoSql graph = (OracleGraphNoSql) model.getGraph();
      System.out.println("*** Got a graph from the thinggy");

      model.removeAll(); // removes all the triples from the associated 
                         // model, this will remove all asserted and 
                         // inferred triples
      System.out.println("*** Removed everything from the model");

      System.out.println("*** Now adding things");
      graph.add(Triple.create(Node.createURI("u:John"), Node.createURI("u:parentOf"), Node.createURI("u:Mary")));
          
      graph.add(Triple.create(Node.createURI("u:John"), Node.createURI("u:parentOf"), Node.createURI("u:Jack")));
          
      graph.add(Triple.create(Node.createURI("u:Amy"), Node.createURI("u:parentOf"), Node.createURI("u:Jack")));   

      System.out.println("*** Stuff has been added");

      // This object handles all the inferred triples of the default graph 
      // produced with rulebase ID
      InferredGraphNoSql inferredGraph = 
                                 new InferredGraphNoSql(conn, iRuleBaseId);
          
      inferredGraph.add(Triple.create(Node.createURI("u:Jack"), 
                                      Node.createURI("u:siblingOf"),
                                      Node.createURI("u:Mary")));
          
      inferredGraph.close();
            
          
      String prefix = 
               " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#" +
               "include_rulebase_id=" + iRuleBaseId + ">";
            
      String szQuery = prefix + " select ?x ?y ?z WHERE {?x ?y ?z} ";
          
      System.out.println("Execute query " + szQuery);
          
      Query query = QueryFactory.create(szQuery) ;
      QueryExecution qexec = QueryExecutionFactory.create(query, model);
          
      try {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);
          }
      finally {
            qexec.close();
          }
          
      model.close();
      conn.dispose();

    } catch(Exception e){
      e.printStackTrace();
    }

  }
} 