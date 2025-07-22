package preparation.low_level_design;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LibraryManagementSystem {
    static class Book {
        private String isbn;
        private String title;
        private String author;
        private int copies;

        public Book(String isbn, String title, String author, int copies) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.copies = copies;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public int getCopies() {
            return copies;
        }

        public void setCopies(int copies) {
            this.copies = copies;
        }
    }

    static class User {
        private String name;
        private String id;
        private boolean isActive;

        public User(String name, String id, boolean isActive) {
            this.name = name;
            this.id = id;
            this.isActive = isActive;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
    }

    static class UserBookSchema {
        private String id;
        private String isbn;
        private String title;
        private String userId;
        private LocalDateTime issuedAt;
        private LocalDateTime expectedReturnAt;
        private LocalDateTime returnAt;
        private int price;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public LocalDateTime getIssuedAt() {
            return issuedAt;
        }

        public void setIssuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
        }

        public LocalDateTime getExpectedReturnAt() {
            return expectedReturnAt;
        }

        public void setExpectedReturnAt(LocalDateTime expectedReturnAt) {
            this.expectedReturnAt = expectedReturnAt;
        }

        public LocalDateTime getReturnAt() {
            return returnAt;
        }

        public void setReturnAt(LocalDateTime returnAt) {
            this.returnAt = returnAt;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }

    static class DataStore {
        private Map<String, User> users;
        private Map<String, Book> books;
        private Map<String, UserBookSchema> userBookSchemas;

        public DataStore() {
            users = new ConcurrentHashMap<>();
            books = new ConcurrentHashMap<>();
            userBookSchemas = new ConcurrentHashMap<>();
        }

        public List<User> getUsers() {
            return new ArrayList<>(users.values());
        }

        public List<Book> getBooks() {
            return new ArrayList<>(books.values());
        }

        public List<UserBookSchema> getUserBookSchemas() {
            return new ArrayList<>(userBookSchemas.values());
        }

        public Optional<User> getUser(final String userId) {
            return Optional.ofNullable(users.get(userId));
        }

        public Optional<Book> getBook(final String bookId) {
            return Optional.ofNullable(books.get(bookId));
        }

        public Optional<UserBookSchema> getUserBookSchema(final String isbn, final String userId) {
            return Optional.ofNullable(userBookSchemas.get(buildKey(isbn,userId)));
        }

        public boolean addUser(final User user){
            if(users.containsKey(user.getId())){
                return false;
            }

            return users.put(user.getId(),user) == null;
        }

        public boolean addBook(final Book book){
            if(books.containsKey(book.getIsbn())){
                return false;
            }

            return books.put(book.getIsbn(),book) == null;
        }

        public boolean putBook(final Book book){
            if(!books.containsKey(book.getIsbn())){
                return false;
            }

            return books.put(book.getIsbn(),book) == null;
        }

        public boolean addUserBookSchema(final UserBookSchema userBookSchema){
            if(userBookSchemas.containsKey(userBookSchema.getIsbn())){
                return false;
            }

            return userBookSchemas.put(buildKey(userBookSchema.getIsbn(), userBookSchema.getUserId()),userBookSchema) == null;
        }

        public boolean putUserBookSchema(final UserBookSchema userBookSchema) {
            if(!userBookSchemas.containsKey(userBookSchema.getIsbn())){
                return false;
            }

            return userBookSchemas.put(buildKey(userBookSchema.getIsbn(),  userBookSchema.getUserId()),userBookSchema) == null;
        }



        private static String buildKey(final String isbn, final String userId) {
            return isbn + ":" + userId;
        }
    }

    class Filter {
        private String isbn;
        private String title;
        private String author;

        private Filter() {

        }

        public String getIsbn() {
            return isbn;
        }

        public Filter setIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Filter setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public Filter setAuthor(String author) {
            this.author = author;
            return this;
        }

        public boolean matches(Book book) {
            boolean match = false;
            if(this.isbn != null){
                match = this.isbn.equals(book.getIsbn());
            }

            if(this.title != null){
                match = match && this.title.equals(book.getTitle());
            }

            if(this.author != null){
                match = match && this.author.equals(book.getAuthor());
            }

            return match;
        }
    }

    static class UserBookService {
        private final DataStore dataStore;

        public UserBookService() {
            this.dataStore = new DataStore();
        }

        public List<Book> searchCatalogues(Filter filter) {
            return dataStore.getBooks().stream()
                    .filter(filter::matches)
                    .collect(Collectors.toList());
        }

        public synchronized UserBookSchema borrowBook(final String isbn, final String userId) {
            // make sure the userid exists.
            final User user = dataStore.getUser(userId).orElseThrow();
            // make sure the book exists
            final Book book = dataStore.getBook(isbn).orElseThrow();
            System.out.println("Borrowing book for user " + user.getId() + " Name : "+user.getName() + " Book Name : "+book.getTitle());

            // make sure the user does not have a existing copy.
            final UserBookSchema userBookSchema = dataStore.getUserBookSchema(isbn, user.getId()).orElse(null);

            if(userBookSchema != null && userBookSchema.returnAt == null) {
                    System.out.println("WARN: User is about to borrow the same book which they have not returned a copy.");
                    return null;
            }
            final UserBookSchema newUserBookSchema = new UserBookSchema();
            newUserBookSchema.setIsbn(isbn);
            newUserBookSchema.setUserId(userId);
            newUserBookSchema.setTitle(book.getTitle());
            newUserBookSchema.setIssuedAt(LocalDateTime.now());
            newUserBookSchema.setExpectedReturnAt(LocalDateTime.now().plusMonths(1));
            newUserBookSchema.setPrice(10);

            book.setCopies(book.getCopies() - 1);
            dataStore.addUserBookSchema(newUserBookSchema);
            dataStore.putBook(book);
            return newUserBookSchema;
        }

        public synchronized UserBookSchema returnBook(final String isbn, final String userId) {
            final User user = dataStore.getUser(userId).orElseThrow();
            final Book book = dataStore.getBook(isbn).orElseThrow();
            final UserBookSchema userBookSchema = dataStore.getUserBookSchema(isbn, user.getId()).orElse(null);

            if(userBookSchema != null && userBookSchema.returnAt == null) {
                System.out.println("User "+user.getId()+" is about to return the book "+book.getTitle());
                userBookSchema.setReturnAt(LocalDateTime.now());
                dataStore.putUserBookSchema(userBookSchema);
                return userBookSchema;
            }

            System.out.println("User "+user.getId()+" has not issued any book "+book.getTitle());
            return null;
        }
    }

    public static void main(String[] args) {

    }
}
