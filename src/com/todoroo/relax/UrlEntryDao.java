/*
 * Copyright (c) 2009, Todoroo Inc
 * All Rights Reserved
 * http://www.todoroo.com
 */
package com.todoroo.relax;

import android.util.Log;

import com.todoroo.andlib.data.DatabaseDao;
import com.todoroo.andlib.data.TodorooCursor;
import com.todoroo.andlib.service.Autowired;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Functions;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.data.Task;

/**
 * Data Access layer for {@link Task}-related operations.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class UrlEntryDao extends DatabaseDao<UrlEntry> {

    private static final Criterion TYPE_SEARCH_RESULT = UrlEntry.TYPE.eq(UrlEntry.TYPE_SEARCH_RESULT);
    @Autowired
    private Database database;

	public UrlEntryDao() {
        super(UrlEntry.class);
        DependencyInjectionService.getInstance().inject(this);
        setDatabase(database);
    }

	/** if necessary, read results from the database. is run on separate thread */
	public void initialize(String query) {
	    ImageSource imageSource = new ImageSource();

	    TodorooCursor<UrlEntry> cursor = query(Query.select(Functions.max(UrlEntry.RESULT))
	            .where(TYPE_SEARCH_RESULT));
	    int current = -1;
	    try {
	        cursor.moveToFirst();
	        if(!cursor.isAfterLast())
	            current = cursor.getInt(0);
	    } finally {
	        cursor.close();
	    }
	    current++;

	    UrlEntry searchResult = new UrlEntry();
	    searchResult.setValue(UrlEntry.TYPE, UrlEntry.TYPE_SEARCH_RESULT);
	    while(imageSource.hasMoreResults(current)) {
	        try {
                String[] results = imageSource.search(query, current);
                if(results.length == 0)
                    break;

                for(String url : results) {
                    searchResult.setValue(UrlEntry.URL, url);
                    searchResult.setValue(UrlEntry.RESULT, current++);
                    createNew(searchResult);
                }
            } catch (Exception e) {
                Log.e("relaxomator", "Search Error", e);
            }
	    }
	}

	/** Call this when changing search queries */
	public void clearSearchResults() {
	    deleteWhere(TYPE_SEARCH_RESULT);
	}
}

