package com.howmuchbeer.containers;

class Bottle12 implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "bottles (12oz)";
	}

	@Override
	public double sizeInOunces() {
		return 12.0;
	}	
}

class Case24 implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "cases (24)";
	}

	@Override
	public double sizeInOunces() {
		return 12.0 * 24.0; 
	}
}

class Case30 implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "cases (30)";
	}

	@Override
	public double sizeInOunces() {
		return 12.0 * 30.0;
	}
}

class SixPack implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "six packs";
	}

	@Override
	public double sizeInOunces() {
		return 12.0 * 6.0;
	}
}

class TwelvePack implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "twelve packs";
	}

	@Override
	public double sizeInOunces() {
		return 12.0 * 12.0;
	}
}

class Keg implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "kegs";
	}

	@Override
	public double sizeInOunces() {
		return 15.5 * 128.0;
	}
}

class Pint implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "pints";
	}

	@Override
	public double sizeInOunces() {
		return 16.0;
	}
}

class Forty implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "40s";
	}

	@Override
	public double sizeInOunces() {
		return 40.0;
	}
}

class SixPackTall implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "six packs (tall boys)";
	}

	@Override
	public double sizeInOunces() {
		return 16.0 * 6.0;
	}
}

class Bottle24 implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "bottles (24oz)";
	}

	@Override
	public double sizeInOunces() {
		return 24.0;
	}
}

class HalfKeg implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "'half' kegs";
	}

	@Override
	public double sizeInOunces() {
		return 7.75 * 128.0;
	}
}

class CornyKeg implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "corny kegs";
	}

	@Override
	public double sizeInOunces() {
		return  5.0 * 128.0;
	}
}

class PowerHour implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "power hours";
	}

	@Override
	public double sizeInOunces() {
		return 1.5 * 60.0;
	}
}

class Ounce implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "ounces";
	}

	@Override
	public double sizeInOunces() {
		return 1;
	}
}

class Gallon implements Container {
	@Override
	public long convertToOunces(double quantity) {
		return (long) Math.ceil(quantity * sizeInOunces());
	}

	@Override
	public String pluralizedName() {
		return "gallons";
	}

	@Override
	public double sizeInOunces() {
		return 128.0;
	}
}


public final class ContainerFactory {

	public static final String BOTTLE_12 ="BOTTLE_12";
	public static final String CASE_24 = "CASE_24";
	public static final String CASE_30 = "CASE_30";
	public static final String SIX_PACK = "SIX_PACK";
	public static final String TWELVE_PACK = "TWELVE_PACK";
	public static final String KEG = "KEG";
	public static final String PINT = "PINT";
	public static final String FORTY = "FORTY";
	public static final String SIX_PACK_TALL = "SIX_PACK_TALL";
	public static final String BOTTLE_24 = "BOTTLE_24";
	public static final String HALF_KEG = "HALF_KEG";
	public static final String CORNY_KEG = "CORNY_KEG";
	public static final String POWER_HOUR = "POWER_HOUR";
	public static final String OUNCE = "OUNCE";
	public static final String GALLON = "GALLON";
	
	private ContainerFactory() {}
	
	public static class UnknownContainer extends RuntimeException {}
	
	public static Container containerFromName(String container_name) {
		if (BOTTLE_12.equals(container_name)) {
			return new Bottle12();
		} else if(CASE_24.equals(container_name)) {
			return new Case24();
		} else if(CASE_30.equals(container_name)) {
			return new Case30();
		} else if(SIX_PACK.equals(container_name)) {
			return new SixPack();
		} else if(TWELVE_PACK.equals(container_name)) {
			return new TwelvePack();
		} else if(KEG.equals(container_name)) {
			return new Keg();
		} else if(PINT.equals(container_name)) {
			return new Pint();
		} else if(FORTY.equals(container_name)) {
			return new Forty();
		} else if(SIX_PACK_TALL.equals(container_name)) {
			return new SixPackTall();
		} else if(BOTTLE_24.equals(container_name)) {
			return new Bottle24();
		} else if(HALF_KEG.equals(container_name)) {
			return new HalfKeg();
		} else if(CORNY_KEG.equals(container_name)) {
			return new CornyKeg();
		} else if(POWER_HOUR.equals(container_name)) {
			return new PowerHour();
		} else if(OUNCE.equals(container_name)) {
			return new Ounce();
		} else if(GALLON.equals(container_name)) {
			return new Gallon();
		} else {
			throw new UnknownContainer();
		}
	}
	
}
