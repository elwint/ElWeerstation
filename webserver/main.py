from flask import Flask, request, abort
from psycopg2 import pool
import json

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

###
### Routes
###

@app.route('/')
def index():
    return "Hello world"

@app.route('/windspeed/bulk')
def windspeed_bulk():
	conn = pool.getconn()
	cur = conn.cursor()
	cur.execute("SELECT floor(extract(epoch FROM Time) / 5)::int, avg(windspeed) FROM measurement WHERE Time > now() at time zone 'utc' - interval '10 minutes' GROUP BY 1 ORDER BY 1")
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"time": row[0]*5, "value": + row[1]})

	pool.putconn(conn)

	return json.dumps(result)

@app.route('/windspeed/since')
def windspeed_since():
	time = request.args.get('time')

	try:
		time = int(time)
	except:
		abort(400)

	conn = pool.getconn()
	cur = conn.cursor()
	cur.execute("SELECT floor(extract(epoch FROM Time) / 5)::int, avg(windspeed) FROM measurement WHERE Time > to_timestamp(%s) at time zone 'utc' GROUP BY 1 ORDER BY 1", (time,))
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"time": row[0]*5, "value": + row[1]})

	pool.putconn(conn)

	return json.dumps(result)

@app.route('/rain/top5')
def rain_top5():
	conn = pool.getconn()
	cur = conn.cursor()

	cur.execute("SELECT s.Country || ', ' || s.Name, avg(m.Rain) FROM Measurement m INNER JOIN Stations s ON (s.stn = m.Station_ID) WHERE Time::date = current_date GROUP BY 1 ORDER BY 2 DESC LIMIT 5")
	rows = cur.fetchall()

	result = []
	for row in rows:
		result.append({"station": row[0], "value": + row[1]*100})

	pool.putconn(conn)

	return json.dumps(result)

