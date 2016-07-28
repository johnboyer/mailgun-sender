/**
 * MailgunSender.java
 * MailgunTool
 *
 * Created by John Boyer on Sep 25, 2014.
 * Copyright 2014 Rodax Software, Inc. All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package com.rodaxsoft.mailgun.message.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.beanutils.ConfigurationDynaBean;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.exception.ContextedException;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rodaxsoft.mailgun.EmailRequest;
import com.rodaxsoft.mailgun.ListInfo;
import com.rodaxsoft.mailgun.MailgunAccount;
import com.rodaxsoft.mailgun.MailgunManager;


/**
 * MailgunSender tool class
 * @author John Boyer
 * @version 2016-07-23
 */
public final class MailgunSender {
	/**
	 * Campaign ID option
	 */
	private static final String CAMPAIGN_ID_OPT = "c";
	/**
	 * Default from email property key
	 */
	private static final String DEFAULT_FROM_EMAIL_PROP_KEY = "mailgun.sender.default.from.email";
	/**
	 * Email address option
	 */
	private static final String EMAIL_ADDRESS_OPT = "e";
	/**
	 * Email subject option
	 */
	private static final String EMAIL_SUBJECT_OPT = "s";
	/**
	 * From email address option
	 */
	private static final String FROM_EMAIL_ADDRESS_OPT = "f";
	/**
	 * HTML option
	 */
	private static final String HTML_OPT = "h";
	/**
	 * Logging object
	 */
	private static final Log LOG = LogFactory.getLog(MailgunSender.class);
	/**
	 * Mailing list option
	 */
	private static final String MAILING_LIST_OPT = "m";
	/**
	 * CLI options
	 */
	private static final Options OPTIONS = new Options();
	/**
	 * Plain text option
	 */
	private static final String PLAIN_TXT_OPT = "p";
	/**
	 * Recipients file option
	 */
	private static final String RECIPIENTS_FILE_OPT = "R";
	/**
	 * Reply to email address option
	 */
	private static final String REPLY_TO_EMAIL_ADDRESS_OPT = "r";
	/**
	 * Default email from email address
	 */
	private static String sDefaultFromEmail;
	
	/**
	 * Test mode option
	 */
	private static final String TEST_MODE_OPT = "t";
	
	static {
		
		Option option;
		OptionGroup optionGroup = new OptionGroup();
		
		//Mailing list address [REQ]
		option = Option.builder(MAILING_LIST_OPT)
				       .longOpt("mailing-list-address")
				       .desc("Mailing list address")
				       .hasArg()
				       .argName("List")
				       .build();
		
		optionGroup.addOption(option);
		
		//Email address [REQ]
		option = Option.builder(EMAIL_ADDRESS_OPT)
				       .longOpt("email-address")
				       .desc("Recipient's email address")
				       .hasArg()
				       .argName("Email")
				       .build();
		
		optionGroup.addOption(option);
		
		//Recipients file [REQ]
		option = Option.builder(RECIPIENTS_FILE_OPT)
				       .longOpt("recipients-file")
				       .desc("Recipients list file")
				       .hasArg()
				       .argName("File")
				       .build();
		
		optionGroup.addOption(option);
				
		optionGroup.setRequired(true);
		
		OPTIONS.addOptionGroup(optionGroup);
		
		//From email address [OPTIONAL]
		option = Option.builder(FROM_EMAIL_ADDRESS_OPT)
				       .longOpt("from-email-address")
				       .desc("From email address [default can be set in mailgun-sender.properties]")
				       .hasArg()
				       .argName("Email")
				       .build();
		
		OPTIONS.addOption(option);
		
		//Email subject [REQ]
		option = Option.builder(EMAIL_SUBJECT_OPT)
				       .required()
					   .longOpt("subject")
					   .desc("Email subject title")
					   .hasArg()
					   .argName("Subject")
					   .build();
				
		OPTIONS.addOption(option);
		
		
		//Reply-to email address
		option = Option.builder(REPLY_TO_EMAIL_ADDRESS_OPT)
	                   .longOpt("reply-to-email-address")
	                   .desc("Reply-to email address")
	                   .hasArg()
	                   .argName("Email")
	                   .build();
		
		OPTIONS.addOption(option);
		
		//Campaign
		option = Option.builder(CAMPAIGN_ID_OPT)
	                   .longOpt("campaign-id")
	                   .desc("Campaign identifier")
	                   .hasArg()
	                   .argName("ID")
	                   .build();
		
		OPTIONS.addOption(option);
		
		//Test mode
		option = Option.builder(TEST_MODE_OPT)
                .longOpt("test-mode-flag")
                .desc("Test mode flag [default: false]")
                .hasArg()
                .argName("Flag")
                .type(Boolean.class)
                .build();
		
		OPTIONS.addOption(option);
		
		//Plain Text file [REQ]
		option = Option.builder(PLAIN_TXT_OPT)
				       .required()
                       .longOpt("plain-text-file")
                       .desc("Plain text file content")
                       .hasArg()
                       .argName("File")
                       .build();
		
		OPTIONS.addOption(option);
		
		//HTML file
		option = Option.builder(HTML_OPT)
			           .required()
		               .longOpt("html-file")
		               .desc("HTML file content")
		               .hasArg()
		               .argName("File")
		               .build();
				
		OPTIONS.addOption(option);
		
	}
	
