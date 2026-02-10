import java.sql.*;
import java.util.Scanner;

// --- DOMAIN MODELS ---
enum SiteCategory {
    VILLA, APARTMENT, INDEPENDENT_HOUSE, OPEN_SITE
}

class Property {
    private final int id;
    private final SiteCategory category;
    private final int areaSize;
    private final int annualCharges;
    private final boolean isOccupied;
    private final boolean isBooked;

    public Property(int id, SiteCategory category, boolean occupied, boolean booked, int size) {
        this.id = id;
        this.category = category;
        this.isOccupied = occupied;
        this.isBooked = booked;
        this.areaSize = size;
        // Logic: Occupied sites pay 9/sqft, vacant pay 6/sqft
        this.annualCharges = (occupied ? 9 : 6) * size;
    }

    public int getId() { return id; }
    public SiteCategory getCategory() { return category; }
    public int getAreaSize() { return areaSize; }
    public int getAnnualCharges() { return annualCharges; }
    public boolean isOccupied() { return isOccupied; }
    public boolean isBooked() { return isBooked; }
}

class Account {
    private final String accessLevel;
    private final int refId;

    public Account(String accessLevel, int refId) {
        this.accessLevel = accessLevel;
        this.refId = refId;
    }

    public int getRefId() { return refId; }
    public String getAccessLevel() { return accessLevel; }
}

class Administrator extends Account {
    private static Administrator instance = null;

    private Administrator() {
        super("ADMIN", 0);
    }

    public static Administrator getInstance() {
        if (instance == null) instance = new Administrator();
        return instance;
    }
}

// --- DATA ACCESS LAYER ---
interface MaintenanceService {
    void registerSite(Property p) throws SQLException;
    void assignOwnerToSite(int sId, int oId, String name, long tel) throws SQLException;
    void terminateOwner(int oId) throws SQLException;
    void setupLogin(int oId, String u, String p) throws SQLException;
    void payMaintenance(int sId) throws SQLException;
    void showUpdateRequests() throws SQLException;
    void handleRequest(int reqId, boolean approved) throws SQLException;
    void displayInventory() throws SQLException;
    int checkAuth(String u, String p) throws SQLException;
    void showProfile(int oId) throws SQLException;
    void requestInfoChange(int oId, String name, long tel) throws SQLException;
}

class DBConnectionHelper {
    private static final String URL = "jdbc:postgresql://localhost:5433/layout_maintenance_db";
    private static final String USER = "postgres";
    private static final String PASS = "Svnit@02082002";
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }
}

class ServiceImplementation implements MaintenanceService {

