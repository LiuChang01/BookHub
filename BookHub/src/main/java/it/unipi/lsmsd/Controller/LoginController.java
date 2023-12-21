package it.unipi.lsmsd.Controller;

import it.unipi.lsmsd.Model.Session;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;

public class LoginController {
    private MongoDBManager mongoDBManager;
    public void initalize(){
        mongoDBManager=new MongoDBManager(MongoDBDriver.getInstance().openConnection());
    }
    public boolean checkCredentials(String profileName,String password){
        if(profileName.isEmpty()||password.isEmpty()){
            System.out.println("profileName or password are empty");
            return false;
        }
        User user=mongoDBManager.login(profileName,password);
        if(user==null){
            System.out.println("user doesn't exist, sign up first or password is not correct");
            return false;
        }
        Session.getInstance().setLoggedUser(user);
        return true;
    }
}
