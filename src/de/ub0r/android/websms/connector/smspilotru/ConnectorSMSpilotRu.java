/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.websms.connector.smspilotru;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.ub0r.android.websms.connector.common.BasicConnector;
import de.ub0r.android.websms.connector.common.ConnectorCommand;
import de.ub0r.android.websms.connector.common.ConnectorSpec;
import de.ub0r.android.websms.connector.common.Utils;
import de.ub0r.android.websms.connector.common.WebSMSException;

/**
 * AsyncTask to manage IO to smspilot.ru API.
 * 
 * @author flx
 */
public final class ConnectorSMSpilotRu extends BasicConnector {
	/** Tag for output. */
	private static final String TAG = "smspilot";

	/** SMSpilotRu Gateway URL. */
	private static final String URL = "https://smspilot.ru/api.php";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectorSpec initSpec(final Context context) {
		final String name = context.getString(R.string.connector_smspilot_name);
		ConnectorSpec c = new ConnectorSpec(name);
		c.setAuthor(// .
				context.getString(R.string.connector_smspilot_author));
		c.setBalance(null);
		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
				| ConnectorSpec.CAPABILITIES_SEND
				| ConnectorSpec.CAPABILITIES_PREFS);
		c.addSubConnector("smspilot.ru", name, 0);
		// TODO: SubConnectorSpec.FEATURE_CUSTOMSENDER);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectorSpec updateSpec(final Context context,
			final ConnectorSpec connectorSpec) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) {
			if (p.getString(Preferences.PREFS_APIKEY, "").length() > 0) {
				connectorSpec.setReady();
			} else {
				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
			}
		} else {
			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
		}
		return connectorSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getParamUsername() {
		return "apikey";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getParamPassword() {
		return "password";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getParamRecipients() {
		return "to";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getParamSender() {
		return "from";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getParamText() {
		return "send";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUsername(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		return p.getString(Preferences.PREFS_APIKEY, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPassword(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		return "xxx";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getRecipients(final ConnectorCommand command) {
		return Utils.joinRecipientsNumbers(Utils.national2international(command
				.getDefPrefix(), command.getRecipients()), ";", true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSender(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		// TODO: let user set sender
		final String s = Utils.international2oldformat(Utils.getSender(context,
				command.getDefSender()));
		if (s.startsWith("00")) {
			return s.substring(2);
		}
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUrlBalance(final ArrayList<BasicNameValuePair> d) {
		return URL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getUrlSend(final ArrayList<BasicNameValuePair> d) {
		return URL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean usePost() {
		// TODO: change me
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getEncoding() {
		return "UTF-8";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void parseResponse(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs,
			final String htmlText) {
		if (htmlText == null || htmlText.length() == 0) {
			throw new WebSMSException(context, R.string.error_service);
		}
		String[] lines = htmlText.split("\n");
		int l = lines.length;
		if (l == 0) {
			throw new WebSMSException(context, R.string.error_service);
		} else if (lines[0].startsWith("SUCCESS")) {
			// cs.setBalance(lines[l - 1].trim());
		} else {
			throw new WebSMSException(context, R.string.error, lines[0]);
		}
	}
}
