FROM python

RUN apt-get update \
	&& apt-get install -y --no-install-recommends \
	postgresql-client \
	tzdata \
	&& rm -rf /var/lib/apt/lists/*

COPY . .
WORKDIR /Django-Poll-App-master
RUN pip install -r requirements.txt


EXPOSE 8000
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]