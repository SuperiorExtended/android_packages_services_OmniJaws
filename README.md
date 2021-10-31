About
-----
OmniJaws "Just another weather service"
is a minimized service to fetch weather data from OpenWeatherMap or MET Norway.

MET Norway Weather API
https://api.met.no/

OpenWeatherMap API
http://openweathermap.org/current


IMPORTANT: please request your own API key from OpenWeatherMap at
https://openweathermap.org/appid#get

Client access
-----
Here is a client code example using the content provider
to access weather data src/org/omnirom/omnijaws/client/OmniJawsClient.java

Broadcasts and content observers
-----
There are two ways to register for changes on weather data

Using ContentObserver on URI
```java
content://org.omnirom.omnijaws.provider/weather
```

Using broadcasts
```java
private static final String ACTION_BROADCAST = "org.omnirom.omnijaws.WEATHER_UPDATE";
private static final String ACTION_ERROR = "org.omnirom.omnijaws.WEATHER_ERROR";
```

Units
-----
Depending on the value of the metric setting the following units are used to display the weather

```code
Temperature:
metric = "C"
imperial = "F"

Wind speed:
metric = "km/h"
imperial = "mph"
```

Condition icon packs support
-----
add activity with action "org.omnirom.WeatherIconPack"
the name is used to defined the prefix for the image names
default should be .weather.

```xml
		<activity
			android:name=".weather_vclouds"
			android:label="LockClock (vclouds)" >
			<intent-filter>
				<action android:name="org.omnirom.WeatherIconPack" />

				<category android:name="android.intent.category.DEFAULT" />
			</intent-filter>
		</activity>
```

An example can be found here https://github.com/maxwen/WeatherIconSample


Icons:
Outline weather icon  set is used with permission from  http://emske.com/25-outline-weather-icons/

