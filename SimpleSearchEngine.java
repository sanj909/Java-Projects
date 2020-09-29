package search;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// We follow the Strategy pattern
interface searchStrategy {
    void search(String[] queries, Map<String, ArrayList<Integer>> invIndex, String[] people);
}

// Prints lines containing all words from the query
class searchAll implements searchStrategy {
    @Override
    public void search(String[] queries, Map<String, ArrayList<Integer>> invIndex, String[] people) {
        ArrayList<Integer> linesToPrint = new ArrayList<>();
        for (String query : queries) {
            linesToPrint.retainAll(invIndex.getOrDefault(query.toLowerCase(), new ArrayList<>()));
        }
        System.out.println(linesToPrint.size() > 0 ? "Found people:" : "No matching people found.");
        for (Integer i : linesToPrint) {
            System.out.println(people[i]);
        }
    }
}

// Prints lines containing at least one word from the query
class searchAny implements searchStrategy {
    @Override
    public void search(String[] queries, Map<String, ArrayList<Integer>> invIndex, String[] people) {
        Set<Integer> linesPrinted  = new HashSet<>();
        for (String query : queries) {
            if (invIndex.containsKey(query.toLowerCase())) {
                System.out.println("Found people:");
                for (Integer i : invIndex.get(query.toLowerCase())) {
                    System.out.print((linesPrinted.contains(i)) ? "" : people[i] + "\n");
                    linesPrinted.add(i);
                }
            }
        }
        System.out.print(linesPrinted.size() > 0 ? "No matching people found." + "\n" : "");
    }
}

// Prints lines which do not contain any words from the query
class searchNone implements searchStrategy {
    @Override
    public void search(String[] queries, Map<String, ArrayList<Integer>> invIndex, String[] people) {
        ArrayList<Integer> linesToNotPrint  = new ArrayList<>();
        for (String query : queries) {
            linesToNotPrint.addAll(invIndex.get(query.toLowerCase()));
        }
        System.out.println(linesToNotPrint.size() > 0 ? "Found people:" : "No matching people found.");
        for (int i = 0; i < people.length; i++) {
            System.out.print(linesToNotPrint.contains(i) ? "" : people[i] + "\n");
        }
    }
}

// Context
class setStrategy {
    private final searchStrategy strategy;

    public setStrategy(searchStrategy strategy) {
        this.strategy = strategy;
    }

    public void search(String[] queries, Map<String, ArrayList<Integer>> invIndex, String[] people) {
        this.strategy.search(queries, invIndex, people);
    }
}

public class Main {
    public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    } // This method returns the file (with path fileName) as a String

    public static void main(String[] args) {
        String pathToFile = args[1]; // The arguments are of the form '--data text.txt'

        try (Scanner scanner = new Scanner(System.in)){
            String peopleAsString = readFileAsString(pathToFile);
            String[] people = peopleAsString.split("\n");

            Map<String, ArrayList<Integer>> invIndex = new HashMap<>();
            for (int i = 0; i < people.length; i++) {
                for (String s : people[i].split(" ")) {
                    if (invIndex.containsKey(s.toLowerCase())) {
                        ArrayList<Integer> currentValue = invIndex.get(s.toLowerCase());
                        currentValue.add(i);
                        invIndex.put(s.toLowerCase(), currentValue);
                    } else {
                        invIndex.put(s.toLowerCase(), new ArrayList<>(List.of(i)));
                    }
                }
            }

            while (true) {
                System.out.println(
                        "\n" + "=== Menu ===" + "\n" + "1. Find a person" + "\n" +
                                "2. Print all people" + "\n" + "0. Exit");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) {
                    System.out.println("Bye!");
                    break;
                } else if (choice > 2 || choice < 0) {
                    System.out.println("Incorrect option! Try again.");
                    continue; // While loop restarts just after this statement is executed
                }

                switch (choice) {
                    case 1:
                        System.out.println("\n" + "Select a matching strategy: ALL, ANY, NONE");
                        String strategy = scanner.nextLine();

                        System.out.println("\n" + "Enter data to search people: ");
                        String[] queries = scanner.nextLine().split(" ");
                        switch (strategy) {
                            case "ALL":
                                setStrategy searchAllObject = new setStrategy(new searchAll()); // invoke constructor of context
                                searchAllObject.search(queries, invIndex, people);
                                break;
                            case "ANY":
                                setStrategy searchAnyObject = new setStrategy(new searchAny()); // invoke constructor of context
                                searchAnyObject.search(queries, invIndex, people);
                                break;
                            case "NONE":
                                setStrategy searchNoneObject = new setStrategy(new searchNone()); // invoke constructor of context
                                searchNoneObject.search(queries, invIndex, people);
                                break;
                            default:
                                break;
                        }
                        break;
                    case 2:
                        System.out.println("\n" + "=== List of people ===");
                        for (String s: people) {
                            System.out.println(s);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read file: " + e.getMessage());
        }
    }
}
