package cn.jason31416.authX.handler;

import cn.jason31416.authX.AuthXPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DatabaseHandler {
    public static HikariDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @SneakyThrows
    public static void init() {
        if(dataSource!= null&&!dataSource.isClosed()) dataSource.close();

        HikariConfig otherConfig = new HikariConfig();
        otherConfig.setDriverClassName("org.sqlite.JDBC");
        otherConfig.setJdbcUrl("jdbc:sqlite:" + new File(AuthXPlugin.getInstance().getDataDirectory(), "usermeta.db").getAbsolutePath());
        dataSource = new HikariDataSource(otherConfig);

        try (Connection connection = getConnection()) {
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS authmethods (username VARCHAR(255) PRIMARY KEY, verified VARCHAR(255), preferred VARCHAR(255))").execute();
        }
    }

    @SneakyThrows
    public static void setPreferred(String username, String method){
        try (Connection connection = getConnection()) {
            // if user does not exist, create it
            var st = connection.prepareStatement("INSERT OR IGNORE INTO authmethods (username, verified, preferred) VALUES (?,?,?)");
            st.setString(1, username);
            st.setString(2, "");
            st.setString(3, method);
            st.execute();

            // update preferred method
            st = connection.prepareStatement("UPDATE authmethods SET preferred =? WHERE username =?");
            st.setString(1, method);
            st.setString(2, username);
            st.execute();
        }
    }

    // todo: need to add authmethod when password authentication is complete
    @SneakyThrows
    public static void addAuthMethod(String username, String method) { // it is assumed that user is already created
        try (Connection connection = getConnection()) {
            // add method to verified list
            var st = connection.prepareStatement("UPDATE authmethods SET verified = verified || ? WHERE username =?");
            st.setString(1, "," + method);
            st.setString(2, username);
            st.execute();
        }
    }

    @SneakyThrows
    public static List<String> getAuthMethods(String username) {
        try (Connection connection = getConnection()) {
            var st = connection.prepareStatement("SELECT verified FROM authmethods WHERE username =?");
            st.setString(1, username);
            var rs = st.executeQuery();
            if (rs.next()) {
                var verified = rs.getString("verified");
                if (verified == null || verified.isEmpty()) {
                    return List.of();
                } else {
                    return List.of(verified.substring(1).split(","));
                }
            } else {
                return List.of();
            }
        }
    }
    @SneakyThrows
    public static String getPreferredMethod(String username) {
        try (Connection connection = getConnection()) {
            var st = connection.prepareStatement("SELECT preferred FROM authmethods WHERE username =?");
            st.setString(1, username);
            var rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("preferred");
            } else {
                return null;
            }
        }
    }
}
