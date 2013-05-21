package com.thechange.libs.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.thechange.libs.dbtable.AbsDBTable;
import com.thechange.libs.dbtable.UriMatched;

public abstract class AbsContentProvider extends ContentProvider {
    
    private BaseSqliteOpenHelper mSqliteOpenHelper;
    
    public static final String   SCHEME = "content://";
    
    /**
     * Get authority of this content provider
     * 
     * @return the authority
     */
    abstract String getAuthority();
    
    /**
     * Get name of database
     * 
     * @return the database's name
     */
    abstract String getDBName();
    
    /**
     * Get version of database.
     * 
     * @return the database's version
     */
    abstract int getDBVersion();
    
    /**
     * Get list of {@link AbsDBTable} that supported by this
     * {@link ContentProvider}
     * 
     * @return list of {@link AbsDBTable}
     */
    abstract AbsDBTable[] getTables();
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final UriMatched uriMatched = scanUri(uri);
        int count;
        if (uriMatched == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        } else {
            final long id = uriMatched.getId();
            final String tableName = uriMatched.getTableName();
            // Opens the database object in "write" mode.
            final SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
            if (id == UriMatched.UNKNOW_ID) {
                count = db.delete(tableName, where, whereArgs);
            } else {
                String finalWhere;
                finalWhere = AbsDBTable._ID + " = " + id;
                // If there were additional selection criteria, append them to
                // the final WHERE clause
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }
                // Performs the delete.
                count = db.delete(tableName, // The database table name.
                        finalWhere, // The final WHERE clause
                        whereArgs // The incoming where clause values.
                        );
            }
        }
        /*
         * Gets a handle to the content resolver object for the current context,
         * and notifies it that the incoming URI changed. The object passes this
         * along to the resolver framework, and observers that have registered
         * themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);
        // Returns the number of rows deleted.
        return count;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final UriMatched uriMatched = scanUri(uri);
        // Validates the incoming URI. Only the full provider URI is allowed for
        // inserts.
        if (uriMatched == null || uriMatched.getId() != UriMatched.UNKNOW_ID) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
        // Performs the insert and returns the ID of the new row.
        long rowId = db.insert(uriMatched.getTableName(), null, values);
        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the item ID pattern and the new row ID
            // appended to it.
            Uri rowUri = ContentUris.withAppendedId(uri, rowId);
            // Notifies observers registered against this provider that the data
            // changed.
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        // If the insert didn't succeed, then the rowID is <= 0. Throws an
        // exception.
        throw new SQLException("Failed to insert row into " + uri);
    }
    
    @Override
    public boolean onCreate() {
        mSqliteOpenHelper = new BaseSqliteOpenHelper(getContext(), getDBName(),
                getDBVersion(), getTables());
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final UriMatched uriMatched = scanUri(uri);
        if (uriMatched == null) {
            // If the URI doesn't match any of the known patterns, throw an
            // exception.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Constructs a new query builder and sets its table name
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(uriMatched.getTableName());
        final long id = uriMatched.getId();
        if (id != UriMatched.UNKNOW_ID) {
            /*
             * If the incoming URI is for a single item identified by its ID,
             * chooses the item ID projection, and appends "_ID = <id>" to the
             * where clause, so that it selects that single item.
             */
            qb.appendWhere(AbsDBTable._ID + " = " + id);
        }
        // Opens the database object in "read" mode, since no writes need to be
        // done.
        SQLiteDatabase db = mSqliteOpenHelper.getReadableDatabase();
        /*
         * Performs the query. If no problems occur trying to read the database,
         * then a Cursor object is returned; otherwise, the cursor variable
         * contains null. If no records were selected, then the Cursor object is
         * empty, and Cursor.getCount() returns 0.
         */
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        // Tells the Cursor what URI to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        final UriMatched uriMatched = scanUri(uri);
        if (uriMatched == null) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        // Opens the database object in "write" mode.
        final SQLiteDatabase db = mSqliteOpenHelper.getWritableDatabase();
        final long id = uriMatched.getId();
        final String tableName = uriMatched.getTableName();
        int count;
        if (id == UriMatched.UNKNOW_ID) {
            // Does the update and returns the number of rows updated.
            count = db.update(tableName, // The database table name.
                    values, // A map of column names and new values to use.
                    where, // The where clause column names.
                    whereArgs // The where clause column values to select on.
                    );
        } else {
            String finalWhere;
            /*
             * Starts creating the final WHERE clause by restricting it to the
             * incoming ID.
             */
            finalWhere = AbsDBTable._ID + " = " + id;
            // If there were additional selection criteria, append them to the
            // final WHERE
            // clause
            if (where != null) {
                finalWhere = finalWhere + " AND " + where;
            }
            // Does the update and returns the number of rows updated.
            count = db.update(tableName, values, finalWhere, whereArgs);
        }
        /*
         * Gets a handle to the content resolver object for the current context,
         * and notifies it that the incoming URI changed. The object passes this
         * along to the resolver framework, and observers that have registered
         * themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);
        // Returns the number of rows updated.
        return count;
    }
    
    public SQLiteOpenHelper getOpenHelperForTest() {
        return mSqliteOpenHelper;
    }
    
    private UriMatched scanUri(Uri uri) {
        for (AbsDBTable dbTable : getTables()) {
            final UriMatched uriMatched = dbTable.checkMatched(uri);
            if (uriMatched != null) {
                return uriMatched;
            }
        }
        return null;
    }
}
