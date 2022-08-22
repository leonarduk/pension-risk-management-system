set IMAGE_VERSION_TAG=6

set DOCKER_REPO=leonarduk
set PROJECT=timeseries-lambda

set AWS_ACCOUNT_ID=575836911148
set REGION=eu-west-1
@REM aws configure

set BASE_IMAGE=openjdk:18-jdk

set AWS_REPO=%DOCKER_REPO%-%AWS_ACCOUNT_ID%/%PROJECT%
set AWS_ECR=%AWS_ACCOUNT_ID%.dkr.ecr.%REGION%.amazonaws.com

docker login

echo refetch base image
docker pull %BASE_IMAGE%

@REM Inspect your images and find two or more with the same tag:
@REM docker images
@REM Delete them:
@REM docker rmi --force 'image id'

echo log into AWS ECR
@REM aws ecr get-login-password | docker login --username AWS --password-stdin %AWS_ECR%
aws ecr get-login-password --region %REGION% | docker login --username AWS --password-stdin %AWS_ECR%
docker build -t %AWS_REPO%:%IMAGE_VERSION_TAG% .

@REM echo create repository
aws ecr create-repository --repository-name %AWS_REPO% --image-scanning-configuration scanOnPush=true --image-tag-mutability MUTABLE

docker tag %AWS_REPO%:%IMAGE_VERSION_TAG% %AWS_ECR%/%AWS_REPO%:%IMAGE_VERSION_TAG%

echo Push to ECR - stage 1 - Preparing - count how many layers.
docker push %AWS_ECR %/%AWS_REPO%:%IMAGE_VERSION_TAG%

echo "%IMAGE_VERSION_TAG% is ready for use"
