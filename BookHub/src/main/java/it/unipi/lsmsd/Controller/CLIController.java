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
            System.out.println("Choice->");
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
                    System.out.println("username->");
                    String name=scanner.nextLine();
                    System.out.println("password->");
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
        System.out.println("reg");
    }

    private static void menuAdmin(){
        System.out.println("admin");
    }
}
