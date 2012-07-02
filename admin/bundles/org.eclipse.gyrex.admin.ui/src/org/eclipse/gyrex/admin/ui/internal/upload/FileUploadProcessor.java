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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

final class FileUploadProcessor {

	private static String stripFileName(final String name) {
		String result = name;
		final int lastSlash = result.lastIndexOf('/');
		if (lastSlash != -1) {
			result = result.substring(lastSlash + 1);
		} else {
			final int lastBackslash = result.lastIndexOf('\\');
			if (lastBackslash != -1) {
				result = result.substring(lastBackslash + 1);
			}
		}
		return result;
	}

	private final FileUploadHandler handler;

	private final FileUploadTracker tracker;

	FileUploadProcessor(final FileUploadHandler handler) {
		this.handler = handler;
		tracker = new FileUploadTracker(handler);
	}

	private ProgressListener createProgressListener(final long maxFileSize) {
		final ProgressListener result = new ProgressListener() {
			long prevTotalBytesRead = -1;

			public void update(final long totalBytesRead, final long contentLength, final int item) {
				// Depending on the servlet engine and other environmental factors,
				// this listener may be notified for every network packet, so don't notify unless there
				// is an actual increase.
				if (totalBytesRead > prevTotalBytesRead) {
					prevTotalBytesRead = totalBytesRead;
					// Note: Apache fileupload 1.2.x will throw an exception after the upload is finished.
					// So we handle the file size violation as best we can from here.
					// https://issues.apache.org/jira/browse/FILEUPLOAD-145
					if ((maxFileSize != -1) && (contentLength > maxFileSize)) {
						tracker.setException(new Exception("File exceeds maximum allowed size."));
						tracker.handleFailed();
					} else {
						tracker.setContentLength(contentLength);
						tracker.setBytesRead(totalBytesRead);
						tracker.handleProgress();
					}
				}
			}
		};
		return result;
	}

	private ServletFileUpload createUpload() {
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setFileCleaningTracker(CleaningTrackerUtil.getCleaningTracker(true));
		final ServletFileUpload result = new ServletFileUpload(factory);
		final long maxFileSize = getMaxFileSize();
		result.setFileSizeMax(maxFileSize);
		final ProgressListener listener = createProgressListener(maxFileSize);
		result.setProgressListener(listener);
		return result;
	}

	private long getMaxFileSize() {
		return handler.getMaxFileSize();
	}

	void handleFileUpload(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		try {
			final DiskFileItem fileItem = readUploadedFileItem(request);
			if (fileItem != null) {
				final String fileName = stripFileName(fileItem.getName());
				final String contentType = fileItem.getContentType();
				final long contentLength = fileItem.getSize();
				tracker.setFileName(fileName);
				tracker.setContentType(contentType);
				final FileUploadReceiver receiver = handler.getReceiver();
				final IFileUploadDetails details = new FileUploadDetails(fileName, contentType, contentLength);
				receiver.receive(fileItem.getInputStream(), details);
				tracker.handleFinished();
			} else {
				final String errorMessage = "No file upload data found in request";
				tracker.setException(new Exception(errorMessage));
				tracker.handleFailed();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
			}
		} catch (final FileSizeLimitExceededException exception) {
			// Note: Apache fileupload 1.2 will throw an exception after the upload is finished.
			// Therefore we handle it in the progress listener and ignore this kind of exceptions here
			// https://issues.apache.org/jira/browse/FILEUPLOAD-145
			response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, exception.getMessage());
		} catch (final Exception exception) {
			tracker.setException(exception);
			tracker.handleFailed();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		}
	}

	private DiskFileItem readUploadedFileItem(final HttpServletRequest request) throws FileUploadException {
		final ServletFileUpload upload = createUpload();
		DiskFileItem result = null;
		final List uploadedItems = upload.parseRequest(request);
		// TODO [rst] Support multiple fields in multipart message
		if (uploadedItems.size() > 0) {
			// TODO [rst] Upload fails if the file is not the first field
			final DiskFileItem fileItem = (DiskFileItem) uploadedItems.get(0);
			// Don't check for file size == 0 because this would prevent uploading empty files
			if (!fileItem.isFormField()) {
				result = fileItem;
			}
		}
		return result;
	}

}
