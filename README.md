![Mailgun Sender Logo](http://rodaxsoft.com/images/github/mailgun-sender-logo.png)

*Update Jul 14, 2017*

**Mailgun Sender** is an easy to use Java command line tool that you can use to send messages to mailing lists or email addresses using your [Mailgun account](https://mailgun.com).

> *No programming skills are required to use this tool.*

Many people don't have the time or the skills to code against APIs. So, I built **Mailgun Sender** for you. All you need is your [Mailgun account](https://mailgun.com) info and your message content in plain-text and html.

> Incidentally, if code is your thing, this tool is built on top of my [Mailgun Manager API](https://github.com/johnboyer/mailgun-manager).

## Requirements

* A computer running [Java 7](https://java.com/en) or higher.
* A valid Mailgun account *(Note: You'll need to know your public and private API keys to run the tool)*

## Installation and Setup

1. Download and extract the latest [release](https://github.com/johnboyer/mailgun-sender/releases) to an installation directory on your computer, e.g., `~/Applications`.
2. Navigate to the installation directory.
3. Before running **Mailgun Sender** for the first time, you'll need to configure two properties files (`mailgun.properties` and `mailgun-sender.properties`) located in the `/config` directory.
4. Open a terminal window on your computer, so that you can run **Mailgun Sender** using command line arguments

### mailgun.properties

Set the `publicApiKey`, `privateApiKey`, and the `domain` property values. The API keys can be found in your Mailgun's account settings.

	# Mailgun account properties

	baseUri = https://api.mailgun.net/v3
	publicApiKey = pubkey-my-public-uuid
	privateApiKey = key-my-private-uuid
	domain = mg.example.com

### mailgun-sender.properties

Set the `mailgun.sender.default.from.email` property value.

	# Mailgun Sender properties

	mailgun.sender.default.from.email=Support Team <support@example.com>

## Usage
If you haven't already, open a terminal window on your computer.

> Note: All messages sent via **Mailgun Sender** require a plain-text and HTML version of the email message content. Hence, the path to the plain-text and HTML files are passed via the command line using the `-p` and `-h` options. Additionally, when sending messages there are several optional arguments, which include the following:

	-c,--campaign-id <ID>                 Campaign identifier
	-t,--test-mode-flag <Flag>            Test mode flag [default: false]
	-f,--from-email-address <Email>       From email address [default set in mailgun-sender.properties]
	-r,--reply-to-email-address <Email>   Reply-to email address


### Send Message to an Email Address


	java -jar mailgun-sender.jar -e johndoe@example.com -s 'Test Message from Mailgun Sender!' -p plain-text.txt -h content.html

*Note: In this example, the default from email address (set in the `mailgun-sender.properties` file) is being used because the `-f` option was excluded.*

### Send Message to a Mailing List

Send message to a mailing list.

	java -jar mailgun-sender.jar -m test@mg.example.com -s "Test Mailing List Message from Mailgun Sender" -p plain-text.txt -h content.html

Send a message to a mailing list and set the from email address option, `-f`.

	java -jar mailgun-sender.jar -m test@mg.example.com -f 'Postmaster <postmaster@mg.example.com>' -s "Test Mailling List Message from Mailgun Sender" -p plain-text.txt -h content.html

### Send Message to Recipients from a File

Send message to a list of recipients in the `recipients.txt` file using the `-R` option.

	java -jar mailgun-sender.jar -R recipients.txt -s "Test Message to Recipients from a File" -p plain-text.txt -h content.html  -f postmaster@mg.example.com  -r support@skedi.zendesk.com

*Note: In this example, the default from email address (set in the `mailgun-sender.properties` file) is being overridden with `-f` option and a reply-to email address is set using the `-r` option.*

#### Recipients Format
Text file with a new line for each message recipient.

	bob@gmail.com
	john@mailgun.net
	doe@mailgun.net

### Help

	java -jar mailgun-sender.jar

## Contributors

Contributions can be made by following these steps:

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

If you have any questions, please don't hesitate to contact me at john@rodaxsoft.com.


## License
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
