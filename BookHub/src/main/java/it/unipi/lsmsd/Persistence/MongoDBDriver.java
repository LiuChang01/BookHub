package it.unipi.lsmsd.Persistence;

import com.mongodb.client.MongoClient;
import it.unipi.lsmsd.Utils.Utils;

import java.util.Objects;
import java.util.Properties;

public class MongoDBDriver {
    private static MongoDBDriver instance;
    private MongoClient client=null;
    public String mongoUsername;
    public String mongoPassword;
    public String mongoFirstIp;
    public int mongoFirstPort;
    public String mongoSecondIp;
    public int mongoSecondPort;
    public String mongoThirdIp;
    public int mongoThirdPort;
    public String mongodbName;
     private MongoDBDriver(Properties configurationParameters){
         this.mongoUsername = configurationParameters.getProperty("mongoUsername");
         this.mongoPassword = configurationParameters.getProperty("mongoPassword");
         this.mongoFirstIp = configurationParameters.getProperty("mongoFirstIp");
         this.mongoFirstPort = Integer.parseInt(configurationParameters.getProperty("mongoFirstPort"));
         this.mongoSecondIp = configurationParameters.getProperty("mongoSecondIp");
         this.mongoSecondPort = Integer.parseInt(configurationParameters.getProperty("mongoSecondPort"));
         this.mongoThirdIp = configurationParameters.getProperty("mongoThirdIp");
         this.mongoThirdPort = Integer.parseInt(configurationParameters.getProperty("mongoThirdPort"));
         this.mongodbName = configurationParameters.getProperty("mongoDbName");
     }
    public static MongoDBDriver getInstance() {
        if (instance == null)
            instance = new MongoDBDriver(Objects.requireNonNull(Utils.readConfigurationParameters()));
        return instance;
    }
    public void closeConnection() {
        if (client != null)
            System.out.println("Connection closed ...");
        client.close();
    }
    public MongoClient openConnection(){
         if(client!=null){
             return client;
         }

    }
}
