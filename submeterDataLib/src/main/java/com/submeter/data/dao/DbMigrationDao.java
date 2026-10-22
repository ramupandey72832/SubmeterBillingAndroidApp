package com.submeter.data.dao;

import java.sql.SQLException;

public interface DbMigrationDao {
    boolean executeMigration() throws SQLException;
}