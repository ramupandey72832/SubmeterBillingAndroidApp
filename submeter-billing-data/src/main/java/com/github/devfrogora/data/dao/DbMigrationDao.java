package com.github.devfrogora.data.dao;


import java.sql.SQLException;

public interface DbMigrationDao {
    boolean executeMigration() throws SQLException;
}