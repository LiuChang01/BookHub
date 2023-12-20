package it.unipi.lsmsd.Persistence;
import it.unipi.lsmsd.Model.User;
import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;
public class Neo4jDBManager {
    Driver driver;
    public Neo4jDBManager(Driver driver){
        this.driver=driver;
    }

    public boolean addUser(User user){
        try(Session session= driver.session()){
            session.writeTransaction(tx->{
                tx.run("CREATE (u:User {name:$name})",
                        parameters("name",user.getprofileName()));
                return null;
            });
            return true;
        }catch (Exception e){
            System.out.println("problems with adding the user to neo4j");
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteUser(User user) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (u:User {name: $name}) DETACH DELETE u",
                        parameters("name", user.getprofileName()));
                return null;
            });
            return true;
        } catch (Exception e) {
            System.out.println("Problems with deleting the user from Neo4j");
            e.printStackTrace();
            return false;
        }

    }

    public int getNumFollowersUser(User user) {
        int numFollowers;
        try (Session session = driver.session()) {
            numFollowers = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {name: $username})<-[r:FOLLOWS]-() " +
                        "RETURN COUNT(r) AS numFollowers", parameters("username", user.getprofileName()));
                return result.next().get("numFollowers").asInt();
            });
        }
        return numFollowers;
    }
    public int getNumFollowingUser(User user) {
        int numFollowing;
        try (Session session = driver.session()) {
            numFollowing = session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {name: $username})-[r:FOLLOWS]->() " +
                        "RETURN COUNT(r) AS numFollowing", parameters("username", user.getprofileName()));
                return result.next().get("numFollowing").asInt();
            });
        }
        return numFollowing;
    }
    public boolean createFollowRelationship(User userA, User userB) {
        if (userA.getprofileName().equals(userB.getprofileName())) {
            System.out.println("Error: User A and User B cannot be the same.");
            return false;
        }

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (a:User {name: $usernameA}), (b:User {name: $usernameB}) " +
                                "MERGE (a)-[:FOLLOWS]->(b)",
                        parameters("usernameA", userA.getprofileName(), "usernameB", userB.getprofileName()));
                return null;
            });
        } catch (Exception e) {
            System.out.println("Problems with creating the FOLLOWS relationship in Neo4j");
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public boolean userLikesAuthor(User user, String authorName) {
        try (Session session = driver.session()) {
            boolean relationshipCreated = session.writeTransaction(tx -> {
                Result result = tx.run("MATCH (u:User {name: $username}), (a:Author {name: $authorName}) " +
                        "RETURN COUNT(*) > 0 AS authorExists", parameters("username", user.getprofileName(), "authorName", authorName));

                if (result.next().get("authorExists").asBoolean()) {
                    tx.run("MERGE (u)-[:LIKES]->(a)", parameters("username", user.getprofileName(), "authorName", authorName));
                    return true;
                } else {
                    return false; // Author does not exist, relationship not created
                }
            });
            return relationshipCreated;
        } catch (Exception e) {
            System.out.println("Problems with creating the LIKES relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }







}
