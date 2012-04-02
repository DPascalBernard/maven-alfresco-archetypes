@ECHO OFF
REM Name:   m2-bootstrap.bat
REM Author: Stijn de Reede
REM Based on linux shell script by g.columbro@sourcesense.com
REM The old usage instructions for the linux shell script are below.
REM
REM Usage:
REM Download alfresco.war, share.war and the SDK zip. Place them in a temporary directory. Extract alfresco.war to a subdirectory called
REM alfresco, share to a subdirectory called share, and the SDK zip to a subdirectory called sdk. If you want to save time, you can only
REM extract the WEB-INF/lib directories of the WAR files, and places them in the proper directory structure on the file system.
REM This batch file uploads the Alfresco artifacts, including sources and javadoc, in your repository.
REM Optionally, the Share Point Protocol AMP can be placed in the temporary directory, called something like alfresco-%RELEASE%-spp-%VERSION%.amp.
REM
REM Run the following command (assuming download is the directory where you placed everything):
REM
REM 		m2-bootstrap.bat  %BASE_DIR%   %TARGET_REPO%     %TARGET_REPO_URL%                                                                           %VERSION%  %RELEASE% 
REM e.g:   	m2-bootstrap.bat  download     thirdparty     	 http://www.example.org:9080/nexus/content/repositories/thirdparty/  						 3.4.7      enterprise
REM
REM Note: depending on the security set on the repository, you might need to add a <server> section in your Maven settings.xml with the ID 
REM of the repository and with your username and password.
REM Note: in older Alfresco versions (3.3.x) the alfresco-data-model-%VERSION%.jar doesn't exist, so there might fail one artifact upload.

REM Old instructions, copied from the Linux shell script:
REM
REM Description:
REM This script is needed *only* in case you *don't* have you don't have alfresco artifacts available in any public repo,
REM and you can't connect to Sourcesense public repo. 
REM So you can manually download JAR and WAR alfresco artifacts in $BASE_DIR (1st param)
REM and have them deployed to $TARGET_REPO (2nd param) and $TARGET_REPO_URL (3rd param)
REM by running this script and passing the 5 params in the command line. 4th param indicates the version
REM while the 5th one the alfresco distro (community vs enterprise) we're going to deploy.
REM
REM Note: 
REM This script works for alfresco > 3.0 artifacts. It must be modified for
REM earlier versions (as share.war won't be present)
REM
REM License:
REM Licensed to the Apache Software Foundation (ASF) under one or more
REM contributor license agreements.  See the NOTICE file distributed with
REM this work for additional information regarding copyright ownership.
REM The ASF licenses this file to You under the Apache License, Version 2.0
REM (the "License"); you may not use this file except in compliance with
REM the License.  You may obtain a copy of the License at
REM    
REM http://www.apache.org/licenses/LICENSE-2.0
REM    
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM
REM Artifacts will be deployed with the following pattern:
REM
REM  org.alfresco:alfresco[-*]:[jar|war]:[VERSION]:[RELEASE]
REM
REM To have this fully working you need to have the following alfresco BASE_DIR layout:
REM
REM   BASE_DIR
REM        |____ alfresco.war
REM        |____ alfresco
REM        |            |__ WEB-INF
REM        |                    |__ lib
REM        |                         |___ alfresco-*.jar
REM        |____ share.war
REM        |____ share
REM                |__ WEB-INF
REM                        |__ alfresco-*.jar
REM
REM which you can easily obtain downloading an alfresco WAR distribution and unpacking both alfresco.war and share.war in folders with the same name


REM 1st command line param: 
REM directory where jar and war dependencies are stored
SET BASE_DIR=%1
REM 2st command line param: 
REM target repo id (matches in settings)
SET TARGET_REPO=%2
REM 3rd command line param: 
REM target repo url
SET TARGET_REPO_URL=%3
REM 4th command line param: 
REM Version 
SET VERSION=%4
REM 5th command line param: 
REM Release [community|enterprise]
SET RELEASE=%5

SET /A SUCCESS = 0
SET /A FAIL = 0

echo Starting upload of 8 Alfresco JARs to repo %TARGET_REPO% at %TARGET_REPO_URL%...
REM 8 alfresco JAR artifacts
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-core-%VERSION%.jar           -DartifactId=alfresco-core          -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-core-src.zip         -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-core-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-deployment-%VERSION%.jar     -DartifactId=alfresco-deployment    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-deployment-src.zip   -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-deployment-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-data-model-%VERSION%.jar     -DartifactId=alfresco-datamodel     -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-datamodel-src.zip    -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-datamodel-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-remote-api-%VERSION%.jar     -DartifactId=alfresco-remote-api    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-remote-api-src.zip   -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-remote-api-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-repository-%VERSION%.jar     -DartifactId=alfresco-repository    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-repository-src.zip   -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-repository-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-web-client-%VERSION%.jar     -DartifactId=alfresco-web-client    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE% -Dsources=%BASE_DIR%/sdk/src/alfresco-web-client-src.zip   -Djavadoc=%BASE_DIR%/sdk/doc/alfresco-web-client-doc.zip
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-jlan-embed-%VERSION%.jar     -DartifactId=alfresco-jlan-embed    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/Alfresco/WEB-INF/lib/alfresco-mbeans-%VERSION%.jar         -DartifactId=alfresco-mbeans        -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )

echo Starting upload of 2 Alfresco Share JARs to repo %TARGET_REPO% at %TARGET_REPO_URL%...
REM 2 share JAR artifacts
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/share/WEB-INF/lib/alfresco-share-%VERSION%.jar             -DartifactId=alfresco-share            -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/share/WEB-INF/lib/alfresco-web-framework-commons-%VERSION%.jar -DartifactId=alfresco-web-framework-commons    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=jar -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )

echo Starting upload of 2 Alfresco WAR to repo %TARGET_REPO% at %TARGET_REPO_URL%...
REM 2 alfresco and share WAR artifacts
echo "Starting Alfresco WARs uploading to repo %TARGET_REPO% at %TARGET_REPO_URL%"
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/alfresco.war                                               -DartifactId=alfresco                -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=war -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )
call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/share.war                                                  -DartifactId=share                    -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=war -Dclassifier=%RELEASE%
IF errorlevel 1 ( SET /A FAIL = FAIL + 1) else ( SET /A SUCCESS = SUCCESS + 1 )

REM optionally deploy SPP AMP artifact
IF EXIST %BASE_DIR%/alfresco-%RELEASE%-spp-%VERSION%.amp call mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file -Dfile=%BASE_DIR%/alfresco-%RELEASE%-spp-%VERSION%.amp  -DartifactId=spp-module  -DrepositoryId=%TARGET_REPO% -DgroupId=org.alfresco  -Dversion=%VERSION% -Durl=%TARGET_REPO_URL% -Dpackaging=amp -Dclassifier=%RELEASE%

echo %SUCCESS% artifacts uploaded, %FAIL% artifacts failed