	/**
	 * Handles option error and throws and <code>ContextedRuntimeException</code>.
	 * @param cmd Command line arguments
	 * @param msg Error message string
	 * @throws ContextedRuntimeException to handle the option value error
	 */
	private static void handleOmittedOptionError(CommandLine cmd,
			final String msg) throws ContextedRuntimeException {
		LOG.error(msg);
		Iterator<Option> it = cmd.iterator();
		List<String> options = new ArrayList<String>();
		while(it.hasNext()) {
			final String desc = it.next().toString();
			options.add(desc);
			LOG.debug(desc);				
		}

		throw new ContextedRuntimeException(msg).addContextValue("options", options);
	}
	
	private static void initializeMailgun(CommandLine cmd) throws ContextedException, ConfigurationException {

		//Configure Mailgun account info
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder;
		builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		Parameters params = new Parameters();
		builder.configure(params.properties().setFileName("config/mailgun.properties"));
		Configuration config = builder.getConfiguration();
		
		DynaBean bean = new ConfigurationDynaBean(config);
		MailgunAccount acct = new MailgunAccount(bean);
//		Register the MailgunAccount object
		MailgunManager.register(acct);
		
		LOG.info("Configured mailgun.properties");
		
		//Set the default from email address
		setDefaultFromEmail(cmd);
		

	}

	/**
	 * Main method
	 * @param args Command line arguments
	 * @throws ConfigurationException if a configuration error occurs
	 */
	public static void main(String[] args)  {
		
		//Parse the command line
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( OPTIONS, args);
			initializeMailgun(cmd);
		} catch(ParseException | ContextedException | ConfigurationException e) {
			System.err.println(e.getMessage());
			System.err.println();
			printHelp();
			System.exit(-1);
		}
		
		//Read the plain text and HTML content files
		String text = null;
		String html = null;
		try {
			text = readFileToString(cmd.getOptionValue(PLAIN_TXT_OPT));
			html = readFileToString(cmd.getOptionValue(HTML_OPT));
		} catch (IOException e) {
			LOG.error("Error reading file", e);
			System.exit(-1);
		}
			
