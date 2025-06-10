package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Commit.BLOB_DIR;
import static gitlet.Commit.COMMIT_DIR;
import static gitlet.Removal.REMOV_DIR;
import static gitlet.Removal.REMOV_FILE;
import static gitlet.RepositoryData.DATA_DIR;
import static gitlet.RepositoryData.DATA_FILE;
import static gitlet.StagedFile.STAGED;
import static gitlet.Utils.*;

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
    private static final String INIT_COMMIT_MESSAGE =
            "initial commit";
    /**
     * command error message
     */
    private static final String INIT_ERROR =
            "A Gitlet version-control system already exists in the current directory.";
    private static final String ADD_ERROR =
            "File does not exist.";
    private static final String COMMIT_NO_CHANGE_ERROR =
            "No changes added to the commit.";
    private static final String COMMIT_NO_MESSAGE_ERROR =
            "Please enter a commit message";
    private static final String REMOVE_ERROR =
            "No reason to remove the file.";
    private static final String FIND_ERROR =
            "Found no commit with that message.";
    private static final String CHECKOUT_FILE_NOT_FOUND_ERROR =
            "File does not exist in that commit.";
    private static final String CHECKOUT_COMMIT_NOT_FOUND_ERROR =
            "No commit with that id exists";
    private static final String CHECKOUT_BRANCH_NOT_FOUND_ERROR =
            "No such branch exists";
    private static final String CHECKOUT_CURRENT_BRANCH_ERROR =
            "No need to checkout the current branch.";
    private static final String CHECKOUT_UNTRACKED_ERROR =
            "There is an untracked file in the way; delete it, or add and commit it first.";
    private static final String BRANCH_ERROR =
            "A branch with that name already exists.";
    private static final String RM_BRANCH_NOT_EXIST_ERROR =
            "A branch with that name does not exist.";
    private static final String RM_CURRENT_BRANCH_ERROR =
            "Cannot remove the current branch.";
    private static final String RESET_ERROR =
            "No commit with that id exists.";
    private static final String MERGE_GIVEN_BRANCH_IS_ANCESTOR_ERROR =
            "Given branch is an ancestor of the current branch.";
    private static final String MERGE_CURRENT_BRANCH_IS_ANCESTOR_ERROR =
            "Current branch fast-forwarded.";
    private static final String MERGE_WITH_STAGED_OR_REMOVAL_ERROR =
            "You have uncommitted changes.";
    private static final String MERGE_BRANCH_NOT_EXIST_ERROR =
            "A branch with that name does not exist.";
    private static final String MERGE_ITSELF_ERROR =
            "Cannot merge a branch with itself.";
    private static final String MERGE_CONFLICT =
            "Encountered a merge conflict.";
    public static final String WITHOUT_INIT =
            "Not in an initialized Gitlet directory.";
    private static final String OPERAND_ERROR =
            "Incorrect operands.";
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
    private static HashMap<String, String> branches;

    private static String getTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        String timeZoneOffset = String.format("%+03d%02d",
                offsetHours,
                offsetMinutes);
        return String.format(Locale.ENGLISH,
                "%ta %<tb %<td %<tT %<tY %s",
                calendar,
                timeZoneOffset);
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
            StagedFile staged = StagedFile.fromFile();
            Removal removed = Removal.fromFile();
            if (staged.isEmpty() && removed.isEmpty()) {
                System.out.println(COMMIT_NO_CHANGE_ERROR);
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
        StagedFile.init();
        createFile(STAGED);
        StagedFile staged = new StagedFile();
        staged.saveFile();
    }

    private static void removalInit() {
        REMOV_DIR.mkdir();
        createFile(REMOV_FILE);
        Removal remov = new Removal();
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
        HEAD = repoData.getHead();
        CURRENT_BRANCH = repoData.getCurrentBranch();
        branches = repoData.getBranches();
    }

    private static void setData() {
        RepositoryData repoData = RepositoryData.fromFile();
        repoData.setOffsetHours(offsetHours);
        repoData.setOffsetMinutes(offsetMinutes);
        repoData.setHead(HEAD);
        repoData.setCurrentBranch(CURRENT_BRANCH);
        repoData.setBranches(branches);
        repoData.saveFile();
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println(INIT_ERROR);
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
            HEAD = makeCommit(initTime, INIT_COMMIT_MESSAGE);
            makeBranch("master", HEAD);
            setData();
        }
    }

    public static void add(String file) {
        getData();
        File desFile = join(CWD, file);
        if (!desFile.exists()) {
            System.out.println(ADD_ERROR);
            exit();
        } else {
            StagedFile staged = StagedFile.fromFile();
            Commit currentCommit = Commit.fromFile(HEAD);
            String commitHashCode = currentCommit.find(file);
            String hashCode = sha1(readContents(desFile));
            Removal removed = Removal.fromFile();
            if (removed.find(file)) {
                removed.remove(file);
                removed.saveFile();
            } else if (!hashCode.equals(commitHashCode)) {
                staged.put(desFile, file);
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
            System.out.println(COMMIT_NO_MESSAGE_ERROR);
            exit();
        }
        getData();
        if (message[1].isEmpty()) {
            System.out.println(COMMIT_NO_MESSAGE_ERROR);
            exit();
        }
        HEAD = makeCommit(getTime(new Date()), message[1]);
        makeBranch(CURRENT_BRANCH, HEAD);
        setData();
    }

    public static void remove(String file) {
        getData();
        File desFile = join(CWD, file);
        StagedFile staged = StagedFile.fromFile();
        Removal removed = Removal.fromFile();
        Commit currentCommit = Commit.fromFile(HEAD);
        boolean isRemoved = false;
        if (staged.find(file)) {
            isRemoved = true;
            staged.remove(file);
            staged.saveFile();
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
            System.out.println(REMOVE_ERROR);
        }
    }

    private static String print(String id) {
        Commit commit = Commit.fromFile(id);
        System.out.println("===");
        System.out.println("commit " + id);
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
            System.out.println(FIND_ERROR);
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

        StagedFile.fromFile().print();

        Iterator<String> it2 = Removal.fromFile().iterator();
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

    private static void checkoutFile(String file, String commitId) {
        Commit currentCommit = Commit.fromFile(commitId);
        String hashCode = currentCommit.find(file);
        if (hashCode == null) {
            System.out.println(CHECKOUT_FILE_NOT_FOUND_ERROR);
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
                System.out.println(CHECKOUT_UNTRACKED_ERROR);
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

        StagedFile staged = StagedFile.fromFile();
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
                System.out.println(CHECKOUT_BRANCH_NOT_FOUND_ERROR);
                exit();
            } else if (checkoutBranch.equals(CURRENT_BRANCH)) {
                System.out.println(CHECKOUT_CURRENT_BRANCH_ERROR);
                exit();
            } else {
                checkoutBranchFile(args[1]);
            }
        } else if (args.length == 4 && args[2].equals("--")) {
            Commit desCommit = Commit.fromFile(args[1]);
            if (desCommit == null) {
                System.out.println(CHECKOUT_COMMIT_NOT_FOUND_ERROR);
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
            System.out.println(BRANCH_ERROR);
            exit();
        }
        branches.put(branchName, HEAD);
        setData();
    }

    public static void rmBranch(String branchName) {
        getData();
        if (!branches.containsKey(branchName)) {
            System.out.println(RM_BRANCH_NOT_EXIST_ERROR);
            exit();
        } else if (CURRENT_BRANCH.equals(branchName)) {
            System.out.println(RM_CURRENT_BRANCH_ERROR);
            exit();
        }
        branches.remove(branchName);
        setData();
    }

    public static void reset(String commitID) {
        getData();
        Commit commit = Commit.fromFile(commitID);
        if (commit == null) {
            System.out.println(RESET_ERROR);
            exit();
        }
        resetCommit(commitID);
        setData();
    }

    public static void merge(String givenBranch) {
        getData();
        StagedFile staged = StagedFile.fromFile();
        Removal removed = Removal.fromFile();
        if (!staged.isEmpty() || !removed.isEmpty()) {
            System.out.println(MERGE_WITH_STAGED_OR_REMOVAL_ERROR);
            exit();
        }
        if (!branches.containsKey(givenBranch)) {
            System.out.println(MERGE_BRANCH_NOT_EXIST_ERROR);
            exit();
        }
        if (CURRENT_BRANCH.equals(givenBranch)) {
            System.out.println(MERGE_ITSELF_ERROR);
            exit();
        }
        String currentBranchId = branches.get(CURRENT_BRANCH);
        String givenBranchId = branches.get(givenBranch);
        String ancestorId = Commit.findAncestor(currentBranchId, givenBranchId);
        if (ancestorId.equals(givenBranchId)) {
            System.out.println(MERGE_GIVEN_BRANCH_IS_ANCESTOR_ERROR);
            exit();
        }
        if (ancestorId.equals(currentBranchId)) {
            String currentBranch = CURRENT_BRANCH;
            checkoutBranchFile(givenBranch);
            CURRENT_BRANCH = currentBranch;
            branches.put(CURRENT_BRANCH, HEAD);
            System.out.println(MERGE_CURRENT_BRANCH_IS_ANCESTOR_ERROR);
            setData();
            exit();
        }
        isConflict = false;
        isChanged = false;
        Commit currentBranchCommit = Commit.fromFile(currentBranchId);
        Commit givenBranchCommit = Commit.fromFile(givenBranchId);
        Commit AncestorCommit = Commit.fromFile(ancestorId);
        checkUntrack(currentBranchCommit, givenBranchCommit, AncestorCommit);
        mergeCommit(currentBranchCommit,
                givenBranchCommit,
                AncestorCommit,
                staged,
                true);
        mergeCommit(givenBranchCommit,
                currentBranchCommit,
                AncestorCommit,
                staged,
                false);
        if (!isChanged) {
            System.out.println(COMMIT_NO_CHANGE_ERROR);
            exit();
        }
        Commit newCommit = new Commit(getTime(new Date()),
                "Merged " + givenBranch + " into " + CURRENT_BRANCH + ".",
                currentBranchId,
                givenBranchId,
                currentBranchCommit.depth() + 1
        );
        staged.toCommit(newCommit);
        HEAD = newCommit.saveFile();
        branches.put(CURRENT_BRANCH, HEAD);
        branches.put(givenBranch, HEAD);

        if (isConflict) {
            System.out.println(MERGE_CONFLICT);
        }
        isConflict = false;

        setData();
    }

    private static void checkUntrack(Commit currentCommit, Commit givenCommit, Commit LCAcommit) {
        for (Map.Entry<String, String> x : givenCommit.entrySet()) {
            String fileName = x.getKey();
            String fileHashCode = x.getValue();
            String fileLCAHashCode = LCAcommit.find(fileName);
            File desFile = join(CWD, fileName);
            if (desFile.exists() && currentCommit.find(fileName) == null && (fileHashCode.equals(fileLCAHashCode) || fileLCAHashCode == null)) {
                System.out.println(CHECKOUT_UNTRACKED_ERROR);
                exit();
            }
        }
    }

    private static void mergeCommit(Commit currentCommit, Commit givenCommit, Commit LCAcommit, StagedFile staged, boolean flag) {
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
                    File desFile = join(CWD, fileName);
                    mergeConflictFile(join(BLOB_DIR, fileHashCode),
                            join(BLOB_DIR, fileGivenHashCode),
                            desFile,
                            flag
                    );
                    staged.put(desFile, fileName);
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
                    File desFile = join(CWD, fileName);
                    mergeConflictFile(join(BLOB_DIR, fileHashCode),
                            null,
                            desFile,
                            flag
                    );
                    staged.put(desFile, fileName);
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

    private static void mergeFile(File srcFile, String fileName, StagedFile staged) {
        staged.put(srcFile, fileName);
        File WorkingFile = join(CWD, fileName);
        if (!WorkingFile.exists()) {
            createFile(WorkingFile);
        }
        writeContents(WorkingFile, readContents(srcFile));
    }
}
