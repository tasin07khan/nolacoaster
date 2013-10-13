package com.nbeckman.megabudget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Simple container class for holding pre-decimal point and
// post decimal point values.
public class DollarsAndCents {
	public long dollars = 0L;
	public long cents = 0L;
	
	public String print(String decimal) {
		String cents_string = Long.toString(cents);
		if (cents_string.length() > 2) {
			cents_string = cents_string.substring(0, 2);
		}
		return Long.toString(dollars) + decimal + cents_string;
	}
	
	// TODO(nbeckman): I'm a moron for inventing my own money-representing class.
	public DollarsAndCents add(DollarsAndCents other) {
		long new_dollars = this.dollars + other.dollars;
		long new_cents = this.cents + other.cents;
		if (new_cents > 100) {
			new_cents = new_cents % 100;
			new_dollars += 1;
		}
		DollarsAndCents result = new DollarsAndCents();
		return result;
	}
	
	public static DollarsAndCents parse(String value, String decimal_characters) {
		final Pattern pattern = Pattern.compile("([0-9]*)" + 
				Pattern.quote(decimal_characters) + "([0-9]*)");
		final Matcher matcher = pattern.matcher(value);
		if (!matcher.matches()) {
			return null;
		}
		final String dollar_string = matcher.group(1);
		long dollars = 0L;
		try {
			dollars = Long.parseLong(dollar_string);
		} catch (NumberFormatException e) {
			// Ignore
		}
		final String cents_string = matcher.group(2);
		long cents = 0L;
		try {
			cents = Long.parseLong(cents_string);
		} catch (NumberFormatException e) {
			// Ignore
		}
		DollarsAndCents result = new DollarsAndCents();
		result.dollars = dollars;
		result.cents = cents;
		return result;
	}
}
