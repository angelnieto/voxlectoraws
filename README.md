** Deploy app in AppEngine with prod profile : **
mvn clean package -Dspring.profiles.active=prod appengine:deploy

** Read production logs : **
gcloud app logs read

** Read production logs in streaming mode : **
gcloud app logs tail