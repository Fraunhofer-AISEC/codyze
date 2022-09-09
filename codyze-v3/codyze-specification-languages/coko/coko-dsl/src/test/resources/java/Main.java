import java.sql.*;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import javalinjwt.JavalinJWT;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class Main {
    static Connection conn;

    static Handler action = new HandlerImpl(conn);
    // The CPG can't handle the following:
    /*static Handler action= ctx -> {
        String decodedJWT = JavalinJWT.getTokenFromHeader(ctx).get();
        Statement statement = conn.createStatement();
        statement.executeUpdate("INSERT INTO data VALUES ('2006-01-05','very important')");
        logger.info("[audit] " + decodedJWT  + " did something in the database");
    };*/

    public static void main(String[] args) {
        try {
            Main.conn = DriverManager.getConnection("jdbc:msql://localhost:1234/test","","");
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE TABLE data (date text, data text)");
        } catch(Exception e) {
            // Nothing to do
        }

        Javalin app = Javalin.create().start(8080);
        app.post("/do", action);
    }
}

class HandlerImpl implements Handler {
    static Connection conn;
    static Logger logger = Logger.getLogger("Main");

    public HandlerImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Optional<String> optJWT = JavalinJWT.getTokenFromHeader(ctx);
        String decodedJWT = optJWT.get();
        Statement statement = conn.createStatement();
        statement.executeUpdate("INSERT INTO data VALUES ('2006-01-05','very important')");
        logger.info("[audit] " + decodedJWT  + " did something in the database");
    }
}