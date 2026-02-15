import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class OnlineReservationSystem {

    static final String URL = "jdbc:mysql://localhost:3306/reservation_system";
    static final String USER = "root";
    static final String PASSWORD = "F@123iqr1211"; 

    static Scanner sc = new Scanner(System.in);
    static String loggedInUser = null;

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("MySQL Driver not found!");
            return;
        }

        while (true) {
            System.out.println("\n===== ONLINE RESERVATION SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String input = sc.nextLine().trim();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (Exception e) {
                System.out.println("Invalid input!");
                continue;
            }

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    if (loginUser()) {
                        reservationMenu();
                    }
                    break;
                case 3:
                    System.out.println("Thank you!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ================= REGISTER =================
    public static void registerUser() {

        System.out.print("Enter Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Fields cannot be empty!");
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            String checkQuery = "SELECT * FROM users WHERE username=?";
            PreparedStatement checkPs = con.prepareStatement(checkQuery);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                System.out.println("Username already exists!");
                return;
            }

            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(insertQuery);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            System.out.println("Registration Successful!");

        } catch (SQLException e) {
            System.out.println("Database error during registration!");
        }
    }

    // ================= LOGIN =================
    public static boolean loginUser() {

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            String query = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInUser = username;
                System.out.println("Login Successful!");
                return true;
            } else {
                System.out.println("Invalid Credentials!");
            }

        } catch (SQLException e) {
            System.out.println("Database error during login!");
        }

        return false;
    }

    // ================= RESERVATION MENU =================
    public static void reservationMenu() {

        while (true) {
            System.out.println("\n1. View Available Trains");
            System.out.println("2. Make Reservation");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");

            String input = sc.nextLine().trim();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (Exception e) {
                System.out.println("Invalid input!");
                continue;
            }

            switch (choice) {
                case 1:
                    viewTrains();
                    break;
                case 2:
                    makeReservation();
                    break;
                case 3:
                    cancelReservation();
                    break;
                case 4:
                    loggedInUser = null;
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ================= VIEW TRAINS =================
    public static void viewTrains() {

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM trains");

            System.out.println("\nAvailable Trains:");
            while (rs.next()) {
                System.out.println("Train No: " + rs.getString("train_no") +
                        " | Name: " + rs.getString("train_name") +
                        " | From: " + rs.getString("source") +
                        " | To: " + rs.getString("destination"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching trains!");
        }
    }

    // ================= MAKE RESERVATION =================
    public static void makeReservation() {

        viewTrains();

        System.out.print("\nEnter Train Number: ");
        String trainNo = sc.nextLine().trim();

        System.out.print("Enter Class Type (1A/2A/3A/SL): ");
        String classType = sc.nextLine().trim();

        System.out.print("Enter Journey Date (YYYY-MM-DD): ");
        String date = sc.nextLine().trim();

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            String checkTrain = "SELECT * FROM trains WHERE train_no=?";
            PreparedStatement psCheck = con.prepareStatement(checkTrain);
            psCheck.setString(1, trainNo);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("Invalid Train Number!");
                return;
            }

            String insertQuery = "INSERT INTO reservations (username, train_no, class_type, journey_date) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, loggedInUser);
            ps.setString(2, trainNo);
            ps.setString(3, classType);
            ps.setDate(4, Date.valueOf(date));

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                System.out.println("Reservation Successful!");
                System.out.println("Your PNR Number is: " + keys.getInt(1));
            }

        } catch (Exception e) {
            System.out.println("Error making reservation!");
        }
    }

    // ================= CANCEL RESERVATION =================
    public static void cancelReservation() {

        System.out.print("Enter PNR Number to Cancel: ");
        String input = sc.nextLine().trim();

        int pnr;

        try {
            pnr = Integer.parseInt(input);
        } catch (Exception e) {
            System.out.println("Invalid PNR!");
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            String checkQuery = "SELECT * FROM reservations WHERE pnr=? AND username=?";
            PreparedStatement checkPs = con.prepareStatement(checkQuery);
            checkPs.setInt(1, pnr);
            checkPs.setString(2, loggedInUser);

            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {

                String deleteQuery = "DELETE FROM reservations WHERE pnr=?";
                PreparedStatement deletePs = con.prepareStatement(deleteQuery);
                deletePs.setInt(1, pnr);
                deletePs.executeUpdate();

                System.out.println("Reservation Cancelled Successfully!");

            } else {
                System.out.println("PNR not found or not yours!");
            }

        } catch (SQLException e) {
            System.out.println("Error during cancellation!");
        }
    }
}


//javac -cp ".;lib/mysql-connector-j-9.6.0/mysql-connector-j-9.6.0.jar" OnlineReservationSystem.java
//java -cp ".;lib/mysql-connector-j-9.6.0/mysql-connector-j-9.6.0.jar" OnlineReservationSystem
