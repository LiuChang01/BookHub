package it.unipi.lsmsd.Controller;

import it.unipi.lsmsd.Model.LastBookReviews;
import it.unipi.lsmsd.Model.Session;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Persistence.Neo4jDBDriver;
import it.unipi.lsmsd.Persistence.Neo4jDBManager;

import java.util.List;

public class UserController {
    private MongoDBManager mongoDBManager;
    private Neo4jDBManager neo4jDBManager;
    public void initialize(){
        mongoDBManager= new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        neo4jDBManager= new Neo4jDBManager(Neo4jDBDriver.getInstance().openConnection());
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
}
