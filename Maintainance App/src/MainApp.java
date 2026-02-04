import java.sql.*;
import java.util.Scanner;

// --- CONNECTION HELPER ---
class DBConnectionHelper {
    private static final String URL = "jdbc:postgresql://localhost:5433/layout_maintenance_db";
    private static final String USER = "postgres";
    private static final String PASS = "Svnit@02082002";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}


// --- DAO LAYER ---
class MaintenanceDAO {
    
    public void addSite(int id, int typeChoice, int size, boolean occupied) throws SQLException {
        String[] types = {"VILLA", "APARTMENT", "INDEPENDENT_HOUSE", "OPEN_SITE"};
        String type = types[typeChoice - 1];
        int charges = size * (occupied ? 9 : 6);
        
        String sql = "INSERT INTO site_table (site_id, type, size_sqft, maintenance_charges, occupied, booked) VALUES (?, ?, ?, ?, ?, false)";
        try (Connection conn = DBConnectionHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, type);
            ps.setInt(3, size);
            ps.setInt(4, charges);
            ps.setBoolean(5, occupied);
            ps.executeUpdate();
            System.out.println("Site added successfully.");
        }
    }

    public void assignOwner(int sId, int oId, String name, String phone) throws SQLException {
        try (Connection conn = DBConnectionHelper.getConnection()) {
            // 1. Add to owner table
            String sqlOwner = "INSERT INTO owner_table (owner_id, site_id, owner_name, owner_phone) VALUES (?, ?, ?, ?)";
            PreparedStatement ps1 = conn.prepareStatement(sqlOwner);
            ps1.setInt(1, oId); ps1.setInt(2, sId); ps1.setString(3, name); ps1.setString(4, phone);
            ps1.executeUpdate();

            // 2. Mark site as booked
            String sqlSite = "UPDATE site_table SET booked = true WHERE site_id = ?";
            PreparedStatement ps2 = conn.prepareStatement(sqlSite);
            ps2.setInt(1, sId);
            ps2.executeUpdate();
            System.out.println("Owner assigned successfully.");
        }
    }

    public void createCredentials(int oId, String user, String pass) throws SQLException {
        String sql = "INSERT INTO password_table (username, pass, owner_id) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setInt(3, oId);
            ps.executeUpdate();
            System.out.println("Credentials created.");
        }
    }

    public void viewAllSites() throws SQLException {
        String sql = "SELECT s.*, o.owner_name, o.owner_phone FROM site_table s LEFT JOIN owner_table o ON s.site_id = o.site_id ORDER BY s.site_id";
        try (Connection conn = DBConnectionHelper.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("==========================================================================================================");
            System.out.printf("%-5s | %-15s | %-10s | %-10s | %-10s | %-12s | %-20s\n", "ID", "Type", "Size", "Charges", "Occupied", "Status", "Owner & Contact");
            System.out.println("----------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                String status = rs.getBoolean("booked") ? "BOOKED" : "AVAILABLE";
                String owner = rs.getString("owner_name") != null ? rs.getString("owner_name") + " (" + rs.getString("owner_phone") + ")" : "---";
                System.out.printf("%-5d | %-15s | %-10d | %-10d | %-10s | %-12s | %-20s\n", 
                    rs.getInt("site_id"), rs.getString("type"), rs.getInt("size_sqft"), 
                    rs.getInt("maintenance_charges"), rs.getBoolean("occupied")?"YES":"NO", status, owner);
            }
        }
    }
}

// --- MAIN APP ---
public class MainApp {
    static Scanner sc = new Scanner(System.in);
    static MaintenanceDAO dao = new MaintenanceDAO();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Layout Management System ===");
            System.out.println("1. Admin Login\n2. Site Owner Login\n3. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();

            if (choice == 1) adminFlow();
            else if (choice == 2) ownerFlow();
            else break;
        }
    }

    private static void adminFlow() {
        System.out.print("Password: ");
        String p = sc.next();
        if (!p.equals("admin123")) { System.out.println("Denied."); return; }
        
        while (true) {
            System.out.println("\n--- Admin Menu ---\n1. Add Site\n2. Assign Owner\n3. Remove Owner\n4. Credentials\n5. Collect Maintenance\n6. View Pending\n7. View All Sites\n8. Logout");
            int opt = sc.nextInt();
            try {
                switch (opt) {
                    case 1:
                        System.out.print("ID: "); int id = sc.nextInt();
                        System.out.print("Type (1:Villa, 2:Apt, 3:House, 4:Open): "); int t = sc.nextInt();
                        System.out.print("Size: "); int s = sc.nextInt();
                        System.out.print("Occupied (true/false): "); boolean occ = sc.nextBoolean();
                        dao.addSite(id, t, s, occ);
                        break;
                    case 2:
                        System.out.print("Site ID: "); int sid = sc.nextInt();
                        System.out.print("Owner ID: "); int oid = sc.nextInt();
                        System.out.print("Name: "); String name = sc.next();
                        System.out.print("Phone: "); String ph = sc.next();
                        dao.assignOwner(sid, oid, name, ph);
                        break;
                    case 4:
                        System.out.print("Owner ID: "); int credOid = sc.nextInt();
                        System.out.print("User: "); String u = sc.next();
                        System.out.print("Pass: "); String pass = sc.next();
                        dao.createCredentials(credOid, u, pass);
                        break;
                    case 7:
                        dao.viewAllSites();
                        break;
                    case 8: return;
                }
            } catch (Exception e) { System.out.println("Database Error: " + e.getMessage()); }
        }
    }

    private static void ownerFlow() {
        System.out.print("User: "); String u = sc.next();
        System.out.print("Pass: "); String p = sc.next();
        
        try (Connection conn = DBConnectionHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT owner_id FROM password_table WHERE username=? AND pass=?");
            ps.setString(1, u); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int oid = rs.getInt("owner_id");
                ownerMenu(oid);
            } else { System.out.println("Invalid."); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void ownerMenu(int oid) {
        while (true) {
            System.out.println("\n--- Owner Menu ---\n1. View Details\n2. Request Update\n3. Logout");
            int opt = sc.nextInt();
            if (opt == 1) {
                try (Connection conn = DBConnectionHelper.getConnection()) {
                    String sql = "SELECT * FROM owner_table o JOIN site_table s ON o.site_id = s.site_id WHERE o.owner_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, oid);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        System.out.println("\n--- My Profile & Site ---");
                        System.out.println("Owner: " + rs.getString("owner_name") + " | Phone: " + rs.getString("owner_phone"));
                        System.out.println("Site: " + rs.getInt("site_id") + " (" + rs.getString("type") + ") | Size: " + rs.getInt("size_sqft"));
                        System.out.println("Occupied: " + rs.getBoolean("occupied") + " | Maint. Paid: " + rs.getBoolean("maintenance_paid"));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            } else if (opt == 3) return;
        }
    }
}