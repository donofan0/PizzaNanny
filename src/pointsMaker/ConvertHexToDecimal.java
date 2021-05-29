package pointsMaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class ConvertHexToDecimal {

	public static void main(String[] args) {
		readConsole();
	}

	public static void readConsole() {
		Scanner scan = new Scanner(System.in);
		String row;
		row = scan.nextLine();
		System.out.print("public static final byte [] uno = {");
		String[] data = row.split(",");
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i].substring(2, 4);
			System.out.print((byte) Integer.parseInt(data[i], 16));
			System.out.print(",");
		}
		System.out.print("};");
		scan.close();
	}

	public static void readFile() throws NumberFormatException, IOException {
		BufferedReader csvReader = new BufferedReader(new FileReader("UnoCards/unoCardsHex.csv"));
		String row;
		System.out.print("public static final byte [] uno = {");
		while ((row = csvReader.readLine()) != null) {
			String[] data = row.split(",");
			for (int i = 0; i < data.length; i++) {
				data[i] = data[i].substring(2, 4);
				System.out.print((byte) Integer.parseInt(data[i], 16));
				System.out.print(",");
			}
		}
		System.out.print("};");
		csvReader.close();
	}

	public static byte[] extractBytes(String ImageName) {
		File fi = new File(ImageName);
		byte[] fileContent = {};
		try {
			fileContent = Files.readAllBytes(fi.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (FileOutputStream fos = new FileOutputStream("uno0output.txt")) {
			fos.write(fileContent);
			// fos.close(); There is no more need for this line since you had created the
			// instance of "fos" inside the try. And this will automatically close the
			// OutputStream
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileContent;
	}

}
