package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Removal implements Serializable {
    private TreeSet<String> removed;
    public static final File REMOV_DIR = join(GITLET_DIR, ".removal");
    public static final File REMOV_FILE = join(REMOV_DIR, "removal");

    public void remove(String file) {
        removed.remove(file);
    }

    public boolean find(String file) {
        return removed.contains(file);
    }

    public Removal() {
        removed = new TreeSet<>();
    }

    Iterator<String> iterator() {
        return removed.iterator();
    }

    public void put(String file) {
        removed.add(file);
    }

    public void clear() {
        removed.clear();
    }

    public boolean isEmpty() {
        return removed.isEmpty();
    }

    public static Removal fromFile() {
        return readObject(REMOV_FILE, Removal.class);
    }

    public void saveFile() {
        writeObject(REMOV_FILE, this);
    }
}
