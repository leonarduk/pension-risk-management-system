package com.leonarduk.finance.chart;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ChartDisplay {
	public static void saveChartAsPng(String imageFileName, JFreeChart chart, int width, int height)
			throws IOException {
		final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

		ChartUtilities.saveChartAsPNG(new File(imageFileName), chart, width, height, info);

	}

	public static StringBuilder saveImageAsPngAndReturnHtmlLink(String imageNamePrefix, int width, int height,
			JFreeChart chart) throws IOException {

		String imageName = imageNamePrefix + ".png";
		ChartDisplay.saveChartAsPng(imageName, chart, width, height);
		StringBuilder sb = new StringBuilder("<IMG SRC=\"").append(imageName).append("\" WIDTH=\"").append(width)
				.append("\" HEIGHT=\"").append(height).append("\" BORDER=\"0\" USEMAP=\"#chart\"/>");
		return sb;
	}

	public static StringBuilder saveImageAsSvgAndReturnHtmlLink(String imageNamePrefix, int width, int height,
			JFreeChart chart) throws IOException {

		SVGGraphics2D g2 = new SVGGraphics2D(width, height);
		Rectangle r = new Rectangle(0, 0, width, height);
		chart.draw(g2, r);
		String imageName = imageNamePrefix + ".svg";
		File f = new File(imageName);
		SVGUtils.writeToSVG(f, g2.getSVGElement());

		// <img src="data:image/jpg;base64,BASE64ENCODEDSTRINGHERE" />
		StringBuilder sb = new StringBuilder("<IMG SRC=\"").append(imageName).append("\" WIDTH=\"").append(width)
				.append("\" HEIGHT=\"").append(height).append("\" BORDER=\"0\" USEMAP=\"#chart\"/>");
		return sb;
	}

	public static void displayChartInFrame(JFreeChart chart, int width, int height, String title) {
		// Chart panel
		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		panel.setPreferredSize(new java.awt.Dimension(width, height));
		// Application frame
		ApplicationFrame frame = new ApplicationFrame(title);
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	public static StringBuilder getTable(Map<?, ?> map, String col1, String col2) {
		StringBuilder sb = new StringBuilder("<table><tr><th>").append(col1).append("</th><th>").append(col2)
				.append("</th></tr>");
		map.entrySet().stream().forEach(entry -> sb.append("<tr><td>").append(entry.getKey().toString())
				.append("</td><td>").append(entry.getValue().toString()).append("</td></tr>"));
		sb.append("</table>");
		return sb;
	}
}
