package it.unipi.lsmsd;

import it.unipi.lsmsd.Model.LastBookReviews;
import it.unipi.lsmsd.Model.User;
import it.unipi.lsmsd.Persistence.MongoDBDriver;
import it.unipi.lsmsd.Persistence.MongoDBManager;
import it.unipi.lsmsd.Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println(mongoDBManager.getCategories().size());
        System.out.println(mongoDBManager.searchBooksByParameters("The Two Swords (Forgotten Realms Novel: Hunter's Blades Trilogy)", Arrays.asList("R.A. Salvatore"),"2000-01-01","2020-01-01",Arrays.asList("Fiction"),0,3));
        System.out.println( "Hello World!" );
    }
}
