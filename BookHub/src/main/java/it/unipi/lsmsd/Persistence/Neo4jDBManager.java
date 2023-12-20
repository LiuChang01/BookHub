package it.unipi.lsmsd.Persistence;
import it.unipi.lsmsd.Model.Review;
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

            return session.writeTransaction(tx -> {
                // Check if userB exists in the graph
                Result userBResult = tx.run("MATCH (b:User {name: $usernameB}) RETURN COUNT(b) AS userBCount",
                        parameters("usernameB", userB.getprofileName()));

                int userBCount = userBResult.next().get("userBCount").asInt();

                if (userBCount > 0) {
                    // UserB exists, create FOLLOWS relationship
                    tx.run("MATCH (a:User {name: $usernameA}), (b:User {name: $usernameB}) " +
                                    "MERGE (a)-[:FOLLOWS]->(b)",
                            parameters("usernameA", userA.getprofileName(), "usernameB", userB.getprofileName()));
                    return true;
                } else {
                    // UserB does not exist, do not create relationship
                    System.out.println("User B does not exist in the graph. FOLLOWS relationship not created.");
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("Problems with creating the FOLLOWS relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }

    public boolean userLikesAuthor(User user, String authorName) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                Result result = tx.run("MATCH (a:Author {name: $authorName}) RETURN COUNT(*) AS authorCount",
                        parameters("authorName", authorName));

                int authorCount = result.next().get("authorCount").asInt();

                if (authorCount > 0) {
                    tx.run("MERGE (u:User {name: $username}) " +
                                    "MERGE (a:Author {name: $authorName}) " +
                                    "MERGE (u)-[:LIKES]->(a)",
                            parameters("username", user.getprofileName(), "authorName", authorName));
                    return true;
                } else {
                    return false; // Author does not exist, relationship not created
                }
            });
        } catch (Exception e) {
            System.out.println("Problems with creating the LIKES relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    public boolean userPrefersGenre(User user, String newGenre) {
        try (Session session = driver.session()) {

            return session.writeTransaction(tx -> {
                // Check if the genre exists
                Result genreResult = tx.run("MATCH (g:Genre {name: $newGenre}) RETURN COUNT(g) AS genreCount",
                        parameters("newGenre", newGenre));

                int genreCount = genreResult.next().get("genreCount").asInt();

                if (genreCount > 0) {
                    // Check if the user has an existing preference
                    Result existingResult = tx.run("MATCH (u:User {name: $username})-[:PREFERS]->(oldGenre:Genre) " +
                            "RETURN oldGenre.name AS oldGenreName", parameters("username", user.getprofileName()));

                    if (existingResult.hasNext()) {
                        // Detach the user from the previous preference
                        String oldGenreName = existingResult.next().get("oldGenreName").asString();
                        tx.run("MATCH (u:User {name: $username})-[r:PREFERS]->(oldGenre:Genre {name: $oldGenreName}) " +
                                "DELETE r", parameters("username", user.getprofileName(), "oldGenreName", oldGenreName));
                    }

                    // Create a new relationship with the new genre
                    tx.run("MERGE (u:User {name: $username}) " +
                            "MERGE (g:Genre {name: $newGenre}) " +
                            "MERGE (u)-[:PREFERS]->(g)", parameters("username", user.getprofileName(), "newGenre", newGenre));

                    return true;
                } else {
                    // Genre does not exist, do not create relationship
                    System.out.println("Genre " + newGenre + " does not exist. PREFERS relationship not created.");
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("Problems with updating the PREFERS relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }

    public boolean createUserBookReview(Review review) {
        try (Session session = driver.session()) {

            return session.writeTransaction(tx -> {
                // Match the user and book using profileName and ISBN + title
                tx.run(
                        "MATCH (u:User {name: $profileName}), (b:Book {ISBN: $isbn, title: $title}) " +
                                "MERGE (u)-[r:REVIEWS]->(b) " +
                                "SET r.score = $score",
                        parameters(
                                "profileName", review.getProfileName(),
                                "isbn", review.getISBN(),
                                "title", review.getTitle(),
                                "score", review.getScore()
                        )
                );

                return true;
            });
        } catch (Exception e) {
            System.out.println("Problems with creating the REVIEWS relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }

    










}
