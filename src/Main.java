import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static ArrayList<Customer> customers = new ArrayList<Customer>();
	public static long largestDistance = 0;
	public static Point2D.Double origin = new Point2D.Double(53.37521, -6.6103);

	public static void main(String[] args) {
		try (Scanner scan = new Scanner(System.in)) {
			int numOfCustomers = scan.nextInt();
			scan.nextLine();
			for (int i = 0; i < numOfCustomers; i++) {
				String[] input = scan.nextLine().split(", ");
				if (input.length >= 4) {
					Customer customer = new Customer(input[0], Integer.parseInt(input[1]), Double.parseDouble(input[2]),
							Double.parseDouble(input[3]));
					customers.add(customer);
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Gui gui = new Gui();
	}
}
