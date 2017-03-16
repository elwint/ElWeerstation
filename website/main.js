// filters

var country = "EL SALVADOR";

var stationFilter = document.getElementById("station_filter")
var station = "";

function updateStationFilter() {
	var req = new XMLHttpRequest();
	req.onreadystatechange = function () {
		if (req.readyState == 4) {
			stationFilter.innerHTML = "<option value=\"\" selected>Station</option>";
			var json = JSON.parse(req.responseText);
			json.forEach(function(elem) {
				stationFilter.innerHTML += "<option value="+elem.number+">"+elem.name+"</option>";
			})
		}
	}
	station = ""
	req.open("GET", getURL("http://145.33.225.152/stations"), true);
	req.send();
}

function stationFilterChanged() {
	station = stationFilter.value;
	resetAllGraphs();
}
stationFilter.onchange = stationFilterChanged;

updateStationFilter();

function getURL(url) {
	if (country == "") {
		return url
	}

	var c = '?'
	if (url.includes('?')) {
		c = '&'
	}

	var params = c + 'country=' + encodeURI(country)
	if (station != "") {
		params += "&station="+station
	}
	return url + params
}

// graphs

document.getElementById("defaultOpen").click();

Chart.defaults.scale.ticks.beginAtZero = true;

var temperatureChart;
var dewPointChart;
var airPressureChart;
var visibilityChart;
var rainFallChart;
var snowDepthChart;
var cloudinessChart;
var windSpeedChart;

var tempData = [];
var dewData = [];
var airData = [];
var visData = [];
var rainData = [];
var snowData = [];
var cloudData = [];
var windSpeedData = [];

function clearData(data) {
	l = data.length
	data.splice(0);
	for (i = 0; i < l; i++) {
		data.push(null);
	}
}

function setGraph(id, label, data) {
	data.splice(0);
	for (i = 0; i < 7; i++) {
		data.push(null);
	}

	var labels = ["7 days ago", "6 days ago", "5 days ago", "4 days ago", "3 days ago", "2 days ago", "yesterday"];
	return new Chart(id, {
		type: 'line',
		data: {
			labels: labels,
			datasets: [
			{
				label: label,
				fill: false,
				lineTension: 0.2,
				backgroundColor: "rgba(15,120,192,0.4)",
				borderColor: "rgba(15,120,192,1)",
				pointBorderColor: "rgba(75,192,192,1)",
				pointBackgroundColor: "#fff",
				pointBorderWidth: 1,
				pointHoverRadius: 5,
				pointHoverBackgroundColor: "rgba(75,192,192,1)",
				pointHoverBorderColor: "rgba(220,220,220,1)",
				pointHoverBorderWidth: 2,
				pointHitRadius: 20,
				data: data,
				spanGaps: false,
			}
			]
		}
	});
}

var temperatureChart = setGraph('temperatureChart', 'Temperature', tempData);
var dewPointChart    = setGraph('dewPointChart', 'Dew point', dewData);
var airPressureChart = setGraph('airPressureChart', 'Air pressure', airData);
var visibilityChart  = setGraph('visibilityChart', 'Visibility', visData);
var rainFallChart    = setGraph('rainFallChart', 'Rain fall', rainData);
var snowDepthChart   = setGraph('snowDepthChart', 'Snow depth', snowData);
var cloudinessChart  = setGraph('cloudinessChart', 'Cloudiness', cloudData);
var windSpeedChart   = setGraph('windSpeedChart', 'Wind speed', windSpeedData);

function updateWeekGraphs() {
	clearData(rainData);
	clearData(snowData);
	clearData(cloudData);
	clearData(dewData);
	clearData(tempData);
	clearData(windSpeedData);
	clearData(airData);
	clearData(visData);

	temperatureChart.update()
	dewPointChart.update();
	airPressureChart.update();
	visibilityChart.update();
	rainFallChart.update();
	snowDepthChart.update();
	cloudinessChart.update();
	windSpeedChart.update();

	var req = new XMLHttpRequest();
	req.onreadystatechange = function () {
		if (req.readyState == 4) {
			var json = JSON.parse(req.responseText);
			json.forEach(function(elem) {
				rainData[7-elem.since] = elem.rain;
				snowData[7-elem.since] = elem.snow;
				cloudData[7-elem.since] = elem.cloudiness;
				dewData[7-elem.since] = elem.dew;
				tempData[7-elem.since] = elem.temp;
				windSpeedData[7-elem.since] = elem.windspeed;
				airData[7-elem.since] = elem.airpressure;
				visData[7-elem.since] = elem.windspeed;

				temperatureChart.update()
				dewPointChart.update();
				airPressureChart.update();
				visibilityChart.update();
				rainFallChart.update();
				snowDepthChart.update();
				cloudinessChart.update();
				windSpeedChart.update();
			});
		}
	}
	req.open("GET", getURL("http://145.33.225.152/weekly"), true);
	req.send();
}

