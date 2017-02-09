from flask import Flask, request, abort
from psycopg2 import pool
import json
import decimal

###
### Setup database connection
###

try:
	pool = pool.ThreadedConnectionPool(8, 16, "dbname='elweerstation' user='elpython' host='172.22.2.10'")
except Exception as e:
	print('Exception:', e)
	exit()

###
###
###

app = Flask(__name__)
app.config['PROPAGATE_EXCEPTIONS'] = True

@app.after_request
def apply_caching(response):
    response.headers["Content-Type"] = "application/json"
    response.headers["Access-Control-Allow-Origin"] = "*"
    return response

@app.errorhandler(400)
def handle_badrequest(error):
	return 'Bad request'

## python pls
def decimal_default(obj):
    if isinstance(obj, decimal.Decimal):
        return float(obj)
    raise TypeError

###
### Routes
###

@app.route('/')
def index():
    return "Hello world"

@app.route('/windspeed/since')
def windspeed_since():
	country = request.args.get('country')
	station = request.args.get('station')

	if station != "" and station != None:
		try:
			station = int(station)
		except:
			abort(400)
	else:
		station = 0

	time = request.args.get('time')

	try:
		time = int(time)
	except:
		abort(400)


	v = (time,)

	q = "SELECT floor(extract(epoch FROM Time) / 5)::int, avg(windspeed) FROM measurement m INNER JOIN Stations s ON (s.stn = m.Station_ID) WHERE Time > to_timestamp(%s) at time zone 'utc'"

	if station > 0:
		q += " AND s.stn = %s"
		v += (station,)
	elif country != "" and country != None:
		q += " AND s.Country = %s"
		v += (country,)

	q += " GROUP BY 1 ORDER BY 1"

	conn = pool.getconn()
	cur = conn.cursor()
	cur.execute(q, v)
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"time": row[0]*5, "value": + row[1]})

	pool.putconn(conn)

	return json.dumps(result)

@app.route('/rain/top5')
def rain_top5():
	country = request.args.get('country')
	station = request.args.get('station')

	if station != "" and station != None:
		try:
			station = int(station)
		except:
			abort(400)
	else:
		station = 0

	v = ()

	q = "SELECT s.Country || ', ' || s.Name, avg(m.Rain) FROM Measurement m INNER JOIN Stations s ON (s.stn = m.Station_ID) WHERE Time::date = current_date"

	if station > 0:
		q += " AND s.stn = %s"
		v += (station,)
	elif country != "" and country != None:
		q += " AND s.Country = %s"
		v += (country,)

	q += " GROUP BY 1 ORDER BY 2 DESC LIMIT 5"

	conn = pool.getconn()
	cur = conn.cursor()
	cur.execute(q, v)
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"station": row[0], "value": + row[1]*100})

	pool.putconn(conn)

	return json.dumps(result)

@app.route('/weekly')
def get_weekly_data():
	country = request.args.get('country')
	station = request.args.get('station')

	if station != "" and station != None:
		try:
			station = int(station)
		except:
			abort(400)
	else:
		station = 0

	v = ()

	q = "SELECT Time::date - current_date, avg(m.Rain), avg(m.Snow), avg(m.Temperature), avg(m.AirPressureSeaLevel), avg(m.Cloudiness), avg(m.Visibility), avg(m.WindSpeed), avg(m.DewPoint) FROM Measurement m INNER JOIN Stations s ON (s.stn = m.Station_ID) WHERE Time::date > current_date - 7 AND Time::date < current_date"

	if station > 0:
		q += " AND s.stn = %s"
		v += (station,)
	elif country != "" and country != None:
		q += " AND s.Country = %s"
		v += (country,)

	q += " GROUP BY 1 ORDER BY 1 LIMIT 7"

	print(q)

	conn = pool.getconn()
	cur = conn.cursor()
	cur.execute(q, v)
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({
			"since": -1 * row[0],
			"rain": row[1],
			"snow": row[2], 
			"temp": row[3],
			"airpressure": row[4],
			"cloudiness": row[5],
			"visibility": row[6],
			"windspeed": row[7],
			"dew": row[8]
		})

	pool.putconn(conn)

	return json.dumps(result, default=decimal_default)

@app.route('/countries')
def country_list():
	conn = pool.getconn()
	cur = conn.cursor()

	cur.execute("SELECT DISTINCT Country FROM Stations ORDER BY 1")
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append(row[0])

	pool.putconn(conn)

	return json.dumps(result)

@app.route('/stations')
def station_list():
	country = request.args.get('country')

	conn = pool.getconn()
	cur = conn.cursor()

	cur.execute("SELECT DISTINCT stn, Name FROM stations WHERE Country = %s ORDER BY 2", (country,))
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"number": row[0], "name": row[1]})

	pool.putconn(conn)

	return json.dumps(result)
