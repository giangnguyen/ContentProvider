package com.thechange.libs.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.thechange.libs.dbtable.AbsDBTable;

/**
 * An derived class of {@link SQLiteOpenHelper} class.
 * @author giangnguyen
 *
 */
public class BaseSqliteOpenHelper extends SQLiteOpenHelper {
    
    private static final String     TAG = BaseSqliteOpenHelper.class.getName();
    
    private final AbsDBTable[] tables;
    
    public BaseSqliteOpenHelper(Context context, String databaseName,
            int databaseVersion, AbsDBTable[] tables) {
        super(context, databaseName, null, databaseVersion);
        this.tables = tables;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (AbsDBTable table : tables) {
            db.execSQL(table.getCreateScript());
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        for (AbsDBTable table : tables) {
            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
        }
        // Recreates the database with a new version
        onCreate(db);
    }
}
