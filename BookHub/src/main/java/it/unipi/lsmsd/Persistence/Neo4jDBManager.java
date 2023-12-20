package it.unipi.lsmsd.Persistence;
import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Review;
import it.unipi.lsmsd.Model.User;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;
public class Neo4jDBManager {
    Driver driver;
    /**
     * Constructs a Neo4jDBManager with the provided Driver.
     *
     * @param driver The Driver used for Neo4j database connectivity.
     */

    public Neo4jDBManager(Driver driver){
        this.driver=driver;
    }
    /**
     * Adds a new user node to Neo4j.
     *
     * @param user The User object representing the user to be added.
     * @return true if the user is added successfully, false otherwise.
     */

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
    /**
     * Deletes a user node from Neo4j based on the provided User object.
     *
     * @param user The User object representing the user to be deleted.
     * @return true if the user is deleted successfully, false otherwise.
     */
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
    /**
     * Retrieves the number of followers for a given user in Neo4j.
     *
     * @param user The User object representing the user for whom to get the number of followers.
     * @return The number of followers for the specified user.
     */
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
    /**
     * Retrieves the number of users a given user is following in Neo4j.
     *
     * @param user The User object representing the user for whom to get the number of following users.
     * @return The number of users the specified user is following.
     */
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
    /**
     * Creates a FOLLOWS relationship between two users in Neo4j.
     *
     * @param userA The User object representing the follower.
     * @param userB The User object representing the user being followed.
     * @return true if the FOLLOWS relationship is created successfully, false otherwise.
     */
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
    /**
     * Creates a LIKES relationship between a user and an author in Neo4j.
     *
     * @param user       The User object representing the user who likes the author.
     * @param authorName The name of the author being liked.
     * @return true if the LIKES relationship is created successfully, false otherwise.
     */
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
    /**
     * Updates the user's preference for a genre in Neo4j.
     * If the genre exists, it updates the user's preference.
     * If the genre does not exist, it prints an error message and does not create the relationship.
     *
     * @param user      The User object representing the user whose preference is being updated.
     * @param newGenre  The name of the new genre the user prefers.
     * @return true if the PREFERS relationship is updated successfully, false otherwise.
     */
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
    /**
     * Creates a REVIEWS relationship between a user and a book in Neo4j, along with the provided review score.
     *
     * @param review The Review object representing the user's review for the book.
     * @return true if the REVIEWS relationship is created successfully, false otherwise.
     */
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
    /**
     * Creates or updates a Book node in Neo4j based on the provided Book object.
     *
     * @param book The Book object representing the book to be created or updated.
     * @return true if the Book node is created or updated successfully, false otherwise.
     */
    private boolean createBook(Book book) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (b:Book {ISBN: $isbn}) SET b.title = $title",
                        parameters("isbn", book.getISBN(), "title", book.getTitle())
                );

                return true;
            });
        } catch (Exception e) {
            System.out.println("Problems with creating or updating the Book node in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Creates or updates relationships between a Book node and multiple Genre nodes based on the book's categories.
     *
     * @param book The Book object representing the book for which categories are linked.
     * @return true if the relationships are created or updated successfully, false otherwise.
     */
    private boolean linkOrCreateCategories(Book book){
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                for (String genre:book.getCategories()){
                    tx.run("MERGE (b:Book {ISBN: $isbn}) " +
                            "MERGE (g:Genre {name: $newGenre}) " +
                            "MERGE (b)-[:BELONGS_TO]->(g)", parameters("isbn", book.getISBN(), "newGenre",genre ));
                }
                return true;
            });
        } catch (Exception e) {
            System.out.println("Problems with creating or updating the Book categories node in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Creates or updates relationships between a Book node and multiple Author nodes based on the book's authors.
     *
     * @param book The Book object representing the book for which authors are linked.
     * @return true if the relationships are created or updated successfully, false otherwise.
     */
    private boolean linkOrCreateAuthors(Book book){
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                for (String author:book.getAuthors()){
                    tx.run("MERGE (b:Book {ISBN: $isbn}) " +
                            "MERGE (a:Author {name: $author}) " +
                            "MERGE (a)-[w:WROTE]->(b)"+
                            "SET w.publicationDate = $publicationDate", parameters("isbn", book.getISBN(), "author",author,"publicationDate",Values.value(book.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())));
                }
                return true;
            });
        } catch (Exception e) {
            System.out.println("Problems with creating or updating the Book authors node in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Adds a book to Neo4j by creating or updating a Book node and establishing relationships with categories and authors.
     *
     * @param book The Book object representing the book to be added.
     * @return true if the book is added successfully, false otherwise.
     */
    public boolean addBook(Book book){
        return createBook(book)&&linkOrCreateCategories(book)&&linkOrCreateAuthors(book);
    }
    /**
     * Detaches the LIKES relationship between a user and an author in Neo4j.
     *
     * @param user       The User object representing the user who dislikes the author.
     * @param authorName The name of the author being disliked.
     * @return true if the LIKES relationship is detached successfully, false otherwise.
     */
    public boolean userDisLikesAuthor(User user, String authorName) {
        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                Result result = tx.run("MATCH (a:Author {name: $authorName}) RETURN COUNT(*) AS authorCount",
                        parameters("authorName", authorName));

                int authorCount = result.next().get("authorCount").asInt();

                if (authorCount > 0) {
                    // Detach the existing LIKES relationship (if any)
                    tx.run("MATCH (u:User {name: $username})-[r:LIKES]->(a:Author {name: $authorName}) DELETE r",
                            parameters("username", user.getprofileName(), "authorName", authorName));
                    return true;
                } else {
                    return false; // Author does not exist, relationship not created
                }
            });
        } catch (Exception e) {
            System.out.println("Problems with detaching the LIKES relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Deletes the FOLLOWS relationship between two users in Neo4j.
     *
     * @param userA The User object representing the follower.
     * @param userB The User object representing the user being followed.
     * @return true if the FOLLOWS relationship is deleted successfully, false otherwise.
     */
    public boolean deleteFollowRelationship(User userA, User userB) {
        if (userA.getprofileName().equals(userB.getprofileName())) {
            System.out.println("Error: User A and User B cannot be the same.");
            return false;
        }

        try (Session session = driver.session()) {
            return session.writeTransaction(tx -> {
                // Delete the FOLLOWS relationship between userA and userB
                tx.run("MATCH (a:User {name: $usernameA})-[r:FOLLOWS]->(b:User {name: $usernameB}) DELETE r",
                        parameters("usernameA", userA.getprofileName(), "usernameB", userB.getprofileName()));
                return true;
            });
        } catch (Exception e) {
            System.out.println("Problems with deleting the FOLLOWS relationship in Neo4j");
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Retrieves a list of user names with the most followers based on the specified limit.
     *
     * @param limit The maximum number of users to retrieve.
     * @return A list of user names with the most followers.
     */
    public List<String> getUsersWithMostFollowers(int limit) {
        List<String> resultUserNames = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(
                        "MATCH (follower:User)-[:FOLLOWS]->(u:User) " +
                                "WITH u, COUNT(follower) AS numFollowers " +
                                "ORDER BY numFollowers DESC LIMIT $limit " +
                                "RETURN u.name AS mostFollowed",
                        parameters("limit", limit)
                );

                while (result.hasNext()) {
                    Record record = result.next();
                    String userName = record.get("mostFollowed").asString();
                    resultUserNames.add(userName);
                }

                return null;
            });
        }

        return resultUserNames;
    }

    /**
     * Generates book recommendations based on authors liked by a user.
     *
     * @param user  The user for whom the recommendations are generated.
     * @param limit The maximum number of recommendations to retrieve.
     * @return A list of ISBNs representing recommended books.
     */
    public List<String> recommendationBasedOnAuthorsLiked(User user,int limit) {
        List<String> resultBooks = new ArrayList<>();

        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(
                        "MATCH (u:User {name: $userName})-[:LIKES]->(a:Author)-[w:WROTE]->(b:Book) " +
                                "WITH b, a, w " +
                                "ORDER BY w.publicationDate DESC " +
                                "RETURN b.ISBN AS ISBN " +
                                "LIMIT $limit",
                        parameters("userName", user.getprofileName(),"limit",limit)
                );

                while (result.hasNext()) {
                    Record record = result.next();
                    String isbn = record.get("ISBN").asString();

                    resultBooks.add(isbn);
                }

                return null;
            });
        }

        return resultBooks;
    }











}
