package com.thechange.libs.dbtable;

import android.text.TextUtils;

/**
 * 
 * @author giangnguyen
 * 
 */
public class DBColumn {
    
    private StringBuilder mStringBuilder;

	/**
	 * Constructor of DBColumn object.
	 * 
	 * @param name
	 *            column's name
	 * @param type
	 *            database's type
	 * @param extraInfor
	 *            the extra information for the column. e.g.
	 *            <code>PRIMARY KEY AUTOINCREMENT...</code>
	 */
	public DBColumn(String name, DataType type, String extraInfor) {
		mStringBuilder = new StringBuilder();
		mStringBuilder.append(name + " " + type.toString());
		if (!TextUtils.isEmpty(extraInfor)) {
			mStringBuilder.append(" " + extraInfor);
		}
	}

	@Override
	public String toString() {
		return mStringBuilder.toString();
	}
}
