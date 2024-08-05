#!/bin/bash

echo "===================== Starting Program Execution ===================== "
echo ""
echo ""
echo "===================== Packaging Application ====================="
echo ""
echo ""
sh -c "mvn package"

cd docker

echo ""
echo ""
echo "===================== Starting Database ====================="
echo ""
echo ""
sh -c "docker compose up -d"
echo ""
echo ""

echo "===================== Building Docker Image ====================="
echo ""
echo ""
cd ../
timestamp=$(date +%Y%m%d%H%M%S)

docker build -t simple-bank-account:$timestamp .

echo ""
echo ""
echo "===================== Running application ====================="
echo ""
echo ""
docker run -e DB_PASSWORD=S3cret -e DB_URL=postgresql://host.docker.internal:5432/simple_bank_account_assignment \
       -e DB_USER=user -e ENCRYPTION_KEY=5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4= -p 8080:8080 \
       --name simple-bank-account-$timestamp simple-bank-account:$timestamp







