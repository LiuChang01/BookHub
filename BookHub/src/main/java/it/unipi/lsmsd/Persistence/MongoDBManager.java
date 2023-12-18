package it.unipi.lsmsd.Persistence;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.*;

import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Model.User;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import java.text.SimpleDateFormat;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.inc;

public class MongoDBManager {
    public MongoDatabase db;
    private MongoCollection userCollection;
    private MongoCollection bookCollection;
    private MongoCollection reviewCollection;

    public MongoDBManager(MongoClient client){
        this.db= client.getDatabase("bookHubMongo");
        userCollection=db.getCollection("users");
        bookCollection=db.getCollection("book");
        reviewCollection=db.getCollection("reviews");
    }
    public User login(String profileName, String password){
        Document result=(Document) userCollection.find(Filters.and(eq("profileName",profileName),eq("password",password))).first();
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }
    private User checkExistence(String profilename){
        Document result=(Document) userCollection.find(Filters.and(eq("profileName",profilename))).first();
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }
    public boolean addUser(User user){
        try{
            if(user.getprofileName().isEmpty()||user.getPassword().isEmpty()){
                System.out.println("Enter a good profileName and Password");
            }
            User exists=checkExistence(user.getprofileName());
            if(exists !=null){
                System.out.println("profileName already exists");
                return false;
            }
            Document document= new Document("profileName", user.getprofileName()).append("password", user.getPassword()).append("type",user.getType()).append("last_reviews",user.getLast_reviews());
            userCollection.insertOne(document);
            return true;
        }catch (Exception e){
            System.out.println("problems in insert in db of new user register");
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteUser(User user){
        //dipende
        return false;
    }
    public boolean updateUser(User user){
        try{
            if (user.getPassword().isEmpty()) {
                System.out.println("password empty");
                return false;
            }
            Document document = new Document("password", user.getPassword());
            Bson update = new Document("$set", document);
            userCollection.updateOne(new Document("profileName", user.getprofileName()), update);
            return true;
        }catch (Exception e){
            System.out.println("problems with update user password");
            e.printStackTrace();
            return false;
        }
    }
    public User getUserByProfileName(String profileName){
        return checkExistence(profileName);
    }

    public boolean addReview(Book book, Review review){
        try {
            SimpleDateFormat dateFormat= new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            Document document_last_review=new Document("profileName",review.getProfileName()).append("Title",review.getTitle()).append("score",review.getScore()).append("time", dateFormat.format(review.getTime())).append("review",review.getReview());
            Document document_last_users_review=new Document("ISBN",review.getISBN()).append("Title",review.getTitle()).append("score",review.getScore()).append("time", dateFormat.format(review.getTime())).append("review",review.getReview());
            Document document_review=new Document("ISBN",review.getISBN()).append("Title",review.getTitle()).append("profileName",review.getProfileName()).append("score",review.getScore()).append("time", dateFormat.format(review.getTime())).append("review",review.getReview()).append("categories",review.getCategories()).append("authors",review.getAuthors());
            Bson find=and(eq("ISBN",book.getISBN()));
            reviewCollection.insertOne(document_review);



            //da finire
            return true;
        }catch (Exception e ){
            System.out.println("problems with insert of a comment");
            e.printStackTrace();
            return false;
        }
    }
}
