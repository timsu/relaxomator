/**
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.relax;

import com.todoroo.andlib.service.AbstractDependencyInjector;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.service.HttpRestClient;
import com.todoroo.andlib.service.ExceptionService.AndroidLogReporter;
import com.todoroo.andlib.service.ExceptionService.ErrorReporter;

/**
 * Astrid application dependency injector loads classes in Astrid with the
 * appropriate instantiated objects necessary for their operation. For
 * more information on Dependency Injection, see {@link DependencyInjectionService}
 * and {@link AbstractDependencyInjector}.
 *
 * @author Tim Su <tim@todoroo.com>
 *
 */
public class RelaxDependencyInjector extends AbstractDependencyInjector {

    /**
     * Boolean bit to prevent multiple copies of this injector to be loaded
     */
    private static RelaxDependencyInjector instance = null;

    /**
     * Initialize list of injectables. Special care must used when
     * instantiating classes that themselves depend on dependency injection
     * (i.e. {@link ErrorReporter}.
     */
    @Override
    @SuppressWarnings("nls")
    protected void addInjectables() {
        // com.todoroo.android.service
        injectables.put("applicationName", "relax");
        injectables.put("restClient", HttpRestClient.class);

        // com.todoroo.astrid.dao
        injectables.put("database", Database.class);
        injectables.put("searchResultDao", UrlEntryDao.class);

        // these make reference to fields defined above
        injectables.put("errorReporters", new ErrorReporter[] {
                new AndroidLogReporter(),
        });
    }

    /**
     * Install this service as the default Dependency Injector
     */
    public static void initialize() {
        if(instance != null)
            return;
        synchronized(RelaxDependencyInjector.class) {
            if(instance == null)
                instance = new RelaxDependencyInjector();
            DependencyInjectionService.getInstance().addInjector(instance);
        }
    }

    RelaxDependencyInjector() {
        // prevent instantiation
        super();
    }

    /**
     * Flush dependency injection cache. Useful for unit tests.
     */
    public synchronized static void flush() {
        instance.flushCreated();
    }
}
