/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.relax;


import android.content.ContentValues;

import com.todoroo.andlib.data.AbstractModel;
import com.todoroo.andlib.data.Property;
import com.todoroo.andlib.data.Table;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.data.Property.IntegerProperty;
import com.todoroo.andlib.data.Property.LongProperty;
import com.todoroo.andlib.data.Property.StringProperty;

/**
 * Data Model which represents an image search result
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
@SuppressWarnings("nls")
public final class UrlEntry extends AbstractModel {

    // --- table and uri

    /** table for this model */
    public static final Table TABLE = new Table("urls", UrlEntry.class);

    // --- properties

    /** ID */
    public static final LongProperty ID = new LongProperty(
            TABLE, ID_PROPERTY_NAME);

    /** url */
    public static final StringProperty URL = new StringProperty(
            TABLE, "url");

    /** type flag */
    public static final IntegerProperty TYPE = new IntegerProperty(
            TABLE, "type");

    /** search result index */
    public static final IntegerProperty RESULT = new IntegerProperty(
            TABLE, "result");

    // ---

    public static final int TYPE_SEARCH_RESULT = 1;

    public static final int TYPE_HISTORY = 2;

    /** List of all properties for this model */
    public static final Property<?>[] PROPERTIES = generateProperties(UrlEntry.class);

    // --- defaults

    /** Default values container */
    private static final ContentValues defaultValues = new ContentValues();

    static {
    }

    @Override
    public ContentValues getDefaultValues() {
        return defaultValues;
    }

    // --- data access boilerplate

    public UrlEntry() {
        super();
    }

    public UrlEntry(TodorooCursor<UrlEntry> cursor) {
        this();
        readPropertiesFromCursor(cursor);
    }

    public void readFromCursor(TodorooCursor<UrlEntry> cursor) {
        super.readPropertiesFromCursor(cursor);
    }

    @Override
    public long getId() {
        return getIdHelper(ID);
    }

    @Override
    protected Creator<? extends AbstractModel> getCreator() {
        return null;
    }

}
