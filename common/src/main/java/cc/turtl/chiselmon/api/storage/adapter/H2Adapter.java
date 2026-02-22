package cc.turtl.chiselmon.api.storage.adapter;

import cc.turtl.chiselmon.api.storage.StorageScope;
import org.h2.jdbcx.JdbcDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * H2-backed ScopeStorage. One embedded database file per scope.
 */
public class H2Adapter<T> implements StorageAdapter<T> {

    private final String filename;
    private final Function<Connection, T> factory;
    private final Consumer<T> onSave;
    private final Consumer<T> onClose;

    private H2Adapter(String filename, Function<Connection, T> factory, Consumer<T> onSave, Consumer<T> onClose) {
        this.filename = filename;
        this.factory = factory;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public static <T> H2Adapter<T> of(String filename, Function<Connection, T> factory, Consumer<T> onSave, Consumer<T> onClose) {
        return new H2Adapter<>(filename, factory, onSave, onClose);
    }

    /**
     * For DBs that write in real time save is a no-op, only close matters.
     */
    public static <T> H2Adapter<T> of(String filename, Function<Connection, T> factory, Consumer<T> onClose) {
        return new H2Adapter<>(filename, factory, t -> {
        }, onClose);
    }

    @Override
    public T load(StorageScope scope) {
        try {
            Path file = scope.dataFile(filename);
            Files.createDirectories(file.getParent());

            String dbPath = file.toAbsolutePath().toString().replaceAll("\\.db$", "");

            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:" + dbPath + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

            return factory.apply(ds.getConnection());
        } catch (Exception e) {
            throw new RuntimeException("Failed to open DB '" + filename + "' for " + scope, e);
        }
    }

    @Override
    public void save(StorageScope scope, T data) {
        onSave.accept(data);
    }

    @Override
    public void close(StorageScope scope, T data) {
        onClose.accept(data);
    }
}