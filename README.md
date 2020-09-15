# knget
[![Build Status](https://travis-ci.com/jamesd/knget.svg?branch=master)](https://travis-ci.com/jamesd/knget)
[![Version](https://img.shields.io/badge/Version-0.2.2-blue.svg?style=flat)](https://github.com/jamesd/knget/releases)
[![License](http://img.shields.io/badge/license-EPL-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html)

A simple utility to capture video codes from
[Knowledge Network](https://knowledge.ca) show URLs into makefile
snippets that can used, with
[youtube-dl](https://rg3.github.io/youtube-dl/), to download the
available videos.

## Installation

Download from [github.com/jamesd/knget](https://github.com/jamesd/knget)
and build:

	$ lein uberjar

## Usage

Simply run the standalone jar file and provide Knowledge Network URL(s)
with videos you want to capture in makefile snippets
([see Examples](#Examples)).

    $ java -jar knget-0.2.2-standalone.jar [url...]

Creating a shell wrapper in your PATH to hide the java invocation
details I find easier:

	$ cat ~/bin/knget
	#!/bin/bash
	exec java -jar /path/to/jar/knget-0.2.2-standalone.jar "$@"

### Options

There is only one optional parameter for mapping show names so you can
provide a clojure map to get the show name you desire from the show name
specified by Knowledge Network ([see Examples](#Examples) for
usage). Otherwise, only a list of URLs to examine is required.

## Examples

The use case for `knget` is to create a poor man's PVR of available
episodes from Knowledge Network shows.

For each episode found in the specified URL, a makefile will be created
such that it can be included in a parent `Makefile`:

```make
URLS := "https://www.knowledge.ca/program/museum-diaries"

_all: update
	@$(MAKE) --no-print-directory all

update:
	knget $(URLS)

-include *.mak
```

Then just run make to cause `youtube-dl` to fetch the available
episodes:

```
$ make
knget "https://www.knowledge.ca/program/museum-diaries"
youtube-dl -f 6 -o "Fakes_and_Frauds_Exposed_ep01.mp4" "https://content.jwplatform.com/feeds/xtof6VUs.json"
[JWPlatform] xtof6VUs: Downloading JSON metadata
[JWPlatform] xtof6VUs: Downloading m3u8 information
[download] Destination: Fakes_and_Frauds_Exposed_ep01.mp4
[download] 100% of 731.03MiB in 03:15
```

The makefile snippet created for the above episode is:

```make
all::	Fakes_and_Frauds_Exposed_ep01.mp4

Fakes_and_Frauds_Exposed_ep01.mp4:
	youtube-dl -f 6 -o "$@" "https://content.jwplatform.com/feeds/xtof6VUs.json"
```

A re-run of make (e.g, via `cron`) will create new makefile snippets for
episodes that were not previously available and only the new episodes
will be downloaded.

If the episode list does not match up with [The TV DB](https://thetvdb.com), you
can specify an episode rename map:

```make
# https://www.thetvdb.com/?tab=seasonall&id=250675&lid=7

URLS := "https://www.knowledge.ca/program/waterfront-cities-world?season=1" \
	"https://www.knowledge.ca/program/waterfront-cities-world?season=2" \
	"https://www.knowledge.ca/program/waterfront-cities-world?season=3" \
	"https://www.knowledge.ca/program/waterfront-cities-world?season=4" \
	"https://www.knowledge.ca/program/waterfront-cities-world?season=5"

MAP := '{ \
	"Helsinki_s01e01.mp4" "Helsinki_s01e05.mp4" \
	"Havana_s01e03.mp4" "Havana_s01e09.mp4" \
	"Lisbon_s01e04.mp4" "Lisbon_s01e13.mp4" \
	"Tel_Aviv_s01e05.mp4" "Tel_Aviv_s01e03.mp4" \
	"Bangkok_s01e06.mp4" "Bangkok_s01e04.mp4" \
	"Buenos_Aires_s01e07.mp4" "Buenos_Aires_s01e12.mp4" \
	"Melbourne_s01e08.mp4" "Melbourne_s01e06.mp4" \
	"Houston_s01e09.mp4" "Houston_s01e07.mp4" \
	"Reykjavik_s01e10.mp4" "Reykjavik_s01e08.mp4" \
	"San_Francisco_s01e11.mp4" "San_Francisco_s01e01.mp4" \
	"Marseille_s01e12.mp4" "Marseille_s01e11.mp4" \
	"Moscow_s01e13.mp4" "Moscow_s01e10.mp4" \
	\
	"Singapore_s02e01.mp4" "Singapore_s02e02.mp4" \
	"Venice_s02e02.mp4" "Venice_s02e01.mp4" \
	"Panama_City_s02e03.mp4" "Panama_City_s02e11.mp4" \
	"Boston_s02e04.mp4" "Boston_s02e09.mp4" \
	"Tokyo_s02e05.mp4" "Tokyo_s02e13.mp4" \
	"Valencia_s02e08.mp4" "Valencia_s02e04.mp4" \
	"Saint_Petersburg_s02e09.mp4" "Saint_Petersburg_s02e08.mp4" \
	"Istanbul_s02e10.mp4" "Istanbul_s02e03.mp4" \
	"Salvador_de_Bahia_s02e11.mp4" "Salvador_de_Bahia_s02e10.mp4" \
	"Vancouver_s02e13.mp4" "Vancouver_s02e05.mp4" \
	\
	"Honolulu_s03e02.mp4" "Honolulu_s03e12.mp4" \
	"Doha_s03e03.mp4" "Doha_s03e10.mp4" \
	"Naples_s03e04.mp4" "Naples_s03e05.mp4" \
	"Miami_s03e05.mp4" "Miami_s03e08.mp4" \
	"Lima_s03e06.mp4" "Lima_s03e11.mp4" \
	"Ho_Chi_Minh_City_s03e07.mp4" "Ho_Chi_Minh_City_s03e13.mp4" \
	"Hamburg_s03e08.mp4" "Hamburg_s03e03.mp4" \
	"Riga_s03e10.mp4" "Riga_s03e07.mp4" \
	"London_s03e11.mp4" "London_s03e04.mp4" \
	"Barcelona_s03e12.mp4" "Barcelona_s03e06.mp4" \
	"New_York_City_s03e13.mp4" "New_York_City_s03e02.mp4" \
	\
	"Oslo_s04e04.mp4" "Oslo_s04e05.mp4" \
	"Dublin_s04e05.mp4" "Dublin_s04e04.mp4" \
	"Seoul_s04e06.mp4" "Seoul_s04e08.mp4" \
	"Los_Angeles_s04e07.mp4" "Los_Angeles_s04e12.mp4" \
	"Auckland_s04e08.mp4" "Auckland_s04e11.mp4" \
	"Cartegena_s04e09.mp4" "Cartegena_s04e07.mp4" \
	"Rio_de_Janeiro_s04e10.mp4" "Rio_de_Janeiro_s04e13.mp4" \
	"Mumbai_s04e11.mp4" "Mumbai_s04e09.mp4" \
	"Shanghai_Part_1_s04e12.mp4" "Shanghai_1_International_City_s04e06.mp4" \
	"Shanghai_Part_2_s04e13.mp4" "Shanghai_2_City_of_Opportunities_s04e10.mp4" \
	}'

_all: update
	@$(MAKE) --no-print-directory all

update:
	knget -m $(MAP) $(URLS)

-include *.mak
```

## License

Copyright © 2017 James Davidson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
