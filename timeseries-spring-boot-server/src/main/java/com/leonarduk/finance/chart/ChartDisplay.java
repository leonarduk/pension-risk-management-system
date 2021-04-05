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

import com.leonarduk.finance.utils.HtmlTools;

public class ChartDisplay {
	public static void displayChartInFrame(final JFreeChart chart, final int width, final int height,
			final String title) {
		// Chart panel
		final ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		panel.setPreferredSize(new java.awt.Dimension(width, height));
		// Application frame
		final ApplicationFrame frame = new ApplicationFrame(title);
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	public static StringBuilder getTable(final Map<?, ?> map, final String col1, final String col2) {
		final StringBuilder sb = new StringBuilder("<table><tr><th>").append(col1).append("</th><th>").append(col2)
				.append("</th></tr>");
		map.entrySet().stream().forEach(entry -> sb.append("<tr><td>").append(entry.getKey().toString())
				.append("</td><td>").append(entry.getValue().toString()).append("</td></tr>"));
		sb.append("</table>");
		return sb;
	}

	public static void saveChartAsPng(final String imageFileName, final JFreeChart chart, final int width,
			final int height) throws IOException {
		final ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

		ChartUtilities.saveChartAsPNG(new File(imageFileName), chart, width, height, info);

	}

	public static StringBuilder saveImageAsPngAndReturnHtmlLink(final String imageNamePrefix, final int width,
			final int height, final JFreeChart chart) throws IOException {

		final String imageName = imageNamePrefix + ".png";
		ChartDisplay.saveChartAsPng(imageName, chart, width, height);
		return getImgLink(width, height, imageNamePrefix);
	}

	private static StringBuilder getImgLink(final int width, final int height, final String imageName) {
		return new StringBuilder("<img src=\"/images/jpg/").append(imageName).append("\" WIDTH=\"").append(width)
				.append("\" HEIGHT=\"").append(height).append("\" BORDER=\"0\" USEMAP=\"#chart\"/>").append("</img>");
	}

	public static StringBuilder saveImageAsSvgAndReturnHtmlLink(final String imageNamePrefix, final int width,
			final int height, final JFreeChart chart) throws Exception {

		File svgFile = createSvgFile(imageNamePrefix, width, height, chart);

		File jpegFile = new File(svgFile.getParentFile(), imageNamePrefix + ".jpg");
		HtmlTools.convertSvgToJPG(svgFile, jpegFile);

		return getImgLink(width, height, imageNamePrefix);
	}

	private static File createSvgFile(final String imageNamePrefix, final int width, final int height,
			final JFreeChart chart) throws IOException {
		final SVGGraphics2D g2 = new SVGGraphics2D(width, height);
		final Rectangle r = new Rectangle(0, 0, width, height);
		chart.draw(g2, r);
		final String imageName = imageNamePrefix + ".svg";
		final File f = new File("target", imageName);
		SVGUtils.writeToSVG(f, g2.getSVGElement());
		return f;
	}
}
