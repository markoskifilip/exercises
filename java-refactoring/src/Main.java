import java.io.File;
import java.nio.file.Files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 */
public class Main {

	enum string_code {
		tragedy, comedy, none
	}

	/**
	 * @param inString
	 *            - the inString
	 * @return the {@link string_code}
	 */
	static string_code hashit(String inString) {
		if (inString.equals("tragedy"))
			return string_code.tragedy;
		else if (inString.equals("comedy"))
			return string_code.comedy;
		else
			return string_code.none;
	}

	static String statement(JsonArray invoices, JsonObject plays) {

		int totalAmount = 0;
		int volumeCredits = 0;
		StringBuilder resultSb = new StringBuilder();
		String getCustomerName = invoices.get(0).getAsJsonObject().get("customer").getAsString();

		resultSb.append(String.format("Statement for %s \n", getCustomerName));
		JsonArray performances = invoices.get(0).getAsJsonObject().get("performances").getAsJsonArray();
		int thisAmount = 0;
		JsonObject play;

		for (int i = 0; i < performances.size(); ++i) {
			play = plays.get(performances.get(i).getAsJsonObject().get("playID").getAsString()).getAsJsonObject();
			int audience = performances.get(i).getAsJsonObject().get("audience").getAsInt();

			string_code type = hashit(play.get("type").getAsString());

			switch (type) {
				case tragedy:
					thisAmount = 40000;
					if (audience > 30) {
						thisAmount += 1000 * (audience - 30);
					}
					break;
				case comedy:
					thisAmount = 30000;
					if (audience > 20) {
						thisAmount += 10000 + 500 * (audience - 20);
					}
					thisAmount += 300 * audience;
					break;
				default:
					return "error";
			}

			// add volume credits
			volumeCredits += Math.max(audience - 30, 0);

			// add extra credit for every five comedy attendees
			if (play.get("type").getAsString().equals("comedy"))
				volumeCredits += audience / 5;

			// print line for this order
			String nameOfPlay = play.get("name").getAsString();
			float amountPerPlay = thisAmount / 100;

			resultSb.append(String.format("    %s: $%.2f (%d seats)\n", nameOfPlay, amountPerPlay, audience));
			totalAmount += thisAmount;
		}

		float amountOwed = totalAmount / 100;

		resultSb.append(String.format("  Amount owed is $%,.2f\n", amountOwed));
		resultSb.append(String.format("  You earned %d credits\n", volumeCredits));

		return resultSb.toString();
	}

	public static void main(String[] args) throws Exception {

		JsonParser parser = new JsonParser();

		String playsFile = new String(Files.readAllBytes(new File("plays.json").toPath()));

		String invoicesFile = new String(Files.readAllBytes(new File("invoices.json").toPath()));

		JsonObject plays = parser.parse(playsFile).getAsJsonObject();

		JsonArray invoices = parser.parse(invoicesFile).getAsJsonArray();

		System.out.println(statement(invoices, plays));
	}

}
