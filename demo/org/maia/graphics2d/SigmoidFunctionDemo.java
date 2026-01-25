package org.maia.graphics2d;

import java.text.NumberFormat;
import java.util.Locale;

import org.maia.graphics2d.function.SigmoidFunction2D;

public class SigmoidFunctionDemo {

	public static void main(String[] args) {
		new SigmoidFunctionDemo().startDemo();
	}

	private void startDemo() {
		NumberFormat fmt = NumberFormat.getNumberInstance(Locale.UK);
		fmt.setMinimumFractionDigits(3);
		fmt.setMaximumFractionDigits(3);
		SigmoidFunction2D ft = SigmoidFunction2D.createCappedFunction(1.0, 5.0, 13.0, 2.0);
		double xFrom = 0;
		double xTo = 15.0;
		double xStep = 0.1;
		for (double x = xFrom; x <= xTo; x += xStep) {
			double y = ft.evaluate(x);
			System.out.println(fmt.format(x) + "," + fmt.format(y));
		}
	}

}