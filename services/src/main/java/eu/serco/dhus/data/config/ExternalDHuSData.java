/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.serco.dhus.data.config;

public class ExternalDHuSData {
	private String username;
	private String password;
	private String url;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ExternalDHuSData(String username, String password, String url) {
		super();
		this.username = username;
		this.password = password;
		this.url = url;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ExternalDHuSData
				&& ((ExternalDHuSData) o).username == this.username
				&& ((ExternalDHuSData) o).password == this.password
				&& ((ExternalDHuSData) o).url == this.url;
	}

	public ExternalDHuSData copy() {
		ExternalDHuSData copy = new ExternalDHuSData(username, password, url);
		return copy;
	}

}