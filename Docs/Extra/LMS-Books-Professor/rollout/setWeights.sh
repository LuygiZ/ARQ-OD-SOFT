#!/bin/bash

BLUE="$1"
GREEN="$2"

cat > ./dynamic/canaryConfig.yml <<EOF
http:
  services:
    lmsbooks_split:
      weighted:
        services:
          - name: lmsbooks_blue@swarm
            weight: ${BLUE}
          - name: lmsbooks_green@swarm
            weight: ${GREEN}

  routers:
    lmsbooks_router:
      entryPoints: ["web"]
      rule: "PathPrefix(\`/\`)"
      service: lmsbooks_split
EOF

echo "Updated weights: blue=${BLUE}, green=${GREEN}"