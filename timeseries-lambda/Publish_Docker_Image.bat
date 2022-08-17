set DOCKER_REPO=leonarduk
set PROJECT=timeseries-lambda

set AWS_ACCOUNT_ID=575836911148
set REGION=eu-west-1

set BASE_IMAGE=openjdk:18-jdk

set DOCKER_IMAGE=%DOCKER_REPO%/%PROJECT%
set AWS_REPO=%DOCKER_REPO%-%AWS_ACCOUNT_ID%/%PROJECT%
set AWS_ECR=%AWS_ACCOUNT_ID%.dkr.ecr.%REGION%.amazonaws.com

docker login

@REM refetch base image
docker pull %BASE_IMAGE%

@REM to build the image
docker build -t %DOCKER_IMAGE% .

@REM to check for vulnerabilities
docker scan %DOCKER_IMAGE%

@REM to publish
docker push %DOCKER_IMAGE%

aws configure

aws ecr create-repository --repository-name %AWS_REPO% --image-scanning-configuration scanOnPush=true --image-tag-mutability MUTABLE
docker tag  %DOCKER_IMAGE%:latest %AWS_ECR%/%AWS_REPO%:latest
aws ecr get-login-password | docker login --username AWS --password-stdin %AWS_ECR%
docker push %AWS_ECR%/%AWS_REPO%:latest
