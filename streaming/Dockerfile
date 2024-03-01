FROM geopraevent/python-poetry:1.5.1-python3.10-bullseye

WORKDIR /app

COPY pyproject.toml pyproject.toml
COPY poetry.lock poetry.lock
COPY src src

RUN poetry install

ENTRYPOINT ["poetry", "run"]
CMD ["producer"]
