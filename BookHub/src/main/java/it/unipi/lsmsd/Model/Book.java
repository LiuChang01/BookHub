package it.unipi.lsmsd.Model;

import java.util.List;

public class Book {
    private String ISBN;
    private String Title;
    private String description;
    List<String>authors;
    List<String>categories;

    public Book(String ISBN, String title, String description, List<String> authors, List<String> categories) {
        this.ISBN = ISBN;
        Title = title;
        this.description = description;
        this.authors = authors;
        this.categories = categories;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "Book{" +
                "ISBN='" + ISBN + '\'' +
                ", Title='" + Title + '\'' +
                ", description='" + description + '\'' +
                ", authors=" + authors +
                ", categories=" + categories +
                '}';
    }
}
