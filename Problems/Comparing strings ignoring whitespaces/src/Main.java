import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] lineOne = scanner.nextLine().toLowerCase().trim().split("\\s+");
        String[] lineTwo = scanner.nextLine().toLowerCase().trim().split("\\s+");
        StringBuilder firstLine = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();

        for (String word : lineOne) {
            firstLine.append(word);
        }

        for (String word : lineTwo) {
            secondLine.append(word);
        }

        System.out.println(firstLine.toString().equals(secondLine.toString()));
    }
}