package org.jkiss.dbeaver.ext.gbase8a.internal;

import org.eclipse.core.runtime.Plugin;
import org.jkiss.dbeaver.model.impl.preferences.BundlePreferenceStore;
import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GBase8aActivator extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.jkiss.dbeaver.ext.gbase8a";

    // The shared instance
    private static GBase8aActivator plugin;
    private BundlePreferenceStore preferenceStore;
    // The preferences
    private DBPPreferenceStore preferences;

    /**
     * The constructor
     */
    public GBase8aActivator() {
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        preferences = new BundlePreferenceStore(getBundle());
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static GBase8aActivator getDefault() {
        return plugin;
    }

    public DBPPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            preferenceStore = new BundlePreferenceStore(getBundle());
        }
        return preferenceStore;
    }

}
