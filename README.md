# Position-Tracker

## RUN THE FOLLOWING STEPS >>> AFTER POSITION-SIMULATOR 

1. Build Docker Image of position-tracker [mooneshkachroo/lt-position-tracker:0.0.1-RELEASE] 
2. Run mongo container : docker run -d -p 27017:27017 --name mongodb --network locationtracker mongo:3.6.5-jessie
3. Run position-tracker: docker run -d -p 8090:8090 --name positiontracker --network locationtracker --env spring.activemq.broker-url=tcp://myqueue:61616 --env fleetman.position.queue=positionQueue --env spring.data.mongodb.host=mongodb mooneshkachroo/lt-position-tracker:0.0.1-RELEASE
4. Check if the messages are being pushed at: http://localhost:8090/history/Secret Service A
 
  

