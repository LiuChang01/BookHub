package it.unipi.lsmsd.Controller;

import com.mongodb.client.ClientSession;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Persistence.Neo4jDBDriver;
import it.unipi.lsmsd.Persistence.Neo4jDBManager;

import java.util.List;

public class BookController {
    private MongoDBManager mongoDBManager;
    private Neo4jDBManager neo4jDBManager;
    public void initialize(){
        mongoDBManager= new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        neo4jDBManager= new Neo4jDBManager(Neo4jDBDriver.getInstance().openConnection());
    }
    public boolean addReview(Book book, Review review) {
        try{
            ClientSession session=MongoDBDriver.getInstance().openConnection().startSession();
            session.startTransaction();
            if(mongoDBManager.addReview(book,review,session)){
                if(neo4jDBManager.createUserBookReview(review)){
                    session.commitTransaction();
                    return true;
                }else{
                    session.abortTransaction();
                    return false;
                }
            }else {
                session.abortTransaction();
                return false;
            }
        }catch (Exception e){
            System.out.println("error in add review controller");
            e.printStackTrace();
            return false;
        }
    }
    public void deleteReview(Book book,Review review){
        mongoDBManager.deleteReview(book,review);
    }

}
