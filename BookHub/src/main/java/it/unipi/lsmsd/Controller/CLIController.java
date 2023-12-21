package it.unipi.lsmsd.Controller;

import it.unipi.lsmsd.Model.Session;

import java.util.Scanner;

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
            System.out.println("3-Exit");
            System.out.print("Choice->");
            switch (Integer.parseInt(scanner.nextLine())){
                case 1:{
                    System.out.println("username->");
                    String name=scanner.nextLine();
                    System.out.println("password->");
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
                case 3:
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
                case 1:
                    userController.showProfile(Session.getInstance().getLoggedUser());
                    break;
                case 2:
                    System.out.print("Enter username to find:");
                    String usernameToFind = scanner.nextLine();
                    userController.getUserByProfileName(usernameToFind);
                    break;
                case 3:
                    System.out.println("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    boolean passwordChanged = userController.changePassword(Session.getInstance().getLoggedUser(), newPassword);
                    if (passwordChanged) {
                        System.out.println("Password changed successfully.");
                    } else {
                        System.out.println("Password change failed.");
                    }
                    break;
                case 4:
                    System.out.println("Enter book title to find:");
                    String bookTitleToFind = scanner.nextLine();
                    // Implement find book functionality using bookController.searchBooksByTitle(bookTitleToFind)
                    break;
                case 5:
                    System.out.println("Enter review ID to delete:");
                    int reviewIdToDelete = Integer.parseInt(scanner.nextLine());
                    // Implement delete review functionality using userController.deleteReview(reviewIdToDelete)
                    break;
                case 6:
                    // Implement add book functionality using bookController.addBook()
                    break;
                case 7:
                    System.out.println("Enter username to ban:");
                    String usernameToBan = scanner.nextLine();
                    // Implement ban user functionality using userController.banUser(usernameToBan)
                    break;
                case 8:
                    // Implement statistics on user functionality using userController.statisticsOnUser()
                    break;
                case 9:
                    // Implement statistics on books functionality using bookController.statisticsOnBooks()
                    break;
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
