package connectionDB;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DatabaseHelper {
    private static final String DB_PATH = "database/voting.db";
    private static Connection connection;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Please add the dependency.");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get database connection
     */
    private static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        }
        return connection;
    }

    /**
     * Initialize database tables if they don't exist
     */
    private static void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement stmt = conn.createStatement()) {

            // Create candidates table
            String createCandidates = "CREATE TABLE IF NOT EXISTS candidates (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "position TEXT NOT NULL," +
                    "course TEXT," +
                    "year TEXT," +
                    "section TEXT," +
                    "description TEXT" +
                    ")";
            stmt.execute(createCandidates);

            // Create votes table
            String createVotes = "CREATE TABLE IF NOT EXISTS votes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "timestamp TEXT NOT NULL," +
                    "candidate TEXT NOT NULL," +
                    "position TEXT," +
                    "course TEXT," +
                    "year TEXT," +
                    "section TEXT" +
                    ")";
            stmt.execute(createVotes);

            // Create voters table
            String createVoters = "CREATE TABLE IF NOT EXISTS voters (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "studentID TEXT NOT NULL UNIQUE," +
                    "name TEXT NOT NULL," +
                    "email TEXT," +
                    "course TEXT," +
                    "year TEXT," +
                    "section TEXT," +
                    "hasVoted INTEGER DEFAULT 0," +
                    "voteTimestamp TEXT" +
                    ")";
            stmt.execute(createVoters);

            // Check if candidates table is empty and populate with sample data
            String checkCandidates = "SELECT COUNT(*) as count FROM candidates";
            try (ResultSet rs = stmt.executeQuery(checkCandidates)) {
                if (rs.next() && rs.getInt("count") == 0) {
                    initializeSampleData();
                }
            }
        }
    }

    /**
     * Initialize sample candidate data
     */
    private static void initializeSampleData() throws SQLException {
        List<Candidate> samples = Arrays.asList(
            new Candidate("Juan E. Dela Cruz", "President", "BSCS", "3rd", "A",
                          "Position: President\nPlatform: Academic Excellence"),
            new Candidate("Jack N. Jill", "Vice President", "BSHM", "2nd", "B",
                          "Position: Vice President\nPlatform: Student Welfare"),
            new Candidate("Mang E. juan", "Secretary", "BSED", "4th", "C",
                          "Position: Secretary\nPlatform: Campus Development")
        );

        for (Candidate c : samples) {
            appendCandidate(c);
        }
    }


    // ============ CANDIDATE DATABASE METHODS ============

    public static List<Candidate> readCandidates() {
        List<Candidate> out = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, position, course, year, section, description FROM candidates")) {

            while (rs.next()) {
                Candidate c = new Candidate(
                    rs.getString("name"),
                    rs.getString("position"),
                    rs.getString("course"),
                    rs.getString("year"),
                    rs.getString("section"),
                    rs.getString("description")
                );
                out.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void appendCandidate(Candidate c) {
        String query = "INSERT INTO candidates (name, position, course, year, section, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, c.name);
            pstmt.setString(2, c.position);
            pstmt.setString(3, c.course);
            pstmt.setString(4, c.year);
            pstmt.setString(5, c.section);
            pstmt.setString(6, c.description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update a candidate record by matching the old name. Returns true if an update occurred.
     */
    public static boolean updateCandidate(String oldName, Candidate newCandidate) {
        String query = "UPDATE candidates SET name = ?, position = ?, course = ?, year = ?, section = ?, description = ? WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newCandidate.name);
            pstmt.setString(2, newCandidate.position);
            pstmt.setString(3, newCandidate.course);
            pstmt.setString(4, newCandidate.year);
            pstmt.setString(5, newCandidate.section);
            pstmt.setString(6, newCandidate.description);
            pstmt.setString(7, oldName);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a candidate record by name. Returns true if a deletion occurred.
     */
    public static boolean deleteCandidate(String name) {
        String query = "DELETE FROM candidates WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ============ VOTE DATABASE METHODS ============

    public static void recordVote(Vote v) {
        String query = "INSERT INTO votes (timestamp, candidate, position, course, year, section) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, v.timestamp);
            pstmt.setString(2, v.candidate);
            pstmt.setString(3, v.position);
            pstmt.setString(4, v.course);
            pstmt.setString(5, v.year);
            pstmt.setString(6, v.section);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Vote> readVotes() {
        List<Vote> out = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT timestamp, candidate, position, course, year, section FROM votes")) {

            while (rs.next()) {
                Vote v = new Vote(
                    rs.getString("timestamp"),
                    rs.getString("candidate"),
                    rs.getString("position"),
                    rs.getString("course"),
                    rs.getString("year"),
                    rs.getString("section")
                );
                out.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static Map<String, Integer> countVotes() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT candidate, COUNT(*) as count FROM votes GROUP BY candidate")) {

            while (rs.next()) {
                counts.put(rs.getString("candidate"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public static Vote makeVote(String candidate, String position, String course, String year, String section) {
        return new Vote(Instant.now().toString(), candidate == null ? "" : candidate, position == null ? "" : position,
                        course == null ? "" : course, year == null ? "" : year, section == null ? "" : section);
    }

    // ============ VOTER DATABASE METHODS ============

    public static List<Voter> readVoters() {
        List<Voter> out = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT studentID, name, email, course, year, section, hasVoted, voteTimestamp FROM voters")) {

            while (rs.next()) {
                Voter v = new Voter(
                    rs.getString("studentID"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("course"),
                    rs.getString("year"),
                    rs.getString("section"),
                    rs.getInt("hasVoted") == 1,
                    rs.getString("voteTimestamp")
                );
                out.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void addVoter(Voter voter) {
        String query = "INSERT INTO voters (studentID, name, email, course, year, section, hasVoted, voteTimestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, voter.studentID);
            pstmt.setString(2, voter.name);
            pstmt.setString(3, voter.email);
            pstmt.setString(4, voter.course);
            pstmt.setString(5, voter.year);
            pstmt.setString(6, voter.section);
            pstmt.setInt(7, voter.hasVoted ? 1 : 0);
            pstmt.setString(8, voter.voteTimestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a student with this ID has already voted.
     */
    public static boolean hasVoted(String studentID) {
        String query = "SELECT hasVoted FROM voters WHERE studentID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("hasVoted") == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark a voter as having voted and record the timestamp.
     */
    public static boolean markVoterAsVoted(String studentID) {
        String query = "UPDATE voters SET hasVoted = 1, voteTimestamp = ? WHERE studentID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, Instant.now().toString());
            pstmt.setString(2, studentID);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get a voter by student ID.
     */
    public static Voter getVoterByID(String studentID) {
        String query = "SELECT studentID, name, email, course, year, section, hasVoted, voteTimestamp FROM voters WHERE studentID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Voter(
                        rs.getString("studentID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("course"),
                        rs.getString("year"),
                        rs.getString("section"),
                        rs.getInt("hasVoted") == 1,
                        rs.getString("voteTimestamp")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Check if a student ID already exists in the voter database.
     */
    public static boolean voterExists(String studentID) {
        return getVoterByID(studentID) != null;
    }

    /**
     * Get count of voters who have voted vs total voters.
     */
    public static Map<String, Integer> getVoterStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String query = "SELECT COUNT(*) as total, SUM(CASE WHEN hasVoted = 1 THEN 1 ELSE 0 END) as voted FROM voters";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int voted = rs.getInt("voted");
                stats.put("total", total);
                stats.put("voted", voted);
                stats.put("notVoted", total - voted);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
