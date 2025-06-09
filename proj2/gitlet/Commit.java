package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 * does at a high level.
 *
 * @author Felicity
 */
public class Commit implements Serializable {
    /*
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    public static final File COMMIT_DIR = join(GITLET_DIR, ".commit");
    public static final File BLOB_DIR = join(GITLET_DIR, ".blob");
    private String timestamp;
    private HashMap<String, String> files;
    private String parent1;
    private String parent2;

    /**
     * The message of this Commit.
     */
    private String message;

    public Commit(String ts, String mes, String p1, String p2) {
        timestamp = ts;
        message = mes;
        parent1 = p1;
        parent2 = p2;
        files = new HashMap<>();
    }

    public static void init() {
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
    }

    Iterator<String> iterator() {
        return files.keySet().iterator();
    }

    public String find(String file) {
        return files.getOrDefault(file, null);
    }

    public void put(String file, String hashCode) {
        files.put(file, hashCode);
    }

    public void remove(String file) {
        files.remove(file);
    }

    public void CopyParentCommit() {
        Commit p = fromFile(parent1);
        files.putAll(p.files);
    }

    public boolean hasNext() {
        return !parent1.isEmpty();
    }

    public void print() {
        System.out.println("Date: " + timestamp);
        System.out.println(message);
        System.out.println();
    }

    public String parent1ID() {
        return parent1;
    }

    public String parent2ID() {
        return parent2;
    }

    public String getMessage() {
        return message;
    }

    public static Commit fromFile(String commitId) {
        File desFile = join(COMMIT_DIR, commitId);
        if (!desFile.exists()) {
            return null;
        }
        return readObject(join(COMMIT_DIR, commitId), Commit.class);
    }

    public String saveFile() {
        String hashCode = sha1(serialize(this));
        File desFile = join(COMMIT_DIR, hashCode);
        if (!desFile.exists()) {
            Repository.createFile(desFile);
            writeObject(desFile, this);
        }
        return hashCode;
    }
}
