@echo off
echo Verifying OSS Configuration...
echo Ensure ALIYUN_OSS_ACCESS_KEY_ID and ALIYUN_OSS_ACCESS_KEY_SECRET are set in your environment variables.
mvn -f backend/pom.xml -Dtest=OssVerifyTest test
pause
