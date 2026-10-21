##FROM docker.mgo.com/centos7base:latest

#FROM python:3.7-slim
FROM python:3.8-slim-buster

WORKDIR /
RUN pip3 install --upgrade pip
RUN pip3 install  --upgrade setuptools
RUN pip3 --no-cache-dir install chess
# reportlab draws the PDF, svglib turns python-chess's SVG boards into PDF vectors,
# pillow is what reportlab needs to place board_key.png
# versions pinned to the last ones that support python 3.8: current reportlab
# calls md5(usedforsecurity=...), which needs 3.9+. lxml is pinned too, because
# svglib otherwise pulls lxml 6, whose only aarch64 wheels carry tags an older
# pip cannot match, and it then tries to compile it against libxml2 headers the
# slim image does not have. pip is upgraded in this same layer so a cached old
# pip from an earlier build cannot be used to resolve these.
RUN pip3 install --no-cache-dir --upgrade pip && \
    pip3 install --no-cache-dir reportlab==3.6.13 svglib==1.5.1 pillow==9.5.0 lxml==4.9.4

WORKDIR /tmp
USER root
ENTRYPOINT ["python3","/tmp/main.py"]
