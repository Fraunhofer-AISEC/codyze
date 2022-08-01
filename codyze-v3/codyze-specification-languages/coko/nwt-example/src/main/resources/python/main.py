#!/usr/bin/env python3

import flask
from flask_jwt import JWT, jwt_required, current_identity

import sqlite3
import logging
con = sqlite3.connect(':memory:')

# prepare DB
cur = con.cursor()
cur.execute('''CREATE TABLE data
               (date text, data text)''')
con.commit()

app = flask.Flask(__name__)
app.config["DEBUG"] = True


@app.route('/do', methods=['POST'])
@jwt_required()
def do():
    cur = con.cursor()
    cur.execute(
        "INSERT INTO data VALUES ('2006-01-05','very important')")
    con.commit()

    logging.info('[audit] %s did something in the database', current_identity)


app.run()
