package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

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
    private int depth;
    /**
     * The message of this Commit.
     */
    private String message;

    public Commit(String ts, String mes, String p1, String p2, int d) {
        timestamp = ts;
        message = mes;
        parent1 = p1;
        parent2 = p2;
        depth = d;
        files = new HashMap<>();
    }

    public static void init() {
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
    }

    Iterator<String> iterator() {
        return files.keySet().iterator();
    }

    public int depth() {
        return depth;
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

    public void copyParentCommit() {
        Commit p = fromFile(parent1);
        files.putAll(p.files);
        depth = p.depth + 1;
    }

    public void print() {
        if (!parent1.isEmpty() && !parent2.isEmpty()) {
            System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));
        }
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
        if (commitId.length() == 40) {
            File desFile = join(COMMIT_DIR, commitId);
            if (!desFile.exists()) {
                return null;
            }
            return readObject(join(COMMIT_DIR, commitId), Commit.class);
        } else {
            List<String> filesList = plainFilenamesIn(COMMIT_DIR);
            for (String file : filesList) {
                if (file.startsWith(commitId)) {
                    File f = join(COMMIT_DIR, file);
                    return readObject(f, Commit.class);
                }
            }
            return null;
        }
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

    public static String ancestorHelper(String commitId1, String commitId2) {
        Commit commit1 = fromFile(commitId1), commit2 = fromFile(commitId2);
        if (commit1.depth < commit2.depth) {
            Commit tempCommit = commit1;
            commit1 = commit2;
            commit2 = tempCommit;
            String tempString = commitId1;
            commitId1 = commitId2;
            commitId2 = tempString;
        }
        while (commit1.depth > commit2.depth) {
            commitId1 = commit1.parent1;
            commit1 = fromFile(commitId1);
        }
        if (commitId1.equals(commitId2)) {
            return commitId1;
        }
        while (!commit1.parent1.equals(commit2.parent1)) {
            commitId1 = commit1.parent1;
            commitId2 = commit2.parent1;
            commit1 = fromFile(commitId1);
            commit2 = fromFile(commitId2);
        }
        return commit1.parent1;
    }

    public static String findAncestor(String commitId1, String commitId2) {
        Commit commit1 = fromFile(commitId1), commit2 = fromFile(commitId2);
        String latestCommonAncestor = ancestorHelper(commitId1, commitId2);
        if (commitId1.equals(latestCommonAncestor)) {
            return latestCommonAncestor;
        }
        if (!commit1.parent2.isEmpty()) {
            Commit ancestorCommonParent =
                    fromFile(ancestorHelper(commit1.parent1, commit1.parent2));
            String ancestorParent2 = ancestorHelper(commit1.parent2, commitId2);
            Commit commitAncestorParent2 = fromFile(ancestorParent2);
            if (ancestorCommonParent.depth < commitAncestorParent2.depth) {
                return ancestorParent2;
            }
        }
        return latestCommonAncestor;
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return files.entrySet();
    }

}
