package capers;

import java.io.File;
import java.io.IOException;

import static capers.Utils.*;

/**
 * A repository for Capers
 *
 * @author The structure of a Capers Repository is as follows:
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 * - dogs/ -- folder containing all of the persistent data for dogs
 * - story -- file containing the current story
 */
public class CapersRepository {
    /**
     * Current Working Directory.
     */
    static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * Main metadata folder.
     */
    static final File CAPERS_FOLDER = join(CWD, ".capers");

    /**
     * Does required filesystem operations to allow for persistence.
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     * - dogs/ -- folder containing all of the persistent data for dogs
     * - story -- file containing the current story
     */
    public static void setupPersistence() {
        if (!CAPERS_FOLDER.exists()) {
            CAPERS_FOLDER.mkdir();
        }
        File STORY = join(CAPERS_FOLDER, "story");
        if (!STORY.exists()) {
            try {
                STORY.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File DOGS = join(CAPERS_FOLDER, "dogs");
        if (!DOGS.exists()) {
            DOGS.mkdir();
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     *
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        File STORY = join(CAPERS_FOLDER, "story");
        String writeString = readContentsAsString(STORY);
        if (writeString.isEmpty()) {
            writeString = text;
        } else {
            writeString = writeString + "\n" + text;
        }
        writeContents(STORY, writeString);
        System.out.println(writeString);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Dog dogObj = new Dog(name, breed, age);
        dogObj.saveDog();
        System.out.println(dogObj);
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     *
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        Dog dogObj = Dog.fromFile(name);
        if (dogObj != null) {
            dogObj.haveBirthday();
            dogObj.saveDog();
        }
    }
}