function loadPage(page){
	var i, tabcontent, tablinks;
	
	tabcontent = document.getElementsByClassName("tabcontent");
	for (i = 0; i < tabcontent.length; i++){
		tabcontent[i].style.display = "none";
	}
		
	tablinks = document.getElementsByClassName("tablinks");
	for(i = 0; i < tablinks.length; i++){
		tablinks[i].className = tablinks[i].className.replace("active","");
		
	document.getElementById(page).style.display = "block";
	}
}
	
function getTime(){
	var d = new Date();
	var minutes = "0"+d.getMinutes();
	return d.getHours()+":"+minutes.slice(-2);
}


// Highest peak rain chart
{

var peakRainData = [];
var peakRainLabels = [];

function updatePeakRain() {
	peakRainData.splice(0);
	peakRainLabels.splice(0);
	peakRainChart.update();

	peakRainReq = new XMLHttpRequest();
	peakRainReq.onreadystatechange = function () {
		if (peakRainReq.readyState == 4) {
			var json = JSON.parse(peakRainReq.responseText);
			json.forEach(function(elem) {
				s = elem.station
				if (country != "") {
					s = s.replace(country+', ', '');
				}
				peakRainLabels.push(s);
				peakRainData.push(elem.value);
				peakRainChart.update();
			});
		}
	}
	peakRainReq.open("GET", getURL("http://145.33.225.152/rain/top5"), true);
	peakRainReq.send();
}

var peakRainChart = new Chart('highestPeakChart', {
		type: 'bar',
		data: {
		labels: peakRainLabels,
		datasets: [
			{
				label: "millimeters of rain",
				backgroundColor: [	'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
								],
				data: peakRainData,
			}
		]
}})
}


// WindForce Chart
var windForceData = [];
var windForceLabels = [];

var windForceChart = new Chart('windForceChart', {
	type: 'line',
	data: {
	labels: windForceLabels,
	datasets: [
		{
			label: "Wind Force",
			fill: false,
			lineTension: 0.1,
			backgroundColor: "rgba(75,192,192,0.4)",
			borderColor: "rgba(75,192,192,1)",
			pointBorderColor: "rgba(75,192,192,1)",
			pointBackgroundColor: "#fff",
			pointBorderWidth: 1,
			pointHoverRadius: 5,
			pointHoverBackgroundColor: "rgba(75,192,192,1)",
			pointHoverBorderColor: "rgba(220,220,220,1)",
			pointHoverBorderWidth: 2,
			pointHitRadius: 20,
			data: windForceData,
			spanGaps: false,
		}]}});

var lastTime = Date.now();

function formatDate(d) {
	return d.getFullYear() + "-" + d.getMonth() + "-" + d.getDay() + " " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
}

function updateWindForceGraph(elem) {
	windForceData.splice(0,1);
	windForceData.push(elem.value);
	lastTime = (elem.time+5)*1000;
	windForceChart.update();
}

var windForceReq = new XMLHttpRequest();
windForceReq.onreadystatechange = function () {
	if (windForceReq.readyState == 4) {
		var json = JSON.parse(windForceReq.responseText);
		json.forEach(updateWindForceGraph);
	}
}

function getWindForceData() {
	windForceReq.open("GET", getURL("http://145.33.225.152/windspeed/since?time="+Math.floor(lastTime/1000)), true);
	windForceReq.send();
}

var windForceInterval;

function loadWindForceGraph() {
	clearInterval(windForceInterval);

	for (var i = 120; i > 0; i--) {
		if (windForceLabels.length > 120) {
			windForceData.splice(0,1);
			windForceLabels.splice(0,1);
		}

		windForceData.push(null);
		if (i%12 == 0) {
			windForceLabels.push(i/12 + " minutes ago");
		} else {
			windForceLabels.push("")
		}
	}

	windForceChart.update();

	lastTime = Date.now() - (1000 * 60 * 60 * 2);
	getWindForceData();

	windForceInterval = setInterval(getWindForceData, 5000);
}

function resetAllGraphs() {
	loadWindForceGraph();
	updatePeakRain();
	updateWeekGraphs();
}

resetAllGraphs();
