package com.leonarduk.finance.stockfeed.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.leonarduk.finance.portfolio.Position;

public class InvestmentsFileReaderTest {

	TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		this.folder.create();
	}

//	@Test
//	public final void testGetPositionsFromCSVFile() throws IOException {
//		final List<Position> positions = InvestmentsFileReader
//		        .getPositionsFromCSVFile("resources/data/portfolios.csv");
//		Assert.assertEquals(75, positions.size());
//	}

	@Test
	public final void testGetStocksFromCSVFile() throws IOException {
		final File file = this.folder.newFile("stocks.csv");
		final FileWriter writer = new FileWriter(file);
		// writer.write(str);
	}

}
