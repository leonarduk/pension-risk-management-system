// NOTE: If you run this file locally
// You will not get a server status
// You can comment out lines 9 and 26 to make it work locally

var xhr = new XMLHttpRequest();
// Create XMLHttpRequest object

xhr.onload = function() {// When readystate changes
	// The following conditional check will not work locally - only on a server
	// if(xhr.status === 200) { // If server status was ok
	responseObject = JSON.parse(xhr.responseText);

	// BUILD UP STRING WITH NEW CONTENT (could also use DOM manipulation)
	var newContent = '<table border="1"><tr><th>Instrument</th><th>Amount</th></tr>';
	// Variable to hold HTML
	for (var i = 0; i < responseObject.holdings.length; i++) {// Loop through
																// object

		/*
		 * amount 24475.88 instrument name "CASH"
		 */
		newContent += '<tr><td>' + responseObject.holdings[i].instrument.name
				+ '</td><td>' + responseObject.holdings[i].amount
				+ '</td></tr>';
	}

	// Update the page with the new content
	document.getElementById('content').innerHTML = newContent;

	// }
};

xhr.open('GET', 'http://leonarduk.ddns.info:8091/portfolio/api/display', true);
// Prepare the request
xhr.send(null);
// Send the request

// When working locally in Firefox, you may see an error saying that the JSON is
// not well-formed.
// This is because Firefox is not reading the correct MIME type (and it can
// safely be ignored).

// If you get it on a server, you may need to se the MIME type for JSON on the
// server (application/JSON).
