package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class RepositoryData implements Serializable {
    private boolean isConflict;
    private int offsetHours;
    private int offsetMinutes;
    private String HEAD;
    private String CURRENT_BRANCH;
    private String p1;
    private String p2;
    private HashMap<String, String> branches;
    public static final File DATA_DIR = join(GITLET_DIR, ".data");
    public static final File DATA_FILE = join(DATA_DIR, "data");

    public RepositoryData() {
        isConflict = false;
        offsetHours = 0;
        offsetMinutes = 0;
        HEAD = "";
        CURRENT_BRANCH = "";
        p1 = "";
        p2 = "";
        branches = new HashMap<>();
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP1(String x) {
        p1 = x;
    }

    public void setP2(String x) {
        p2 = x;
    }

    public boolean getIsConflict() {
        return isConflict;
    }

    public void setIsConflict(boolean x) {
        isConflict = x;
    }

    public int getOffsetHours() {
        return offsetHours;
    }

    public int getOffsetMinutes() {
        return offsetMinutes;
    }

    public String getHEAD() {
        return HEAD;
    }

    public String getCURRENT_BRANCH() {
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

    public void setHEAD(String s) {
        HEAD = s;
    }

    public void setCURRENT_BRANCH(String s) {
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
