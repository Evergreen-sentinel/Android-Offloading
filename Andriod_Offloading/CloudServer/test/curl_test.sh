#!/bin/bash
# Usage: ./curl_test.sh /path/to/image.jpg
IMG=${1:-sample.jpg}
SERVER=http://YOUR_VM_IP:5000
curl -v -X POST -F "file=@${IMG}" ${SERVER}/upload --output output.jpg
if [ $? -eq 0 ]; then
  echo "Saved result to output.jpg"
else
  echo "Request failed"
fi
