package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

public class TestDBResourceManager2 {

    DbLibService dblibSvc;
    DBResourceManager dbm;

    @Before
    public void setUp() throws Exception {
        URL propUrl = getClass().getResource("/dblib.properties");

        InputStream propStr = getClass().getResourceAsStream("/dblib.properties");

        Properties props = new Properties();

        props.load(propStr);

        // Start MariaDB4j database
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        // Override jdbc URL, database name, and recovery
        props.setProperty("org.onap.ccsdk.sli.jdbc.database", "test");
        props.setProperty("org.onap.ccsdk.sli.jdbc.url", config.getURL("test"));
        props.setProperty("org.onap.dblib.connection.recovery", "true");
        

        dblibSvc = new DBResourceManager(props);
        dbm = new DBResourceManager(props);
        dblibSvc.writeData("CREATE TABLE DBLIB_TEST2 (name varchar(20));", null, null);
        dblibSvc.getData("SELECT * FROM DBLIB_TEST2", null, null);
        

    }

    @Test
    public void testForceRecovery() {
        dbm.testForceRecovery();
    }

    @Test
    public void testGetConnection() throws SQLException {
        assertNotNull(dbm.getConnection());
        assertNotNull(dbm.getConnection("testUser", "testPaswd"));
    }

    @Test
    public void testCleanup() {
        dbm.cleanUp();

    }

    @Test
    public void testGetLogWriter() throws SQLException {
        assertNull(dbm.getLogWriter());
    }

}
