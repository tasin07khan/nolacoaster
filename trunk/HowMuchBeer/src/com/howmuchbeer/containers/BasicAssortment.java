package com.howmuchbeer.containers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BasicAssortment implements ContainerAssortment {

	private final static List<Container> CONTAINERS = new LinkedList<Container>();
	
	static {
		Comparator<Container> c = new Comparator<Container>() {
			@Override
			public int compare(Container arg0, Container arg1) {
				// Sort backwards!
				return (int)(arg1.sizeInOunces() - arg0.sizeInOunces());
			}};
		
		CONTAINERS.add(new Case24());
		CONTAINERS.add(new SixPack());
		Collections.sort(CONTAINERS, c);
	}
	
	@Override
	public Iterable<String> resultsForOunces(final long ounces) {
		return new Iterable<String>(){

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					long ounces_left = ounces;

					@Override
					public boolean hasNext() {
						if(ounces_left > 0)
							return true;
						else
							return false;
					}

					@Override
					public String next() {
						int index = 0;
						for (Container c : CONTAINERS) {
							if( c.sizeInOunces() < ounces_left )  {
								long quantity = (long) (ounces_left / c.sizeInOunces());
								ounces_left = (long) (ounces_left % c.sizeInOunces());
								
								// Check min case...
								if(index == CONTAINERS.size()-1 && ounces_left > 0) {
									ounces_left = 0;
									quantity++;
								}
								
								return quantity + " " + c.pluralizedName();
							}
							index++;
						}
						throw new RuntimeException("I think this is impossible.");
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}};
	}
	
}