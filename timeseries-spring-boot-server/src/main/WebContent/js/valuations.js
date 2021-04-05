// NOTE: If you run this file locally
// You will not get a server status
// You can comment out lines 9 and 26 to make it work locally

var xhr = new XMLHttpRequest();
// Create XMLHttpRequest object

xhr.onload = function() {// When readystate changes
		var newContent = "here";
	// The following conditional check will not work locally - only on a server
	if (xhr.status === 200) {// If server status was ok
		var responseObject = JSON.parse(xhr.responseText);

		// BUILD UP STRING WITH NEW CONTENT (could also use DOM manipulation)
		newContent += "<table border="1"><tr><th>Name</th><th>ISIN</th><th>Code</th><th>Sector</th><th>Type</th>"//
		+ "<th>Quantity Owned</th>" + "<th>Value Owned</th>" + "<th>Price</th>" + "<th>AsOf</th>"//
		+ "<th>1D</th>" + "<th>5D</th>" + "<th>21D</th>" + "<th>63D</th>" + "<th>365D</th>"//
		+ "<th>SMA (12days)</th>" + "<th>SMA (20days)</th>" + "<th>SMA (50days)</th>"//
		+ "<th>Global Extrema</th>" + "<th>Moving Momentum</th>" + "</tr>";

		// Variable to hold HTML
		for (var i = 0; i < responseObject.valuations.length; i++) {//
			// Loop through object
			newContent += "<tr><td>" + responseObject.valuations[i].position.instrument.name//
			+ "</td><td>" + responseObject.valuations[i].position.instrument.isin//
			+ "</td><td>" + responseObject.valuations[i].position.instrument.code//
			+ "</td><td>" + responseObject.valuations[i].position.instrument.category//
			+ "</td><td>" + responseObject.valuations[i].position.instrument.assetType//
			+ "</td><td>" + responseObject.valuations[i].valuation//
			+ "</td><td>" + responseObject.valuations[i].position.amount//
			+ "</td><td>" + responseObject.valuations[i].price//
			+ "</td><td>" + responseObject.valuations[i].valuationDate//
			+ "</td><td>" + responseObject.valuations[i].returns.P1D//
			+ "</td><td>" + responseObject.valuations[i].returns.P5D//
			+ "</td><td>" + responseObject.valuations[i].returns.P21D//
			+ "</td><td>" + responseObject.valuations[i].returns.P63D//
			+ "</td><td>" + responseObject.valuations[i].returns.P365D//
			+ "</td><td>" + responseObject.valuations[i].recommendation.SMA12days//
			+ "</td><td>" + responseObject.valuations[i].recommendation.SMA20days//
			+ "</td><td>" + responseObject.valuations[i].recommendation.SMA50days//
			+ "</td><td>" + responseObject.valuations[i].recommendation.GlobalExtrema//
			+ "</td><td>" + responseObject.valuations[i].recommendation.MovingMomentum//
			+ "</td></tr>";
		}
		newContent += "<tr><td>" + responseObject.portfolioValuation.position.instrument.name//
		+ "</td><td>"// ISIN
		+ "</td><td>"// code
		+ "</td><td>"// category
		+ "</td><td>"//assetType
		+ "</td><td>" + responseObject.portfolioValuation.valuation//
		+ "</td><td>"// amount
		+ "</td><td>"//price
		+ "</td><td>" + responseObject.portfolioValuation.valuationDate//
		+ "</td><td>" + responseObject.portfolioValuation.returns.P1D//
		+ "</td><td>" + responseObject.portfolioValuation.returns.P5D//
		+ "</td><td>" + responseObject.portfolioValuation.returns.P21D//
		+ "</td><td>" + responseObject.portfolioValuation.returns.P63D//
		+ "</td><td>" + responseObject.portfolioValuation.returns.P365D//
		+ "</td><td>"// SMA12
		+ "</td><td>"//SMA20
		+ "</td><td>"//SMA50
		+ "</td><td>"//GlobalExtrema
		+ "</td><td>"// MovingMomentun
		+ "</td></tr></table>";


	}
		// Update the page with the new content
		document.getElementById("content").innerHTML = newContent;
 };

 xhr.open("GET", "http://localhost:8091/portfolio/api/report", true);
//xhr.open("GET", "http://leonarduk.ddns.info:8091/portfolio/api/display", true);
// xhr.open("GET", "valuations.json", true);
// Prepare the request
xhr.send(null);
// Send the request

// When working locally in Firefox, you may see an error saying that the JSON is
// not well-formed.
// This is because Firefox is not reading the correct MIME type (and it can
// safely be ignored).

// If you get it on a server, you may need to se the MIME type for JSON on the
// server (application/JSON).
