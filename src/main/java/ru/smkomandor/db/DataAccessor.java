package ru.smkomandor.db;

import org.postgresql.ds.PGSimpleDataSource;
import ru.smkomandor.model.Basket;
import ru.smkomandor.model.Good;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DataAccessor {
    private static final DataAccessor DA;
    private final Connection connection;

    static {
        try {
            DA = new DataAccessor();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private DataAccessor() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("{aGe%r@xy~");
        connection = dataSource.getConnection();
    }

    public static DataAccessor getDataAccessor() {
        return DA;
    }

    public List<Good> getAllGoods() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, name, cost FROM cashtest.goods");
        return getGoodsList(resultSet);
    }

    public List<Good> getGoods(String substringName) throws SQLException {
        PreparedStatement prepareStatement = connection.prepareStatement(
                "SELECT id, name, cost FROM cashtest.goods WHERE upper(name) LIKE ?"
        );
        prepareStatement.setString(1, "%" + substringName.toUpperCase() + "%");
        ResultSet resultSet = prepareStatement.executeQuery();
        return getGoodsList(resultSet);
    }

    private List<Good> getGoodsList(ResultSet resultSet) throws SQLException {
        List<Good> goods = new ArrayList<>();
        while (resultSet.next()) {
            Integer id = Integer.valueOf(resultSet.getString("id"));
            String name = resultSet.getString("name");
            BigDecimal cost = new BigDecimal(resultSet.getString("cost"));
            goods.add(new Good(id, name, cost));
        }
        return goods;
    }

    public void save() throws SQLException {
        boolean isAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        long checkId = insertToChecksTable();
        insertToChecklinesTable(checkId);
        connection.commit();
        if (isAutoCommit) {
            connection.setAutoCommit(true);
        }
    }

    private long insertToChecksTable() throws SQLException {
        long result;
        PreparedStatement prepareStatement = connection.prepareStatement(
                "INSERT INTO cashtest.checks (date, time, amount)" +
                        " VALUES (?, ?, ?)", new String[]{"id"});
        prepareStatement.setDate(1, Date.valueOf(
                        new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
                ));
        prepareStatement.setTime(2, Time.valueOf(
                new SimpleDateFormat("24HH:mm:ss").format(System.currentTimeMillis())
                ));
        prepareStatement.setBigDecimal(3, Basket.get().getAmount());
        prepareStatement.executeUpdate();
        ResultSet resultSet = prepareStatement.getGeneratedKeys();
        resultSet.next();
        result = resultSet.getLong("ID");
        prepareStatement.close();
        return result;
    }

    private void insertToChecklinesTable(Long checkId) throws SQLException {
        PreparedStatement prepareStatement = connection.prepareStatement(
                "INSERT INTO cashtest.checklines (check_id, good_id, line_number, count, amount)" +
                        " VALUES (?, ?, ?, ?, ?)");
        for (Basket.Row row : Basket.get().getRows()) {
            prepareStatement.setLong(1, checkId);
            prepareStatement.setInt(2, row.getId());
            prepareStatement.setInt(3, row.getLineNumber());
            prepareStatement.setInt(4, row.getCount());
            prepareStatement.setBigDecimal(5, row.getLineAmount());
            prepareStatement.executeUpdate();
        }
        prepareStatement.close();
    }
}
