package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class stagedFile implements Serializable {

    public static final File STAGED_DIR = join(GITLET_DIR, ".staged");
    public static final File STAGED = join(STAGED_DIR, "staged");
    public static final File STAGED_FILE_DIR = join(STAGED_DIR, "stagedFile");
    private TreeSet<String> staged;


    public static void init() {
        STAGED_DIR.mkdir();
        STAGED_FILE_DIR.mkdir();
    }

    public stagedFile() {
        staged = new TreeSet<>();
    }

    public static stagedFile fromFile() {
        return readObject(STAGED, stagedFile.class);
    }

    Iterator<String> iterator() {
        return staged.iterator();
    }

    public void saveFile() {
        writeObject(STAGED, this);
    }

    public void put(File srcFile, String file) {
        File desFile = join(STAGED_FILE_DIR, file);
        Repository.createFile(desFile);
        writeContents(desFile, readContents(srcFile));
        staged.add(file);
    }

    public void print() {
        System.out.println("=== Staged Files ===");
        for (String file : staged) {
            System.out.println(file);
        }
        System.out.println();
    }

    public boolean find(String file) {
        return staged.contains(file);
    }

    public void remove(String file) {
        staged.remove(file);
    }

    public boolean isEmpty() {
        return staged.isEmpty();
    }

    public void clear() {
        List<String> stagedLeft = plainFilenamesIn(STAGED_FILE_DIR);
        for (String file : stagedLeft) {
            File desFile = join(STAGED_FILE_DIR, file);
            desFile.delete();
        }
        staged.clear();
    }


}
