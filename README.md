Face Collector
==============
Face Collector tries to recognize faces in [Twitch](http://twitch.tv) streams and publishes those to [Discord](https://discordapp.com/) guilds.

Database
--------
Face Collector stores data in PostgreSQL. Start it using docker: 

`docker run -P -e POSTGRES_DB=face-collector -d postgres`

Properties
----------
Face Collector makes use of a few API's to connect with, mainly Discord and Twitch. You will need an application with both in order to run your own instance of Face Collector.

`discord.clientId`, `discord.token` and `twitch.clientId` are the properties you will need to set. In addition, for the OAuth2 authentication to work you will need to set `spring.security.oauth2.client.registration.discord.client-id` and `spring.security.oauth2.client.registration.discord.client-secret` 
                                                                                                                                                                                        client-secret
                                                                                                                                                                                        client-secret