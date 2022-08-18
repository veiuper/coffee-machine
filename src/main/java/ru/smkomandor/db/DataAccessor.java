package ru.smkomandor.db;

import org.postgresql.ds.PGSimpleDataSource;
import ru.smkomandor.model.Good;

import java.math.BigDecimal;
import java.sql.*;
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

    public void save() {

    }
}