    @Override
    public void registerSite(Property p) throws SQLException {
        String query = "INSERT INTO site_table (site_id, type, size, maintenance_charges, occupied, booked) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(query)) {
            stmt.setInt(1, p.getId());
            stmt.setString(2, p.getCategory().name());
            stmt.setInt(3, p.getAreaSize());
            stmt.setInt(4, p.getAnnualCharges());
            stmt.setBoolean(5, p.isOccupied());
            stmt.setBoolean(6, p.isBooked());
            stmt.executeUpdate();
            System.out.println(">> Site successfully registered in database.");
        }
    }

    @Override
    public void assignOwnerToSite(int sId, int oId, String name, long tel) throws SQLException {
        Connection db = DBConnectionHelper.getConnection();
        db.setAutoCommit(false);
        try {
            // Verify if site is available
            try (PreparedStatement check = db.prepareStatement("SELECT booked FROM site_table WHERE site_id = ?")) {
                check.setInt(1, sId);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getBoolean("booked")) {
                    System.out.println("!! Operation Failed: Site already has an owner.");
                    return;
                }
            }

            String ins = "INSERT INTO owner_table (owner_id, site_id, owner_name, owner_phone_no, maintenance_paid) VALUES (?, ?, ?, ?, false)";
            try (PreparedStatement stmt = db.prepareStatement(ins)) {
                stmt.setInt(1, oId);
                stmt.setInt(2, sId);
                stmt.setString(3, name);
                stmt.setLong(4, tel);
                stmt.executeUpdate();
            }

            db.createStatement().execute("UPDATE site_table SET booked = true WHERE site_id = " + sId);
            db.commit();
            System.out.println(">> Owner successfully linked to site.");
        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Override
    public void terminateOwner(int oId) throws SQLException {
        Connection db = DBConnectionHelper.getConnection();
        db.setAutoCommit(false);
        try {
            int targetSite = -1;
            try (PreparedStatement find = db.prepareStatement("SELECT site_id FROM owner_table WHERE owner_id = ?")) {
                find.setInt(1, oId);
                ResultSet rs = find.executeQuery();
                if (rs.next()) targetSite = rs.getInt("site_id");
            }

            if (targetSite != -1) {
                db.createStatement().execute("DELETE FROM password WHERE owner_id = " + oId);
                db.createStatement().execute("DELETE FROM owner_table WHERE owner_id = " + oId);
                db.createStatement().execute("UPDATE site_table SET booked = false WHERE site_id = " + targetSite);
                db.commit();
                System.out.println(">> Owner records cleared. Site is now available.");
            }
        } catch (SQLException e) {
            db.rollback();
            throw new SQLException("Cannot remove owner: Pending requests exist.");
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Override
    public void setupLogin(int oId, String u, String p) throws SQLException {
        String sql = "INSERT INTO password (username, pass, owner_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, u);
            stmt.setString(2, p);
            stmt.setInt(3, oId);
            stmt.executeUpdate();
            System.out.println(">> Access credentials established.");
        }
    }

    @Override
    public void payMaintenance(int sId) throws SQLException {
        String update = "UPDATE owner_table SET maintenance_paid = true WHERE site_id = ?";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(update)) {
            stmt.setInt(1, sId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println(">> Maintenance payment verified.");
            else System.out.println("!! No active booking found for this Site ID.");
        }
    }

    @Override
    public void showUpdateRequests() throws SQLException {
        String sql = "SELECT * FROM request_table";
        try (Statement st = DBConnectionHelper.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n--- INCOMING PROFILE UPDATE REQUESTS ---");
            while (rs.next()) {
                System.out.printf("Ticket #%d | Owner: %d | Requested Name: %s\n",
                        rs.getInt("request_id"), rs.getInt("owner_id"), rs.getString("owner_name"));
            }
        }
    }

    @Override
    public void handleRequest(int reqId, boolean approved) throws SQLException {
        Connection db = DBConnectionHelper.getConnection();
        db.setAutoCommit(false);
        try {
            if (approved) {
                try (PreparedStatement fetch = db.prepareStatement("SELECT * FROM request_table WHERE request_id = ?")) {
                    fetch.setInt(1, reqId);
                    ResultSet rs = fetch.executeQuery();
                    if (rs.next()) {
                        String update = "UPDATE owner_table SET owner_name = ?, owner_phone_no = ? WHERE owner_id = ?";
                        try (PreparedStatement ups = db.prepareStatement(update)) {
                            ups.setString(1, rs.getString("owner_name"));
                            ups.setLong(2, rs.getLong("owner_phone"));
                            ups.setInt(3, rs.getInt("owner_id"));
                            ups.executeUpdate();
                        }
                    }
                }
            }
            db.createStatement().execute("DELETE FROM request_table WHERE request_id = " + reqId);
            db.commit();
            System.out.println(approved ? ">> Profile updated successfully." : ">> Request declined.");
        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Override
    public void displayInventory() throws SQLException {
        String sql = "SELECT s.*, o.owner_name, o.owner_phone_no FROM site_table s LEFT JOIN owner_table o ON s.site_id = o.site_id ORDER BY s.site_id";
        try (Statement st = DBConnectionHelper.getConnection().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("-".repeat(110));
            System.out.printf("%-6s | %-15s | %-8s | %-10s | %-12s | %-20s\n", "ID", "TYPE", "SIZE", "CHARGE", "STATUS", "OWNER INFO");
            System.out.println("-".repeat(110));
            while (rs.next()) {
                String contact = rs.getBoolean("booked") ? rs.getString("owner_name") : "VACANT";
                System.out.printf("%-6d | %-15s | %-8d | %-10d | %-12s | %-20s\n",
                        rs.getInt("site_id"), rs.getString("type"), rs.getInt("size"),
                        rs.getInt("maintenance_charges"), rs.getBoolean("booked") ? "BOOKED" : "OPEN", contact);
            }
        }
    }

    @Override
    public int checkAuth(String u, String p) throws SQLException {
        String sql = "SELECT owner_id FROM password WHERE username=? AND pass=?";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, u);
            stmt.setString(2, p);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("owner_id") : -1;
        }
    }

    @Override
    public void showProfile(int oId) throws SQLException {
        String sql = "SELECT s.*, o.* FROM site_table s JOIN owner_table o ON s.site_id = o.site_id WHERE o.owner_id = ?";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, oId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("\n--- PERSONAL DASHBOARD ---");
                System.out.println("Name: " + rs.getString("owner_name") + " | Phone: " + rs.getLong("owner_phone_no"));
                System.out.println("Property: " + rs.getString("type") + " (ID: " + rs.getInt("site_id") + ")");
                System.out.println("Maintenance Status: " + (rs.getBoolean("maintenance_paid") ? "PAID" : "DUE"));
            }
        }
    }

    @Override
    public void requestInfoChange(int oId, String name, long tel) throws SQLException {
        String sql = "INSERT INTO request_table (owner_id, owner_name, owner_phone) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DBConnectionHelper.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, oId);
            stmt.setString(2, name);
            stmt.setLong(3, tel);
            stmt.executeUpdate();
            System.out.println(">> Update request sent for Admin approval.");
        }
    }
}

