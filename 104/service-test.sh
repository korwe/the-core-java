#!/bin/bash

echo "Create session"
curl -X POST -d@initSession.txt http://korwe.dnsalias.com/webcore/
sleep 1

echo "Post requests"
curl -X POST -d@ping.txt http://korwe.dnsalias.com/webcore/
curl -X POST -d@synd.txt http://korwe.dnsalias.com/webcore/
sleep 1

echo "Fecth results"
echo "ping response"
curl http://korwe.dnsalias.com/webcore/response/ngtestsession01/NGNGNGNG-0001-4299-B561-428E36ECD722
echo "ping data"
curl http://korwe.dnsalias.com/webcore/data/ngtestsession01/NGNGNGNG-0001-4299-B561-428E36ECD722
echo "synd response"
curl http://korwe.dnsalias.com/webcore/response/ngtestsession01/NGNGNGNG-000B-4299-B561-428E36ECD722
echo "synd data"
curl http://korwe.dnsalias.com/webcore/data/ngtestsession01/NGNGNGNG-000B-4299-B561-428E36ECD722

echo "Kill session"
curl -X POST -d@killSession.txt http://korwe.dnsalias.com/webcore/
