/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - copied from RAP Incubator
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

public final class FileUploadDetails implements IFileUploadDetails {

	private final String fileName;
	private final String contentType;
	private final long contentLength;

	public FileUploadDetails(final String fileName, final String contentType, final long contentLength) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.contentLength = contentLength;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public String getFileName() {
		return fileName;
	}
}
