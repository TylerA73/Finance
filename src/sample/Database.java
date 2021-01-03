package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Database
 *
 * Description: Created to handle of the the database interactions for Transactions and Types
 */
public class Database {

    String host;
    String port;
    String db;
    String user;
    String pass;

    public Database() {
        host = "10.0.0.52";
        port = "3306";
        db = "finance";
        user = "dev";
        pass = "password";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the connection for each database interaction
     *
     * @return Connection conn
     */
    private Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Fetches all of the types of transactions from the types table
     *
     * @return List types
     * @throws SQLException
     */
    public List<Type> findAllTypes() throws SQLException{
        List<Type> types = new ArrayList<Type>();

        Connection con = getConnection();

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from types;");

        while (rs.next()) {
            types.add(new Type(rs.getInt(1), rs.getString(2)));
        }

        con.close();

        return types;
    }

    /**
     * Fetch a list of Transactions between two dates
     *
     * @param to
     * @param from
     * @return ObservableList transactions
     * @throws SQLException
     */
    public ObservableList<Transaction> findTransactions(LocalDate to, LocalDate from) throws SQLException {

        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        Connection con = getConnection();

        String sql = "select t.id, t.description, t.date, t.amount, t.type_id, ty.description " +
                "from transactions t, types ty where t.type_id = ty.id and t.date >=? and t.date <=?;";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, from.toString());
        stmt.setString(2, to.toString());

        ResultSet rs = stmt.executeQuery();

        // Generate a new ObservableList of Transactions using the results
        while (rs.next()) {
            Type type = new Type(rs.getInt(5), rs.getString(6));
            Transaction trans = new Transaction(rs.getInt(1), rs.getString(2), rs.getDouble(4),
                    rs.getDate(3).toLocalDate(), type);
            transactions.add(trans);
        }

        con.close();

        return transactions;
    }

    /**
     * Handles the insertion of a Transaction into the database
     *
     * @param transaction
     * @return boolean
     * @throws SQLException
     */
    public boolean insertTransaction(Transaction transaction) throws SQLException {
        /*String sql = "INSERT INTO transactions (description, date, amount, type_id) values ('" +
                     transaction.getDescription() + "', '" + transaction.getDate() + "', " +
                     transaction.getAmount() + ", " + transaction.getType().getId() + ");";*/
        String sql = "INSERT INTO transactions (description, date, amount, type_id) values (?,?,?,?);";

        Connection con = getConnection();

        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, transaction.getDescription());
        stmt.setString(2, transaction.getDate().toString());
        stmt.setDouble(3, transaction.getAmount());
        stmt.setInt(4, transaction.getType().getId());

        boolean success = stmt.execute();

        con.close();

        return success;
    }

    /**
     * Handles the update of a transaction in the database
     *
     * @param transaction
     * @throws SQLException
     */
    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET description =?, date =?, amount =?, type_id =? where id =?";
        Connection con = getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, transaction.getDescription());
        stmt.setString(2, transaction.getDate().toString());
        stmt.setDouble(3, transaction.getAmount());
        stmt.setInt(4, transaction.getType().getId());
        stmt.setInt(5, transaction.getId());

        stmt.executeUpdate();

        con.close();
    }

    /**
     * Handles the deletion of a transaction from the database
     *
     * @param transaction
     * @throws SQLException
     */
    public void deleteTransaction(Transaction transaction) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id =?";
        Connection con = getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setInt(1, transaction.getId());
        stmt.execute();

        con.close();
    }

}
