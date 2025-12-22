
service_name=$1
db_name=$2
db_port=$3

echo "entering shutdown"
echo $service_name
echo $db_name
echo $db_port

docker service scale $service_name=0
remove=$(docker service rm $service_name)

if [[ "$remove" == $service_name ]]; then
  echo "Stopped ${service_name}."

    latest_i=$(docker ps --filter "name=^${db_name}[1-9][0-9]*$" --format "{{.Names}}" | sort -V | tail -n 1 | grep -oE '[0-9]+$')

    for ((i = 1; i <= latest_i; i++)); do

            echo "1.${i} ${db_name}"

      db_name_aux="$db_name${i}"
      db_port=$(($db_port + i))

      echo "2.${i} ${db_name_aux}"

      docker stop ${db_name_aux}
      docker rm ${db_name_aux}

      echo "Stopped ${db_name_aux} on port ${db_port}."
    done
else
  echo "Could not stop ${service_name}."
fi