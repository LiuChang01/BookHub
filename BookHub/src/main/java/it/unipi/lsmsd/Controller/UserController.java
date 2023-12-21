package it.unipi.lsmsd.Controller;

import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Persistence.Neo4jDBDriver;
import it.unipi.lsmsd.Persistence.Neo4jDBManager;

public class UserController {
    private MongoDBManager mongoDBManager;
    private Neo4jDBManager neo4jDBManager;
    public void initialize(){
        mongoDBManager= new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        neo4jDBManager= new Neo4jDBManager(Neo4jDBDriver.getInstance().openConnection());
    }
    
}
