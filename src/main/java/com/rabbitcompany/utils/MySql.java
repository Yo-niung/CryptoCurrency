package com.rabbitcompany.utils;

import com.rabbitcompany.CryptoCurrency;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySql {
    private static HikariDataSource dataSource;

    public static double getPlayerBalance(String player_uuid, String player_name, String crypto){
        String query = "SELECT balance FROM cryptocurrency_" + crypto + " WHERE uuid = '" + player_uuid + "' OR username = '" + player_name + "';";
        double balance = 0;
        try {
            Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) balance = rs.getDouble("balance");
            conn.close();
        } catch (SQLException ignored) {}
        return balance;
    }

    public static Map<UUID, Double> getPlayerBalances(String crypto) {
        Map<UUID, Double> balances = new HashMap<>();
        String query = "SELECT uuid, balance FROM cryptocurrency_" + crypto + ";";
        try {
            Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String uuidStr = rs.getString("uuid");
                UUID uuid = UUID.fromString(uuidStr);
                double balance = rs.getDouble("balance");
                balances.put(uuid, balance);
            }
            conn.close();
        } catch (SQLException | IllegalArgumentException ignored) {}
        return balances;
    }


    public static boolean setPlayerBalance(String player_uuid, String player_name, String balance, String crypto){
        try {
            Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
            conn.createStatement().executeUpdate("UPDATE cryptocurrency_" + crypto + " SET balance = " + balance + " WHERE uuid = '" + player_uuid + "' OR username = '" + player_name + "';");
            conn.close();
            return true;
        } catch (SQLException ignored) {}
        return false;
    }

    public static boolean isPlayerInDatabase(String player_name, String crypto){
        String query = "SELECT * FROM cryptocurrency_" + crypto + " WHERE username = '" + player_name + "';";
        try {
            Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            conn.close();
            if(rs.next()) return true;
        } catch (SQLException ignored) {}
        return false;
    }

    public static boolean createPlayerWallet(String player_uuid, String player_name, String crypto){
        String query = "INSERT INTO cryptocurrency_" + crypto + " (uuid, username, balance) SELECT ?, ?, ? FROM DUAL WHERE NOT EXISTS (SELECT * FROM cryptocurrency_" + crypto + " WHERE uuid = ? OR username = ?);";
        try (Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, player_uuid);
            ps.setString(2, player_name);
            ps.setDouble(3, 0);
            ps.setString(4, player_uuid);
            ps.setString(5, player_name);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double getCryptoSupply(String crypto){
        String query = "SELECT SUM(balance) as supply FROM cryptocurrency_" + crypto + ";";
        double supply = 0;
        try {
            Connection conn = CryptoCurrency.getInstance().getHikari().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) supply = rs.getDouble("supply");
            conn.close();
        } catch (SQLException ignored) {}
        return supply;
    }
    public static boolean checkConnection() {
        try (Connection conn = CryptoCurrency.getHikari().getConnection()) {
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/cryptocurrency");
        config.setUsername("root");
        config.setPassword("password");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

}