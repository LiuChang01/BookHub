package it.unipi.lsmsd.Controller;

import it.unipi.lsmsd.Model.Book;
import it.unipi.lsmsd.Model.Session;

import java.text.SimpleDateFormat;
import java.util.*;

public class CLIController {
    private static final LoginController loginController = new LoginController();
    private static final  RegisterController registerController = new RegisterController();
    private static final UserController userController = new UserController();
    private static final BookController bookController = new BookController();
    private static final Scanner scanner=new Scanner(System.in);
    private static void init(){
        loginController.initalize();
        registerController.initialize();
        userController.initialize();
        bookController.initialize();
    }
    public static void startApplication(){
        init();
        while (true){
            if(Session.getInstance().getLoggedUser()==null){
                menuUnreg();
                continue;
            }
            switch (Session.getInstance().getLoggedUser().getType()){
                case 0:{
                    menuReg();
                    break;
                }
                case 1:{
                    menuAdmin();
                    break;
                }
            }
        }
    }
    private static void menuUnreg(){
        while (true){
            System.out.println("1-Login");
            System.out.println("2-Sign Up");
            System.out.println("3-Find Book");
            System.out.println("4-Exit");
            System.out.print("Choice->");
            switch (Integer.parseInt(scanner.nextLine())){
                case 1:{
                    System.out.print("username->");
                    String name=scanner.nextLine();
                    System.out.print("password->");
                    String pass=scanner.nextLine();
                    if(loginController.checkCredentials(name,pass)){
                        return;
                    }
                    break;
                }
                case 2:{
                    System.out.print("username->");
                    String name=scanner.nextLine();
                    System.out.print("password->");
                    String pass=scanner.nextLine();
                    if(registerController.signUp(name,pass)){
                        System.out.println("Now you are registered");
                    }else{
                        System.out.println("Sign up failed");
                    }
                    break;
                }
                case 3:{
                    System.out.print("Enter book title to find:");
                    String bookTitleToFind = scanner.nextLine();
                    System.out.print("From date:");
                    String startDate = scanner.nextLine();
                    System.out.print("To Date:");
                    String endDate = scanner.nextLine();
                    System.out.print("List of Authors(Separated by ,):");
                    List<String> authors = Arrays.asList(scanner.nextLine().split(","));
                    System.out.print("List of categories(Separated by ,):");
                    List<String> categories = Arrays.asList(scanner.nextLine().split(","));
                    List<Book> books=userController.searchBooksByParameters(bookTitleToFind, authors, startDate, endDate, categories, 0, 5) ;
                    for(Book book:books){
                        System.out.println(book);
                    }
                    break;
                }

                case 4:
                    System.out.println("bye");
                    System.exit(0);
            }
        }
    }
    private static void menuReg(){
        while (true){
            System.out.println("1-search book");
            System.out.println("2-search user");
            System.out.println("3-Show profile");
            System.out.print("Choice->");
            switch (Integer.parseInt(scanner.nextLine())){
                case 3:{
                    userController.showProfile(Session.getInstance().getLoggedUser());
                    break;
                }
            }

        }
    }

