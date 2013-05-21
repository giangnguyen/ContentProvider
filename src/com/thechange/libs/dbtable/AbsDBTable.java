package com.thechange.libs.dbtable;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;

/**
 * Abstract class that present for database table
 * 
 * @author giangnguyen
 * 
 */
public abstract class AbsDBTable {
    
    public static final String _ID           = "_id";
    
    private static final int   SINGLE_ITEM   = 1;
    
    private static final int   MULTIPLE_ITEM = 2;
    
    private final UriMatcher   sUriMatcher;
    
    /**
     * Constructor of {@link AbsDBTable}
     * 
     * @param authority
     *            the <b>authority</b> of {@link ContentProvider}
     */
    public AbsDBTable(String authority) {
        final String tableName = getTableName();
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(authority, tableName, MULTIPLE_ITEM);
        sUriMatcher.addURI(authority, tableName + "/#", SINGLE_ITEM);
    }
    
    /**
     * Get the create table query follow SQL syntax
     * 
     * @return
     */
    public String getCreateScript() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE " + getTableName() + " (");
        final List<DBColumn> columns = getColumns();
        columns.add(0, new DBColumn(_ID, DataType.INTEGER,
                "PRIMARY KEY AUTOINCREMENT"));
        for (int i = 0, count = columns.size(); i < count; i++) {
            builder.append(columns.get(i));
            if (i < count - 1) {
                builder.append(",");
            }
        }
        builder.append(");");
        return builder.toString();
    }
    
    /**
     * Check if the {@link Uri} match to this table define.
     * 
     * @param uri
     *            the {@link Uri} object to check
     * @return {@link UriMatched} object present for this matched
     */
    public UriMatched checkMatched(Uri uri) {
        final String tableName = getTableName();
        switch (sUriMatcher.match(uri)) {
        case SINGLE_ITEM:
            final long id = ContentUris.parseId(uri);
            return new UriMatched(id, tableName);
        case MULTIPLE_ITEM:
            return new UriMatched(UriMatched.UNKNOW_ID, tableName);
        default:
            return null;
        }
    }
    
    /**
     * Get the database table's name
     * 
     * @return database table's name
     */
    public abstract String getTableName();
    
    /**
     * Get list of {@link DBColumn} of this table
     * 
     * @return list of {@link DBColumn} of this table
     */
    public abstract List<DBColumn> getColumns();
}