		if(!cmd.hasOption(RECIPIENTS_FILE_OPT))	{	
			sendMessageToEmailAddress(cmd, text, html);
			
		} else {
			sendMessageToRecipientsInFile(cmd, text, html);
		}
	}

	/**
	 * Sets the default from email address or <code>sDefaultFromEmail</code> from the 
	 * properties file if no <code>-f</code> option exists.
	 * @param cmd Command line arguments
	 */
	private static void setDefaultFromEmail(CommandLine cmd) throws ContextedException {
		if(!cmd.hasOption(FROM_EMAIL_ADDRESS_OPT)) {
			
			Parameters params = new Parameters();
			FileBasedConfigurationBuilder<FileBasedConfiguration> builder;
			final Class<PropertiesConfiguration> propConfigClass = PropertiesConfiguration.class;
			builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(propConfigClass);
			builder.configure(params.properties().setFileName("config/mailgun-sender.properties"));
			
			try {
				FileBasedConfiguration config = builder.getConfiguration();
				sDefaultFromEmail = config.getString(DEFAULT_FROM_EMAIL_PROP_KEY);
				if(null == sDefaultFromEmail) {
					throw new ContextedException("Missing " + DEFAULT_FROM_EMAIL_PROP_KEY + " key")
					             .addContextValue(DEFAULT_FROM_EMAIL_PROP_KEY, sDefaultFromEmail)
					             .addContextValue("help", "Must set from email address with option -f or in the `mailgun-sender.properties` file");
				}
				
				LOG.info("Configured mailgun-sender.properties");

			} catch (ConfigurationException e) {
				throw new ContextedException("Error loading `mailgun-sender.properties`", e);
			}
		}
	}

	/**
	 * Makes an email request object
	 * @param cmd Command line args
	 * @param text Plain text content
	 * @param html HTML content
	 * @param toAddress Recipient address
	 * @return
	 */
	private static EmailRequest makeEmailRequest(CommandLine cmd, String text, String html, String toAddress) {
		return new EmailRequest()
		              .setTo(toAddress)
		              .setFrom(cmd.getOptionValue(FROM_EMAIL_ADDRESS_OPT, sDefaultFromEmail))
		              .setSubject(cmd.getOptionValue(EMAIL_SUBJECT_OPT))
		              .setTextBody(text)
		              .setHTMLBody(html);
	}

	/**
	 * Prints usage
	 */
	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		String header = "\nChoose options:\n";
		String footer = "\nThis tool is used for sending messages to mailing lists or email addresses.";
		String className = MailgunSender.class.getSimpleName();
		formatter.printHelp(className, header, OPTIONS, footer, true);	
	}

	/**
	 * Reads file to string
	 * @param fileName The file to read
	 * @return The file content as a string
	 * @throws IOException if an I/O error occurs
	 */
	private static String readFileToString(String fileName) throws IOException {
		File file = new File(fileName);
		Charset set = Charset.defaultCharset();
		return FileUtils.readFileToString(file, set);
	}

	/**
	 * Sends the message
	 * @param cmd Command line args
	 * @param er Email request object
	 */
	private static void sendMessage(CommandLine cmd, EmailRequest er) {
		//Set optional campaign
		if(cmd.hasOption(CAMPAIGN_ID_OPT)) {
			er = er.setCampaign(cmd.getOptionValue(CAMPAIGN_ID_OPT));
		}
		
		//Set optional test mode
		if(cmd.hasOption(TEST_MODE_OPT)) {
			final String value = cmd.getOptionValue(TEST_MODE_OPT);
			Boolean mode = new Boolean(value);
			er.setTestMode(mode);		
		}
		
		//Set reply-to email address
		if(cmd.hasOption(REPLY_TO_EMAIL_ADDRESS_OPT)) {
			er = er.setReplyTo(cmd.getOptionValue(REPLY_TO_EMAIL_ADDRESS_OPT));
		}      
		    
		try {
			LOG.info("Sending message...");
			boolean success = MailgunManager.sendMessage(er);
			
			if(success) {
				LOG.info("Message sent successfully");
			}
			else {
				LOG.error("Failed to send message");
			}
		} catch (ContextedException e) {
			LOG.error(e.getMessage());
			LOG.debug("Mailgun Message Send Error", e);
		}
	}

	/**
	 * Sends a message to a mailing list (<code>-m</code> option) or 
	 * single email address (<code>-e</code> option).
	 * @param cmd Command line arguments
	 * @param text Plain text email content
	 * @param html HTML email content
	 */
	private static void sendMessageToEmailAddress(CommandLine cmd, String text, String html) {
		
		if(cmd.hasOption(MAILING_LIST_OPT) || cmd.hasOption(EMAIL_ADDRESS_OPT)) {
			final boolean isMailingList = cmd.hasOption(MAILING_LIST_OPT);
			final String toAddress = isMailingList ? cmd.getOptionValue(MAILING_LIST_OPT) : 
				cmd.getOptionValue(EMAIL_ADDRESS_OPT);
			if(isMailingList) {

				try {
					validateMailingListAddress(toAddress);
				} catch (ContextedException e) {
					LOG.error(e.getMessage());
					LOG.debug("Mailgun Mailing List Error", e);
					System.exit(-1);
				}
			} 
			//Build the email request object
			final EmailRequest er = makeEmailRequest(cmd, text, html, toAddress);
			LOG.trace(er);

			sendMessage(cmd, er);
		} else {
			final String msg = "Option value must be an email address or mailing list.";
			handleOmittedOptionError(cmd, msg);
		}
	}

	/**
	 * Sends message to the recipients specified by the <code>-R</code> option.
	 * @param cmd Command line arguments
	 * @param text Plain text email content
	 * @param html HTML email content
	 * @throws ContextedRuntimeException if the recipients option or -R is omitted.
	 */
	private static void sendMessageToRecipientsInFile(CommandLine cmd, String text, String html) {
		
		if(cmd.hasOption(RECIPIENTS_FILE_OPT)) {
			LineIterator it = null;
			try {
				it = FileUtils.lineIterator(new File(cmd.getOptionValue(RECIPIENTS_FILE_OPT)), "UTF-8");

				while (it.hasNext()) {
					final String to = it.nextLine();
					//Build the email request object
					final EmailRequest er = makeEmailRequest(cmd, text, html, to);
					LOG.trace(er);

					sendMessage(cmd, er);
				}

			} catch (IOException e) {
				LOG.error("Error occurre while sending from recipients file", e);

			} finally {
				LineIterator.closeQuietly(it);
			}
		} else {

			final String msg = "Option must be a recipients file";
			handleOmittedOptionError(cmd, msg);
		}
	}
	
	/**
	 * Validates mailing list address
	 * @param address The address to validate
	 * @throws ContextedException if it's invalid or a processing error occurs
	 */
	private static void validateMailingListAddress(final String address) 
			                                       throws ContextedException {

		Collection<ListInfo> lists = MailgunManager.getMailingLists();

		Predicate<ListInfo> predicate = new Predicate<ListInfo>() {
			@Override
			public boolean evaluate(ListInfo info) {
				return info.getAddress().equals(address);
			}
		};

		if(null == IterableUtils.find(lists, predicate)) {
			throw new ContextedException("Invalid mailing address")
			              .addContextValue("address", address);
		}
	}

}

