# 1. Authenticate and extract token
AUTH_HEADER=$(curl -s -D - -o /dev/null \
  -X POST http://localhost:8084/api/public/login \
  -H "Content-Type: application/json" \
  -d '{
        "username": "manuel@gmail.com",
        "password": "Manuelino123!"
      }' | grep -i '^Authorization:')

# Extract just the token
TOKEN=$(echo "$AUTH_HEADER" | sed -E 's/Authorization: (.*)/\1/i')

# Fail if empty
if [ -z "$TOKEN" ]; then
  echo "ERROR: Token not found in Authorization header"
  exit 1
fi

# 2. Run load-balancing test
for i in {1..100}; do
  curl -s \
       -H "Authorization Bearer $TOKEN" \
       http://localhost/actuator/info | grep version
done | sort | uniq -c