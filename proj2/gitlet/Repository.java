package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Commit.BLOB_DIR;
import static gitlet.Commit.COMMIT_DIR;
import static gitlet.RepositoryData.DATA_DIR;
import static gitlet.RepositoryData.DATA_FILE;
import static gitlet.Utils.*;
import static gitlet.removal.REMOV_DIR;
import static gitlet.removal.REMOV_FILE;
import static gitlet.stagedFile.STAGED;
import static gitlet.stagedFile.STAGED_FILE_DIR;

/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author Felicity
 */
public class Repository {
    /*
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    private static final String initCommitMessage = "initial commit";
    /**
     * command error message
     */
    private static final String initError = "A Gitlet version-control already exists in the current directory.";
    private static final String addError = "File does not exist.";
    private static final String removeError = "No reason to remove the file.";
    private static final String findError = "Found no commit with that message.";
    private static final String checkoutFileNotFoundError = "File does not exist in that commit.";
    private static final String checkoutCommitNotFoundError = "No commit with that id exists";
    private static final String checkoutBranchNotFoundError = "No such branch exists";
    private static final String checkoutCurrentBranchError = "No need to checkout the current branch.";
    private static final String checkoutUntrackedError = "There is an untracked file in the way; delete it, or add and commit it first.";
    private static final String branchError = "A branch with that name already exists.";
    private static final String rmBranchNotExistError = "A branch with that name does not exist.";
    private static final String rmCurrentBranchError = "Cannot remove current branch.";
    private static final String resetError = "No commit with that id exists.";
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * HEAD commit's hashID
     */
    private static int offsetHours;
    private static int offsetMinutes;
    private static String HEAD = "";
    private static String CURRENT_BRANCH = "";
    private static HashMap<String, String> branches;

