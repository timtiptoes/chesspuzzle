#!/bin/sh
java -jar /app/app.jar &
nginx -g 'daemon off;'
