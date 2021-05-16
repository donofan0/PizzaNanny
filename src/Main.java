import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static ArrayList<Customer> customers = new ArrayList<Customer>();

	public static void main(String[] args) {
		try (Scanner scan = new Scanner(System.in)) {
			int numOfCustomers = scan.nextInt();
			scan.nextLine();
			for (int i = 0; i < numOfCustomers; i++) {
				String[] input = scan.nextLine().split(", ");
				if (input.length >= 4) {
					Customer customer = new Customer(input[1], Integer.parseInt(input[2]), Double.parseDouble(input[3]),
							Double.parseDouble(input[4]));
					customers.add(customer);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		Gui gui = new Gui();
	}
}
