#!/bin/sh
awslocal dynamodb create-table --region ap-northeast-1 --table-name todo_item --attribute-definitions AttributeName=PK,AttributeType=S --key-schema AttributeName=PK,KeyType=HASH --billing-mode PAY_PER_REQUEST
