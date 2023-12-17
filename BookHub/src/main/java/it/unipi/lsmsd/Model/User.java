package it.unipi.lsmsd.Model;


import java.util.List;

public class User {
    private String username;
    private String password;
    private int type;
    List<LastBookReviews>reviews;

    public User(String username, String password, int type, List<LastBookReviews> reviews) {
        this.username = username;
        this.password = password;
        this.type = type;
        this.reviews = reviews;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<LastBookReviews> getReviews() {
        return reviews;
    }

    public void setReviews(List<LastBookReviews> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", type=" + type +
                ", reviews=" + reviews +
                '}';
    }
}
