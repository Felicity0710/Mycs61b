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
    private static final String initError = "A Gitlet version-control system already exists in the current directory.";
    private static final String addError = "File does not exist.";
    private static final String commitNoChangeError = "No changes added to the commit.";
    private static final String commitNoMessageError = "Please enter a commit message";
    private static final String removeError = "No reason to remove the file.";
    private static final String findError = "Found no commit with that message.";
    private static final String checkoutFileNotFoundError = "File does not exist in that commit.";
    private static final String checkoutCommitNotFoundError = "No commit with that id exists";
    private static final String checkoutBranchNotFoundError = "No such branch exists";
    private static final String checkoutCurrentBranchError = "No need to checkout the current branch.";
    private static final String checkoutUntrackedError = "There is an untracked file in the way; delete it, or add and commit it first.";
    private static final String branchError = "A branch with that name already exists.";
    private static final String rmBranchNotExistError = "A branch with that name does not exist.";
    private static final String rmCurrentBranchError = "Cannot remove the current branch.";
    private static final String resetError = "No commit with that id exists.";
    private static final String mergeGivenBranchIsAncestorError = "Given branch is an ancestor of the current branch.";
    private static final String mergeCurrentBranchIsAncestorError = "Current branch fast-forward.";
    private static final String mergeWithStagedOrRemovalError = "You have uncommitted changes.";
    private static final String mergeBranchNotExistError = "A branch with that name does not exist.";
    private static final String mergeItselfError = "Cannot merge a branch with itself.";
    private static final String mergeConflict = "Encountered a merge conflict.";
    public static final String WITHOUT_INIT = "Not in an initialized Gitlet directory.";
    private static final String OPERAND_ERROR = "Incorrect operands.";
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
    private static boolean isChanged;
    private static boolean isConflict;
    private static int offsetHours;
    private static int offsetMinutes;
    private static String HEAD = "";
    private static String CURRENT_BRANCH = "";
    private static String p1 = "";
    private static String p2 = "";
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
        Commit newCommit = new Commit(ts, mes, HEAD, "", 0);
        if (!HEAD.isEmpty()) {
            stagedFile staged = stagedFile.fromFile();
            removal removed = removal.fromFile();
            if (staged.isEmpty() && removed.isEmpty()) {
                System.out.println(commitNoChangeError);
                exit();
            }
            newCommit.copyParentCommit();
            staged.toCommit(newCommit);
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
        isConflict = repoData.getIsConflict();
        offsetHours = repoData.getOffsetHours();
        offsetMinutes = repoData.getOffsetMinutes();
        HEAD = repoData.getHEAD();
        CURRENT_BRANCH = repoData.getCURRENT_BRANCH();
        branches = repoData.getBranches();
        p1 = repoData.getP1();
        p2 = repoData.getP2();
    }

    private static void setData() {
        RepositoryData repoData = RepositoryData.fromFile();
        repoData.setIsConflict(isConflict);
        repoData.setOffsetHours(offsetHours);
        repoData.setOffsetMinutes(offsetMinutes);
        repoData.setHEAD(HEAD);
        repoData.setCURRENT_BRANCH(CURRENT_BRANCH);
        repoData.setBranches(branches);
        repoData.setP1(p1);
        repoData.setP2(p2);
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
                removal removed = removal.fromFile();
                if (removed.find(file)) {
                    removed.remove(file);
                }
            } else {
                if (staged.find(file)) {
                    staged.remove(file);
                }
            }
            staged.saveFile();
        }
    }

    public static void commit(String[] message) {
        if (message.length == 1) {
            System.out.println(commitNoMessageError);
            exit();
        }
        getData();
        if (!isConflict) {
            if (message[1].isEmpty()) {
                System.out.println(commitNoMessageError);
                exit();
            }
            HEAD = makeCommit(getTime(new Date()), message[1]);
            makeBranch(CURRENT_BRANCH, HEAD);
        } else {
            isConflict = false;
            String parent1 = branches.get(p1), parent2 = branches.get(p2);
            Commit newCommit = new Commit(getTime(new Date()),
                    "Merged " + p1 + " into " + p2 + ".",
                    branches.get(p1),
                    branches.get(p2),
                    Commit.fromFile(parent1).depth() + 1
            );
            stagedFile staged = stagedFile.fromFile();
            staged.toCommit(newCommit);
            staged.saveFile();
            HEAD = newCommit.saveFile();
            makeBranch(CURRENT_BRANCH, HEAD);
            branches.remove(p2);
        }
        setData();
    }

    public static void remove(String file) {
        getData();
        File desFile = join(CWD, file);
        stagedFile staged = stagedFile.fromFile();
        removal removed = removal.fromFile();
        Commit currentCommit = Commit.fromFile(HEAD);
        boolean isRemoved = false;
        if (staged.find(file)) {
            isRemoved = true;
            staged.remove(file);
            staged.saveFile();
            removed.put(file);
            removed.saveFile();
        }
        if (currentCommit.find(file) != null) {
            isRemoved = true;
            if (desFile.exists()) {
                desFile.delete();
            }
            removed.put(file);
            removed.saveFile();
        }
        if (!isRemoved) {
            System.out.println(removeError);
        }
    }

    private static String print(String ID) {
        Commit commit = Commit.fromFile(ID);
        System.out.println("===");
        System.out.println("commit " + ID);
        commit.print();
        return commit.parent1ID();
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

        Iterator<String> it2 = removal.fromFile().iterator();
        System.out.println("=== Removed Files ===");
        while (it2.hasNext()) {
            System.out.println(it2.next());
        }
        System.out.println();
        //Modificaitons、untracked not completed

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
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


    /**
     * check untracked files
     * delete files nowTracked but not in commitID
     * overwrite files from commitID
     * clear staged-area
     */
    private static void setFile(String commitID) {
        String currentBranchCommitID = branches.get(CURRENT_BRANCH);
        Commit branchCommit = Commit.fromFile(commitID);
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

        stagedFile staged = stagedFile.fromFile();
        staged.clear();
        staged.saveFile();
    }

    private static void checkoutBranchFile(String branch) {
        String branchCommitID = branches.get(branch);
        setFile(branchCommitID);
        CURRENT_BRANCH = branch;
        HEAD = branchCommitID;
    }

    private static void resetCommit(String commitID) {
        setFile(commitID);
        branches.put(CURRENT_BRANCH, commitID);
        HEAD = commitID;
    }

    public static void checkout(String[] args) {
        getData();
        if (args.length == 3 && args[1].equals("--")) {
            checkoutFile(args[2], HEAD);
        } else if (args.length == 2) {
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
        } else if (args.length == 4 && args[2].equals("--")) {
            Commit desCommit = Commit.fromFile(args[1]);
            if (desCommit == null) {
                System.out.println(checkoutCommitNotFoundError);
                exit();
            } else {
                checkoutFile(args[3], args[1]);
            }
        } else {
            System.out.println(OPERAND_ERROR);
            exit();
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
        getData();
        Commit commit = Commit.fromFile(commitID);
        if (commit == null) {
            System.out.println(resetError);
            exit();
        }
        resetCommit(commitID);
        setData();
    }

    public static void merge(String givenBranch) {
        getData();
        stagedFile staged = stagedFile.fromFile();
        removal removed = removal.fromFile();
        if (!staged.isEmpty() || !removed.isEmpty()) {
            System.out.println(mergeWithStagedOrRemovalError);
            exit();
        }
        if (!branches.containsKey(givenBranch)) {
            System.out.println(mergeBranchNotExistError);
            exit();
        }
        if (CURRENT_BRANCH.equals(givenBranch)) {
            System.out.println(mergeItselfError);
            exit();
        }
        String currentBranchID = branches.get(CURRENT_BRANCH), givenBranchID = branches.get(givenBranch);
        String LCA = Commit.findLCA(currentBranchID, givenBranchID);
        if (LCA.equals(givenBranchID)) {
            System.out.println(mergeGivenBranchIsAncestorError);
            exit();
        }
        if (LCA.equals(currentBranchID)) {
            String currentBranch = CURRENT_BRANCH;
            checkoutBranchFile(givenBranch);
            CURRENT_BRANCH = currentBranch;
            branches.put(CURRENT_BRANCH, HEAD);
            System.out.println(mergeCurrentBranchIsAncestorError);
            setData();
            exit();
        }
        isConflict = false;
        isChanged = false;
        Commit currentBranchCommit = Commit.fromFile(currentBranchID);
        Commit givenBranchCommit = Commit.fromFile(givenBranchID);
        Commit LCACommit = Commit.fromFile(LCA);
        checkUntrack(currentBranchCommit, givenBranchCommit, LCACommit);
        mergeCommit(currentBranchCommit, givenBranchCommit, LCACommit, staged, true);
        mergeCommit(givenBranchCommit, currentBranchCommit, LCACommit, staged, false);
        if (!isChanged) {
            System.out.println(commitNoChangeError);
            exit();
        }
        if (!isConflict) {
            Commit newCommit = new Commit(getTime(new Date()),
                    "Merged " + givenBranch + " into " + CURRENT_BRANCH + ".",
                    currentBranchID,
                    givenBranchID,
                    currentBranchCommit.depth() + 1
            );
            staged.toCommit(newCommit);
            HEAD = newCommit.saveFile();
            branches.put(CURRENT_BRANCH, HEAD);
            branches.remove(givenBranch);
        } else {
            System.out.println(mergeConflict);
            staged.saveFile();
            p1 = CURRENT_BRANCH;
            p2 = givenBranch;
        }
        setData();
    }

    private static void checkUntrack(Commit currentCommit, Commit givenCommit, Commit LCAcommit) {
        for (Map.Entry<String, String> x : givenCommit.entrySet()) {
            String fileName = x.getKey();
            String fileHashCode = x.getValue();
            String fileLCAHashCode = LCAcommit.find(fileName);
            File desFile = join(CWD, fileName);
            if (desFile.exists() && currentCommit.find(fileName) == null && (fileHashCode.equals(fileLCAHashCode) || fileLCAHashCode == null)) {
                System.out.println(checkoutUntrackedError);
                exit();
            }
        }
    }

    private static void mergeCommit(Commit currentCommit, Commit givenCommit, Commit LCAcommit, stagedFile staged, boolean flag) {
        for (Map.Entry<String, String> x : currentCommit.entrySet()) {
            String fileName = x.getKey();
            String fileHashCode = x.getValue();
            String fileGivenHashCode = givenCommit.find(fileName);
            String fileLCAHashCode = LCAcommit.find(fileName);
            if (fileHashCode.equals(fileGivenHashCode)) {
                mergeFile(join(BLOB_DIR, fileHashCode), fileName, staged);
            } else if (fileGivenHashCode != null) {
                if (fileHashCode.equals(fileLCAHashCode)) {
                    isChanged = true;
                    mergeFile(join(BLOB_DIR, fileGivenHashCode), fileName, staged);
                } else if (fileGivenHashCode.equals(fileLCAHashCode)) {
                    mergeFile(join(BLOB_DIR, fileHashCode), fileName, staged);
                } else {
                    isChanged = true;
                    mergeConflictFile(join(BLOB_DIR, fileHashCode),
                            join(BLOB_DIR, fileGivenHashCode),
                            join(CWD, fileName),
                            flag
                    );
                }
            } else {
                if (fileHashCode.equals(fileLCAHashCode)) {
                    if (flag) {
                        isChanged = true;
                    }
                    File desFile = join(CWD, fileName);
                    if (desFile.exists()) {
                        desFile.delete();
                    }
                } else if (fileLCAHashCode != null) {
                    isChanged = true;
                    mergeConflictFile(join(BLOB_DIR, fileHashCode),
                            null,
                            join(CWD, fileName),
                            flag
                    );
                } else {
                    if (!flag) {
                        isChanged = true;
                    }
                    mergeFile(join(BLOB_DIR, fileHashCode), fileName, staged);
                }
            }
        }
    }

    private static void mergeConflictFile(File file1, File file2, File desFile, boolean flag) {
        isConflict = true;
        if (!desFile.exists()) {
            createFile(desFile);
        }
        String file1Content = "";
        String file2Content = "";
        if (file1 != null) {
            file1Content = readContentsAsString(file1);
        }
        if (file2 != null) {
            file2Content = readContentsAsString(file2);
        }
        if (!flag) {
            String t = file1Content;
            file1Content = file2Content;
            file2Content = t;
        }
        writeContents(desFile,
                "<<<<<<< HEAD\n" + file1Content + "=======\n" + file2Content + ">>>>>>>\n"
        );
    }

    private static void mergeFile(File srcFile, String fileName, stagedFile staged) {
        staged.put(srcFile, fileName);
        File CWDFile = join(CWD, fileName);
        if (!CWDFile.exists()) {
            createFile(CWDFile);
        }
        writeContents(CWDFile, readContents(srcFile));
    }
}
