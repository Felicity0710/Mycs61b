package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class removal implements Serializable {
    private LinkedList<String> removed;
    public static final File REMOV_DIR = join(GITLET_DIR, ".removal");
    public static final File REMOV_FILE = join(REMOV_DIR, "removal");

    public removal() {
        removed = new LinkedList<>();
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

    public static removal fromFile() {
        return readObject(REMOV_FILE, removal.class);
    }

    public void saveFile() {
        writeObject(REMOV_FILE, this);
    }
}
