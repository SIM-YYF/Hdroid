package com.hdroid.downloader.dao;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hdroid.downloader.DownloadTask;
import com.hdroid.log.snow.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 *
 * @author snowdream <yanghui1986527@gmail.com>
 * @version v1.0
 * @date 2013-6-10
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something
    // appropriate for your app
    private static final String DATABASE_NAME = "snowdream-android-downloader.db";

    // any time you make changes to your database objects, you may have to
    // increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO object we use to access the table
    private Dao<DownloadTask, Integer> taskDao = null;

    public DatabaseHelper(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION, R.assets.ormlite_config);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should
     * call createTable statements here to create the tables that will store
     * your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i("onCreate");
            TableUtils.createTable(connectionSource, DownloadTask.class);
        } catch (SQLException e) {
            Log.e("Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher
     * version number. This allows you to adjust the various data to match the
     * new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
                          int newVersion) {
        try {
            Log.i("onUpgrade");
            TableUtils.dropTable(connectionSource, DownloadTask.class, true);

            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e("Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our Image class. It will
     * create it or just give the cached value.
     */
    public Dao<DownloadTask, Integer> getTaskDao() throws SQLException {
        if (taskDao == null) {
            taskDao = getDao(DownloadTask.class);
        }
        return taskDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        taskDao = null;
    }
}
