package com.revolut.db;

import com.revolut.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.java.Log;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

@ApplicationScoped
@Log
public class DbOperation {
    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;

    @PostConstruct
    void init() {
        config.setJdbcUrl("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    @PreDestroy
    void destroy() {
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public <T> T executeAndReturn(Function<DSLContext, T> databaseFunction) {
        try (Connection connection = getConnection()) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.H2);
            return databaseFunction.apply(dslContext);
        } catch (Exception exception) {
            log.severe("exception occurred " + exception);
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }

    public void execute(Consumer<DSLContext> databaseFunction) {
        try (Connection connection = getConnection()) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.H2);
            databaseFunction.accept(dslContext);
        } catch (Exception exception) {
            log.severe("exception occurred " + exception);
            throw new DatabaseException(exception.getMessage(), exception);
        }
    }
}