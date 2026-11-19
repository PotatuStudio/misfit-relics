package io.github.potatustudio.misfitrelics.relic;

/** Números para tooltips y JEI: enteros sin decimales y el resto con uno (2, 2,5). */
public final class TooltipNumbers {
	private TooltipNumbers() {
	}

	public static String fmt(float value) {
		return value == Math.round(value) ? String.valueOf(Math.round(value)) : String.format("%.1f", value);
	}
}
