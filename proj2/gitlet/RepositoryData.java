package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class RepositoryData implements Serializable {
    private int offsetHours;
    private int offsetMinutes;
    private String HEAD;
    private String CURRENT_BRANCH;
    private HashMap<String, String> branches;
    public static final File DATA_DIR = join(GITLET_DIR, ".data");
    public static final File DATA_FILE = join(DATA_DIR, "data");

    public RepositoryData() {
        offsetHours = 0;
        offsetMinutes = 0;
        HEAD = "";
        CURRENT_BRANCH = "";
        branches = new HashMap<>();
    }

    public int getOffsetHours() {
        return offsetHours;
    }

    public int getOffsetMinutes() {
        return offsetMinutes;
    }

    public String getHead() {
        return HEAD;
    }

    public String getCurrentBranch() {
        return CURRENT_BRANCH;
    }

    public HashMap<String, String> getBranches() {
        return branches;
    }

    public void setOffsetHours(int x) {
        this.offsetHours = x;
    }

    public void setOffsetMinutes(int x) {
        this.offsetMinutes = x;
    }

    public void setHead(String s) {
        HEAD = s;
    }

    public void setCurrentBranch(String s) {
        CURRENT_BRANCH = s;
    }

    public void setBranches(HashMap<String, String> mp) {
        branches = mp;
    }

    public static RepositoryData fromFile() {
        return readObject(DATA_FILE, RepositoryData.class);
    }

    public void saveFile() {
        writeObject(DATA_FILE, this);
    }
}
