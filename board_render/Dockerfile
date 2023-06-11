##FROM docker.mgo.com/centos7base:latest

#FROM python:3.7-slim
FROM python:3.8-slim-buster

WORKDIR /
RUN pip3 install --upgrade pip
RUN pip3 install  --upgrade setuptools
RUN pip3 --no-cache-dir install chess

WORKDIR /tmp
USER root
#ENTRYPOINT ["print('caterwaul')"]
ENTRYPOINT ["python3","/tmp/main.py"]
