document.getElementById("defaultOpen").click();

Chart.defaults.scale.ticks.beginAtZero = true;

var counter = 0;

var windFChart = document.getElementById('windForceChart');
var tempChart = document.getElementById('temperatureChart');
var highestPChart = document.getElementById('highestPeakChart');

var tempData = [20.1, 22, 23, 21.3, 19.8, 18.6, 19.2];
var windForceData = [];
var windForceLabels = [];
var highestPData = [12,10.5,11,13.3,12,];
var highestPLables = [];

var windForceChart = new Chart(windFChart, {
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
		
var highestPeakChart = new Chart(highestPChart, {
		type: 'bar',
		data: {
		labels: ["Station 1", "Station 2", "Station 3", "Station 4", "Station 5"],
		datasets: [
			{
				label: "Amount of rain",
				backgroundColor: [	'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
									'rgba(5,159,255,0.5)',
								],
				data: highestPData,
			}
		]
}})
	
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
	
	function updateGraph(graph,data,labels){
		data.splice(0,1);
		data.push((Math.random()*10)+5);
		labels.splice(0,1);
		if(counter%2 == 0){
			labels.push(getTime());
		}else{
			labels.push("");
		}
		graph.update();
		counter++;
	}
	
	function getTime(){
		var d = new Date();
		var minutes = "0"+d.getMinutes();
		return d.getHours()+":"+minutes.slice(-2);
	}
	
	function getWindForceData(){
	var i;
	for(i = 0; i < 200; i++){
		windForceData.push((Math.random()*10)+5);
		windForceLabels.push("");
	}
}
	
	getWindForceData();
	
	var myInterval = setInterval(updateGraph,500,windForceChart,windForceData,windForceLabels);
	
	