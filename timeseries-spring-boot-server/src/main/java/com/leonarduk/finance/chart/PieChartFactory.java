/*
 * =========================================================== JFreeChart : a
 * free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info: http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. in the
 * United States and other countries.]
 *
 * ------------------ ImageMapDemo6.java ------------------ (C) Copyright 2004,
 * by Object Refinery Limited and Contributors.
 *
 * Original Author: David Gilbert (for Object Refinery Limited); Contributor(s):
 * -;
 *
 * $Id: ImageMapDemo6.java,v 1.2 2004/04/26 19:11:55 taqua Exp $
 *
 * Changes ------- 31-Mar-2004 : Version 1 (DG);
 *
 */

package com.leonarduk.finance.chart;

import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Creates an HTML image map for a multiple pie chart.
 */
public class PieChartFactory {

	private final DefaultPieDataset	dataset;

	private final String			title;

	/**
	 * Starting point for the demo.
	 *
	 * @param args
	 *            ignored.
	 */
	public static void main(final String[] args) {
		final PieChartFactory factory = new PieChartFactory("Title");
		factory.put("A", 12.2).put("B", 13.2).put("C", 31.2);

		try {
			// write an HTML page incorporating the image with an image map
			final File file2 = new File("multipiechart100.html");
			final OutputStream out = new BufferedOutputStream(
			        new FileOutputStream(file2));
			final PrintWriter writer = new PrintWriter(out);
			writer.println("<HTML>");
			writer.println(
			        "<HEAD><TITLE>JFreeChart Image Map Demo</TITLE></HEAD>");
			writer.println("<BODY>");
			// ChartUtilities.writeImageMap(writer, "chart", info);
			writer.println(ChartDisplay.saveImageAsPngAndReturnHtmlLink("345",
			        400, 400, factory.buildChart()));
			writer.println("</BODY>");
			writer.println("</HTML>");
			writer.close();

		}
		catch (final IOException e) {
			System.out.println(e);
		}

	}

	public PieChartFactory(final String title) {
		this.dataset = new DefaultPieDataset();
		this.title = title;
	}

	public PieChartFactory add(final String name, final Double double1) {
		if (double1.equals(Double.NaN)) {
			return this;
		}
		if (this.dataset.getKeys().contains(name)) {
			final Double value = this.dataset.getValue(name).doubleValue();
			this.put(name, double1 + value);
		}
		else {
			this.put(name, double1);
		}
		return this;
	}

	public void addAll(final Map<String, Double> map) {
		map.entrySet().stream().forEach(entry -> {
			this.add(entry.getKey(), entry.getValue());
		});
	}

	public JFreeChart buildChart() {
		final JFreeChart chart = ChartFactory.createPieChart(this.title,
		        this.dataset);

		final PiePlot p = (PiePlot) chart.getPlot();
		p.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
		p.setInteriorGap(0.01);

		return chart;
	}

	@SuppressWarnings("unchecked")
	public Double getTotal() {
		return (Double) this.dataset.getKeys().stream()
		        .collect(Collectors.summingDouble(key -> {
			        return (Double) this.dataset.getValue((String) key);
		        }));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Double> getValueMap() {
		return (Map<String, Double>) this.dataset.getKeys().stream()
		        .collect(Collectors.toMap(key -> key,
		                key -> (Double) this.dataset.getValue((String) key)));
	}

	public PieChartFactory put(final String key, final Double value) {
		this.dataset.setValue(key, value);
		return this;
	}

	public void putAll(final Map<String, Double> map) {
		map.entrySet().stream().forEach(entry -> {
			this.put(entry.getKey(), entry.getValue());
		});
	}
}
