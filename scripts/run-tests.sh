chmod +x start-redis.sh stop-redis.sh
./start-redis.sh
mvn test
./stop-redis.sh