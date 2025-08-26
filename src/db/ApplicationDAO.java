package db;

import model.Application;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public List<Application> getAllApplications() {
        List<Application> applications = new ArrayList<>();
 
        String query = "SELECT * FROM applications";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Application app = new Application(
                    rs.getInt("id"),
                    rs.getString("company"),
                    rs.getString("position"),
                    rs.getString("application_date"),
                    rs.getString("status"),
                    rs.getString("notes")
                );

                applications.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving applications: " + e.getMessage());
        }

        return applications;
    }

    public void addApplication(Application app) {
        String query = "INSERT INTO applications (company, position, application_date, status, notes) VALUES (?, ?, ?, ?, ?)";
    
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, app.getCompany());
            stmt.setString(2, app.getPosition());
            stmt.setString(3, app.getApplicationDate());
            stmt.setString(4, app.getStatus());
            stmt.setString(5, app.getNotes());
    
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Inserted " + rowsAffected + " application(s).");
    
        } catch (SQLException e) {
            System.out.println("Error inserting application: " + e.getMessage());
        }
    }

    public void deleteApplication(int id) {
        String query = "DELETE FROM applications WHERE id = ?";
    
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
    
            if (rows > 0) {
                System.out.println("Application deleted successfully.");
            } else {
                System.out.println("No application found with that ID.");
            }
    
        } catch (SQLException e) {
            System.out.println("Error deleting application: " + e.getMessage());
        }
    }

    public void updateApplication(Application app) {
        String query = "UPDATE applications SET company=?, position=?, application_date=?, status=?, notes=? WHERE id=?";
    
        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setString(1, app.getCompany());
            stmt.setString(2, app.getPosition());
            stmt.setString(3, app.getApplicationDate());
            stmt.setString(4, app.getStatus());
            stmt.setString(5, app.getNotes());
            stmt.setInt(6, app.getId());
    
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating application: " + e.getMessage());
        }
    }
    
}
