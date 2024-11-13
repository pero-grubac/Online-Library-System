package org.unibl.etf.mdp.dobavljac.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.unibl.etf.mdp.biblioteka.model.Book;

public class App {
	public static final int DOBAVLJAC_SERVER_TCP_PORT = 9000;

	public static void main(String[] args) {
		try {
			List<String> urlList = Arrays.asList("https://www.gutenberg.org/cache/epub/1342/pg1342.txt",
					"https://www.gutenberg.org/cache/epub/1661/pg1661.txt",
					"https://www.gutenberg.org/cache/epub/2701/pg2701.txt");
			
			System.out.println("DOBAVLJAC KLIJENT");
			
			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, DOBAVLJAC_SERVER_TCP_PORT);

			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

			Random rand = new Random();

			for (String url : urlList) {
				out.println(url);
				System.out.println("\nSent URL to server: " + url);
				Book book = (Book) in.readObject();
				System.out.println("Received Book from server: " + rand.nextInt() + " " + book);
			}
			out.println("KRAJ");

			in.close();
			out.close();
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