    private static String getTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        String timeZoneOffset = String.format("%+03d%02d", offsetHours, offsetMinutes);
        return String.format(Locale.ENGLISH, "%ta %<tb %<td %<tT %<tY %s", calendar, timeZoneOffset);
    }

    private static int getTimeOffset() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int offsetMillis = TimeZone.getDefault().getOffset(calendar.getTimeInMillis());
        offsetHours = offsetMillis / (60 * 60 * 1000);
        offsetMinutes = Math.abs(offsetMillis % (60 * 60 * 1000) / (60 * 1000));
        return offsetMillis / (60 * 1000);
    }

    public static String getInitTime() {
        int offsetTotalMinutes = getTimeOffset();
        int year = 1969, month = 11, day = 31;
        int totalMinutes = 24 * 60 + offsetTotalMinutes;
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        day += hour / 24;
        month += day / 32;
        year += month / 12;
        month %= 12;
        day %= 31;
        hour %= 24;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return getTime(calendar.getTime());
    }

    private static void exit() {
        System.exit(0);
    }

    public static void createFile(File newFile) {
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String makeCommit(String ts, String mes) {
        getData();
        Commit newCommit = new Commit(ts, mes, HEAD, "");
        if (!HEAD.isEmpty()) {
            newCommit.CopyParentCommit();

            stagedFile staged = stagedFile.fromFile();
            Iterator<String> it = staged.iterator();
            while (it.hasNext()) {
                String file = it.next();
                File desFile = join(STAGED_FILE_DIR, file);
                byte[] src = readContents(desFile);
                desFile.delete();
                String hashCode = sha1(src);
                File newFile = join(BLOB_DIR, hashCode);
                writeContents(newFile, src);
                newCommit.put(file, hashCode);
            }
            staged.clear();
            staged.saveFile();

            removal removed = removal.fromFile();
            Iterator<String> it2 = removed.iterator();
            while (it2.hasNext()) {
                String file = it2.next();
                if (newCommit.find(file) != null) {
                    newCommit.remove(file);
                }
            }
            removed.clear();
            removed.saveFile();
        }
        return newCommit.saveFile();
    }

    private static void makeBranch(String branch, String hashCode) {
        CURRENT_BRANCH = branch;
        branches.put(branch, hashCode);
    }

    private static void commitInit() {
        Commit.init();
    }

    private static void stagedInit() {
        stagedFile.init();
        createFile(STAGED);
        stagedFile staged = new stagedFile();
        staged.saveFile();
    }

    private static void removalInit() {
        REMOV_DIR.mkdir();
        createFile(REMOV_FILE);
        removal remov = new removal();
        remov.saveFile();
    }

    private static void dataInit() {
        DATA_DIR.mkdir();
        createFile(DATA_FILE);
        RepositoryData repoData = new RepositoryData();
        repoData.saveFile();
    }

    private static void getData() {
        RepositoryData repoData = RepositoryData.fromFile();
        offsetHours = repoData.getOffsetHours();
        offsetMinutes = repoData.getOffsetMinutes();
        HEAD = repoData.getHEAD();
        CURRENT_BRANCH = repoData.getCURRENT_BRANCH();
        branches = repoData.getBranches();
    }

    private static void setData() {
        RepositoryData repoData = RepositoryData.fromFile();
        repoData.setOffsetHours(offsetHours);
        repoData.setOffsetMinutes(offsetMinutes);
        repoData.setHEAD(HEAD);
        repoData.setCURRENT_BRANCH(CURRENT_BRANCH);
        repoData.setBranches(branches);
        repoData.saveFile();
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println(initError);
            exit();
        } else {
            GITLET_DIR.mkdir();
            commitInit();
            stagedInit();
            removalInit();
            dataInit();
            getData();
            String initTime = getInitTime();
            setData();
            HEAD = makeCommit(initTime, initCommitMessage);
            makeBranch("master", HEAD);
            setData();
        }
    }

    public static void add(String file) {
        getData();
        File desFile = join(CWD, file);
        if (!desFile.exists()) {
            System.out.println(addError);
            exit();
        } else {
            stagedFile staged = stagedFile.fromFile();
            Commit currentCommit = Commit.fromFile(HEAD);
            String commitHashCode = currentCommit.find(file);
            String hashCode = sha1(readContents(desFile));
            if (!hashCode.equals(commitHashCode)) {
                staged.put(desFile, file);
            } else {
                if (staged.find(file)) {
                    staged.remove(file);
                }
            }
            staged.saveFile();
        }
    }

    public static void commit(String message) {
        getData();
        HEAD = makeCommit(getTime(new Date()), message);
        makeBranch(CURRENT_BRANCH, HEAD);
        setData();
    }

    public static void remove(String file) {
        getData();
        File desFile = join(CWD, file);
        if (!desFile.exists()) {
            exit();
        }
        stagedFile staged = stagedFile.fromFile();
        boolean isRemoved = false;
        if (staged.find(file)) {
            isRemoved = true;
            staged.remove(file);
            staged.saveFile();
        }
        Commit currentCommit = Commit.fromFile(HEAD);
        if (currentCommit.find(file) != null) {
            isRemoved = true;
            desFile.delete();
            removal remov = removal.fromFile();
            remov.put(file);
            remov.saveFile();
        }
        if (!isRemoved) {
            System.out.println(removeError);
        }
    }

    private static String print(String ID) {
        Commit commit = Commit.fromFile(ID);
        System.out.println("===");
        System.out.println("commit " + ID);
        String p1 = commit.parent1ID(), p2 = commit.parent2ID();
        if (!p1.isEmpty() && !p2.isEmpty()) {
            System.out.println("Merge: " + p1.substring(0, 6) + " " + p2.substring(0, 6));
        }
        commit.print();
        return p1;
    }

    public static void log() {
        getData();
        String nowID = HEAD;
        while (!nowID.isEmpty()) {
            nowID = print(nowID);
        }
    }

    public static void globalLog() {
        getData();
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        for (String ID : commits) {
            print(ID);
        }
    }

    public static void find(String message) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        boolean found = false;
        for (String ID : commits) {
            String mes = Commit.fromFile(ID).getMessage();
            if (mes.equals(message)) {
                found = true;
                System.out.println(ID);
            }
        }
        if (!found) {
            System.out.println(findError);
        }
    }

    public static void status() {
        getData();
        System.out.println("=== Branches ===");
        System.out.println("*" + CURRENT_BRANCH);
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            if (!entry.getKey().equals(CURRENT_BRANCH)) {
                System.out.println(entry.getKey());
            }
        }
        System.out.println();

        stagedFile.fromFile().print();

        removal removed = removal.fromFile();
        Iterator<String> it2 = removal.fromFile().iterator();
        System.out.println("=== Removed Files ===");
        while (it2.hasNext()) {
            System.out.println(it2.next());
        }
        System.out.println();
        //Modificaitons、untracked not completed
    }

    private static void checkoutFile(String file, String commitID) {
        Commit currentCommit = Commit.fromFile(commitID);
        String hashCode = currentCommit.find(file);
        if (hashCode == null) {
            System.out.println(checkoutFileNotFoundError);
            exit();
        } else {
            File desFile = join(CWD, file);
            if (!desFile.exists()) {
                createFile(desFile);
            }
            File srcFile = join(BLOB_DIR, hashCode);
            writeContents(desFile, readContents(srcFile));
        }
    }

    private static void checkoutBranchFile(String branch) {
        String branchCommitID = branches.get(branch);
        String currentBranchCommitID = branches.get(CURRENT_BRANCH);
        Commit branchCommit = Commit.fromFile(branchCommitID);
        Commit currentBranchCommit = Commit.fromFile(currentBranchCommitID);
        Iterator<String> it = branchCommit.iterator();
        while (it.hasNext()) {
            String file = it.next();
            File desFile = join(CWD, file);
            if (desFile.exists() && currentBranchCommit.find(file) == null) {
                System.out.println(checkoutUntrackedError);
                exit();
            }
        }

        it = currentBranchCommit.iterator();
        while (it.hasNext()) {
            String file = it.next();
            if (branchCommit.find(file) == null) {
                File desFile = join(CWD, file);
                desFile.delete();
            }
        }

        it = branchCommit.iterator();
        while (it.hasNext()) {
            String file = it.next();
            String hashCode = branchCommit.find(file);
            File srcFile = join(BLOB_DIR, hashCode);
            File desFile = join(CWD, file);
            if (!desFile.exists()) {
                createFile(desFile);
            }
            writeContents(desFile, readContents(srcFile));
        }

        CURRENT_BRANCH = branch;
        HEAD = branchCommitID;

        stagedFile staged = stagedFile.fromFile();
        staged.clear();
        staged.saveFile();
    }

    public static void checkout(String[] args) {
        getData();
        if (args[1].equals("--")) {
            checkoutFile(args[2], HEAD);
        } else if (args.length == 4) {
            Commit desCommit = Commit.fromFile(args[1]);
            if (desCommit == null) {
                System.out.println(checkoutCommitNotFoundError);
                exit();
            } else {
                checkoutFile(args[3], args[1]);
            }
        } else {
            String checkoutBranch = args[1];
            if (!branches.containsKey(checkoutBranch)) {
                System.out.println(checkoutBranchNotFoundError);
                exit();
            } else if (checkoutBranch.equals(CURRENT_BRANCH)) {
                System.out.println(checkoutCurrentBranchError);
                exit();
            } else {
                checkoutBranchFile(args[1]);
            }
        }
        setData();
    }

    public static void branch(String branchName) {
        getData();
        if (branches.containsKey(branchName)) {
            System.out.println(branchError);
            exit();
        }
        branches.put(branchName, HEAD);
        setData();
    }

    public static void rmBranch(String branchName) {
        getData();
        if (!branches.containsKey(branchName)) {
            System.out.println(rmBranchNotExistError);
            exit();
        } else if (CURRENT_BRANCH.equals(branchName)) {
            System.out.println(rmCurrentBranchError);
            exit();
        }
        branches.remove(branchName);
        setData();
    }

    public static void reset(String commitID) {
        Commit commit = Commit.fromFile(commitID);
        if (commit == null) {
            System.out.println(resetError);
            exit();
        }

    }

}