// --- MAIN APPLICATION ---
public class MainApp {
    static Scanner input = new Scanner(System.in);
    static MaintenanceService service = new ServiceImplementation();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n==== REAL ESTATE MAINTENANCE PORTAL ====");
            System.out.println("1. Administrator Portal\n2. Resident Portal\n3. Shut Down");
            System.out.print("Select Access Level: ");
            int choice = input.nextInt();

            try {
                switch (choice) {
                    case 1 -> performAdminLogin();
                    case 2 -> performResidentLogin();
                    case 3 -> { System.exit(0); }
                    default -> System.out.println("!! Invalid Choice.");
                }
            } catch (SQLException e) {
                System.err.println("Database Alert: " + e.getMessage());
            }
        }
    }

    private static void performAdminLogin() throws SQLException {
        System.out.print("System Password: ");
        if (input.next().equals("admin123")) {
            System.out.println("Access Granted. Initializing Admin Session...");
            runAdminInterface();
        } else {
            System.out.println("!! Access Denied.");
        }
    }

    private static void runAdminInterface() throws SQLException {
        while (true) {
            System.out.println("\n--- ADMIN CONTROL PANEL ---");
            System.out.println("1. New Site | 2. Assign Owner | 3. Evict Owner | 4. Set Login");
            System.out.println("5. Log Payment | 6. Requests | 7. Inventory | 8. Logoff");
            int action = input.nextInt();
            if (action == 8) break;

            switch (action) {
                case 1 -> {
                    System.out.print("Site ID: "); int id = input.nextInt();
                    System.out.print("Type (1:Villa, 2:Apt, 3:House, 4:Open): "); int t = input.nextInt();
                    System.out.print("SqFt: "); int sq = input.nextInt();
                    service.registerSite(new Property(id, SiteCategory.values()[t - 1], t != 4, false, sq));
                }
                case 2 -> {
                    System.out.print("Site ID: "); int sid = input.nextInt();
                    System.out.print("Owner ID: "); int oid = input.nextInt();
                    input.nextLine(); System.out.print("Full Name: "); String name = input.nextLine();
                    System.out.print("Contact: "); long tel = input.nextLong();
                    service.assignOwnerToSite(sid, oid, name, tel);
                }
                case 3 -> {
                    System.out.print("Target Owner ID: ");
                    service.terminateOwner(input.nextInt());
                }
                case 4 -> {
                    System.out.print("Owner ID: "); int oid = input.nextInt();
                    System.out.print("Username: "); String u = input.next();
                    System.out.print("Password: "); String p = input.next();
                    service.setupLogin(oid, u, p);
                }
                case 5 -> {
                    System.out.print("Site ID: ");
                    service.payMaintenance(input.nextInt());
                }
                case 6 -> {
                    service.showUpdateRequests();
                    System.out.print("Request ID to process (0 to skip): ");
                    int rid = input.nextInt();
                    if (rid != 0) {
                        System.out.print("1: Approve / 2: Reject: ");
                        service.handleRequest(rid, input.nextInt() == 1);
                    }
                }
                case 7 -> service.displayInventory();
            }
        }
    }

    private static void performResidentLogin() throws SQLException {
        System.out.print("Username: "); String u = input.next();
        System.out.print("Password: "); String p = input.next();
        int ownerId = service.checkAuth(u, p);
        if (ownerId != -1) runResidentInterface(ownerId);
        else System.out.println("!! Login Failed.");
    }

    private static void runResidentInterface(int ownerId) throws SQLException {
        while (true) {
            System.out.println("\n--- RESIDENT MENU ---\n1. View My Property\n2. Request Name/Phone Change\n3. Exit");
            int choice = input.nextInt();
            if (choice == 3) break;
            if (choice == 1) service.showProfile(ownerId);
            else if (choice == 2) {
                input.nextLine(); System.out.print("New Name: "); String n = input.nextLine();
                System.out.print("New Phone: "); long ph = input.nextLong();
                service.requestInfoChange(ownerId, n, ph);
            }
        }
    }
}

