package it.unipi.lsmsd.Controller;

import com.mongodb.client.ClientSession;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.LastBookReviews;
import it.unipi.lsmsd.Model.Session;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Persistence.Neo4jDBDriver;
import it.unipi.lsmsd.Persistence.Neo4jDBManager;

import java.util.ArrayList;
import java.util.List;

public class UserController {
    private MongoDBManager mongoDBManager;
    private Neo4jDBManager neo4jDBManager;
    private List<String>categories;
    public void initialize(){
        mongoDBManager= new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        neo4jDBManager= new Neo4jDBManager(Neo4jDBDriver.getInstance().openConnection());
        categories=mongoDBManager.getCategories();
    }
    public void showProfile(User user){
        System.out.println("profileName->"+user.getprofileName());
        System.out.println("password->"+user.getPassword());
        System.out.println("type->"+(user.getType()==1?"admin":"normal User"));
        System.out.println("follows-> "+neo4jDBManager.getNumFollowingUser(user));
        System.out.println("following->"+neo4jDBManager.getNumFollowersUser(user));
        List<LastBookReviews> lastBookReviews=(user.getLast_reviews().isEmpty()?mongoDBManager.getUserByProfileName(user.getprofileName()).getLast_reviews():user
                .getLast_reviews());
        System.out.println("last_reviews:");
        if(lastBookReviews==null){
            System.out.println("no last reviews");
            return;
        }
        for (LastBookReviews lastBookReviews1:lastBookReviews){
            System.out.println("\t"+lastBookReviews1);
        }
    }
    public void showFollowings(User user){
        System.out.println("follows-> "+neo4jDBManager.getFollowingUsers(user));
        System.out.println("following->"+neo4jDBManager.getFollowers(user));
    }
    public boolean changePassword(User user,String password){
        if(password.isEmpty()||password.equals(user.getPassword())){
            return false;
        }
        user.setPassword(password);
        boolean ret=mongoDBManager.updateUser(user);
        Session.getInstance().setLoggedUser(user);
        return ret;
    }
    public boolean followUser(User user){
        return neo4jDBManager.createFollowRelationship(Session.getInstance().getLoggedUser(), user);
    }
    public boolean unfollowUser(User user){
        return neo4jDBManager.deleteFollowRelationship(Session.getInstance().getLoggedUser(), user);
    }
    public boolean followAuthor(String name){
        return neo4jDBManager.userLikesAuthor(Session.getInstance().getLoggedUser(), name);
    }
    public boolean unfollowAuthor(String name){
        return neo4jDBManager.userDisLikesAuthor(Session.getInstance().getLoggedUser(), name);
    }
    public boolean setPreferredGenre(String genre){
        return neo4jDBManager.userPrefersGenre(Session.getInstance().getLoggedUser(), genre);
    }
    public List<String> getMostRatedAuthors(int skip, int limit, int numReviews, ArrayList<Double> score){
        return mongoDBManager.getMostRatedAuthors(skip,limit,numReviews,score);
    }
    public List<String> getMostActiveUsers(String startDate,String endDate,int skip,int limit,ArrayList<Integer> counts){
        return mongoDBManager.getMostActiveUsers(startDate,endDate,skip,limit,counts);
    }
    public List<String> getTopCategoriesOfNumOfBookPublished(int skip,int limit){
        return mongoDBManager.getTopCategoriesOfNumOfBookPublished(skip,limit);
    }
    public List<User> getMostVersatileUsers(int skip, int limit){
        if(skip<0||limit<=0){
            System.out.println("error number");
            return null;
        }
        return mongoDBManager.getMostVersatileUsers(skip,limit);
    }
    public List<Book> getTopBooks(int numReview, List<String> categories, int limit, int skip, ArrayList<Double>scores){
        for (String cat:categories){
            if(!categories.contains(cat)){
                System.out.println("some categories doesnt exists");
                return null;
            }
        }
        if(skip<0||limit<=0){
            System.out.println("error number");
            return null;
        }
        return mongoDBManager.getTopBooks(numReview,categories,limit,skip,scores);
    }
    public List<User>getBadUsers(int skip,int limit){
        if(skip<0||limit<=0){
            System.out.println("error number");
            return null;
        }
        return mongoDBManager.getBadUsers(skip,limit);
    }

    public List<User> getUserByKeyword(String keyword,boolean admin,int next){
        return mongoDBManager.getUserByKeyword(keyword,admin,next);
    }
    public User getUserByProfileName(String profileName){
        return mongoDBManager.getUserByProfileName(profileName);
    }

    public List<Book> searchBooksByParameters(String title, List<String> authors, String startDate, String endDate, List<String> categories, int skip, int limit) {
        if(skip<0||limit<=0){
            System.out.println("error number");
            return null;
        }
        for (String cat:categories){
            if(!categories.contains(cat)){
                System.out.println("some categories doesnt exists");
                return null;
            }
        }
        return mongoDBManager.searchBooksByParameters(title,authors,startDate,endDate,categories,skip,limit);
    }
    public boolean addBook(Book book){
        try {
            ClientSession session=MongoDBDriver.getInstance().openConnection().startSession();
            session.startTransaction();
            if(mongoDBManager.addBook(book,session)){
                if(neo4jDBManager.addBook(book)){
                    session.commitTransaction();
                    return true;
                }else {
                    session.abortTransaction();
                    return false;
                }
            }else {
                session.abortTransaction();
                return false;
            }
        }catch (Exception e){
            System.out.println("error in add book controller");
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteUser(User user){
        try {
            ClientSession session=MongoDBDriver.getInstance().openConnection().startSession();
            session.startTransaction();
            if(mongoDBManager.deleteUser(user,session)){
                if(neo4jDBManager.deleteUser(user)){
                    session.commitTransaction();
                    return true;
                }else {
                    session.abortTransaction();
                    return false;
                }
            }else {
                session.abortTransaction();
                return false;
            }
        }catch (Exception e){
            System.out.println("error in delete user controller");
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getUsersWithMostFollowers(int limit) {
        if(limit<=0){
            System.out.println("limit must be positive");
            return null;
        }
        return neo4jDBManager.getUsersWithMostFollowers(limit);
    }
    public List<String> recommendationBasedOnAuthorsLiked(int limit) {
        if(limit<=0){
            System.out.println("limit must be positive");
            return null;
        }
        return neo4jDBManager.recommendationBasedOnAuthorsLiked(Session.getInstance().getLoggedUser(), limit);
    }
    public List<User> recommendUserWithMostFollowersOfFollowings(int limit) {
        if(limit<=0){
            System.out.println("limit must be positive");
            return null;
        }
        return neo4jDBManager.recommendUserWithMostFollowersOfFollowings(Session.getInstance().getLoggedUser(), limit);
    }
    public List<String> recommendBooksBasedOnFriendsCommentsAndPreferredGenre(int limit) {
        if(limit<=0){
            System.out.println("limit must be positive");
            return null;
        }
        return neo4jDBManager.recommendBooksBasedOnFriendsCommentsAndPreferredGenre(Session.getInstance().getLoggedUser(), limit);
    }
    public List<String> recommendBooksBasedOnFriendsComments(int limit) {
        if(limit<=0){
            System.out.println("limit must be positive");
            return null;
        }
        return neo4jDBManager.recommendBooksBasedOnFriendsComments(Session.getInstance().getLoggedUser(), limit);
    }











    }
