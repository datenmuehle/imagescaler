##### Generate Passwords for realm.properties #####

    openssl passwd -crypt -salt xyz user


#### Deploy to heroku ####

    mvn clean -e heroku:deploy