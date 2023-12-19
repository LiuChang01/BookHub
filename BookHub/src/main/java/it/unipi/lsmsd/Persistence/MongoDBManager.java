package it.unipi.lsmsd.Persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Model.User;
import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private static final String reviewDeleted="Review deleted by admin because it doesn't follow the politeness";

    public MongoDBManager(MongoClient client){
        this.db= client.getDatabase("bookHubMongo");
        userCollection=db.getCollection("users");
        bookCollection=db.getCollection("book");
        reviewCollection=db.getCollection("reviews");
    }
    public User login(String profileName, String password){
        Document result= userCollection.find(Filters.and(
                eq("profileName",profileName),
                eq("password",password))).first();
        Gson gson=new Gson();
        return gson.fromJson(gson.toJson(result), User.class);
    }
    private User checkExistence(String profilename){
        Document result= userCollection.find(Filters.and(
                eq("profileName",profilename))).first();
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
        try{
            if(checkExistence(user.getprofileName())==null){
                System.out.println("user doesn't exist");
                return false;
            }
            Bson find=eq("profileName",user.getprofileName());
            userCollection.deleteOne(find);
            Bson findB=eq("last_users_review.profileName",user.getprofileName());
            Bson updateB= Updates.pull("last_users_review",eq("profileName",user.getprofileName()));
            bookCollection.updateMany(findB,updateB);
            reviewCollection.deleteMany(find);
            return true;
        }catch (Exception e){
            System.out.println("problems with deleting the user");
            e.printStackTrace();
            return false;
        }
        //dipende
    }
    public boolean addBook(Book book){
        Document result=bookCollection.find(eq("ISBN",book.getISBN())).first();
        if(result!=null){
            System.out.println("book already exists for the ISBN");
            return false;
        }
        if(book.getISBN().isEmpty()
                ||book.getISBN()==null
                ||book.getAuthors().isEmpty()
                ||book.getAuthors()==null
                ||book.getDescription().isEmpty()
                ||book.getDescription()==null
                ||book.getCategories().isEmpty()
                ||book.getCategories()==null
                ||book.getLast_users_review()==null
                ||book.getPublishedDate()==null
                ||book.getTitle().isEmpty()
                ||book.getTitle()==null){
            System.out.println("Give all parameters to the book");
            return false;
        }
        try{
            SimpleDateFormat dateFormat= new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            Document document=new Document("ISBN",book.getISBN())
                    .append("Title",book.getTitle())
                    .append("description",book.getDescription())
                    .append("authors",book.getAuthors())
                    .append("categories",book.getCategories())
                    .append("publishedDate",dateFormat.format(book.getPublishedDate()))
                    .append("last_users_review",book.getLast_users_review());
            bookCollection.insertOne(document);
            return true;
        }catch (Exception e){
            System.out.println("problems with add book");
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateUser(User user){
        try{
            if (user.getPassword().isEmpty()||checkExistence(user.getprofileName())==null) {
                System.out.println("password empty or user doesn't exist");
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
    //da testare
    public boolean addReview(Book book, Review review){
        try {
            Document result= reviewCollection.find(Filters.and(
                    eq("profileName",review.getProfileName()),
                    eq("ISBN",review.getISBN()))).first();
            if(result!=null){
                System.out.println("Review for that user already exists");
                return false;
            }
            SimpleDateFormat dateFormat= new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            Document document_last_users_review=new Document("profileName",review.getProfileName())
                    .append("time", dateFormat.format(review.getTime()))
                    .append("score",review.getScore())
                    .append("review",review.getReview());
            Document document_last_review=new Document("ISBN",review.getISBN())
                    .append("Title",review.getTitle())
                    .append("score",review.getScore())
                    .append("time", dateFormat.format(review.getTime()))
                    .append("review",review.getReview());
            Document document_review=new Document("ISBN",review.getISBN())
                    .append("Title",review.getTitle())
                    .append("profileName",review.getProfileName())
                    .append("score",review.getScore())
                    .append("time", dateFormat.format(review.getTime()))
                    .append("review",review.getReview())
                    .append("categories",review.getCategories())
                    .append("authors",review.getAuthors());
            reviewCollection.insertOne(document_review);
            Document filter_book=new Document("ISBN",book.getISBN());
            Document update_last_users_review=new Document("$push",
                    new Document("last_users_review",
                            new Document("$each",document_last_users_review)
                                    .append("$position",0)
                                    .append("$slice",-5)));
            Document filter_user=new Document("profileName",review.getProfileName());
            Document update_last_review=new Document("$push",
                    new Document("last_reviews",
                            new Document("$each",document_last_review)
                                    .append("$position",0)
                                    .append("$slice",-5)));
            userCollection.updateOne(filter_user,update_last_review);
            bookCollection.updateOne(filter_book,update_last_users_review);
            return true;
        }catch (Exception e ){
            System.out.println("problems with insert of a comment");
            e.printStackTrace();
            return false;
        }
    }
    //update reviews non esiste

    //delete comments policy?
    public void deleteReview(Book book,Review review){
        Document findB=new Document("ISBN",book.getISBN())
                .append("last_users_review",
                        new Document("$elemMatch",
                                new Document("profileName",review.getProfileName())
                                        .append("review",review.getReview())));
        Document findR=new Document("ISBN",book.getISBN())
                .append("profileName",review.getProfileName());
        Document findU=new Document("profileName",review.getProfileName())
                .append("last_reviews",
                        new Document("$elemMatch",
                                new Document("ISBN",book.getISBN())
                                        .append("review",review.getReview())));
        Document updateR=new Document("$set",
                new Document("review",reviewDeleted));
        UpdateResult updateResult=reviewCollection.updateOne(findR,updateR);
        if(updateResult.getModifiedCount()==0){
            System.out.println("no updated in review because not found review");
            return;
        }
        Document updateUReview=new Document("$set",
                new Document("last_reviews.$.review",reviewDeleted));
        updateResult=userCollection.updateOne(findU,updateUReview);
        if(updateResult.getModifiedCount()==0){
            System.out.println("user not modified maybe because in user there was no review in his last 5");
        }
        Document updateBReview=new Document("$set",
                new Document("last_users_review.$.review",reviewDeleted));
        updateResult=bookCollection.updateOne(findB,updateBReview);
        if(updateResult.getModifiedCount()==0){
            System.out.println("book has no that review maybe is not in the last 5 of that book");
        }
    }


    public Book getBookByISBN(String ISBN){
        try {
            if(ISBN.isEmpty()){
                System.out.println("ISBN empty");
                return null;
            }
            Book b;
            Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            Document document= bookCollection.find(and(
                    eq("ISBN",ISBN))).first();
            b=gson.fromJson(gson.toJson(document), Book.class);
            return b;
        }catch (JsonSyntaxException e){
            System.out.println("problems with conversion in the getBookByISBN");
            e.printStackTrace();
            return null;
        }
    }
    public List<Book> searchBooksByParameters(String title, List<String> authors, String startDate, String endDate, List<String> categories, int skip, int limit) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        List<Bson>pipeline=new ArrayList<>();
        if(title!=null&&!title.isEmpty()){
            Pattern pattern=Pattern.compile("^.*" + title + ".*$");
            pipeline.add(Aggregates.match(Filters.or
                    (Filters.regex("Title",pattern),
                            Filters.eq("Title",title))));
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
                        Filters.gte("publishedDate",
                                new BsonDateTime(start.getTime())),
                        Filters.lte("publishedDate",
                                new BsonDateTime(end.getTime()))
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
        Bson filter=Aggregates.match(
                Filters.or(
                        Filters.regex("profileName",pattern),
                        Filters.eq("profileName",keyword)));
        if(admin){
            userCollection.aggregate(Arrays.asList(filter,match(eq("type",1)),skip(next*5),limit(5))).forEach(converter);
        }else{
            userCollection.aggregate(Arrays.asList(filter,skip(next*5),limit(5))).forEach(converter);
        }
        return results;
    }

    //get bad users
    public List<User>getBadUsers(int skip,int limit){
        List<User> results=new ArrayList<>();
        ArrayList<Document>pipeline=new ArrayList<>();
        pipeline.add(new Document("$match",
                new Document("review",reviewDeleted)));
        pipeline.add(new Document("$group",
                new Document("_id","profileName").append("count",
                        new Document("$sum",1))));
        pipeline.add(new Document("$sort",
                new Document("count",-1)));
        pipeline.add(new Document("$project",
                new Document("profileName","$_id").append("count",1).append("_id",0)));
        pipeline.add(new Document("$skip",skip));
        pipeline.add(new Document("$limit",limit));
        Iterable<Document>result=reviewCollection.aggregate(pipeline);
        for(Document document:result){
            User user=getUserByProfileName(document.getString("profileName"));
            if(user!=null){
                results.add(user);
            }
        }
        return results;
    }
    //get versatile users
    public List<Book> getTopBooks(int numReview,List<String> categories,int limit,int skip,ArrayList<Double>scores){
        List<Book> results=new ArrayList<>();
        List<Document>pipeline=new ArrayList<>();
        if(categories!=null&&categories.isEmpty()){
            pipeline.add(new Document("$match",
                    new Document("categories",
                            new Document("$in",categories))));
        }
        pipeline.addAll(Arrays.asList(
                new Document("$group",
                        new Document("_id","$ISBN")
                                .append("averageScore",
                                        new Document("$avg","$score"))
                                .append("totalReviews",
                                        new Document("$sum",1))),
                new Document("$match",
                        new Document("totalReviews",
                                new Document("$gte",numReview))),
                new Document("$sort",
                        new Document("averageScore",-1)),
                new Document("$project",
                        new Document("ISBN","$_id")
                                .append("averageScore",1)
                                .append("totalReviews",1)
                                .append("_id",0)),
                new Document("$skip",skip),
                new Document("$limit",limit)
        ));
        AggregateIterable<Document> documentAggregateIterable  =reviewCollection.aggregate(pipeline);
        for(Document document:documentAggregateIterable){
            results.add(getBookByISBN(document.getString("ISBN")));
            scores.add(document.getDouble("averageScore"));
        }
        return results;
    }
    public List<User> getMostVersatileUsers(int skip, int limit){
        List<Document> pipeline=Arrays.asList(
                new Document("$group",
                        new Document("_id","$profileName")
                                .append("uniqueCategories",
                                        new Document("$addToSet","$categories"))),
                new Document("$project",
                        new Document("profileName","$_id")
                                .append("uniqueCategories","$uniqueCategories")
                                .append("numUniqueCategories",
                                        new Document("$size","$uniqueCategories"))),
                new Document("$sort",
                        new Document("numUniqueCategories",-1)),
                new Document("$skip",skip),
                new Document("$limit",limit)
        );
        AggregateIterable<Document> documentAggregateIterable=reviewCollection.aggregate(pipeline);
        ArrayList<User> result=new ArrayList<>();
        for(Document document:documentAggregateIterable){
            result.add(getUserByProfileName(document.getString("profileName")));
        }
        return result;
    }
    public List<String> getTopCategoriesOfNumOfBookPublished(int skip,int limit){
        List<Document> pipeline=Arrays.asList(
                new Document("$unwind","$categories"),
                new Document("$group",
                        new Document("_id","$categories")
                                .append("count",
                                        new Document("$sum",1))),
                new Document("$sort",
                        new Document("count",-1)),
                new Document("$skip",skip),
                new Document("$limit",limit)
        );
        AggregateIterable<Document> documentAggregateIterable= bookCollection.aggregate(pipeline);
        List<String> results=new ArrayList<>();
        for(Document document:documentAggregateIterable){
            results.add(document.getString("_id"));
        }
        return results;

    }
    public List<String> getMostActiveUsers(String startDate,String endDate,int skip,int limit,ArrayList<Integer> counts){

        List<Document> pipeline;

        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);
                pipeline=Arrays.asList(
                        new Document("$match",
                                new Document("time",
                                        new Document("$gte",start)
                                                .append("$lte",end))),
                        new Document("$group",
                                new Document("_id","$profileName")
                                        .append("count",
                                                new Document("$sum",1))),
                        new Document("$sort",
                                new Document("count",-1)),
                        new Document("$project",
                                new Document("_id",0)
                                        .append("profileName","$_id")
                                        .append("reviewCount","$count")),
                        new Document("$skip",skip),
                        new Document("$limit",limit)
                );
            } catch (ParseException e) {
                System.out.println("problems with parse the date in getMostActiveUsers");
                e.printStackTrace();
                return null;
            }
            AggregateIterable<Document> results=reviewCollection.aggregate(pipeline);
            List<String> topReviewersName=new ArrayList<>();
            for (Document document:results){
                String profileName=document.getString("profileName");
                topReviewersName.add(profileName);
                counts.add(document.getInteger("reviewCount"));
            }
            return topReviewersName;
        }else{
            System.out.println("enter a good start and end date");
            return null;
        }
    }
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
        Collections.sort(categories);
        return categories;
    }
}
