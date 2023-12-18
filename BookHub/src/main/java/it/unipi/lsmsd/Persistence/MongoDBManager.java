package it.unipi.lsmsd.Persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Model.User;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class MongoDBManager {
    public MongoDatabase db;
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> bookCollection;
    private final MongoCollection<Document> reviewCollection;

    public MongoDBManager(MongoClient client){
        this.db= client.getDatabase("bookHubMongo");
        userCollection=db.getCollection("users");
        bookCollection=db.getCollection("book");
        reviewCollection=db.getCollection("reviews");
    }
    public User login(String profileName, String password){
        Document result= userCollection.find(Filters.and(eq("profileName",profileName),eq("password",password))).first();
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }
    private User checkExistence(String profilename){
        Document result= userCollection.find(Filters.and(eq("profileName",profilename))).first();
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
    //da testare e find che use
    public boolean addReview(Book book, Review review){
        try {
            SimpleDateFormat dateFormat= new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            Document document_last_users_review=new Document("profileName",review.getProfileName()).append("time", dateFormat.format(review.getTime())).append("score",review.getScore()).append("review",review.getReview());
            Document document_last_review=new Document("ISBN",review.getISBN()).append("Title",review.getTitle()).append("score",review.getScore()).append("time", dateFormat.format(review.getTime())).append("review",review.getReview());
            Document document_review=new Document("ISBN",review.getISBN()).append("Title",review.getTitle()).append("profileName",review.getProfileName()).append("score",review.getScore()).append("time", dateFormat.format(review.getTime())).append("review",review.getReview()).append("categories",review.getCategories()).append("authors",review.getAuthors());
            Bson find=and(eq("ISBN",book.getISBN()));
            reviewCollection.insertOne(document_review);
            Document filter_book=new Document("ISBN",book.getISBN());
            Document update_last_users_review=new Document("$push",new Document("last_users_review",new Document("$each",document_last_users_review).append("$position",0))).append("$slice",-5);
            Document filter_user=new Document("profileName",review.getProfileName());
            Document update_last_review=new Document("$push",new Document("last_reviews",new Document("$each",document_last_review).append("$position",0))).append("$slice",-5);
            userCollection.updateOne(filter_user,update_last_review,new UpdateOptions().upsert(true));
            bookCollection.updateOne(filter_book,update_last_users_review,new UpdateOptions().upsert(true));
            return true;
        }catch (Exception e ){
            System.out.println("problems with insert of a comment");
            e.printStackTrace();
            return false;
        }
    }
    //update reviews??

    //delete comments policy?


    public Book getBookByISBN(String ISBN){
        try {
            if(ISBN.isEmpty()){
                System.out.println("ISBN empty");
                return null;
            }
            Book b;
            Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            Document document= bookCollection.find(and(eq("ISBN",ISBN))).first();
            b=gson.fromJson(gson.toJson(document), Book.class);
            return b;
        }catch (JsonSyntaxException e){
            System.out.println("problems with conversion in the getBookByISBN");
            e.printStackTrace();
            return null;
        }
    }
    public List<Book> searchBooksByParameters(String title, List<String> authors, String startDate, String endDate, List<String> categories, int skip, int limit) {
        List<Book>books=new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        List<Bson>pipeline=new ArrayList<>();
        if(title!=null&&!title.isEmpty()){
            Pattern pattern=Pattern.compile("^.*" + title + ".*$");
            pipeline.add(Aggregates.match(Filters.or(Filters.regex("Title",pattern),Filters.eq("Title",title))));
        }
        if (authors != null && !authors.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.in("authors", authors)));
        }
        if (categories != null && !categories.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.in("categories", categories)));
        }
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);
                pipeline.add(Aggregates.match(Filters.and(
                        Filters.gte("publishedDate", new BsonDateTime(start.getTime())),
                        Filters.lte("publishedDate", new BsonDateTime(end.getTime()))
                )));
            } catch (ParseException e) {
                System.out.println("problems with parse the date in findBooksByParameters");
                e.printStackTrace();
                return null;
            }
        }
        pipeline.add((sort(descending("publishedDate"))));
        pipeline.add(skip(skip));
        pipeline.add(limit(limit));
        List<Document> result = bookCollection.aggregate(pipeline).into(new ArrayList<>());
        if(result.isEmpty()){
            return null;
        }
        return gson.fromJson(gson.toJson(result),new TypeToken<List<Book>>(){}.getType());
    }
    public List<User> getUserByKeyword(String keyword,boolean admin,int next){
        List<User> results=new ArrayList<>();
        Gson gson= new GsonBuilder().serializeSpecialFloatingPointValues().create();
        Consumer<Document> converter=document -> {
            User user=gson.fromJson(gson.toJson(document), User.class);
            results.add(user);
        };
        Pattern pattern=Pattern.compile("^.*" + keyword + ".*$", Pattern.CASE_INSENSITIVE);
        Bson filter=Aggregates.match(Filters.or(Filters.regex("profileName",pattern),Filters.eq("profileName",keyword)));
        if(admin){
            userCollection.aggregate(Arrays.asList(filter,match(eq("type",1)),skip(next*5),limit(5))).forEach(converter);
        }else{
            userCollection.aggregate(Arrays.asList(filter,skip(next*5),limit(5))).forEach(converter);
        }
        return results;
    }

    //get bad users
    //get versatile users
    //all comments writed days ago
    //mot commented books
    //categories summary by #books
    //browse top categories with top comments
    public List<String> getCategories(){
        List<String> categories=new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                unwind("$categories"),
                group("$categories", first("dummyField", "$categories")),  // Adding a dummy field
                project(Projections.fields(Projections.exclude("dummyField")))  // Project to remove the dummy field
        );
        for (Document result : bookCollection.aggregate(pipeline)) {
            categories.add(result.getString("_id"));
        }
        return categories;
    }
}
