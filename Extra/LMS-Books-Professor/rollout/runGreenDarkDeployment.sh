# Check if the node is already part of a swarm
SWARM_STATUS=$(docker info --format '{{.Swarm.LocalNodeState}}' 2>/dev/null)

if [ "$SWARM_STATUS" != "active" ]; then
    echo "Node is not in a swarm. Initializing swarm..."
    docker swarm init
fi

## Ensure overlay network exists before starting containers
if ! docker network ls --filter name=^lms_overlay_attachable_network$ --format '{{.Name}}' | grep -q '^lms_overlay_attachable_network$'; then
  docker network create --driver=overlay --attachable lms_overlay_attachable_network 2>/dev/null
fi

service_name="lmsbooks_green"
db_name="lmsbooks_green_db_"
db_port=55000

if [[ $1 =~ ^-?[0-9]+$ ]]; then
  # It's a valid integer (including negatives)
  if (( $1 < 1 )); then
    ./shutdown.sh $service_name $db_name $db_port
    exit
  fi
else
  echo "Error: Argument is not a valid number"
  exit
fi

latest_i=$(docker ps --filter "name=^${db_name}[1-9][0-9]*$" --format "{{.Names}}" | sort -V | tail -n 1 | grep -oE '[0-9]+$' 2>/dev/null)

if((latest_i > $1)); then
  for ((i = $1+1; i <= latest_i; i++)); do
    db_name="$db_name${i}"
    db_port=$(($db_port + i))

    docker stop ${db_name} 1>/dev/null
    docker rm ${db_name} 1>/dev/null

    echo "Stopped ${db_name} on port ${db_port}"
  done

else
  if ((latest_i < $1)); then

    for ((i = latest_i+1; i <= $1; i++)); do
      db_name_aux="$db_name${i}"
      db_port_aux=$((db_port + i))

      docker run -d \
        --name "${db_name_aux}" \
        --network lms_overlay_attachable_network \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD=password \
        postgres

      echo "Started ${db_name_aux} on port ${db_port_aux}"
    done
  fi
fi

echo Running $1 instances of $service_name, each connecting to a different/specific Postgres DBMS

if docker service ls --filter "name=${service_name}" --format "{{.Name}}" | grep -q "^${service_name}$"; then
  docker service scale $service_name=$1
else
  db_connection="jdbc:postgresql://$db_name{{.Task.Slot}}:5432/postgres"
  echo $db_connection
  docker service create -d \
     --name $service_name \
     --env spring.datasource.url=$db_connection \
     --env SPRING_PROFILES_ACTIVE=bootstrap \
     --env spring.datasource.username=postgres \
     --env spring.datasource.password=password \
     --env file.upload-dir=/tmp/uploads-psoft-g1-instance{{.Task.Slot}} \
     --env spring.rabbitmq.host=rabbitmq \
     --mount type=volume,source=uploaded_files_volume_{{.Task.Slot}},target=/tmp \
     --network lms_overlay_attachable_network \
     --env management.endpoints.web.exposure.include=info,health \
     --env management.info.git.mode=full \
     --env management.info.env.enabled=true \
     --env management.endpoint.info.cache.time-to-live=0 \
     lmsbooks:2.0.2

  docker service scale $service_name=$1
fi