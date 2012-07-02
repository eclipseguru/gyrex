/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - copied from RAP Incubator
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

final class FileUploadTracker {

	private final class InternalFileUploadEvent extends FileUploadEvent {

		private static final long serialVersionUID = 1L;

		private InternalFileUploadEvent(final FileUploadHandler source) {
			super(source);
		}

		void dispatchAsFailed() {
			super.dispatchFailed();
		}

		void dispatchAsFinished() {
			super.dispatchFinished();
		}

		void dispatchAsProgress() {
			super.dispatchProgress();
		}

		@Override
		public long getBytesRead() {
			return bytesRead;
		}

		@Override
		public long getContentLength() {
			return contentLength;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public Exception getException() {
			return exception;
		}

		@Override
		public String getFileName() {
			return fileName;
		}
	}

	private final FileUploadHandler handler;
	private String contentType;
	private String fileName;
	private long contentLength;
	private long bytesRead;

	private Exception exception;

	FileUploadTracker(final FileUploadHandler handler) {
		this.handler = handler;
	}

	void handleFailed() {
		new InternalFileUploadEvent(handler).dispatchAsFailed();
	}

	void handleFinished() {
		new InternalFileUploadEvent(handler).dispatchAsFinished();
	}

	void handleProgress() {
		new InternalFileUploadEvent(handler).dispatchAsProgress();
	}

	void setBytesRead(final long bytesRead) {
		this.bytesRead = bytesRead;
	}

	void setContentLength(final long contentLength) {
		this.contentLength = contentLength;
	}

	void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	void setException(final Exception exception) {
		this.exception = exception;
	}

	void setFileName(final String fileName) {
		this.fileName = fileName;
	}
}
