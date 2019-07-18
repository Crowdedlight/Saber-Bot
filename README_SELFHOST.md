# Instructions for self-hosting the bot

* Install [Java 8 Runtime enviroment](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* Install [MongoDB default installation for localhost](https://docs.mongodb.com/manual/installation/)
* Either compile from source or use the included ``.jar`` file.
* Use whatever service you use for application management on your server to execute the ``.jar`` file and keep it running
at all times. (I use [supervisor](http://supervisord.org/) for that)
* On first attempt a configuration file will be created if not already existing. 
* Fill out ``discord_token``, ``admin_id``, ``google_service_key`` and ``google_oauth_secret``. The steps below will talk you though it.
* Set other settings you want here, instead of having to do it manually through the bot later

### Discord token & admin_id
* Follow the steps [here](https://support.discordapp.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-) to get ``admin_id``
* Get discord token by going to [discord developer portal](https://discordapp.com/developers/applications/) and login with your discord account
* Create a new application for the saber bot
* Select the application and choose the menu section "Bot"
* Create the bot and you can get the token from the "copy" button. 
* Go to the "General information" on your application and save the ``Client_id`` as that is required to invite the bot to the discord

### Google service key and google oauth
* Go to [google online api](https://console.developers.google.com/apis/dashboard) and create a new project for your bot
* Under the menu "credentials" create a ``service account key`` and download it as json. 
This is your ``google_service_key`` that should be placed next to the ``jar`` main file and ``.toml`` configuration file
* Now create new credentials, but this time select: ``oauth client id``. Select ``other`` application type and give it a name
* Download the newly created credentials as json and place next to the ``jar`` file like the ``google_service_key``.
* Remember to rename the files or the configuration path in the ``.toml`` file to properly match the credential files. 

#### Optional, consent screen
* Under the credentials page in google api there is a tab called ``Oauth consent screen``. Modify it to your liking 
as this is what is seen when trying to approve the oauth from discord to sync the calendar. 

### Enable google calendar api
* Again go to the [google online api](https://console.developers.google.com/apis/dashboard) and your project for the bot
* At the top there is a button called ``Enable APIs and services``. Select that and search for ``Google Calendar API``. 
* Enable that api as it is required to sync to the calendar. 

**OBS. It might take some time from enabling this to the bot can use the api. If you get "invalid address" errors wait a bit before trying again**

### Invite the bot to your server
Use the following url but replace the ``CLIENT_ID`` with the one found earlier in your discord developer portal.
This way the permissions needed are already set when it joins. 
<https://discordapp.com/oauth2/authorize?client_id=CLIENT_ID&scope=bot&permissions=523344>

### Use sync from the bot
The sync command is changed slightly for this version as it is hardcoded to always sync from discord to google. 
* First use ``!oauth`` to auth your discord-username with google. 
* Create a calendar on google and make it public. 
* In the settings for the created calendar go to ``Access Permissions`` and enable the ``Make calendar public``
* Further down in the settings find ``Integrate Calendar`` and right under that is the ``Calendar-id``. Copy that id. 
* Use the command ``!sync <calendar_id>`` and it should report success. It will from then on sync once a day at the chosen time
* Command ``!sync`` with no arguments will show the current calendar address getting synced and do a manual sync immediately. 
