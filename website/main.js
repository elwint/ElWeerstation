document.getElementById("defaultOpen").click();

Chart.defaults.scale.ticks.beginAtZero = true;

var counter = 0;

var tempChart = document.getElementById('temperatureChart');

var tempData = [20.1, 22, 23, 21.3, 19.8, 18.6, 19.2];
	
var temperatureChart = new Chart(tempChart, {
	type: 'line',
	data: {
	labels: ["Today-7", "Today-6", "Today-5", "Today-4", "Today-3", "Today-2", "Today-1"],
	datasets: [
		{
			label: "Temperature",
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
			data: tempData,
			spanGaps: false,
		}]}});

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
	peakRainData = [];
	peakRainLabels = [];
	peakRainReq = new XMLHttpRequest();
	peakRainReq.onreadystatechange = function () {
		if (peakRainReq.readyState == 4) {
			var json = JSON.parse(peakRainReq.responseText);
			json.forEach(function(elem) {
				peakRainLabels.push(elem.station);
				peakRainData.push(elem.value);
				peakRainChart.update()
			});
		}
	}
	peakRainReq.open("GET", "http://localhost:9090/rain/top5", true);
	peakRainReq.send();
}

updatePeakRain();
setInterval(updatePeakRain,30000);
		
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
{
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
var totalItems = 0;

function formatDate(d) {
	return d.getFullYear() + "-" + d.getMonth() + "-" + d.getDay() + " " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
}

function updateWindForceGraph(elem) {
	if (totalItems > 120) {
		windForceData.splice(0,1);
		windForceLabels.splice(0,1);
	}
	windForceData.push(elem.value);
	windForceLabels.push(formatDate(new Date(elem.time*1000)));
	lastTime = elem.time*1000;
	windForceChart.update();
	totalItems++;
}

windForceReq = new XMLHttpRequest();
windForceReq.onreadystatechange = function () {
	if (windForceReq.readyState == 4) {
		var json = JSON.parse(windForceReq.responseText);
		json.forEach(updateWindForceGraph);
	}
}
windForceReq.open("GET", "http://localhost:9090/windspeed/bulk", true);
windForceReq.send();

setInterval(function(){
	windForceReq = new XMLHttpRequest();
	windForceReq.onreadystatechange = function () {
		if (windForceReq.readyState == 4) {
			var json = JSON.parse(windForceReq.responseText);
			json.forEach(updateWindForceGraph);
		}
	}
	windForceReq.open("GET", "http://localhost:9090/windspeed/since?time="+Math.floor(lastTime/1000), true);
	windForceReq.send();
}, 5000);
}
