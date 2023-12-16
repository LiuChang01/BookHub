package it.unipi.lsmsd.Persistence;

import com.mongodb.client.MongoClient;

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

}
