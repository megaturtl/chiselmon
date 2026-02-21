package cc.turtl.chiselmon.data.storage;

import cc.turtl.chiselmon.data.Scope;
import org.h2.jdbcx.JdbcDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * H2-backed ScopeStorage. One embedded database file per scope.
 */
public class SQLiteStorage<T> implements ScopeStorage<T> {

    private final String filename;
    private final Function<Connection, T> factory;
    private final Consumer<T> onSave;
    private final Consumer<T> onClose;

    private SQLiteStorage(String filename, Function<Connection, T> factory, Consumer<T> onSave, Consumer<T> onClose) {
        this.filename = filename;
        this.factory = factory;
        this.onSave = onSave;
        this.onClose = onClose;
    }

    public static <T> SQLiteStorage<T> of(String filename, Function<Connection, T> factory, Consumer<T> onSave, Consumer<T> onClose) {
        return new SQLiteStorage<>(filename, factory, onSave, onClose);
    }

    /**
     * For DBs that write in real time save is a no-op, only close matters.
     */
    public static <T> SQLiteStorage<T> of(String filename, Function<Connection, T> factory, Consumer<T> onClose) {
        return new SQLiteStorage<>(filename, factory, t -> {
        }, onClose);
    }

    @Override
    public T load(Scope scope) {
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
    public void save(Scope scope, T data) {
        onSave.accept(data);
    }

    @Override
    public void close(Scope scope, T data) {
        onClose.accept(data);
    }
}