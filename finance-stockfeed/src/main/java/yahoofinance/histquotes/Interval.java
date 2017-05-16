
package yahoofinance.histquotes;

/**
 *
 * @author Stijn Strickx
 */
public enum Interval {

	DAILY("d"), MONTHLY("m"), WEEKLY("w");

	private final String tag;

	Interval(final String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return this.tag;
	}

}