    private static void menuAdmin(){
        while (true){
            System.out.println("1-Show profile");
            System.out.println("2-Find User");
            System.out.println("3-Change Password");
            System.out.println("4-Find Book");
            System.out.println("5-Delete Review");
            System.out.println("6-Add Book");
            System.out.println("7-Ban User");
            System.out.println("8-Statistics on User");
            System.out.println("9-Statistics on Books");
            System.out.println("10-LogOut");
            System.out.print("Choice->");
            switch (Integer.parseInt(scanner.nextLine())){
                case 1: {
                    userController.showProfile(Session.getInstance().getLoggedUser());
                    break;
                }
                case 2: {
                    System.out.print("Enter username to find:");
                    String usernameToFind = scanner.nextLine();
                    userController.getUserByProfileName(usernameToFind);
                    break;
                }
                case 3: {
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    boolean passwordChanged = userController.changePassword(Session.getInstance().getLoggedUser(), newPassword);
                    if (passwordChanged) {
                        System.out.println("Password changed successfully.");
                    } else {
                        System.out.println("Password change failed.");
                    }
                    break;
                }
                case 4: {
                    System.out.print("Enter book title to find:");
                    String bookTitleToFind = scanner.nextLine();
                    System.out.print("From date:");
                    String startDate = scanner.nextLine();
                    System.out.print("To Date:");
                    String endDate = scanner.nextLine();
                    System.out.print("List of Authors(Separated by ,):");
                    List<String> authors = Arrays.asList(scanner.nextLine().split(","));
                    System.out.print("List of categories(Separated by ,):");
                    List<String> categories = Arrays.asList(scanner.nextLine().split(","));
                    List<Book> books=userController.searchBooksByParameters(bookTitleToFind, authors, startDate, endDate, categories, 0, 5) ;
                    System.out.println(books);
                    //chiedi se ci vuole fare qualcosa
                    break;
                }
                case 5:
                    System.out.println("Enter review ID to delete:");
                    int reviewIdToDelete = Integer.parseInt(scanner.nextLine());
                    // Implement delete review functionality using userController.deleteReview(reviewIdToDelete)
                    //fatto dopo il cerca?
                    break;
                case 6: {
                    System.out.print("ISBN:");
                    String isbn = scanner.nextLine();
                    System.out.print("Title:");
                    String title = scanner.nextLine();
                    System.out.print("Authors(Separated by ,):");
                    List<String> authors = Arrays.asList(scanner.nextLine().split(","));
                    System.out.print("Categories(Separated by ,):");
                    List<String> categories = Arrays.asList(scanner.nextLine().split(","));
                    System.out.print("Description:");
                    String description = scanner.nextLine();
                    System.out.print("PublishedDate(in format yyyy-mm-dd ):");
                    Date date;
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        date = dateFormat.parse(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("error in parsing the date");
                        e.printStackTrace();
                        return;
                    }
                    Book book = new Book(isbn, title, description, authors, categories, date, new ArrayList<>());
                    userController.addBook(book);
                    break;
                }
                case 7:
                    System.out.println("Enter username to ban:");
                    String usernameToBan = scanner.nextLine();
                    // va forse messa con quello del cerca utenti
                    break;
                case 8: {
                    System.out.println("Top 5 versatile Users");
                    System.out.println(userController.getMostVersatileUsers(0, 5));
                    System.out.println("Top 5 Most Followed Users -> Possible Influencers");
                    System.out.println(userController.getUsersWithMostFollowers(5));
                    System.out.print("To get Most Rated Authors please enter a Num Min of Review that He/She has->");
                    ArrayList<Double> scores = new ArrayList<>();
                    System.out.println(userController.getMostRatedAuthors(0, 5, Integer.parseInt(scanner.nextLine()), scores));
                    System.out.println(scores);
                    System.out.println("Top 5 bad Users");
                    System.out.println(userController.getBadUsers(0, 5));
                    System.out.println("To get Most Active User Please Specify start and end date in format yyyy-mm-dd ");
                    System.out.print("Start:");
                    String start = scanner.nextLine();
                    System.out.print("End:");
                    String end = scanner.nextLine();
                    ArrayList<Integer> counts = new ArrayList<>();
                    System.out.println(userController.getMostActiveUsers(start, end, 0, 5, counts));
                    System.out.println(counts);
                    break;
                }
                case 9: {
                    System.out.println("Top 5 categories by num of book published");
                    System.out.println(userController.getTopCategoriesOfNumOfBookPublished(0, 5));
                    System.out.println("To get Top 5 books please enter a Min num of reviews and a list of categories separated by ,");
                    System.out.print("Categories:");
                    List<String> categories = Arrays.asList(scanner.nextLine().split(","));
                    System.out.print("Min Num:");
                    ArrayList<Double> scores = new ArrayList<>();
                    System.out.println(userController.getTopBooks(Integer.parseInt(scanner.nextLine()), categories, 5, 0, scores));
                    System.out.println(scores);
                    break;
                }
                case 10:
                    // Logout the user
                    Session.resetInstance();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }
}
