echo "stopping possible old docker images..."
docker stop naurandir-discord-clem
docker rm naurandir-discord-clem
docker rmi naurandir/discord-clem

echo "building new docker image..."
docker build -t naurandir/discord-clem .
docker-compose --compatibility up -d
