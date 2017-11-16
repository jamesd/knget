# knget

A simple utility to capture video codes from
[Knowledge Network](https://knowledge.ca) show URLs into makefile
snippets that can used, with
[youtube-dl](https://rg3.github.io/youtube-dl/), to download the
available videos.

## Installation

Download from https://github.com/jamesd/knget.

## Usage

Simply run the standalone jar file and provide Knowledge Network URL(s)
with videos you want to capture in makefile snippets
([see Examples](#Examples)).

    $ java -jar knget-0.1.0-standalone.jar [url...]

Creating a shell wrapper in your PATH to hide the java invocation
details I find easier:

	$ cat ~/bin/knget
	#!/bin/bash
	exec java -jar /path/to/jar/knget-0.1.0-standalone.jar "$@"

### Options

The only options for `knget` is a list of URLs.

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
youtube-dl -f 6 -o Fakes_and_Frauds_Exposed_ep01.mp4 "https://content.jwplatform.com/feeds/xtof6VUs.json"
[JWPlatform] xtof6VUs: Downloading JSON metadata
[JWPlatform] xtof6VUs: Downloading m3u8 information
[download] Destination: Fakes_and_Frauds_Exposed_ep01.mp4
[download] 100% of 731.03MiB in 03:15
```

The makefile snippet created for the above episode is:

```make
all::	Fakes_and_Frauds_Exposed_ep01.mp4

Fakes_and_Frauds_Exposed_ep01.mp4:
	youtube-dl -f 6 -o $@ "https://content.jwplatform.com/feeds/xtof6VUs.json"
```

A re-run of make (e.g, via `cron`) will create new makefile snippets for
episodes that were not previously available and only the new episodes
will be downloaded.

## License

Copyright © 2017 James Davidson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
