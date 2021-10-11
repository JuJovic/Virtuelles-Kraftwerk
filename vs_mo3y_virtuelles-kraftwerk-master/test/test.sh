# ip address of central container
ip=`docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' central`

# send unexpected data to the udp socket of the central
echo "" > /dev/udp/$ip/6543 #no data
echo "This is some data" > /dev/udp/$ip/6543 #mal formatted
dd if=bytes.raw bs=203 count=1 > /dev/udp/$ip/6543 #correct formatted but invalid information