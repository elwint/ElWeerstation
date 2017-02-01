document.getElementById("defaultOpen").click();

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
            lineTension: 0.1,
            backgroundColor: "rgba(75,192,192,0.4)",
            borderColor: "rgba(75,192,192,1)",
            borderCapStyle: 'butt',
            borderDash: [],
            borderDashOffset: 0.0,
            borderJoinStyle: 'miter',
            pointBorderColor: "rgba(75,192,192,1)",
            pointBackgroundColor: "#fff",
            pointBorderWidth: 1,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: "rgba(75,192,192,1)",
            pointHoverBorderColor: "rgba(220,220,220,1)",
            pointHoverBorderWidth: 2,
            pointRadius: 1,
            pointHitRadius: 10,
            data: tempData,
            spanGaps: false,
        }
    ]
	}
	});

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
	
	function updateGraph(){
		tempData.splice(0,1);
		tempData.push((Math.random()*25)+1);
		temperatureChart.update();
	}
	
	var myInterval = setInterval(updateGraph, 1000);
	
	