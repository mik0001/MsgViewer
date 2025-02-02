package at.redeye.FrameWork.base;

import at.redeye.FrameWork.Plugin.Plugin;
import at.redeye.FrameWork.base.dll_cache.DLLCache;
import at.redeye.FrameWork.base.dll_cache.DLLExtractor;
import at.redeye.FrameWork.base.translation.MLHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.AccessControlException;
import java.util.*;

public class LocalRoot extends Root {

    private static final Logger logger = LogManager.getLogger(LocalRoot.class);
    private final LocalSetup setup;
    private final DLLCache dll_cache;
    private final Collection<BaseDialogBase> dialogs = new Vector<>();
    private DBConnection db_connection;
    private boolean appExitAllowed = true;
    private final Map<String, Plugin> plugins = new HashMap<>();
    private MLHelper ml_helper;

    public LocalRoot(String app_name) {
        this(app_name, app_name);
    }

    public LocalRoot(String app_name, String title) {
        super(app_name, title);

        dll_cache = new DLLCache(this);
        setup = new LocalSetup(this, this.app_name);
    }

    @Override
    public Setup getSetup() {
        return setup;
    }

    @Override
    public boolean saveSetup() {

        if (ml_helper != null)
            ml_helper.saveMissingProps();

        if (setup.saveProps())
            return setup.saveGlobalProps();

        return false;
    }

    @Override
    public void setDBConnection(DBConnection con) {
        if (db_connection != null)
            db_connection.close();

        db_connection = con;
    }

    @Override
    public DBConnection getDBConnection() {
        return db_connection;
    }

    private void closeDBConnection() {
        setDBConnection(null);
    }


    @Override
    public void informWindowOpened(BaseDialogBase dlg) {
        dialogs.add(dlg);
    }

    @Override
    public void informWindowClosed(BaseDialogBase dlg) {
        dialogs.remove(dlg);

        if (dialogs.isEmpty() && appExitAllowed) {
            System.out.println("All Windows closed, normal exit");
            appExit();
        }
    }

    @Override
    public void closeAllWindowsNoAppExit() {
        appExitAllowed = false;
        closeAllWindowsExceptThisOne(null);
        appExitAllowed = true;
    }

    @Override
    public void closeAllWindowsExceptThisOne(BaseDialogBase dlg) {
        List.copyOf(dialogs).stream()
                .filter(frame -> frame != dlg)
                .forEach(BaseDialogBase::closeNoAppExit);
    }

    @Override
    public void appExit() {
        saveSetup();
        closeDBConnection();
        System.exit(0);
    }

    @Override
    public void addDllExtractorToCache(DLLExtractor extractor) {
        dll_cache.addDllExtractor(extractor);
        dll_cache.initEnv();
    }

    @Override
    public void registerPlugin(Plugin plugin) {
        if (plugins.containsKey(plugin.getName()))
            return;

        if (plugin.isAvailable()) {
            try {
                plugin.initPlugin(this);
                plugins.put(plugin.getName(), plugin);
            } catch (AccessControlException ex) {
                logger.error(ex, ex);
            }
        }
    }

    @Override
    public List<Plugin> getRegisteredPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public Plugin getPlugin(String name) {
        Plugin plugin = plugins.get(name);
        return plugin != null && plugin.isAvailable() ? plugin : null;
    }

    @Override
    public String MlM(String message) {
        if (ml_helper == null)
            ml_helper = new MLHelper(this);

        return ml_helper.MlM(message);
    }

    /**
     * load a MlM file for a spacific class
     *
     * @param impl_locale the locale the class was originaly implemented
     *                    eg "de" for german
     */
    @Override
    public void loadMlM4Class(Object obj, String impl_locale) {
        if (ml_helper == null)
            ml_helper = new MLHelper(this);


        ml_helper.autoLoadFile4Class(obj, getDisplayLanguage(), impl_locale);
    }

    /**
     * load a MlM file for a spacific class
     *
     * @param impl_locale the locale the class was originaly implemented
     *                    eg "de" for german
     */
    @Override
    public void loadMlM4ClassName(String name, String impl_locale) {
        if (ml_helper == null)
            ml_helper = new MLHelper(this);


        ml_helper.autoLoadFile4ClassName(name, getDisplayLanguage(), impl_locale);
    }

    @Override
    public void setLanguageTranslationResourcePath(String path) {
        super.setLanguageTranslationResourcePath(path);

        if (ml_helper == null)
            ml_helper = new MLHelper(this);

        ml_helper.autoLoadCurrentLocale();
    }

}
