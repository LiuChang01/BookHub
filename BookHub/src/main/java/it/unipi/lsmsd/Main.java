package it.unipi.lsmsd;

import com.mongodb.client.ClientSession;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.LastBookReviews;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Persistence.Neo4jDBDriver;
import it.unipi.lsmsd.Persistence.Neo4jDBManager;
import it.unipi.lsmsd.Utils.Utils;

import java.util.*;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        System.out.println(Utils.readConfigurationParameters());
        Neo4jDBManager neo4jDBManager= new Neo4jDBManager(Neo4jDBDriver.getInstance().openConnection());
        User chang=new User("LiuChangUser"," ",0, new ArrayList<>());
        //System.out.println(neo4jDBManager.addUser(new User("LiuChangUser"," ",0, new ArrayList<>())));
        //System.out.println(neo4jDBManager.getNumFollowingUser(new User("D. Oppenheimer"," ",0,new ArrayList<>())));
        System.out.println(neo4jDBManager.userLikesAuthor(new User("LiuChangUser"," ",0, new ArrayList<>()),"Alessandro Manzoni"));
        //Review review=new Review("B000NRFXJI","The Betrothed (I Promessi Sposi)","LiuChangUser",2F, new Date(),"is a good book",new ArrayList<>(),new ArrayList<>());
        //System.out.println(neo4jDBManager.addBook(new Book("12345678","Bro sei un meme",null,Arrays.asList("Alessandro Manzoni","The best Bro"),Arrays.asList("Fiction","Broo"),new Date(), new ArrayList<>())));
        //System.out.println(neo4jDBManager.userPrefersGenre(new User("LiuChangUser"," ",0, new ArrayList<>()),"Horror films"));
        //System.out.println(neo4jDBManager.createFollowRelationship(new User("LiuChangUser"," ",0, new ArrayList<>()),new User("D. Oppenheimer"," ",0,new ArrayList<>())));
        //System.out.println(neo4jDBManager.userDisLikesAuthor(chang,"Alessandro Manzoni"));
        //System.out.println(neo4jDBManager.getUsersWithMostFollowers(10));
        //System.out.println(neo4jDBManager.deleteFollowRelationship(chang,new User("D. Oppenheimer"," ",0,new ArrayList<>())));

        System.out.println(neo4jDBManager.recommendationBasedOnAuthorsLiked(chang,2));
        /*


        MongoDBManager mongoDBManager = new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        ArrayList<Integer> couts=new ArrayList<>();
        System.out.println(mongoDBManager.getMostActiveUsers("2005-01-01","2023-01-01",0,5,couts));
        System.out.println(couts);
        ArrayList<Double> scores=new ArrayList<>();
        System.out.println(mongoDBManager.getTopBooks(100,Arrays.asList("Fiction"),5,0,scores));
        System.out.println(scores);
        System.out.println(mongoDBManager.searchBooksByParameters("Sword", Arrays.asList("R.A. Salvatore"),"2000-01-01","2020-01-01",Arrays.asList("Fiction"),0,3).size());
        System.out.println(mongoDBManager.getMostVersatileUsers(0,5));
        System.out.println(mongoDBManager.getTopCategoriesOfNumOfBookPublished(0,5));
        ArrayList<Double> out=new ArrayList<>();
        System.out.println(mongoDBManager.getMostRatedAuthors(0,5,20,out));
        System.out.println(out);

         */
        System.out.println( "Hello World!" );
    }
}
