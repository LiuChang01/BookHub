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
        ArrayList<Integer> couts=new ArrayList<>();
        System.out.println(mongoDBManager.getMostActiveUsers("2005-01-01","2023-01-01",0,5,couts));
        System.out.println(couts);
        ArrayList<Double> scores=new ArrayList<>();
        System.out.println(mongoDBManager.getTopBooks(100,Arrays.asList("Fiction"),5,0,scores));
        System.out.println(scores);
        System.out.println(mongoDBManager.searchBooksByParameters("Sword", Arrays.asList("R.A. Salvatore"),"2000-01-01","2020-01-01",Arrays.asList("Fiction"),0,3).size());
        System.out.println( "Hello World!" );
    }
}
