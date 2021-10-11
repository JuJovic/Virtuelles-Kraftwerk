docker run -v "$PWD/common-code/src/main/thrift:/data" thrift thrift -o /data --gen java /data/status.thrift
docker run -v "$PWD/common-code/src/main/thrift:/data" thrift thrift -o /data --gen java /data/componentController.thrift
docker build --pull --rm -f "central/Dockerfile" -t central .
docker build --pull --rm -f "client/Dockerfile" -t client .
docker build --pull --rm -f "component/Dockerfile" -t component .
docker-compose up