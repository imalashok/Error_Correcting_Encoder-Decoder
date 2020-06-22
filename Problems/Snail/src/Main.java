import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int h = scanner.nextInt();
        int up = scanner.nextInt();
        int down = scanner.nextInt();
        int days = 0;

        while (h > 0) {
            h -= up;
            days++;
            if (h < 1) {
                break;
            }
            h += down;
        }
        System.out.print(days);
    }
}