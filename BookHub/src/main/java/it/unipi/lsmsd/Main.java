package it.unipi.lsmsd;

import it.unipi.lsmsd.Model.LastBookReviews;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        System.out.println(Utils.readConfigurationParameters());
        MongoDBManager mongoDBManager=new MongoDBManager(MongoDBDriver.getInstance().openConnection());
        System.out.println(mongoDBManager.getUserByProfileName("LiuChang"));
        System.out.println( "Hello World!" );
    }
}
